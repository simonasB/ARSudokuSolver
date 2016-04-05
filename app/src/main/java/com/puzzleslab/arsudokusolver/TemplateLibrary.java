package com.puzzleslab.arsudokusolver;

import android.util.Log;
import android.util.Pair;

import org.opencv.core.Mat;
import org.opencv.core.Size;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Simonas on 2016-04-02.
 */
public final class TemplateLibrary {
    private static final String TAG = "TemplateLibrary";
    private static final double templateWidth = 50.0;
    private static final double templateHeight = 25.0;

    private static final Size templateSize = new Size(templateWidth, templateHeight);

    public static final String TemplateResource = "templates.csv";

    private static final List<Mat> templatesList() {
        String csvFile = "templates.csv";
        BufferedReader br = null;
        String line;
        String cvsSplitBy = ",";
        List<Integer[]> digits = new ArrayList<>();
        int i = 0;
        List<Mat> matList = new ArrayList<>();
        try {
            br = new BufferedReader(new FileReader(csvFile));
            while ((line = br.readLine()) != null) {
                // use comma as separator
                String[] numbers = line.split(cvsSplitBy);
                for (int j = 0; j < numbers.length; j++) {
                    if (numbers[j] == "0") {
                        digits.get(i)[j] = 0;
                    }
                    else {
                        digits.get(i)[j] = 255;
                    }
                }
                i++;
            }
            matList.add(OpenCV.toMat(digits.get(i), templateSize));
        } catch (FileNotFoundException e) {
            Log.e(TAG, "Could not find " + csvFile, e);
        } catch (IOException e) {
            Log.e(TAG, "Unexpected error occured.", e);
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    Log.e(TAG, "Unexpected error occured while closing file.", e);
                }
            }
        }
        return matList;
    }

    public static final Pair<Integer, Double> detectNumber(Mat candidate) {
        Mat resizedCandidate = OpenCV.resize(candidate, TemplateLibrary.templateSize); // since templates are 25 x 50
        List<Pair<Integer, Double>> results = new ArrayList<>();
        for (int i = 0; i < templatesList().size(); i++) {
            results.add(OpenCV.matchTemplate(resizedCandidate, i + 1, templatesList().get(i)));
        }
        Collections.sort(results, (a, b) -> a.second.compareTo(b.second));

        return results.get(results.size() - 1);
    }
}
