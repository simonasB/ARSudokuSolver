package com.puzzleslab.arsudokusolver.Modules;

import android.content.Context;

import com.puzzleslab.arsudokusolver.Solvers.BacktrackingKudokuSolver;
import com.puzzleslab.arsudokusolver.Utils.CommonUtils;
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
    public SCandidate(FramePipeline framePipeline, Context context) throws SudokuException {
        this.framePipeline = framePipeline;
        this.corners = SudokuUtils.detectSudokuCorners(framePipeline.getDilated(), 30);
        this.foundCorners = !corners.empty();
        if(!this.foundCorners) {
            throw new SudokuException("Could not detect sudoku corners.", "CORNERS_NOT_FOUND");
        }
        this.destCorners = OpenCV.mkCorners(framePipeline.getFrame().size());
        this.sudokuCanvas = OpenCV.warp(framePipeline.getFrame(), corners, destCorners);
        this.sudokuCanvas = OpenCV.resize(this.sudokuCanvas, new Size(270, 270)); // Resizing that picture width and height could be divided by sudoku row and column count equally
        CommonUtils.printMatToPicture(this.sudokuCanvas, "bbb.png");
        this.cellSize = OpenCV.mkCellSize(sudokuCanvas.size());
        this.cellWidth = ((int) cellSize.width);
        this.cellHeight = ((int) cellSize.height);
        this.templateLibrary = new TemplateLibrary(context);
    }

    private FramePipeline framePipeline;
    private MatOfPoint2f corners;
    private boolean foundCorners;
    private MatOfPoint2f destCorners;
    private Mat sudokuCanvas;
    private Size cellSize;
    private int cellWidth;
    private int cellHeight;
    private TemplateLibrary templateLibrary;
    private List<Rect> cellRects;

    public List<Rect> getCellRects() {
        if(cellRects == null) {
            List<Rect> cellRects = new ArrayList<>();
            for (int i = 0; i < Parameters.CELLCOUNT; i++) {
                cellRects.add(new Rect(Parameters.getCol(i) * cellWidth, Parameters.getRow(i) * cellHeight, cellWidth, cellHeight));
            }
            this.cellRects = cellRects;
        }
        return cellRects;
    }

    private List<SCell> getDetectedCells() {
        List<SCell> sCells = new ArrayList<>();
        int i = 1;
        for (Rect cellRect: getCellRects()) {
            CommonUtils.printMatToPicture(sudokuCanvas.submat(cellRect), i++ + ".png");
            sCells.add(OpenCV.detectCell(sudokuCanvas, cellRect, templateLibrary));
        }
        return sCells;
    }

    public Mat calc() {
        List<SCell> detectedScells = getDetectedCells();

        String unsolvedSudoku = SudokuUtils.convertDetectedSCellsToString(detectedScells);
        String solvedSudoku = new BacktrackingKudokuSolver().solve(unsolvedSudoku);

        List<Mat> digitsDigitalData = SudokuUtils.getResizedDigitsDigitalData(templateLibrary, cellSize);
        Mat paintedSolution = SudokuUtils.paintSolution(sudokuCanvas, solvedSudoku, getCellRects(), digitsDigitalData);

        CommonUtils.printMatToPicture(paintedSolution, "paintedsolution.png");

        Mat unwarped = OpenCV.warp(paintedSolution, destCorners, corners);
        Mat solutionMat = OpenCV.copySrcToDestWithMask(unwarped, framePipeline.getFrame(), unwarped);

        CommonUtils.printMatToPicture(solutionMat, "solutionmat.png");

        return solutionMat;
    }
}
