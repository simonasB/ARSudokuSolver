package com.puzzleslab.arsudokusolver.Utils;

import android.graphics.Bitmap;
import android.os.Environment;
import android.util.Log;
import android.util.Pair;

import com.puzzleslab.arsudokusolver.Modules.SCell;
import com.puzzleslab.arsudokusolver.Modules.SudokuException;

import org.apache.commons.io.FileUtils;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * Created by Simonas on 2016-04-02.
 */
public final class SudokuUtils {
    private static final String TAG = "SudokuUtils";

    public static final MatOfPoint2f detectSudokuCorners(Mat input, int ratio) {
        Pair<Double, MatOfPoint> curveWithMaxArea = OpenCV.extractCurveWithMaxArea(OpenCV.coreFindContours(input));
        /*double expectedMaxArea = Imgproc.contourArea(OpenCV.mkCorners(input.size())) / ratio;
        if (curveWithMaxArea.first > expectedMaxArea) {*/
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
        } /*else {
            Log.e(TAG, "The detected area of interest was too small. Expected: " + expectedMaxArea +
            ". Was: " + curveWithMaxArea.first);
            return OpenCV.EmptyCorners;
        }*/

    public static final Mat paintSolution(Mat canvas, String solvedSudoku, List<Rect> rects, List<SCell> detectedScells) {
        for (int i = 0; i < solvedSudoku.length(); i++) {
            if(detectedScells.get(i).getValue() == 0) {
                Imgproc.putText(canvas, String.valueOf(solvedSudoku.charAt(i)),
                        new Point(rects.get(i).x + (int) rects.get(i).size().width / 4, rects.get(i).y + (int) rects.get(i).size().height / 1.2),
                        3, 3, new Scalar(0, 0, 0), 4, 8, false);
            }
        }
        return canvas;
    }

    public static final String convertDetectedSCellsToString(List<SCell> detectedScells) {
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

    public static final Mat convertFileToMat(String fileAbsolutePath, String tag) {
        byte[] bytes = new byte[0];
        try {
            bytes = FileUtils.readFileToByteArray(new File(fileAbsolutePath));
        } catch (IOException e) {
                Log.e(tag, "Could not find file to convert to Map object.", e);
        }
        return Imgcodecs.imdecode(new MatOfByte(bytes), Imgcodecs.CV_LOAD_IMAGE_UNCHANGED);
    }

    public static final void printMatToPicture(Mat mat, String imgName) {
        String externalStoragePath = "storage/sdcard1/DCIM/Camera/" + imgName;
        Bitmap bitmap = Bitmap.createBitmap(mat.cols(), mat.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(mat, bitmap);
        try {
            FileOutputStream fos = new FileOutputStream(externalStoragePath);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 85, fos);
        } catch (IOException e) {
            Log.e(TAG, "Could not print Mat object to picture.", e);
        }
    }

    public static final void logAndThrowSudokuException(String errorMessage) throws SudokuException {
        Log.e(TAG, errorMessage);
        throw new SudokuException(errorMessage);
    }

    public static final int getRow(int i) {
        return i / 9;
    }

    public static final int getCol(int i) {
        return i % 9;
    }
}
