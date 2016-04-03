package com.puzzleslab.arsudokusolver;

/**
 * Created by Simonas on 2016-04-02.
 */
public class SudokuState {
    public SudokuState() {}
    public SudokuState(HitCounters hitCounters, DigitLibrary digitLibrary) {
        this.hitCounters = hitCounters;
        this.digitLibrary = digitLibrary;
    }

    public HitCounters getHitCounters() {
        return hitCounters;
    }

    public void setHitCounters(HitCounters hitCounters) {
        this.hitCounters = hitCounters;
    }

    public DigitLibrary getDigitLibrary() {
        return digitLibrary;
    }

    public void setDigitLibrary(DigitLibrary digitLibrary) {
        this.digitLibrary = digitLibrary;
    }

    private HitCounters hitCounters = new HitCounters();
    private DigitLibrary digitLibrary = new DigitLibrary();
}
