package com.puzzleslab.arsudokusolver;

import android.test.suitebuilder.annotation.SmallTest;

import com.puzzleslab.arsudokusolver.Modules.SCell;
import com.puzzleslab.arsudokusolver.Modules.SudokuException;

import org.junit.Test;
import org.opencv.core.Rect;

/**
 * Created by simonas_b on 5/18/2016.
 */

@SmallTest
public class SCellTests {

    @Test(expected = SudokuException.class)
    public void sCell_CreateWithNegativeValue() throws SudokuException {
        new SCell(-1, 55, new Rect(0, 0, 0, 0));
    }

    @Test(expected = SudokuException.class)
    public void sCell_CreateWithValueGreaterThanNine() throws SudokuException {
        new SCell(10, 55, new Rect(0, 0, 0, 0));
    }

    @Test(expected = SudokuException.class)
    public void sCell_CreateWithNegativeQuality() throws SudokuException {
        new SCell(10, -1, new Rect(0, 0, 0, 0));
    }

    @Test
    public void sCell_CreateWithValidValues() throws SudokuException {
        new SCell(0, 0, new Rect(0, 0, 0, 0));
    }
}
