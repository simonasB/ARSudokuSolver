package com.puzzleslab.arsudokusolver.Utils;

/**
 * Created by Simonas on 2016-04-02.
 */
public final class Parameters {
    // number of different values a cell can have before the cell is label 'ambiguous'
    public static final int ambiguitiesCount = 5;

    // how many cells are allowed to have ambiguous information before number detection process is restarted
    public static final int ambiCount = 5;

    public static final int SSIZE = 9;
    public static final int CELLCOUNT = SSIZE * SSIZE;
    public static final int VALID_SUDOKU_CELLS_SUM = 405;

    public static final int getRow(int i) {
        return i / 9;
    }

    public static final int getCol(int i) {
        return i % 9;
    }
}
