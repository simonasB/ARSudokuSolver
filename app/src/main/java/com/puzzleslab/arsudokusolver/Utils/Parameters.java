package com.puzzleslab.arsudokusolver.Utils;

import android.os.Environment;

import com.puzzleslab.arsudokusolver.Modules.Config;

/**
 * Created by Simonas on 2016-04-02.
 */
public final class Parameters {
    public static final int SSIZE = 9;
    public static final int CELL_COUNT = SSIZE * SSIZE;
    public static final int TIME_LIMIT_IN_SECONDS = 2;
    public static final String EXTERNAL_STORAGE_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + "/";
    public static final String INITIAL_SUDOKU_FILE_NAME = "InitialSudoku.png";
    public static final String SOLUTION_FILE_NAME = "Solution.png";
    public static Config CONFIG;
}
