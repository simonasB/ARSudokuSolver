package com.puzzleslab.arsudokusolver.Modules;

/**
 * Created by simonas_b on 4/5/2016.
 */
public class SSuccess implements SudokuResult {
    private InputFrame inputFrame;
    private SudokuFrame sudokuFrame;

    public SolutionFrame getSolutionFrame() {
        return solutionFrame;
    }

    public void setSolutionFrame(SolutionFrame solutionFrame) {
        this.solutionFrame = solutionFrame;
    }

    public SudokuFrame getSudokuFrame() {
        return sudokuFrame;
    }

    public void setSudokuFrame(SudokuFrame sudokuFrame) {
        this.sudokuFrame = sudokuFrame;
    }

    public InputFrame getInputFrame() {
        return inputFrame;
    }

    public void setInputFrame(InputFrame inputFrame) {
        this.inputFrame = inputFrame;
    }

    private SolutionFrame solutionFrame;

    public SSuccess(InputFrame inputFrame, SudokuFrame sudokuFrame, SolutionFrame solutionFrame) {
        this.inputFrame = inputFrame;
        this.sudokuFrame = sudokuFrame;
        this.solutionFrame = solutionFrame;
    }


}
