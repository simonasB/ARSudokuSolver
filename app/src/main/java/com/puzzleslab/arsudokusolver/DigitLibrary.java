package com.puzzleslab.arsudokusolver;

import android.util.Pair;

import org.opencv.core.Mat;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Simonas on 2016-04-02.
 */
/**
 * records for each number from 0 to 9 the best hit (quality) along with its digital data
 */
public class DigitLibrary extends HashMap<Integer, Pair<Double, Mat>>{
    public DigitLibrary() {
        super();
    }
}
