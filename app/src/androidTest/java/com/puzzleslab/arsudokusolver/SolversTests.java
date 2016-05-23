package com.puzzleslab.arsudokusolver;

/**
 * Created by Simonas on 2016-05-18.
 */

import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.SmallTest;

import com.puzzleslab.arsudokusolver.solvers.BacktrackingKudokuSolver;
import com.puzzleslab.arsudokusolver.solvers.BacktrackingSimpleSolver;
import com.puzzleslab.arsudokusolver.solvers.SudokuSolver;

import org.junit.*;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Arrays;
import java.util.List;

@SmallTest
@RunWith(AndroidJUnit4.class)
public class SolversTests {
    private List<SudokuSolver> solvers;

    private static final String[] unsolvedSudokus =
            {".......12........3..23..4....18....5.6..7.8.......9.....85.....9...4.5..47...6...",
                    ".2..5.7..4..1....68....3...2....8..3.4..2.5.....6...1...2.9.....9......57.4...9..",
                    "........3..1..56...9..4..7......9.5.7.......8.5.4.2....8..2..9...35..1..6........",
                    "12.3....435....1....4........54..2..6...7.........8.9...31..5.......9.7.....6...8",
                    "1.......2.9.4...5...6...7...5.9.3.......7.......85..4.7.....6...3...9.8...2.....1",
                    ".......39.....1..5..3.5.8....8.9...6.7...2...1..4.......9.8..5..2....6..4..7.....",
                    "12.3.....4.....3....3.5......42..5......8...9.6...5.7...15..2......9..6......7..8",
                    "..3..6.8....1..2......7...4..9..8.6..3..4...1.7.2.....3....5.....5...6..98.....5.",
                    "....9..5..1.....3...23..7....45...7.8.....2.......64...9..1.....8..6......54....7"};

    private static final String[] solvedSudokus =
            {"839465712146782953752391486391824675564173829287659341628537194913248567475916238",
                    "123456789457189236869273154271548693346921578985637412512394867698712345734865921",
                    "562987413471235689398146275236819754714653928859472361187324596923568147645791832",
                    "126395784359847162874621953985416237631972845247538691763184529418259376592763418",
                    "174385962293467158586192734451923876928674315367851249719548623635219487842736591",
                    "751846239892371465643259871238197546974562318165438927319684752527913684486725193",
                    "125374896479618325683952714714269583532781649968435172891546237257893461346127958",
                    "123456789457189236896372514249518367538647921671293845364925178715834692982761453",
                    "743892156518647932962351748624589371879134265351276489496715823287963514135428697"};

    @Before
    public void setUp() {
        solvers = Arrays.asList(new BacktrackingKudokuSolver(), new BacktrackingSimpleSolver());
    }

    @Test
    public void sudokuSolvers_SolveSolvableSudokus() {
        for (SudokuSolver solver : solvers) {
            for (int i = 0; i < unsolvedSudokus.length; i++) {
                String solvedSudoku = solver.solve(unsolvedSudokus[i]);
                Assert.assertEquals(solvedSudokus[i], solvedSudoku);
            }
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void sudokuSolvers_NullStringPassedToSolver() {
        solvers.get(0).solve(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void sudokuSolvers_EmptyStringPassedToSolver() {
        solvers.get(0).solve("");
    }

    @Test(expected = IllegalArgumentException.class)
    public void sudokuSolvers_TooShortStringPassedToSolver() {
        solvers.get(0).solve("1.......8.9.5.6.7");
    }

    @Test(expected = IllegalArgumentException.class)
    public void sudokuSolvers_StringWithInvalidContentPassedToSolver() {
        solvers.get(0).solve(".........+....3.85..1.2.......5.7.....4...1...9.......5......73..2.1........4.a.9");
    }
}

