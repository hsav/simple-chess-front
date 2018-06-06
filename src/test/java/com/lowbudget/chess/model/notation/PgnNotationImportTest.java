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
import com.lowbudget.chess.model.notation.pgn.PgnGame;
import com.lowbudget.chess.model.notation.pgn.PgnNotation;
import com.lowbudget.chess.model.notation.pgn.PgnParseException;
import org.junit.Test;

import java.util.List;

import static com.lowbudget.chess.model.notation.pgn.PgnTag.EVENT_TAG_NAME;
import static com.lowbudget.chess.model.notation.pgn.PgnTag.SITE_TAG_NAME;
import static org.junit.Assert.*;

public class PgnNotationImportTest {

	private static final String TAG_ESCAPED_QUOTE_VALUE = "Test \\\" test";
	private static final String TAG_ESCAPED_SLASH_BEFORE_QUOTE_VALUE = "Test \\\\\" test";
	private static final String TAG_ESCAPED_SLASH_BEFORE_ESCAPED_QUOTE_VALUE = "Test \\\\\\\" test";
	private static final String TAG_UNESCAPED_QUOTE_VALUE = "Test \" test";
	private static final String EVENT_TAG_VALUE = "Test event";
	private static final String EVENT_TAG = "[" + EVENT_TAG_NAME + ' ' + "\"" + EVENT_TAG_VALUE + "\"]";

	private static final String SITE_TAG_VALUE = "Test site";
	private static final String SITE_TAG = "[" + SITE_TAG_NAME + ' ' + "\"" + SITE_TAG_VALUE + "\"]";

	private static final Move WHITE_PAWN_E4 = Move.of(Piece.PAWN_WHITE, Square.NONE, Square.E4);
	private static final Move BLACK_PAWN_E5 = Move.of(Piece.PAWN_BLACK, Square.NONE, Square.E5);

	@Test(expected = NullPointerException.class)
	public void testThrowsExceptionOnNullInput() {
		PgnNotation.convertFromString(null);
	}

	@Test
	public void testAcceptsEmptyInput() {
		PgnGame game = PgnNotation.convertFromString("");
		assertEquals(0, game.tagCount());
		assertEquals(0, game.getMoveList().size());
	}

	@Test
	public void testCorrectTag() {
		assertImportedPgnGame(eventTag(), EVENT_TAG_NAME);
	}

	@Test
	public void testTagWithEmptyValue() {
		// tag with empty value i.e. [Event ""]
		assertImportedPgnGame(eventTag(""), EVENT_TAG_NAME);
	}

	@Test
	public void testTagWithWhitespaceBeforeTagName() {
		// space before starting '['
		assertImportedPgnGame(eventTag().replace("[", "[ "), EVENT_TAG_NAME);
	}
	@Test
	public void testTagWithWhitespaceAfterTagValue() {
		// space before ending ']'
		assertImportedPgnGame(eventTag().replace("]", " ]"), EVENT_TAG_NAME);
	}

	@Test
	public void testTagWithoutWhiteSpaceBetweenNameAndValue() {
		assertImportedPgnGame(eventTagCompact(), EVENT_TAG_NAME);
	}

	@Test
	public void testTagNameEndingWithUnderscoreAndWithoutWhiteSpaceBetweenNameAndValue() {
		assertImportedPgnGame("[" + "TestTag_\"" + EVENT_TAG_VALUE + "\"]", "TestTag_");
	}

	@Test(expected = PgnParseException.class)
	public void testOnlyLettersDigitsAndUnderscoreAreAllowedInTagName() {
		// tag name contains '.' - should cause a parse error
		PgnNotation.convertFromString("[t.a \"value\"]");
	}

	@Test(expected = PgnParseException.class)
	public void testWithUnescapedQuoteInTagValue() {
		// a dangling '"' inside the tag value should cause a parse error
		assertImportedPgnGame(eventTag(TAG_UNESCAPED_QUOTE_VALUE), EVENT_TAG_NAME);
	}

	@Test
	public void testWithEscapedQuoteInTagValue() {
		// an escaped quote in tag value should be parsed normally i.e. ' \" '
		assertImportedPgnGame(eventTag(TAG_ESCAPED_QUOTE_VALUE), EVENT_TAG_NAME);
	}

	@Test(expected = PgnParseException.class)
	public void testEscapedSlashBeforeQuoteInTagValue() {
		// the '\' before the quote is escaped itself leaving the quote unescaped i.e. ' \\" ' - this should cause a parse error
		assertImportedPgnGame(eventTag(TAG_ESCAPED_SLASH_BEFORE_QUOTE_VALUE), EVENT_TAG_NAME);
	}

	@Test
	public void testEscapedSlashBeforeEscapedQuoteInTagValue() {
		// there are 3 slashes i.e. ' \\\" ' that means an escaped slashed followed by a single slash that escapes
		// the quote - should parse normally
		assertImportedPgnGame(eventTag(TAG_ESCAPED_SLASH_BEFORE_ESCAPED_QUOTE_VALUE), EVENT_TAG_NAME);
	}

	@Test
	public void testTwoTagsOnSeparateLines() {
		assertImportedPgnGame(
				EVENT_TAG + "\n" + SITE_TAG,
				EVENT_TAG_NAME,
				SITE_TAG_NAME
		);
	}

	@Test
	public void testTwoTagsOnTheSameLine() {
		assertImportedPgnGame(
				EVENT_TAG + " " + SITE_TAG,
				EVENT_TAG_NAME,
				SITE_TAG_NAME
		);
	}

	@Test
	public void testTwoTagsOnTheSameLineWithoutWhiteSpace() {
		assertImportedPgnGame(
				EVENT_TAG + SITE_TAG,
				EVENT_TAG_NAME,
				SITE_TAG_NAME
		);
	}

	@Test
	public void testTwoTagsAndOnlyGameResult() {
		assertImportedPgnGame(EVENT_TAG + SITE_TAG + "\n*", EVENT_TAG_NAME, SITE_TAG_NAME);
	}

	@Test
	public void testParsesGameResultWhenAvailable() {
		PgnGame game = assertImportedPgnGame("1.e4 *", WHITE_PAWN_E4);
		assertEquals(PgnNotation.GameResult.UNDEFINED, game.getGameResult());
	}

	@Test
	public void testStartsWithWhiteMove() {
		assertImportedPgnGame("1.e4", WHITE_PAWN_E4);
	}

	@Test
	public void testWhiteMoveWithoutMoveNumber() {
		assertImportedPgnGame("e4", WHITE_PAWN_E4);
	}

	@Test
	public void testWhiteMoveAndBlackMoveWithBothMoveNumbers() {
		assertImportedPgnGame( "1.e4 1...e5", WHITE_PAWN_E4, BLACK_PAWN_E5);
	}

	@Test
	public void testStartsWithBlackMove() {
		assertImportedPgnGame("1...e5", BLACK_PAWN_E5);
	}

	@Test
	public void testWithBothWhiteAndBlackMove() {
		assertImportedPgnGame("1.e4 e5", WHITE_PAWN_E4, BLACK_PAWN_E5);
	}

	@Test
	public void testWithWhiteAndBlackMoveAndSpaceAfterMoveNumber() {
		assertImportedPgnGame("1. e4 e5", WHITE_PAWN_E4, BLACK_PAWN_E5);
	}

	@Test
	public void testWithWhiteKingSideCastling() {
		assertImportedPgnGame("1.O-O", Move.WHITE_KING_SIDE_CASTLING);
	}

	@Test
	public void testWithWhiteQueenSideCastling() {
		assertImportedPgnGame("1.O-O-O", Move.WHITE_QUEEN_SIDE_CASTLING);
	}

	@Test
	public void testWithBlackKingSideCastling() {
		assertImportedPgnGame("1...O-O", Move.BLACK_KING_SIDE_CASTLING);
	}

	@Test
	public void testWithBlackQueenSideCastling() {
		assertImportedPgnGame("1...O-O-O", Move.BLACK_QUEEN_SIDE_CASTLING);
	}

	@Test
	public void testWithWhitePieceCaptureAndCheck() {
		assertImportedPgnGame("1.Bxe2+", Move.of(Piece.BISHOP_WHITE, Square.NONE, Square.E2));
	}

	@Test
	public void testWithWhitePawnCaptureAndCheck() {
		assertImportedPgnGame("1.dxe5+", Move.of(Piece.PAWN_WHITE, Square.of(-1, ChessConstants.File.D), Square.E5));
	}

	@Test
	public void testWithWhitePieceCaptureAndCheckMate() {
		assertImportedPgnGame("1.Qa6xb7#", Move.of(Piece.QUEEN_WHITE, Square.A6, Square.B7));
	}

	@Test
	public void testWithWhitePawnCapturePromotionAndCheck() {
		assertImportedPgnGame("1.fxg1=Q+", Move.of(Piece.PAWN_WHITE, Square.of(-1, ChessConstants.File.F), Square.G1, PieceType.QUEEN));
	}

	private static PgnGame assertImportedPgnGame(String input, Move... expectedMoves) {
		PgnGame pgn = PgnNotation.convertFromString(input);
		List<Move> moveList = pgn.getMoveList();
		assertEquals(expectedMoves.length, moveList.size());
		for (int i = 0; i < expectedMoves.length; i++) {
			assertEquals( expectedMoves[i], moveList.get(i));
		}
		return pgn;
	}

	private static void assertImportedPgnGame(String input, String... expectedTags) {
		PgnGame pgn = PgnNotation.convertFromString(input);
		assertEquals(expectedTags.length, pgn.tagCount());
		for (String expectedTag : expectedTags) {
			assertTrue(pgn.hasTag(expectedTag));
		}
	}

	private static String eventTag() {
		return eventTag(EVENT_TAG_VALUE);
	}

	private static String eventTagCompact() {
		return "[" + EVENT_TAG_NAME + "\"" + EVENT_TAG_VALUE + "\"]";
	}


	private static String eventTag(String value) {
		return "[" + EVENT_TAG_NAME + " \"" + value + "\"]";
	}
}