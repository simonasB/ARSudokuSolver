package com.puzzleslab.arsudokusolver.services;

import android.content.Context;
import android.util.Log;
import android.util.Pair;

import com.puzzleslab.arsudokusolver.utils.OpenCV;
import com.puzzleslab.arsudokusolver.utils.Parameters;
import com.puzzleslab.arsudokusolver.utils.SudokuUtils;

import org.opencv.core.Mat;
import org.opencv.core.Size;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by Simonas on 2016-04-02.
 */
public class TemplateLibrary {

    public TemplateLibrary(Context context) {
        this.context = context;
        this.templateSize = new Size(templateWidth, templateHeight);
    }

    private static final String TAG = "TemplateLibrary";
    private static final double templateWidth = 25.0;
    private static final double templateHeight = 50.0;
    private Size templateSize;
    private Context context;
    private List<Mat> digitsTemplates;


    public List<Mat> getDigitsTemplates() {
        if(digitsTemplates == null) {
            String csvFile = "templates";
            InputStream inputStream = context.getResources().openRawResource(context.getResources().getIdentifier(csvFile, "raw", context.getPackageName()));
            BufferedReader br = null;
            String line;
            String cvsSplitBy = ",";
            List<Mat> digitsTemplates = new ArrayList<>();
            try {
                br = new BufferedReader(new InputStreamReader(inputStream));
                while ((line = br.readLine()) != null) {
                    // use comma as separator
                    String[] numbers = line.split(cvsSplitBy);
                    Integer[] digitBytes = new Integer[numbers.length];
                    for (int j = 0; j < numbers.length; j++) {
                        if (numbers[j].compareTo("0") == 0) {
                            digitBytes[j] = 0;
                        }
                        else {
                            digitBytes[j] = 255;
                        }
                    }
                    digitsTemplates.add(OpenCV.toMat(digitBytes, templateSize));
                }
                Log.e(TAG, "aaaa");
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
            this.digitsTemplates = digitsTemplates;
        }
        return digitsTemplates;
    }

    public Pair<Integer, Double> detectNumber(Mat candidate) {
        Mat resizedCandidate = OpenCV.resize(candidate, templateSize); // since templates are 25 x 50
        List<Pair<Integer, Double>> results = new ArrayList<>();
        for (int i = 0; i < getDigitsTemplates().size(); i++) {
            if(!Parameters.CONFIG.isProduction()) {
                SudokuUtils.printMatToPicture(getDigitsTemplates().get(i), "template" + (i + 1) + ".png");
                SudokuUtils.printMatToPicture(resizedCandidate, "resizedCandidate.png");
            }
            results.add(OpenCV.matchTemplate(resizedCandidate, i + 1, getDigitsTemplates().get(i)));
        }
        Comparator<Pair<Integer, Double>> comparator = new Comparator<Pair<Integer, Double>>() {
            @Override
            public int compare(Pair<Integer, Double> lhs, Pair<Integer, Double> rhs) {
                return lhs.second.compareTo(rhs.second);
            }
        };
        Collections.sort(results, comparator);

        return results.get(0);
    }
}
