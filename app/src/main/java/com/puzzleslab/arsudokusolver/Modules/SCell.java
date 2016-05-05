package com.puzzleslab.arsudokusolver.Modules;

import org.opencv.core.Rect;

/**
 * Created by Simonas on 2016-04-03.
 */
public class SCell {
    private int value;
    private double quality;
    private Rect roi;

    public SCell(int value, double quality, Rect roi) {
        if (0 <= value && value <= 9 && quality >=0) {
            this.value = value;
            this.quality = quality;
            this.roi = roi;
        }
        else {
            try {
                throw new Exception("Cannot SCell object. Requirements are not met. Value: " + value +
                ". Quality: " + quality + ".");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public double getQuality() {
        return quality;
    }

    public void setQuality(double quality) {
        this.quality = quality;
    }

    public Rect getRoi() {
        return roi;
    }

    public void setRoi(Rect roi) {
        this.roi = roi;
    }
}
