/*
 * MIT License
 *
 * Copyright (c) 2018 Charalampos Savvidis
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.lowbudget.chess.model.board;

import com.lowbudget.chess.model.ChessConstants;
import com.lowbudget.chess.model.board.array.ArrayBoard;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static com.lowbudget.chess.model.board.Perft.assertPerft;

@SuppressWarnings("SpellCheckingInspection")
public class PerftStandardTest {

	private Board board;

	@Before
	public void setup() {
		this.board = new ArrayBoard();
	}

	@Test
	public void testPerft_0() {
		assertInitialPerft(0);
	}

	@Test
	public void testPerft_1() {
		assertInitialPerft(1);
	}

	@Test
	public void testPerft_2() {
		assertInitialPerft(2);
	}

	@Test
	public void testPerft_3() {
		assertInitialPerft(3);
	}

	@Test
	public void testPerft_4() {
		assertInitialPerft(4);
	}

	@Test
	public void testPerft_5() {
		assertInitialPerft(5);
	}

	@Test
	@Ignore("testPerft_6(): Takes 2-3 minutes to complete")
	public void testPerft_6() {
		assertInitialPerft(6);
	}

	@Test
	@Ignore("testPerft_7(): Takes about 50 minutes to complete")
	public void testPerft_7() {
		assertInitialPerft(7);
	}


	private void assertInitialPerft(int depth) {
		board.fromFEN(ChessConstants.INITIAL_FEN);
//        PerftResult result = perft(depth, board);
//        assertEquals(EXPECTED.get(depth), result);
		assertPerft(board, depth, EXPECTED.get(depth).getNodes());
	}

	/**
	 * Perft results for the initial board position can be found online, e.g. https://chessprogramming.wikispaces.com/Perft+Results
	 */
	private static final List<Perft.PerftResult> EXPECTED = Arrays.asList(
			new Perft.PerftResult(0, 1L),
			new Perft.PerftResult(1, 20L),
			new Perft.PerftResult(2, 400L),
			new Perft.PerftResult(3, 8902L, 34L, 0, 12, 0),
			new Perft.PerftResult(4, 197281L, 1576L, 0, 469, 8),
			new Perft.PerftResult(5, 4865609L, 82719L, 258, 27351, 347),
			new Perft.PerftResult(6, 119060324L, 2812008L, 5248, 809099, 10828),
			new Perft.PerftResult(7, 3195901860L)
	);
}
