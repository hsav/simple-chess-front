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

import com.lowbudget.chess.model.*;
import com.lowbudget.chess.model.Move.MoveType;
import com.lowbudget.chess.model.notation.pgn.PgnGame;
import com.lowbudget.chess.model.notation.pgn.PgnNotation;
import com.lowbudget.chess.model.notation.pgn.SimplePgnGame;
import org.junit.Test;

import java.util.EnumSet;

import static com.lowbudget.chess.model.notation.pgn.PgnTag.EVENT_TAG_NAME;
import static com.lowbudget.chess.model.notation.pgn.PgnTag.SITE_TAG_NAME;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class PgnNotationExportTest {

	private static final String EVENT_TAG_VALUE = "Test event";
	private static final String EVENT_TAG = "[" + EVENT_TAG_NAME + ' ' + "\"" + EVENT_TAG_VALUE + "\"]";

	private static final String SITE_TAG_VALUE = "Test site";
	private static final String SITE_TAG = "[" + SITE_TAG_NAME + ' ' + "\"" + SITE_TAG_VALUE + "\"]";

	private static final String EXPECTED_TWO_TAGS = EVENT_TAG + "\n" + SITE_TAG + "\n";

	private static final Move WHITE_PAWN_E4_FULL = Move.of(Piece.PAWN_WHITE, Square.E2, Square.E4);
	private static final Move BLACK_PAWN_E5_FULL = Move.of(Piece.PAWN_BLACK, Square.E7, Square.E5);

	@Test(expected = NullPointerException.class)
	public void testThrowsExceptionOnNullInput() {
		//noinspection ResultOfMethodCallIgnored
		PgnNotation.convertToString(null);
	}

	@Test
	public void testGeneratesEmptyInput() {
		String result = PgnNotation.convertToString(new SimplePgnGame());
		assertTrue(result.isEmpty());
	}

	@Test
	public void testWithOneTag() {
		PgnGame game = newPgnGame(EVENT_TAG_NAME, EVENT_TAG_VALUE);
		assertEquals(eventTag() + "\n", PgnNotation.convertToString(game));
	}

	@Test
	public void testWithTwoTags() {
		PgnGame game = newPgnGameForExportWithTwoTags();
		String pgn = PgnNotation.convertToString(game);
		assertEquals(EXPECTED_TWO_TAGS, pgn);
	}

	@Test
	public void testGeneratesOneMoveNumberPerMovePairWhiteFirstMove() {
		SimplePgnGame game = newPgnGameForExportWithTwoTags(WHITE_PAWN_E4_FULL, BLACK_PAWN_E5_FULL);
		String pgn = PgnNotation.convertToString(game);
		assertEquals(EXPECTED_TWO_TAGS + "1.e4 e5" + "\n", pgn);
	}

	@Test
	public void testGeneratesOneMoveNumberPerMovePairBlackFirstMove() {
		SimplePgnGame game = newPgnGameForExportWithTwoTags(BLACK_PAWN_E5_FULL, WHITE_PAWN_E4_FULL);
		String pgn = PgnNotation.convertToString(game);
		assertEquals(EXPECTED_TWO_TAGS + "1...e5 2.e4" + "\n", pgn);
	}

	@Test
	public void testWritesGameResult() {
		SimplePgnGame game = newPgnGameForExportWithTwoTags(BLACK_PAWN_E5_FULL, WHITE_PAWN_E4_FULL);
		game.setGameResult(PgnNotation.GameResult.DRAW);
		String pgn = PgnNotation.convertToString(game);
		assertEquals(EXPECTED_TWO_TAGS + "1...e5 2.e4 1/2-1/2" + "\n", pgn);
	}

	@Test
	public void testWritesCaptures() {
		SimplePgnGame game = newPgnGameForExportWithTwoTags(
				new Move(Piece.BISHOP_WHITE, Square.F1, Square.E2, Piece.PAWN_BLACK, null, EnumSet.noneOf(MoveType.class))
		);
		String pgn = PgnNotation.convertToString(game);
		assertEquals(EXPECTED_TWO_TAGS + "1.Bxe2" + "\n", pgn);
	}

	@Test
	public void testWritesChecks() {
		SimplePgnGame game = newPgnGameForExportWithTwoTags(
				new Move(Piece.BISHOP_WHITE, Square.F1, Square.E2, null, null, EnumSet.of(MoveType.CHECK))
		);
		String pgn = PgnNotation.convertToString(game);
		assertEquals(EXPECTED_TWO_TAGS + "1.Be2+" + "\n", pgn);
	}

	@Test
	public void testWritesCheckMates() {
		SimplePgnGame game = newPgnGameForExportWithTwoTags(
				new Move(Piece.BISHOP_WHITE, Square.F1, Square.E2, null, null, EnumSet.of(MoveType.CHECKMATE))
		);
		String pgn = PgnNotation.convertToString(game);
		assertEquals(EXPECTED_TWO_TAGS + "1.Be2#" + "\n", pgn);
	}

	@Test
	public void testWritesPromotions() {
		SimplePgnGame game = newPgnGameForExportWithTwoTags(
				new Move(Piece.PAWN_WHITE, Square.E7, Square.E8, null, PieceType.ROOK, EnumSet.noneOf(MoveType.class))
		);
		String pgn = PgnNotation.convertToString(game);
		assertEquals(EXPECTED_TWO_TAGS + "1.e8=R" + "\n", pgn);
	}

	private static PgnGame newPgnGame(String tag1, String value1) {
		SimplePgnGame game = new SimplePgnGame();
		game.addTag(tag1, value1);
		return game;
	}

	private static String eventTag() {
		return "[" + EVENT_TAG_NAME + " \"" + EVENT_TAG_VALUE + "\"]";
	}

	@SuppressWarnings("SameParameterValue")
	private static SimplePgnGame newPgnGame(String tag1, String value1, String tag2, String value2) {
		SimplePgnGame game = (SimplePgnGame) newPgnGame(tag1, value1);
		game.addTag(tag2, value2);
		return game;
	}

	private static SimplePgnGame newPgnGameForExportWithTwoTags(Move... moves) {
		SimplePgnGame game = newPgnGame(EVENT_TAG_NAME, EVENT_TAG_VALUE, SITE_TAG_NAME, SITE_TAG_VALUE);
		for (Move m : moves) {
			game.addMove(m);
		}
		return game;
	}
}