package com.puzzleslab.arsudokusolver.Utils;

import android.util.Log;
import android.util.Pair;

import com.puzzleslab.arsudokusolver.Modules.DigitLibrary;
import com.puzzleslab.arsudokusolver.Modules.HitCounters;
import com.puzzleslab.arsudokusolver.Modules.SCell;
import com.puzzleslab.arsudokusolver.Solvers.BacktrackingSimpleSolver;
import com.puzzleslab.arsudokusolver.Modules.SudokuState;
import com.puzzleslab.arsudokusolver.Modules.Triplet;

import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Rect;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Simonas on 2016-04-02.
 */
public final class SudokuUtils {
    private static final String TAG = "SudokuUtils";

    public static final DigitLibrary mergeDigitLibrary(Mat sudokuCanvas, DigitLibrary digitLibrary, List<SCell> detectedCells) {
        List<SCell> hits = new ArrayList<>();
        for (SCell sCell : detectedCells) {
            if ((sCell.getValue() != 0) && (sCell.getQuality() < digitLibrary.get(sCell.getValue()).first)) { // lower means "better"
                hits.add(sCell);
            }
        }
        Map<Integer, List<SCell>> grouped = new HashMap<>();
        for (SCell sCell : hits) {
            int key = sCell.getValue();
            List<SCell> value = grouped.get(key);
            if (value == null) {
                value = new ArrayList<>();
                value.add(sCell);
            }
            else {
                value.add(sCell);
            }
            grouped.put(key, value);
        }
        Map<Integer, SCell> optimal = new HashMap<>();
        for (Map.Entry<Integer, List<SCell>> entry : grouped.entrySet()) {
            SCell temp = null;
            double maxQuality = -999999;
            for (SCell sCell : entry.getValue()) {
                if (sCell.getQuality() > maxQuality) {
                    maxQuality = sCell.getQuality();
                    temp = sCell;
                }
            }
            optimal.put(entry.getKey(), temp);
        }
        DigitLibrary dLibrary = new DigitLibrary();
        for (Map.Entry<Integer, SCell> entry : optimal.entrySet()) {
            SCell sCell = entry.getValue();
            if (digitLibrary.get(sCell.getValue()).first > sCell.getQuality()) {
                Mat newData = OpenCV.copyMat(sudokuCanvas.submat(sCell.getRoi()));
                dLibrary.put(sCell.getValue(), new Pair<>(sCell.getQuality(), newData));
            }
        }
        return dLibrary;
    }

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

    public static final HitCounters mergeHits(
            HitCounters currentHitCounters, List<Integer> detections) {
        HitCounters hitCounter = new HitCounters();
        for (int i = 0; i < detections.size(); i++) {
            Map<Integer, Integer> frequencies = currentHitCounters.get(i);
            Map<Integer, Integer> mergedHits = new HashMap<>();
            mergedHits.put(detections.get(i), frequencies.get(detections.get(i)) + 1);
            hitCounter.put(i, mergedHits);
        }
        return resetHitsIfThereAreTooMuchAmbiguities(hitCounter);
    }

    public static final HitCounters resetHitsIfThereAreTooMuchAmbiguities(
            HitCounters counters) {
        int cellAmbiguities = 0;
        for (Map.Entry<Integer, Map<Integer, Integer>> entry : counters.entrySet()) {
            if (entry.getValue().size() > Parameters.ambiguitiesCount) {
                cellAmbiguities++;
            }
        }
        if (cellAmbiguities > Parameters.ambiCount) {
            Log.e(TAG, "Too many ambiguities " + cellAmbiguities + ", resetting...");
            return Parameters.DefaultState.getHitCounters();           //TODO: recheck HitCounters class logic. Looks strange getHitCounters().getHitCounters()
        }
        return counters;
    }

    public static final Triplet<List<Character>, List<SCell>, SudokuState> computeSolution(
            HitCounters hitCounters, DigitLibrary digitLibrary, int cap, int minHits, Long maxDuration) {
        Pair<List<Character>, SudokuState> solutionAndState = doIt(hitCounters, digitLibrary, cap, minHits, maxDuration);
        List<SCell> sCells = toSolutionCells(digitLibrary, solutionAndState.first);
        return new Triplet<>(solutionAndState.first, sCells, solutionAndState.second);
    }

    public static final Pair<List<Character>, SudokuState> doIt(
            HitCounters hitCounters, DigitLibrary digitLibrary, int cap,
            int minHits, Long maxDuration) {
        int nrDetections = 0;
        for (Map.Entry<Integer, Map<Integer, Integer>> hitCounter: hitCounters.entrySet()) {
            for (Map.Entry<Integer, Integer> entry : hitCounter.getValue().entrySet()) {
                if (entry.getKey() != 0 && entry.getValue() >= cap) {
                    nrDetections++;
                    break;
                }
            }
        }
        if (nrDetections >= minHits) {
            Log.i(TAG, "NrDetections: " + nrDetections + " minHits: " + minHits);
            List<Character> sudokuToSolve = mkSudokuMatrix(hitCounters, cap);
            String solvedSudoku = new BacktrackingSimpleSolver().solve(charListToString(sudokuToSolve));
            List<Character> solvedSudokuInCharsList = stringToCharList(solvedSudoku);
            return new Pair<>(solvedSudokuInCharsList, new SudokuState(hitCounters, digitLibrary));
        } else {
            List<Character> emptyList = new ArrayList<>();
            return new Pair<>(emptyList, new SudokuState(hitCounters, digitLibrary));
        }
    }

    private static final String charListToString(List<Character> chars) {
        StringBuilder builder = new StringBuilder(chars.size());
        for(Character ch: chars) {
            builder.append(ch);
        }
        return builder.toString();
    }

    private static final ArrayList<Character> stringToCharList(String string) {
        ArrayList<Character> chars = new ArrayList<>();
        for (char c : string.toCharArray()) {
            chars.add(c);
        }
        return chars;
    }

    public static final List<Character> mkSudokuMatrix(HitCounters hitCounters, int cap) {
        List<Integer> values = new ArrayList<>();
        List<Character> chars = new ArrayList<>();
        for (int i = 0; i < Parameters.CELLCOUNT; i++) {
            Map<Integer, Integer> hitCounter = hitCounters.get(i);
            for(Map.Entry<Integer, Integer> entry : hitCounter.entrySet()) {
                if(withCap(cap, entry.getValue())) {
                    values.add(entry.getKey());
                }
            }
            Collections.shuffle(values);
            if (values.get(0) != null) {
                chars.add((char)(values.get(0) + 48));
            }
            else {
                chars.add((char)(46));
            }
        }
        return chars;
    }

    private static final boolean withCap(int cap, int v) {
        return v >= cap;
    }

    public static final List<SCell> toSolutionCells(DigitLibrary digitLibrary, List<Character> digitSolution) {
        List<SCell> allCells = new ArrayList<>();
        for (int i = 0; i < Parameters.CELLCOUNT; i++) {
            int value = Character.getNumericValue(digitSolution.get(i));
            if(value != 0){
                allCells.add(new SCell(value, 0, new Rect()));
            }
        }
        return allCells;
    }

    public static final Mat paintSolution(Mat canvas, List<SCell> someSolution, DigitLibrary digitLibrary, List<Rect> rects) {
        int cellsValuesSum = 0;
        for(SCell cell : someSolution) {
            cellsValuesSum += cell.getValue();
        }
        if(cellsValuesSum == Parameters.VALID_SUDOKU_CELLS_SUM) {
            for(int i = 0; i < someSolution.size(); i++) {
                int cellValue = someSolution.get(i).getValue();
                Mat numberDigitalData = digitLibrary.get(cellValue).second;
                if(numberDigitalData == null) {
                    Log.w(TAG, "Number digital data in digit library could not be found. Digit " + cellValue);
                } else {
                    canvas = OpenCV.copyTo(digitLibrary.get(cellValue).second, canvas, rects.get(i));
                }
            }
        }
        return canvas;
    }
}
