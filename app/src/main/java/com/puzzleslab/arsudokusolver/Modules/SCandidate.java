package com.puzzleslab.arsudokusolver.Modules;

import android.content.Context;
import android.content.res.Resources;
import android.util.Log;
import android.util.Pair;

import com.puzzleslab.arsudokusolver.Utils.OpenCV;
import com.puzzleslab.arsudokusolver.Utils.Parameters;
import com.puzzleslab.arsudokusolver.Utils.SudokuUtils;
import com.puzzleslab.arsudokusolver.Utils.TemplateLibrary;

import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Rect;
import org.opencv.core.Size;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Simonas on 2016-04-02.
 */
public class SCandidate {
    public SCandidate(int nr, FramePipeline framePipeline, Context context) throws SudokuException {
        this.nr = nr;
        this.framePipeline = framePipeline;
        this.corners = SudokuUtils.detectSudokuCorners(framePipeline.getDilated(), 30);
        this.foundCorners = !corners.empty();
        if(!this.foundCorners) {
            throw new SudokuException("Could not detect sudoku corners.", "CORNERS_NOT_FOUND");
        }
        this.destCorners = OpenCV.mkCorners(framePipeline.getFrame().size());
        this.sudokuCanvas = OpenCV.warp(framePipeline.getFrame(), corners, destCorners);
        this.cellSize = OpenCV.mkCellSize(sudokuCanvas.size());
        this.cellWidth = ((int) cellSize.width);
        this.cellHeight = ((int) cellSize.height);
        this.sample = new InputFrame(nr , framePipeline);
        this.templateLibrary = new TemplateLibrary(context);
    }

    private int nr; // number of the frame
    private FramePipeline framePipeline;
    private MatOfPoint2f corners;
    private boolean foundCorners;
    private MatOfPoint2f destCorners;
    private Mat sudokuCanvas;
    private Size cellSize;
    private int cellWidth;
    private int cellHeight;
    private InputFrame sample;
    private TemplateLibrary templateLibrary;
    private Resources resources;

    private List<Rect> cellRects() {
        List<Rect> cellRects = new ArrayList<>();
        for (int i = 0; i < Parameters.CELLCOUNT; i++) {
            cellRects.add(new Rect(Parameters.getCol(i) * cellWidth, Parameters.getRow(i) * cellHeight, cellWidth, cellHeight));
        }
        return cellRects;
    }

    private List<SCell> getSCells() {
        List<SCell> sCells = new ArrayList<>();
        for (Rect cellRect: cellRects()) {
            sCells.add(OpenCV.detectCell(sudokuCanvas, cellRect, templateLibrary));
        }
        return sCells;
    }

    public Pair<SSuccess, SudokuState> calc(SudokuState lastState, int cap, int minHits, Long maxSolvingDuration) {
        List<SCell> detectedScells = getSCells();
        List<Integer> sCellValues = new ArrayList<>();
        for (SCell sCell : detectedScells) {
            sCellValues.add(sCell.getValue());
        }
        DigitLibrary mergedLibrary = SudokuUtils.mergeDigitLibrary(
                sudokuCanvas, lastState.getDigitLibrary(), detectedScells);
        HitCounters hitsToCompute = SudokuUtils.mergeHits(
                lastState.getHitCounters(), sCellValues);

        Pair<List<Character>, SudokuState> solutionAndState = SudokuUtils.doIt(hitsToCompute, mergedLibrary, cap, minHits, maxSolvingDuration);
        List<SCell> sCells = SudokuUtils.toSolutionCells(mergedLibrary, solutionAndState.first);

        Mat withSolution = SudokuUtils.paintSolution(sudokuCanvas, sCells, solutionAndState.second.getDigitLibrary(), cellRects());

        Mat unwarped = OpenCV.warp(withSolution, destCorners, corners);
        Mat solutionMat = OpenCV.copySrcToDestWithMask(unwarped, framePipeline.getFrame(), unwarped);
        SudokuFrame sudokuFrame = new SudokuFrame(sudokuCanvas, detectedScells, corners.toList());
        SolutionFrame solutionFrame = new SolutionFrame(solutionAndState.first, solutionMat);

        return new Pair<>(new SSuccess(sample, sudokuFrame, solutionFrame), solutionAndState.second);
    }
}
