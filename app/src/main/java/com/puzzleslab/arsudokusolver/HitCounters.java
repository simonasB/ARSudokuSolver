package com.puzzleslab.arsudokusolver;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Simonas on 2016-04-02.
 */
public class HitCounters {
    public HitCounters() {

    }

    public  HitCounters(Map<Integer, Map<Integer, Integer>> hitCounters) {
        this.hitCounters = hitCounters;
    }

    public Map<Integer, Map<Integer, Integer>> getHitCounters() {
        return hitCounters;
    }

    public void setHitCounters(Map<Integer, Map<Integer, Integer>> hitCounters) {
        this.hitCounters = hitCounters;
    }
    private Map<Integer, Map<Integer, Integer>> hitCounters = new HashMap<>();
}
