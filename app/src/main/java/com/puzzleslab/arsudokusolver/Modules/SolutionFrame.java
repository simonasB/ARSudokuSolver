package com.puzzleslab.arsudokusolver.Modules;

import org.opencv.core.Mat;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by simonas_b on 4/5/2016.
 */
public class SolutionFrame {
    List<Character> solution = new ArrayList<>();
    Mat solutionMat;

    public SolutionFrame(List<Character> solution, Mat solutionMat) {
        this.solution = solution;
        this.solutionMat = solutionMat;
    }

    public Mat getSolutionMat() {
        return solutionMat;
    }

    public void setSolutionMat(Mat solutionMat) {
        this.solutionMat = solutionMat;
    }

    public List<Character> getSolution() {
        return solution;
    }

    public void setSolution(List<Character> solution) {
        this.solution = solution;
    }
}
