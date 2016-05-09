package com.puzzleslab.arsudokusolver.Modules;

import android.nfc.Tag;
import android.util.Log;
import android.util.Pair;

import com.puzzleslab.arsudokusolver.Utils.OpenCV;
import com.puzzleslab.arsudokusolver.Utils.Parameters;
import com.puzzleslab.arsudokusolver.Utils.SudokuUtils;

import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Rect;
import org.opencv.core.Size;
import org.opencv.engine.OpenCVEngineInterface;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Simonas on 2016-04-02.
 */
public class SCandidate {
    public SCandidate(int nr, FramePipeline framePipeline) {
        this.nr = nr;
        this.framePipeline = framePipeline;
    }

    private int nr; // number of the frame
    private FramePipeline framePipeline;

    private MatOfPoint2f corners = SudokuUtils.detectSudokuCorners(framePipeline.getDilated(), 30);
    private boolean foundCorners = !corners.empty();

    private MatOfPoint2f destCorners = OpenCV.mkCorners(framePipeline.getFrame().size());
    private Mat sudokuCanvas = OpenCV.warp(framePipeline.getFrame(), corners, destCorners);
    private Size cellSize = OpenCV.mkCellSize(sudokuCanvas.size());
    private int cellWidth = ((int) cellSize.width);
    private int cellHeight = ((int) cellSize.height);

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
            sCells.add(OpenCV.detectCell(sudokuCanvas, cellRect));
        }
        return sCells;
    }

    private InputFrame sample = new InputFrame(nr , framePipeline);

    public Pair<SSuccess, SudokuState> calc(SudokuState lastState, int cap, int minHits, Long maxSolvingDuration) {
        if (foundCorners) {
            List<SCell> detectedScells = getSCells();
            List<Integer> sCellValues = new ArrayList<>();
            for  (SCell sCell : detectedScells) {
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
        } else {
            Log.e("SCandidate", "Couldn't detect corners");
            //TODO: Generate error dialog box or similar.
            return null;
        }
    }
    /**
     * paints the solution to the canvas.
     *
     * returns the modified canvas with the solution painted upon.
     *
     * detectedCells contains values from 0 to 9, with 0 being the cells which are 'empty' and thus have to be filled up
     * with numbers.
     *
     * uses digitData as lookup table to paint onto the canvas, thus modifying the canvas.
     */
    public static final Mat paintSolution(Mat canvas, List<Integer> detectedCells,
                                          List<SCell> solution, DigitLibrary digitLibrary, List<Rect> rects) {
        List<Integer> values = new ArrayList<>();
        int sum = 0;
        for (SCell cell : solution) {
            int value = cell.getValue();
            values.add(value);
            sum += value;
        }
        if(sum == 405) {
            //TODO: Finish implementation
        }
        return new Mat();
    }
}