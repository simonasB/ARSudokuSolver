package com.puzzleslab.arsudokusolver.Modules;

import com.puzzleslab.arsudokusolver.Modules.SCell;

import org.opencv.core.Mat;
import org.opencv.core.Point;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by simonas_b on 4/5/2016.
 */
public class SudokuFrame {
    private Mat in;
    private List<SCell> cells = new ArrayList<>();
    private List<Point> corners = new ArrayList<>();

    public SudokuFrame(Mat in, List<SCell> cells, List<Point> corners) {
        this.in = in;
        this.cells = cells;
        this.corners = corners;
    }

    public Mat getIn() {
        return in;
    }

    public void setIn(Mat in) {
        this.in = in;
    }

    public List<SCell> getCells() {
        return cells;
    }

    public void setCells(List<SCell> cells) {
        this.cells = cells;
    }

    public List<Point> getCorners() {
        return corners;
    }

    public void setCorners(List<Point> corners) {
        this.corners = corners;
    }

    public List<SCell> getDetectedCells() {
        List<SCell> detectedCells = new ArrayList<>();
        for (SCell cell : cells) {
            if (cell.getValue() != 0) {
                detectedCells.add(cell);
            }
        }
        return detectedCells;
    }
}
