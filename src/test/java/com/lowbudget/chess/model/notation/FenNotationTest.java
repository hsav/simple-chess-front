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

package com.lowbudget.chess.model.notation;

import com.lowbudget.chess.model.ChessConstants;
import com.lowbudget.chess.model.ChessConstants.File;
import com.lowbudget.chess.model.ChessConstants.Rank;
import com.lowbudget.chess.model.Piece;
import com.lowbudget.chess.model.Square;
import com.lowbudget.chess.model.board.FENConstants;
import com.lowbudget.chess.model.notation.fen.FenBoard;
import com.lowbudget.chess.model.notation.fen.FenNotation;
import org.junit.Test;

import java.lang.reflect.Field;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class FenNotationTest {

	//@Test
	@SuppressWarnings("unused")
	public void convertFenToAscii() {
		//noinspection SpellCheckingInspection
		String fen = "3k4/K6r/8/2Pp4/8/8/8/8 w - d6 0 4";
		System.out.println(FenNotation.toPrettyAscii(fen, "\t\t"));
	}

	/**
	 * Utility method that helps re-print all FEN positions in FENConstants class
	 * You would have to copy/add the class to the main classpath
	 * Tip: Use Paste Simple in IDEA otherwise the correct indentation is lost
	 */
	//@Test
	@SuppressWarnings("unused")
	public void printFENConstants() {

		Class<?> clazz = FENConstants.class;

		Field[] fields = clazz.getDeclaredFields();

		StringBuilder sb = new StringBuilder();

		for (Field f : fields) {
			String value;
			try {
				value = (String) f.get(null);
			} catch (IllegalAccessException e) {
				throw new RuntimeException(e);
			}

			sb.append("   /**").append('\n');
			sb.append("    <pre>").append('\n');
			sb.append(FenNotation.toPrettyAscii(value, "\t\t")).append('\n');
			sb.append("    </pre>").append('\n');
			sb.append("    */").append('\n');
			sb.append("   public static final String ").append(f.getName()).append(" = \"").append(value).append("\";").append('\n');
			sb.append('\n');
		}
		System.out.println(sb.toString());
	}

	@Test
	public void testSquaresAreRestoredCorrectly() {
		FenBoard board = FenNotation.convertFromString(ChessConstants.KINGS_ONLY_FEN);
		Iterable<Square> occupiedSquares = board.getAllOccupiedSquares();
		assertEquals(2, size(occupiedSquares));
		assertTrue( contains(Square.E1, occupiedSquares) );
		assertTrue( contains(Square.E8, occupiedSquares) );

		for (int rank = Rank._1; rank < ChessConstants.MAX_RANKS; rank++) {
			for (int file = File.A; file < ChessConstants.MAX_FILES; file++) {
				Square square = Square.of(rank, file);
				if (rank == Rank._1 && file == File.E) {
					assertEquals(Piece.KING_WHITE, board.getPieceAt(square));
				} else if (rank == Rank._8 && file == File.E) {
					assertEquals(Piece.KING_BLACK, board.getPieceAt(square));
				} else {
					assertNull(board.getPieceAt(square));
				}
			}
		}
	}

	@Test
	public void testEnPassantIsRestoredCorrectly() {
		// initial position after move e2-e4
		//noinspection SpellCheckingInspection
		String fen = "rnbqkbnr/pppppppp/8/8/4P3/8/PPPP1PPP/RNBQKBNR b KQkq e3 0 1";
		FenBoard board = FenNotation.convertFromString(fen);
		assertEquals(Square.E3, board.getEnPassant());
	}

	@Test
	public void testFileOf() {
		int file = File.of('e');
		assertEquals(4, file);
	}

	@Test
	public void testRankOf() {
		int rank = Rank.of('4');
		assertEquals(3, rank);
	}

	private boolean contains(Square square, Iterable<Square> squares) {
		for (Square s : squares) {
			if (s.equals(square)) {
				return true;
			}
		}
		return false;
	}

	private int size(Iterable<Square> squares) {
		int count = 0;
		for (Square ignored : squares) {
			count++;
		}
		return count;
	}
}
