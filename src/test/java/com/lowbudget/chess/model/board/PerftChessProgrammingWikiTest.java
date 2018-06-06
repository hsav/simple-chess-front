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

import com.lowbudget.chess.model.board.array.ArrayBoard;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import static com.lowbudget.chess.model.board.Perft.assertPerft;

@SuppressWarnings("SpellCheckingInspection")
public class PerftChessProgrammingWikiTest {

	private Board board;

	@Before
	public void setup() {
		this.board = new ArrayBoard();
	}

	@Test
	public void testPeterEllisJonesPerft_1() {
		board.fromFEN(FENConstants.PETER_ELLIS_JONES_POSITION_1);
		assertPerft(board, 1, 8L);
	}

	@Test
	public void testPeterEllisJonesPerft_2() {
		board.fromFEN(FENConstants.PETER_ELLIS_JONES_POSITION_2);
		assertPerft(board,1, 8L);
	}

	@Test
	public void testPeterEllisJonesPerft_3() {
		board.fromFEN(FENConstants.PETER_ELLIS_JONES_POSITION_3);
		assertPerft(board,1, 19L);
	}

	@Test
	public void testPeterEllisJonesPerft_4() {
		board.fromFEN(FENConstants.PETER_ELLIS_JONES_POSITION_4);
		assertPerft(board,1, 5L);
	}

	@Test
	public void testPeterEllisJonesPerft_5() {
		board.fromFEN(FENConstants.PETER_ELLIS_JONES_POSITION_5);
		assertPerft(board,1, 44L);
	}

	@Test
	public void testPeterEllisJonesPerft_6() {
		board.fromFEN(FENConstants.PETER_ELLIS_JONES_POSITION_6);
		assertPerft(board,1, 39L);
	}

	@Test
	public void testPeterEllisJonesPerft_7() {
		board.fromFEN(FENConstants.PETER_ELLIS_JONES_POSITION_7);
		assertPerft(board,1, 9L);
	}

	@Test
	public void testPeterEllisJonesPerft_8() {
		board.fromFEN(FENConstants.PETER_ELLIS_JONES_POSITION_8);
		assertPerft(board,3, 62379L);
	}

	@Test
	public void testPeterEllisJonesPerft_9() {
		board.fromFEN(FENConstants.PETER_ELLIS_JONES_POSITION_9);
		assertPerft(board,3, 89890L);
	}

	@Test
	public void testPeterEllisJonesPerft_10() {
		board.fromFEN(FENConstants.PETER_ELLIS_JONES_POSITION_10);
		assertPerft(board,6, 1134888L);
	}

	@Test
	public void testPeterEllisJonesPerft_11() {
		board.fromFEN(FENConstants.PETER_ELLIS_JONES_POSITION_11);
		assertPerft(board,6, 1015133L);
	}

	@Test
	public void testPeterEllisJonesPerft_12() {
		board.fromFEN(FENConstants.PETER_ELLIS_JONES_POSITION_12);
		assertPerft(board,6, 1440467L);
	}

	@Test
	public void testPeterEllisJonesPerft_13() {
		board.fromFEN(FENConstants.PETER_ELLIS_JONES_POSITION_13);
		assertPerft(board,6, 661072L);
	}

	@Test
	public void testPeterEllisJonesPerft_14() {
		board.fromFEN(FENConstants.PETER_ELLIS_JONES_POSITION_14);
		assertPerft(board,6, 803711L);
	}

	@Test
	public void testPeterEllisJonesPerft_15() {
		board.fromFEN(FENConstants.PETER_ELLIS_JONES_POSITION_15);
		assertPerft(board,4, 1274206L);
	}

	@Test
	public void testPeterEllisJonesPerft_16() {
		board.fromFEN(FENConstants.PETER_ELLIS_JONES_POSITION_16);
		assertPerft(board,4, 1720476L);
	}

	@Test
	public void testPeterEllisJonesPerft_17() {
		board.fromFEN(FENConstants.PETER_ELLIS_JONES_POSITION_17);
		assertPerft(board,6, 3821001L);
	}

	@Test
	public void testPeterEllisJonesPerft_18() {
		board.fromFEN(FENConstants.PETER_ELLIS_JONES_POSITION_18);
		assertPerft(board,5, 1004658L);
	}

	@Test
	public void testPeterEllisJonesPerft_19() {
		board.fromFEN(FENConstants.PETER_ELLIS_JONES_POSITION_19);
		assertPerft(board,6, 217342L);
	}

	@Test
	public void testPeterEllisJonesPerft_20() {
		board.fromFEN(FENConstants.PETER_ELLIS_JONES_POSITION_20);
		assertPerft(board,6, 92683L);
	}

	@Test
	public void testPeterEllisJonesPerft_21() {
		board.fromFEN(FENConstants.PETER_ELLIS_JONES_POSITION_21);
		assertPerft(board,6, 2217L);
	}

	@Test
	public void testPeterEllisJonesPerft_22() {
		board.fromFEN(FENConstants.PETER_ELLIS_JONES_POSITION_22);
		assertPerft(board,7, 567584L);
	}

	@Test
	public void testPeterEllisJonesPerft_23() {
		board.fromFEN(FENConstants.PETER_ELLIS_JONES_POSITION_23);
		assertPerft(board,4, 23527L);
	}

	@Test
	@Ignore("testChessProgrammingPosition_2_Kiwipete(): Takes 4-5 minutes to complete")
	public void testChessProgrammingPosition_2_Kiwipete() {
		board.fromFEN(FENConstants.CHESS_PROGRAMMING_POSITION_2_KIWIPETE);
		assertPerft(board,5, 193690690L);
	}

	@Test
	public void testChessProgrammingPosition_3() {
		board.fromFEN(FENConstants.CHESS_PROGRAMMING_POSITION_3);
		assertPerft(board,4, 43238L);
	}

	@Test
	public void testChessProgrammingPosition_4() {
		board.fromFEN(FENConstants.CHESS_PROGRAMMING_POSITION_4);
		assertPerft(board,4, 422333L);
	}
}
