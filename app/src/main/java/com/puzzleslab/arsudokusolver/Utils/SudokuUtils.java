package com.puzzleslab.arsudokusolver.Utils;

import android.util.Log;
import android.util.Pair;

import com.puzzleslab.arsudokusolver.Modules.SCell;

import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Rect;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Simonas on 2016-04-02.
 */
public final class SudokuUtils {
    private static final String TAG = "SudokuUtils";

    public static final MatOfPoint2f detectSudokuCorners(Mat input, int ratio) {
        Pair<Double, MatOfPoint> curveWithMaxArea = OpenCV.extractCurveWithMaxArea(OpenCV.coreFindContours(input));
        double expectedMaxArea = Imgproc.contourArea(OpenCV.mkCorners(input.size())) / ratio;
        if (curveWithMaxArea.first > expectedMaxArea) {
            MatOfPoint2f approxCurve = OpenCV.mkApproximation(new MatOfPoint2f(curveWithMaxArea.second.toArray()), 0.02);
            if (OpenCV.has4Sides(approxCurve)) {
                MatOfPoint2f corners = OpenCV.mkSortedCorners(approxCurve);
                if (OpenCV.isSomewhatSquare(corners.toList())) {
                    return corners;
                } else {
                    Log.e(TAG, "Detected " + approxCurve.size() + " shape, but it doesn't look like a sudoku.");
                    return OpenCV.EmptyCorners;
                }
            } else {
                Log.e(TAG, "Detected only " + approxCurve.size() + " shape, but need 1x4.");
                return OpenCV.EmptyCorners;
            }
        } else {
            Log.e(TAG, "The detected area of interest was too small. Expected: " + expectedMaxArea +
            ". Was: " + curveWithMaxArea.first);
            return OpenCV.EmptyCorners;
        }
    }

    public static Mat paintSolution(Mat canvas, String solvedSudoku, List<Rect> rects, List<Mat> digitsDigitalData) {
        for (int i = 0; i < solvedSudoku.length(); i++) {
            Mat numberDigitalData= digitsDigitalData.get((solvedSudoku.charAt(i) - '0' - 1));
            canvas = OpenCV.copyTo(numberDigitalData, canvas, rects.get(i));
        }
        return canvas;
    }

    public static List<Mat> getResizedDigitsDigitalData(TemplateLibrary templateLibrary, Size cellSize) {
        List<Mat> resizedDigitsDigitalData = new ArrayList<>();
        int i = 1;
        for (Mat digitTemplate : templateLibrary.getDigitsTemplates()) {
            Mat resizedDigitDigitalData = OpenCV.resize(digitTemplate, cellSize);
            resizedDigitDigitalData = OpenCV.bitwiseNot(resizedDigitDigitalData);
            CommonUtils.printMatToPicture(resizedDigitDigitalData, "resized" + i++ + ".png");
            resizedDigitsDigitalData.add(resizedDigitDigitalData);
        }
        return resizedDigitsDigitalData;
    }

    public static String convertDetectedSCellsToString(List<SCell> detectedScells) {
        String unsolvedSudoku = "";
        for(SCell scell : detectedScells) {
            if (scell.getValue() == 0) {
                unsolvedSudoku += ".";
            } else {
                unsolvedSudoku += scell.getValue();
            }
        }
        return unsolvedSudoku;
    }
}
