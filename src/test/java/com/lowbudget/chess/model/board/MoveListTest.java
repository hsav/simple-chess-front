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

import com.lowbudget.chess.model.Move;
import com.lowbudget.chess.model.Piece;
import com.lowbudget.chess.model.Square;
import com.lowbudget.chess.model.board.MoveList.BrowseType;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class MoveListTest {

	private MoveList moveList;

	private Move firstWhiteMove;
	private Move secondWhiteMove;
	private Move firstBlackMove;
	private Move secondBlackMove;

	@Before
	public void setup() {
		this.moveList = new MoveList();
		firstWhiteMove = Move.of(Piece.PAWN_WHITE, Square.E2, Square.E4);
		secondWhiteMove = Move.of(Piece.KNIGHT_WHITE, Square.G1, Square.F3);
		firstBlackMove = Move.of(Piece.PAWN_BLACK, Square.E7, Square.E5);
		secondBlackMove = Move.of(Piece.KNIGHT_BLACK, Square.G8, Square.F6);
	}

	@Test
	public void testAddMove() {
		assertEquals(0, moveList.size());
		moveList.addMove(firstWhiteMove);
		assertEquals(1, moveList.size());
	}

	@Test
	public void testGetCurrentMoveIndexWhenFirstMoveIsBlack() {
		moveList.addMove(firstBlackMove);
		assertEquals(0, moveList.getCurrentMoveIndex());
	}

	@Test
	public void testGetMove() {
		addMoves(firstWhiteMove, firstBlackMove);
		assertEquals(firstWhiteMove, moveList.getMove(0));
		assertEquals(firstBlackMove, moveList.getMove(1));
	}

	@Test
	public void testGetMoveByRowAndColumn() {
		addMoves(firstWhiteMove, firstBlackMove);
		assertEquals(firstWhiteMove, moveList.getMove(0, 0));
		assertEquals(firstBlackMove, moveList.getMove(0, 1));
		assertEquals(Move.NONE, moveList.getMove(1, 1)); // invalid indices will return Move.NONE
	}

	@Test
	public void testClear() {
		moveList.addMove(firstWhiteMove);
		assertEquals(1, moveList.size());

		moveList.clear();
		assertEquals(0, moveList.size());
	}

	@Test
	public void testSize() {
		// add a full move (white and black)
		addMoves(firstWhiteMove, firstBlackMove);
		assertEquals(2, moveList.size());
	}

	@Test
	public void testGetTotalMoves() {
		// add a full move (white and black)
		addMoves(firstWhiteMove, firstBlackMove);

		// add one more half-move
		moveList.addMove(secondWhiteMove);

		// size should return 3, but moves should return 2
		assertEquals(3, moveList.size());
		assertEquals(2, moveList.getTotalMoves());
	}

	@Test
	public void testGetTotalMovesWhenFirstMoveBlack() {
		moveList.addMove(firstBlackMove);

		// add one more half-move
		moveList.addMove(secondWhiteMove);

		assertEquals(2, moveList.size());
		assertEquals(2, moveList.getTotalMoves());
	}

	@Test
	public void testGetCurrentMove() {
		moveList.addMove(firstWhiteMove);
		assertEquals(firstWhiteMove, moveList.getCurrentMove());
	}

	@Test
	public void testGetCurrentMoveWhenBrowseToPreviousMove() {
		addMoves(firstWhiteMove, firstBlackMove);

		assertEquals(firstBlackMove, moveList.getCurrentMove());

		moveList.browse(BrowseType.PREVIOUS);
		assertEquals(firstWhiteMove, moveList.getCurrentMove());
	}

	@Test
	public void testGetCurrentMoveWhenBrowseToBeginning() {
		addMoves(firstWhiteMove, firstBlackMove);

		moveList.browse(BrowseType.FIRST);
		assertEquals(Move.NONE, moveList.getCurrentMove());
	}

	@Test
	public void testRemoveLastMove() {
		moveList.addMove(firstWhiteMove);
		moveList.removeLast();
		assertEquals(0, moveList.size());
	}

	@Test
	public void testGetWhiteMove() {
		addMoves(firstWhiteMove, firstBlackMove, secondWhiteMove, secondBlackMove);
		assertEquals(secondWhiteMove, moveList.getMove(1, 0));
	}

	@Test
	public void testGetWhiteMoveWhenFirstMoveBlack() {
		moveList.addMove(firstBlackMove);
		assertSame(Move.NONE, moveList.getMove(0, 0));
	}

	@Test
	public void testGetBlackMove() {
		addMoves(firstWhiteMove, firstBlackMove, secondWhiteMove);
		// second black move should return null since it is not added yet
		assertSame(Move.NONE, moveList.getMove(1, 1));

		// now add the black move, the move should be returned
		moveList.addMove(secondBlackMove);
		assertEquals(secondBlackMove, moveList.getMove(1, 1));
	}

	@Test
	public void testBrowseToFirstMove() {
		addMoves(firstWhiteMove, firstBlackMove, secondWhiteMove);
		moveList.browse(BrowseType.FIRST);
		assertEquals(-1, moveList.getCurrentMoveIndex());
	}

	@Test
	public void testBrowseToFirstMoveWhenFirstMoveBlack() {
		addMoves(firstBlackMove, secondWhiteMove);
		moveList.browse(BrowseType.FIRST);
		assertEquals(-1, moveList.getCurrentMoveIndex());
	}

	@Test
	public void testBrowseToLastMove() {
		addMoves(firstWhiteMove, firstBlackMove, secondWhiteMove);
		moveList.browse(BrowseType.LAST);
		assertEquals(2, moveList.getCurrentMoveIndex());
	}

	@Test
	public void testBrowseToPreviousMove() {
		addMoves(firstWhiteMove, firstBlackMove, secondWhiteMove);
		moveList.browse(BrowseType.PREVIOUS);
		assertEquals(1, moveList.getCurrentMoveIndex());
	}

	@Test
	public void testBrowseToPreviousMoveWhenFirstMoveBlack() {
		moveList.addMove(firstBlackMove);
		moveList.browse(BrowseType.PREVIOUS);
		assertEquals(-1, moveList.getCurrentMoveIndex());
	}

	@Test
	public void testBrowseToPreviousMoveTwoTimesWhenFirstMoveBlack() {
		moveList.addMove(firstBlackMove);
		moveList.browse(BrowseType.PREVIOUS);
		moveList.browse(BrowseType.PREVIOUS);
		assertEquals(-1, moveList.getCurrentMoveIndex());
	}

	@Test
	public void testBrowseToNextMove() {
		addMoves(firstWhiteMove, firstBlackMove, secondWhiteMove);
		moveList.browse(BrowseType.NEXT);
		assertEquals(2, moveList.getCurrentMoveIndex());
	}

	@Test
	public void testGetRowIndexWithWhiteFirstMove() {
		moveList.addMove(firstWhiteMove);
		assertEquals(0, moveList.getRowForCurrentMove());

		moveList.addMove(firstBlackMove);
		assertEquals(0, moveList.getRowForCurrentMove());

		moveList.addMove(secondWhiteMove);
		assertEquals(1, moveList.getRowForCurrentMove());

		moveList.addMove(secondBlackMove);
		assertEquals(1, moveList.getRowForCurrentMove());
	}

	@Test
	public void testGetRowIndexWithBlackFirstMove() {
		moveList.addMove(firstBlackMove);
		assertEquals(0, moveList.getRowForCurrentMove());

		moveList.addMove(secondWhiteMove);
		assertEquals(1, moveList.getRowForCurrentMove());

		moveList.addMove(secondBlackMove);
		assertEquals(1, moveList.getRowForCurrentMove());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testIsCellAtCurrentMoveThrowsExceptionForInvalidColumn() {
		// In tabular format:
		// [ white | black ]
		addMoves(firstWhiteMove, firstBlackMove);
		moveList.isCellAtCurrentMove(0, 2);
	}

	@Test
	public void testIsCellAtCurrentMoveWhenFirstMoveIsWhite() {
		// In tabular format:
		// [ white | black ]
		addMoves(firstWhiteMove, firstBlackMove);

		// the current move is the last one played by default (i.e. without browsing)
		assertFalse(moveList.isCellAtCurrentMove(0, 0));
		assertTrue(moveList.isCellAtCurrentMove(0, 1));
	}

	@Test
	public void testIsCellAtCurrentMoveWhenFirstMoveIsBlack() {
		// In tabular format:
		// [ ...   | black ]
		// [ white |       ]
		addMoves(firstBlackMove, firstWhiteMove);

		// the current move is the last one played by default (i.e. without browsing)
		assertFalse(moveList.isCellAtCurrentMove(0, 0));
		assertFalse(moveList.isCellAtCurrentMove(0, 1));
		assertTrue(moveList.isCellAtCurrentMove(1, 0));
		assertFalse(moveList.isCellAtCurrentMove(1, 1));
	}

	@Test
	public void testIsCellAtCurrentMoveIsAlwaysFalseWhenMoveListIsAtTheBeginning() {
		// In tabular format:
		// [ ...   | black ]
		// [ white |       ]
		addMoves(firstBlackMove, firstWhiteMove);

		moveList.browse(BrowseType.FIRST);

		// all cells should be considered to be "after the current move"
		assertFalse(moveList.isCellAtCurrentMove(0, 0));
		assertFalse(moveList.isCellAtCurrentMove(0, 1));
		assertFalse(moveList.isCellAtCurrentMove(1, 0));
		assertFalse(moveList.isCellAtCurrentMove(1, 1));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testIsCellAfterCurrentMoveThrowsExceptionForInvalidColumn() {
		// In tabular format:
		// [ white | black ]
		addMoves(firstWhiteMove, firstBlackMove);
		moveList.isCellAfterCurrentMove(0, 2);
	}

	@Test
	public void testIsCellAfterCurrentMoveWhenFirstMoveIsWhite() {
		// In tabular format:
		// [ white | black ]
		addMoves(firstWhiteMove, firstBlackMove);

		// the current move is the last one played by default (i.e. without browsing)
		assertFalse(moveList.isCellAfterCurrentMove(0, 0));
		assertFalse(moveList.isCellAfterCurrentMove(0, 1));
	}

	@Test
	public void testIsCellAfterCurrentMoveWhenFirstMoveIsBlack() {
		// In tabular format:
		// [ ...   | black ]
		// [ white |       ]
		addMoves(firstBlackMove, firstWhiteMove);

		// the current move is the last one played by default (i.e. without browsing)
		assertFalse(moveList.isCellAfterCurrentMove(0, 0));
		assertFalse(moveList.isCellAfterCurrentMove(0, 1));
		assertFalse(moveList.isCellAfterCurrentMove(1, 0));

		// last cell is "after" the current move even though it does not contain a move
		assertTrue(moveList.isCellAfterCurrentMove(1, 1));
	}

	@Test
	public void testIsCellAfterCurrentMoveIsAlwaysTrueWhenMoveListIsAtTheBeginning() {
		// In tabular format:
		// [ ...   | black ]
		// [ white |       ]
		addMoves(firstBlackMove, firstWhiteMove);

		moveList.browse(BrowseType.FIRST);

		// all cells should be considered to be "after the current move"
		assertTrue(moveList.isCellAfterCurrentMove(0, 0));
		assertTrue(moveList.isCellAfterCurrentMove(0, 1));
		assertTrue(moveList.isCellAfterCurrentMove(1, 0));
		assertTrue(moveList.isCellAfterCurrentMove(1, 1));
	}

	@Test
	public void testIsCellAfterCurrentMoveWhenBrowse() {
		// In tabular format:
		// [ white | black ]
		// [ white | black ]
		addMoves(firstWhiteMove, firstBlackMove, secondWhiteMove, secondBlackMove);

		// browse back 2 times so current move is the first black move
		moveList.browse(BrowseType.PREVIOUS);
		moveList.browse(BrowseType.PREVIOUS);

		assertFalse(moveList.isCellAfterCurrentMove(0, 0));
		assertFalse(moveList.isCellAfterCurrentMove(0, 1));
		assertTrue(moveList.isCellAfterCurrentMove(1, 0));
		assertTrue(moveList.isCellAfterCurrentMove(1, 1));
	}

	@Test
	public void testToStringWhiteFirstMove() {
		addMoves(firstWhiteMove, firstBlackMove, secondWhiteMove, secondBlackMove);

		String result = moveList.toString();
		assertEquals("MoveList[1.e4 e5 2.Nf3 Nf6]", result);
	}

	@Test
	public void testToStringBlackFirstMove() {
		addMoves(firstBlackMove, secondWhiteMove, secondBlackMove);

		String result = moveList.toString();
		assertEquals("MoveList[1...e5 2.Nf3 Nf6]", result);
	}

	private void addMoves(Move ... moves) {
		for (Move move : moves) {
			moveList.addMove(move);
		}
	}
}
