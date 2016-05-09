package com.puzzleslab.arsudokusolver.Modules;

/**
 * Created by simonas_b on 5/9/2016.
 */
public class SudokuException extends Exception {
    private static final long serialVersionUID = 4664456874499611218L;
    private String errorCode = "Sudoku_Exception";

    public SudokuException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return this.errorCode;
    }
}
