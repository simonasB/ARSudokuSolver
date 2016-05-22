package com.puzzleslab.arsudokusolver.Modules;

import android.content.Context;
import android.util.Log;

import com.puzzleslab.arsudokusolver.Solvers.BacktrackingKudokuSolver;
import com.puzzleslab.arsudokusolver.Utils.OpenCV;
import com.puzzleslab.arsudokusolver.Utils.Parameters;
import com.puzzleslab.arsudokusolver.Utils.SudokuUtils;

import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Rect;
import org.opencv.core.Size;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Created by Simonas on 2016-04-02.
 */
public class Solution {
    public Solution(FramePipeline framePipeline, Context context) throws SudokuException {
        this.framePipeline = framePipeline;
        this.corners = SudokuUtils.detectSudokuCorners(framePipeline.getDilated());
        boolean foundCorners = !corners.empty();
        if(!foundCorners) {
            SudokuUtils.logAndThrowSudokuException(TAG, "Could not detect sudoku corners.");
        }
        this.destCorners = OpenCV.mkCorners(framePipeline.getFrame().size());
        this.sudokuCanvas = OpenCV.warp(framePipeline.getFrame(), corners, destCorners);
        this.sudokuCanvas = OpenCV.resize(this.sudokuCanvas, new Size(1800, 900)); // Resizing that picture width and height could be divided by sudoku row and column count equally
        if(!Parameters.CONFIG.isProduction()) {
            SudokuUtils.printMatToPicture(sudokuCanvas, "warped.png");
            SudokuUtils.printMatToPicture(sudokuCanvas, "resizedFull.png");
        }
        Size cellSize = OpenCV.mkCellSize(sudokuCanvas.size());
        this.cellWidth = ((int) cellSize.width);
        this.cellHeight = ((int) cellSize.height);
        this.templateLibrary = new TemplateLibrary(context);
    }

    private static final String TAG = "Solution";
    private FramePipeline framePipeline;
    private MatOfPoint2f corners;
    private MatOfPoint2f destCorners;
    private Mat sudokuCanvas;
    private int cellWidth;
    private int cellHeight;
    private TemplateLibrary templateLibrary;
    private List<Rect> cellRects;
    private long sudokuSolvingDuration;

    public long getSudokuSolvingDuration() {
        return sudokuSolvingDuration;
    }

    public List<Rect> getCellRects() {
        if(cellRects == null) {
            List<Rect> cellRects = new ArrayList<>();
            for (int i = 0; i < Parameters.CELL_COUNT; i++) {
                cellRects.add(new Rect(SudokuUtils.getCol(i) * cellWidth, SudokuUtils.getRow(i) * cellHeight, cellWidth, cellHeight));
            }
            this.cellRects = cellRects;
        }
        return cellRects;
    }

    private List<SCell> getDetectedCells() throws SudokuException {
        List<SCell> sCells = new ArrayList<>();
        int i = 1;
        for (Rect cellRect: getCellRects()) {
            if(!Parameters.CONFIG.isProduction()) {
                SudokuUtils.printMatToPicture(sudokuCanvas.submat(cellRect), i++ + ".png");
            }
            sCells.add(OpenCV.detectCell(sudokuCanvas, cellRect, templateLibrary, i));
        }
        return sCells;
    }

    public Mat calculate() throws SudokuException {
        SudokuUtils.printMatToPicture(framePipeline.getWorking(), Parameters.INITIAL_SUDOKU_FILE_NAME);
        List<SCell> detectedScells = getDetectedCells();

        String unsolvedSudoku = SudokuUtils.convertDetectedSCellsToString(detectedScells);
        Log.d(TAG, "Detected sudoku: " + unsolvedSudoku);
        if(!unsolvedSudoku.matches(".*\\d+.*")) {
            SudokuUtils.logAndThrowSudokuException(TAG, "Could not detect any numbers.");
        }

        String solvedSudoku = solveSudokuFromString(unsolvedSudoku);
        if(solvedSudoku.isEmpty()) {
            SudokuUtils.logAndThrowSudokuException(TAG, "Detected sudoku is unsolvable.");
        }
        Log.d(TAG, "Solved sudoku: " + solvedSudoku);

        Mat paintedSolution = SudokuUtils.paintSolution(sudokuCanvas, solvedSudoku, getCellRects(), detectedScells);

        Mat resized = OpenCV.resize(paintedSolution, framePipeline.getWorking().size());
        Mat unwarped = OpenCV.warp(resized, destCorners, corners);
        Mat solutionMat = OpenCV.copySrcToDestWithMask(unwarped, framePipeline.getFrame(), unwarped);

        SudokuUtils.printMatToPicture(solutionMat, Parameters.SOLUTION_FILE_NAME);
        return solutionMat;
    }

    private String solveSudokuFromString(final String unsolvedSudoku) throws SudokuException {
        final String[] solvedSudoku = {""};
        ExecutorService service = Executors.newSingleThreadExecutor();
        try {
            Runnable r = new Runnable() {
                @Override
                public void run() {
                    long startTime = System.currentTimeMillis();
                    solvedSudoku[0] = new BacktrackingKudokuSolver().solve(unsolvedSudoku);
                    long stopTime = System.currentTimeMillis();
                    sudokuSolvingDuration = stopTime - startTime;
                }
            };
            Future<?> f = service.submit(r);
            f.get(Parameters.TIME_LIMIT_IN_SECONDS, TimeUnit.SECONDS);
        } catch (final TimeoutException e) {
            SudokuUtils.logAndThrowSudokuException(TAG, "Time limit excedeed while solving sudoku. Time limit: " + Parameters.TIME_LIMIT_IN_SECONDS, e);
        } catch (InterruptedException | ExecutionException e) {
            SudokuUtils.logAndThrowSudokuException(TAG, "Interruption occured while solving sudoku.");
        }
        return solvedSudoku[0];
    }
}
