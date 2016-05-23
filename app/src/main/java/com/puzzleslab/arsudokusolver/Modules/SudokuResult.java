package com.puzzleslab.arsudokusolver.modules;

/**
 * Created by Simonas on 2016-05-21.
 */
public class SudokuResult {
    private long duration;
    private String initial;
    private String solved;
    private SudokuType type;

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public String getInitial() {
        return initial;
    }

    public void setInitial(String initial) {
        this.initial = initial;
    }

    public String getSolved() {
        return solved;
    }

    public void setSolved(String solved) {
        this.solved = solved;
    }

    public SudokuType getType() {
        return type;
    }

    public void setType(SudokuType type) {
        this.type = type;
    }
}
