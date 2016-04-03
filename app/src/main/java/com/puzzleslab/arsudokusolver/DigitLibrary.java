package com.puzzleslab.arsudokusolver;

import android.util.Pair;

import org.opencv.core.Mat;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Simonas on 2016-04-02.
 */
public class DigitLibrary {

    public Map<Integer, Pair<Double, Mat>> getDigitLibrary() {
        return digitLibrary;
    }

    public void setDigitLibrary(Map<Integer, Pair<Double, Mat>> digitLibrary) {
        this.digitLibrary = digitLibrary;
    }

    private Map<Integer, Pair<Double, Mat>> digitLibrary = new HashMap<>();
}
