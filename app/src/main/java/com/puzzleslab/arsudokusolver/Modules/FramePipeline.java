package com.puzzleslab.arsudokusolver.Modules;

import com.puzzleslab.arsudokusolver.Utils.OpenCV;
import com.puzzleslab.arsudokusolver.Utils.SudokuUtils;

import org.opencv.core.Mat;

/**
 * Created by Simonas on 2016-04-02.
 */
public class FramePipeline {
    private Long start;
    private Mat frame;
    private Mat working;
    private Mat grayed;
    private Mat blurred;
    private Mat thresholded;
    private Mat inverted;
    private Mat dilated;

    public FramePipeline(Mat frame) {
        this.frame = frame;
        this.start = System.nanoTime();
        this.working = OpenCV.copySrcToDestWithMask(frame, new Mat(), frame);
        SudokuUtils.printMatToPicture(working, "1workingframe.png");
        this.grayed = OpenCV.toGray(frame);
        SudokuUtils.printMatToPicture(grayed, "1grayedframe.png");
        this.blurred = OpenCV.gaussianBlur(this.grayed);
        SudokuUtils.printMatToPicture(blurred, "1blurredframe.png");
        this.thresholded = OpenCV.adaptiveThreshold(this.blurred, 255, 5);
        SudokuUtils.printMatToPicture(thresholded, "1thresholdedframe.png");
        this.inverted = OpenCV.bitwiseNot(this.thresholded);
        SudokuUtils.printMatToPicture(inverted, "1invertedframe.png");
        this.dilated = OpenCV.dilate(inverted);
        SudokuUtils.printMatToPicture(dilated, "1dilatedframe.png");
    }

    public Long getStart() {
        return start;
    }

    public void setStart(Long start) {
        this.start = start;
    }

    public Mat getDilated() {
        return dilated;
    }

    public void setDilated(Mat dilated) {
        this.dilated = dilated;
    }

    public Mat getInverted() {
        return inverted;
    }

    public void setInverted(Mat inverted) {
        this.inverted = inverted;
    }

    public Mat getThresholded() {
        return thresholded;
    }

    public void setThresholded(Mat thresholded) {
        this.thresholded = thresholded;
    }

    public Mat getBlurred() {
        return blurred;
    }

    public void setBlurred(Mat blurred) {
        this.blurred = blurred;
    }

    public Mat getGrayed() {
        return grayed;
    }

    public void setGrayed(Mat grayed) {
        this.grayed = grayed;
    }

    public Mat getWorking() {
        return working;
    }

    public void setWorking(Mat working) {
        this.working = working;
    }

    public Mat getFrame() {
        return frame;
    }

    public void setFrame(Mat frame) {
        this.frame = frame;
    }
}
