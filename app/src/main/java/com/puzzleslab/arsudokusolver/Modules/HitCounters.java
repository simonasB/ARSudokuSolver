package com.puzzleslab.arsudokusolver.Modules;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Simonas on 2016-04-02.
 */
// contains 81 elements, on each position the number of hits for the digit is counted
// this means if for a cell 2 times a 3 is counted, the Map looks like this:
// Map(... PosAtSudoku -> Map(3 -> 2) ...)
public class HitCounters extends HashMap<Integer, Map<Integer, Integer>>{
    public HitCounters() {
        super();
    }
}
