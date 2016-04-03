package com.puzzleslab.arsudokusolver;

/**
 * Created by Simonas on 2016-03-11.
 */
public class BacktrackingUniqueSolverInProgress {

    public static final int SUDOKU_SIZE = 9;

    public static final int BLOCK_SIZE = 3;

    public static final int EMPTY_CELL = 0;

    private static boolean[] _initialSudoku;

    private static int[] _solution;

    private static int x = -1;

    private static int y = 0;

    private boolean movedForward(int x, int y) {
        do {
            x++;
            if (x >= SUDOKU_SIZE) { // Check if not out of range
                x = 0;
                y++;
            }
            if (y == SUDOKU_SIZE) {
                return false; // Success, sudoku is solved
            }
        } while (_initialSudoku[y * SUDOKU_SIZE + x]);
        return true; // Moved forward
    }


    //y * SUDOKU_SIZE - (SUDOKU_SIZE - x)
    private static void init(String sudoku) {
        validateInitialSudoku(sudoku);

        _initialSudoku = new boolean[sudoku.length()];
        _solution = new int[sudoku.length()];

        for (int i = 0; i < sudoku.length(); i++) {
            int cell = sudoku.charAt(i) >= '1' && sudoku.charAt(i) <= '9' ? sudoku.codePointAt(i) - '0' : 0; // number from 0 to 9
            _solution[i] = cell;
            if (cell != 0)
                _initialSudoku[i] = true;
        }
    }

    public String getSolution(String sudoku) {
        String solution = "";

        init(sudoku);

        do {
            x++;
            if (x >= SUDOKU_SIZE) { // Check if not out of range
                x = 0;
                y++;
            }
            }
         while (_initialSudoku[y * SUDOKU_SIZE + x]);

        for (int i = 1; i <= SUDOKU_SIZE; i++)
            solve(i);

        for (int i = 0; i < sudoku.length(); i++) {
         solution += _solution[i];
        }

        return solution;
    }

    private void solve(int value) {
        if (!validateSolution(value))
            return;
        _solution[y * SUDOKU_SIZE + x] = value;
        do {
            x++;
            if (x >= SUDOKU_SIZE) { // Check if not out of range
                x = 0;
                y++;
            }
            if (y == SUDOKU_SIZE) {
                return;
            }
        } while (_initialSudoku[y * SUDOKU_SIZE + x]);
        for (int i = 1; i <= SUDOKU_SIZE; i++)
            solve(i);
        _solution[y * SUDOKU_SIZE + x] = EMPTY_CELL;
    }

    private static boolean validateSolution(int value) {
        for (int i = SUDOKU_SIZE * y; i < SUDOKU_SIZE * SUDOKU_SIZE; i += SUDOKU_SIZE) {
            if(i != y * SUDOKU_SIZE + x && _solution[i] == value)
                return false;
        }

        for (int i = SUDOKU_SIZE * y; i < SUDOKU_SIZE + SUDOKU_SIZE * y; i++) {
            if(i != y * SUDOKU_SIZE + x && _solution[i] == value)
                return false;
        }

        int blockHorizontalStartingPoint = x / BLOCK_SIZE;

        for (int i = SUDOKU_SIZE * y + blockHorizontalStartingPoint * BLOCK_SIZE; i < SUDOKU_SIZE * y + SUDOKU_SIZE * 3; i += SUDOKU_SIZE)
            for (int j = 0; j < 2; j++) {
                if(_solution[i + j] == value)
                    return false;
            }
        return true;
    }

    private static void validateInitialSudoku(String sudoku) {
        if (sudoku.length() != SUDOKU_SIZE * SUDOKU_SIZE)
            throw new IllegalArgumentException("Invalid string size. Size should be 81 but is " + sudoku.length() + ".");
        if (!sudoku.matches("^[.0-9]+$"))
            throw new IllegalArgumentException("Invalid string content. String should only contain dots '.' or digits.");
    }
}
