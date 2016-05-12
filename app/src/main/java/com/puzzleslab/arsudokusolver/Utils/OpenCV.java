package com.puzzleslab.arsudokusolver.Utils;

import android.util.Pair;

import com.puzzleslab.arsudokusolver.Modules.SCell;
import com.puzzleslab.arsudokusolver.Modules.Triplet;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Range;
import org.opencv.core.Rect;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by Simonas on 2016-04-02.
 */
public final class OpenCV {

    public static final MatOfPoint2f EmptyCorners = new MatOfPoint2f();

    public static final Mat toMat(Integer[] buffer, Size size) {
        return toMat(buffer, ((int) size.width), ((int) size.height));
    }

    public static final Mat toMat(Integer[] buffer, int width, int height) {
        Mat m = new Mat(height, width, CvType.CV_8UC1);
        ByteBuffer byteBuffer = ByteBuffer.allocate(4);
        byteBuffer.order(ByteOrder.nativeOrder());
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                byteBuffer.putInt(buffer[i + width * j]);
                byteBuffer.flip();
                m.put(j, i, byteBuffer.array());
                byteBuffer.clear(); // Otherwise buffer overflows
            }
        }
        return m;
    }

    public static final Mat resize(Mat s, Size size) {
        Mat dest = new Mat();
        Imgproc.resize(s, dest, size);
        return dest;
    }

    public static final Pair<Integer, Double> matchTemplate(Mat candidate, int number, Mat withNeedle) {
        Mat normedCandidateF = norm(candidate);
        Mat normedNeedleF = norm(withNeedle);

        CommonUtils.printMatToPicture(normedCandidateF, "normedCandidateF.png");
        CommonUtils.printMatToPicture(normedNeedleF, "normedNeedleF" + number + ".png");

        int width = candidate.cols() - withNeedle.cols() + 1;
        int height = candidate.rows() - withNeedle.rows() + 1;
        Mat resultImage = new Mat(width, height, CvType.CV_32FC1);
        Imgproc.matchTemplate(normedCandidateF, normedNeedleF, resultImage, Imgproc.TM_SQDIFF);
        Core.MinMaxLocResult minMaxResult = Core.minMaxLoc(resultImage);

        return new Pair<>(number, minMaxResult.minVal);
    }

    public static final Mat norm(Mat mat) {
        return adaptiveThreshold(dilate(gaussianBlur(mat)), 255, 5); // not sure if this is correct
    }

    public static final Mat gaussianBlur(Mat input) {
        Mat dest = new Mat();
        Imgproc.GaussianBlur(input, dest, new Size(11, 11), 0);
        return dest;
    }

    public static final Mat dilate(Mat input) {
        Mat output = new Mat();
        Point anchor = new Point(-1, -1);
        Imgproc.dilate(input, output, mkKernel(3, new byte[]{0, 1, 0, 1, 1, 1, 0, 1, 0}), anchor, 2);
        return output;
    }

    public static final Mat mkKernel(int size, byte[] kernelData) {
        Mat kernel = new Mat(size, size, CvType.CV_8U);
        kernel.put(0, 0, kernelData);
        return kernel;
    }

    public static final Mat adaptiveThreshold(Mat input, double maxValue, int blockSize) {
        Mat thresholded = new Mat();
        Imgproc.adaptiveThreshold(input, thresholded, maxValue, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY, blockSize, 2);
        return thresholded;
    }

    public static final Mat copySrcToDestWithMask(Mat source, Mat destination, Mat pattern) {
        source.copyTo(destination, pattern);
        return destination;
    }

    public static final Mat toGray(Mat input) {
        Mat grayed = new Mat();
        Imgproc.cvtColor(input, grayed, Imgproc.COLOR_BGR2GRAY);
        return grayed;
    }

    public static final Mat bitwiseNot(Mat input) {
        Mat output = new Mat();
        Core.bitwise_not(input, output);
        return output;
    }

    public static final Mat erode(Mat input) {
        Mat output = new Mat();
        Double erSize = 0.0;
        Mat m = Imgproc.getStructuringElement(Imgproc.MORPH_CROSS, new Size(2 * erSize + 1, 2 * erSize + 1),
                new Point(erSize,erSize));
        Imgproc.erode(input, output, m);
        return output;
    }

    public static final Pair<Double, MatOfPoint> extractCurveWithMaxArea(List<MatOfPoint> curveList) {
        List<Pair<Double, MatOfPoint>> curvesAreas = new ArrayList<>();
        for (MatOfPoint curve: curveList) {
            curvesAreas.add(new Pair<>(Imgproc.contourArea(curve), curve));
        }
        Comparator<Pair<Double, MatOfPoint>> comparator = new Comparator<Pair<Double, MatOfPoint>>() {
            @Override
            public int compare(Pair<Double, MatOfPoint> lhs, Pair<Double, MatOfPoint> rhs) {
                return rhs.first.compareTo(lhs.first);
            }
        };
        Collections.sort(curvesAreas, comparator);
        return curvesAreas.get(0);
    }

    public static final List<MatOfPoint> coreFindContours(Mat input) {
        List<MatOfPoint> contours = new ArrayList<>();
        Imgproc.findContours(input, contours, new Mat(), Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);
        return contours;
    }

    public static final MatOfPoint2f mkCorners(Size size) {
        return new MatOfPoint2f(
                new Point(0,0), new Point(size.width, 0),
                new Point(size.width, size.height), new Point(0, size.height));
    }
    // epsilon default value: 0.02
    public static final MatOfPoint2f mkApproximation(MatOfPoint2f curve, double epsilon) {
        double arcLength = Imgproc.arcLength(curve, true);
        MatOfPoint2f approxCurve = new MatOfPoint2f();
        Imgproc.approxPolyDP(curve, approxCurve, epsilon * arcLength, true);
        return approxCurve;
    }

    public static final boolean has4Sides(MatOfPoint2f needle) {
        return needle.size().equals(new Size(1, 4));
    }

    public static final MatOfPoint2f mkSortedCorners(MatOfPoint2f points) {
        List<Point> pointsAsList = points.toList();
        Comparator<Point> comparatorBySum = new Comparator<Point>() {
            @Override
            public int compare(Point lhs, Point rhs) {
                return (lhs.x + lhs.y) > (rhs.y + rhs.x) ? 1 : (lhs.x + lhs.y) < (rhs.y + rhs.x) ? -1 : 0;
            }
        };
        Collections.sort(pointsAsList, comparatorBySum);
        /*List<Point> sortedByDifference = pointsAsList;
        Comparator<Point> comparatorByDifference = new Comparator<Point>() {
            @Override
            public int compare(Point lhs, Point rhs) {
                return (lhs.x - lhs.y) > (rhs.y - rhs.x) ? 1 : (lhs.x - lhs.y) < (rhs.y - rhs.x) ? -1 : 0;
            }
        };
        Collections.sort(sortedByDifference, comparatorByDifference);
        */
        Point bottomLeft = pointsAsList.get(0);
        Point topLeft = pointsAsList.get(1);
        Point bottomRight = pointsAsList.get(2);
        Point topRight = pointsAsList.get(3);

        return new MatOfPoint2f(bottomLeft, bottomRight, topRight, topLeft);
    }

    private static final double calcAngle(Point a, Point b) {
        return Math.atan2(b.y - a.y, b.x - a.x) * 180 / Math.PI;
    }

    public static final boolean isSomewhatSquare(List<Point> corners) {
        return Math.abs(calcAngle(corners.get(0), corners.get(1)) - calcAngle(corners.get(3), corners.get(2))) < 10 &&
                Math.abs(calcAngle(corners.get(0), corners.get(3)) - calcAngle(corners.get(1), corners.get(2))) < 10;
    }

    public static final Mat warp(Mat input, MatOfPoint2f srcCorners, MatOfPoint2f destCorners) {
        Mat transformationMatrix = Imgproc.getPerspectiveTransform(srcCorners, destCorners);
        Mat dest = new Mat();
        Imgproc.warpPerspective(input, dest, transformationMatrix, input.size());
        CommonUtils.printMatToPicture(dest, "warped.jpg");
        return dest;
    }

    public static final Size mkCellSize(Size sudokuSize) {
        return new Size(sudokuSize.width / Parameters.SSIZE, sudokuSize.height / Parameters.SSIZE);
    }

    public static final SCell detectCell(Mat sudokuPlane, Rect roi, TemplateLibrary templateLibrary) {
        Mat contour = extractContour(sudokuPlane.submat(roi));
        CommonUtils.printMatToPicture(sudokuPlane.submat(roi), "aaa.png");
        if(contour == null) {
            return new SCell(0, 0.0, roi);
        }
        Pair<Integer, Double> valueAndQuality = templateLibrary.detectNumber(contour);
        return new SCell(valueAndQuality.first, valueAndQuality.second, roi);
    }

    public static final Mat extractContour(Mat coloredCell) {
        CommonUtils.printMatToPicture(coloredCell, "coloredCell.jpg");
        Mat cell = toGray(coloredCell);
        Mat cellData = getCellData(cell);
        CommonUtils.printMatToPicture(cellData, "cellData.jpg");
        Point center = calcCellCentre(cellData);
        Pair<Double, Double> minMaxArea = minMaxArea(cellData);
        double minArea = minMaxArea.first;
        double maxArea = minMaxArea.second;
        Mat preprocessed = preprocess(cellData);
        CommonUtils.printMatToPicture(preprocessed, "preprocessed.jpg");
        return findCellContour(preprocessed, center, minArea, maxArea);
    }

    public static final Mat getCellData(Mat cellRawData) {
        double height = cellRawData.size().height;
        double width = cellRawData.size().width;
        return new Mat(cellRawData, new Range((int) (height * 0.1), (int) (height * 0.9)),
                new Range((int) (width * 0.1), (int) (width * 0.9)));
    }

    public static final Pair<Double, Double> minMaxArea(Mat cellRawData) {
        double cellArea = getCellData(cellRawData).size().area();
        return new Pair<>(0.15 * cellArea, 0.5 * cellArea);
    }

    public static final Point calcCellCentre(Mat cellRawData) {
        Mat cellData = getCellData(cellRawData);
        return new Point(cellData.size().width / 2, cellData.size().height / 2);
    }

    public static final Mat preprocess(Mat input) {
        /*Mat equalized = equalizeHist(input);
        CommonUtils.printMatToPicture(equalized, "equalized.jpg");
        Mat blurred = gaussianBlur(equalized);
        CommonUtils.printMatToPicture(equalized, "blurred.jpg");*/
        Mat thresholded = OpenCV.threshold(input);
        CommonUtils.printMatToPicture(thresholded, "thresholded.jpg");
        Mat inverted = bitwiseNot(thresholded);
        CommonUtils.printMatToPicture(inverted, "inverted.jpg");
        return inverted;
    }

    public static final Mat equalizeHist(Mat input) {
        Mat output = new Mat();
        Imgproc.equalizeHist(input, output);
        return output;
    }

    public static final Mat threshold(Mat input) {
        Mat output = new Mat();
        Imgproc.threshold(input, output, 127, 255, Imgproc.THRESH_BINARY);
        return output;
    }

    public static final Mat findCellContour(Mat original, Point center, double minArea, double maxArea) {
        Mat input = new Mat();
        original.copyTo(input);
        List<MatOfPoint> contours = coreFindContours(input);
        MatOfPoint bestFit = findBestFit(contours, center, minArea, maxArea);
        if(bestFit == null) {
            return null;
        }
        return original.submat(Imgproc.boundingRect(bestFit));
    }

    public static final MatOfPoint findBestFit(
            List<MatOfPoint> contours, Point center, double minArea, double maxArea) {
        List<Pair<Double, MatOfPoint>> candidates = new ArrayList<>();
        for (MatOfPoint contour: contours) {
            Rect boundingRect = Imgproc.boundingRect(contour);
            double area = boundingRect.area();
            if (minArea < area && area < maxArea && boundingRect.contains(center)) {
                MatOfPoint2f curve = new MatOfPoint2f();
                curve.fromArray(contour.toArray());
                double countourArea = Imgproc.contourArea(curve);
                candidates.add(new Pair<>(countourArea, contour));
            }
        }
        Comparator<Pair<Double, MatOfPoint>> comparator = new Comparator<Pair<Double, MatOfPoint>>() {
            @Override
            public int compare(Pair<Double, MatOfPoint> lhs, Pair<Double, MatOfPoint> rhs) {
                return lhs.first.compareTo(rhs.first);
            }
        };
        Collections.sort(candidates, comparator);

        if(candidates.size() == 0) {
            return null;
        }

        return candidates.get(candidates.size() - 1).second;
    }

    public static final Mat copyMat(Mat orig) {
        Mat dest = new Mat();
        orig.copyTo(dest);
        return dest;
    }

    public static final Mat copyTo(Mat data, Mat canvas, Rect roi) {
        Mat destinationROI = new Mat(canvas, roi);
        data.copyTo(destinationROI);
        //data.copyTo(canvas.rowRange(roi.y, roi.y + (int)roi.size().height).colRange(roi.x, roi.x + (int) roi.size().width));
        return canvas;
    }
}
