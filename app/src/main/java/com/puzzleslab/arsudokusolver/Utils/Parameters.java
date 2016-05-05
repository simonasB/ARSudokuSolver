package com.puzzleslab.arsudokusolver.Utils;

import com.puzzleslab.arsudokusolver.Modules.SudokuState;

/**
 * Created by Simonas on 2016-04-02.
 */
public final class Parameters {
    // number of different values a cell can have before the cell is label 'ambiguous'
    public static final int ambiguitiesCount = 5;

    // how many cells are allowed to have ambiguous information before number detection process is restarted
    public static final int ambiCount = 5;

    // numbers won't get any larger in the status matrix than this number
    public static final int topCap = 5;

    // least number of matches necessary to identify one number
    // if you have a good camera, take 1 to get fast response
    public static final int cap = 3;

    public static final int minHits = 22;

    public static final int SSIZE = 9;
    public static final int CELLCOUNT = SSIZE * SSIZE;
    public static final int VALID_SUDOKU_CELLS_SUM = 405;

    //public static final Range<Integer> range = Range.create(0, SSIZE - 1);
    //public static final Range<Integer> digitRange = Range.create(0, SSIZE);
    //public static final Range<Integer> cellRange = Range.create(0, SSIZE * SSIZE - 1);

    public static final SudokuState DefaultState = new SudokuState();

    public static final int getRow(int i) {
        return i / 9;
    }

    public static final int getCol(int i) {
        return i % 9;
    }
}
