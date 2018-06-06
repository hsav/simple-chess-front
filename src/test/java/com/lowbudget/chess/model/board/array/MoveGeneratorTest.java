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

package com.lowbudget.chess.model.board.array;

import com.lowbudget.chess.model.*;
import com.lowbudget.chess.model.board.Board;
import com.lowbudget.chess.model.board.FENConstants;
import com.lowbudget.chess.model.board.array.MoveGenerator.KingState;
import org.junit.Before;
import org.junit.Test;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class MoveGeneratorTest {

	private Board board;

	@Before
	public void setup() {
		this.board = new ArrayBoard();
	}

	@Test
	public void testPawnNoValidMoves() {
		String fen = FENConstants.FEN_PAWN_NO_VALID_MOVES;
		// pawn is blocked and there are no captures available, so no valid moves should be found
		assertMovesFromSquare(fen, Square.D4);
	}

	@Test
	public void testPawnTwoSquareAdvance() {
		// the white d-pawn at the starting square should have two options for the UP direction:
		// to move one or two squares to d3 and d4
		assertMovesFromSquare(ChessConstants.INITIAL_FEN, Square.D2, Square.D3, Square.D4);
	}

	@Test
	public void testPawnOneSquareAdvance() {
		String fen = FENConstants.FEN_PAWN_ONE_SQUARE_ADVANCE;

		// the white d-pawn should have only one option for the UP direction: to move one square
		assertMovesFromSquare(fen, Square.D3, Square.D4);
	}

	/**
	 * Tests a special case when a pawn is pinned to the king and thus cannot move.
	 * In this scenario the en-passant capture removes two pawns and leaves king in check when the king, the two
	 * pawns involved and a slider are all on the fifth rank (for black, forth for white).
	 * Thus the black pawn is actually pinned, so it should not be able to capture en-passant.
	 */
	@Test
	public void testPawnIsPinnedForEnPassantDiscoveredCheck() {
		String fen = FENConstants.PAWN_EN_PASSANT_CAPTURE_PINNED;
		// the move that captures en-passant should not be available because the king will be left in check
		assertMovesFromSquare(fen, Square.E4, Square.E3);
	}

	@Test
	public void testPawnNormalCapture() {
		String fen = FENConstants.FEN_PAWN_NORMAL_CAPTURE;

		// the white pawn should be able to capture black's pawn for the UP_RIGHT direction
		assertMovesFromSquare(fen, Square.D4, Square.D5, Square.E5);
	}

	@Test
	public void testPawnEnPassantCapture() {
		String fen = FENConstants.FEN_PAWN_EN_PASSANT_CAPTURE;

		// the white pawn should be able to capture black's pawn en-passant for the UP_RIGHT direction
		assertMovesFromSquare(fen, Square.D5, Square.D6, Square.E6);
	}

	@Test
	public void testPawnPromotion() {
		String fen = FENConstants.FEN_PAWN_PROMOTION;

		Square h7 = Square.H7;
		Square h8 = Square.H8;

		board.fromFEN(fen);
		Piece piece = board.getPieceAt(h7);

		// the white pawn should be able to capture black's pawn en-passant for the UP_RIGHT direction
		assertMovesFromSquare(h7,
				Arrays.asList(
						Move.of(piece, h7, h8, PieceType.QUEEN),
						Move.of(piece, h7, h8, PieceType.KNIGHT),
						Move.of(piece, h7, h8, PieceType.BISHOP),
						Move.of(piece, h7, h8, PieceType.ROOK)
				)
		);
	}

	@Test
	public void testPawnPromotionWithCapture() {
		String fen = FENConstants.FEN_PAWN_PROMOTION_CAPTURE;

		Square h7 = Square.H7;
		Square g8 = Square.G8;

		board.fromFEN(fen);
		Piece piece = board.getPieceAt(h7);

		// the white pawn should be able to capture black's pawn en-passant for the UP_RIGHT direction
		assertMovesFromSquare(h7,
				Arrays.asList(
						Move.of(piece, h7, g8, PieceType.QUEEN),
						Move.of(piece, h7, g8, PieceType.KNIGHT),
						Move.of(piece, h7, g8, PieceType.BISHOP),
						Move.of(piece, h7, g8, PieceType.ROOK)
				)
		);
	}

	@Test
	public void testKingNormalMoves() {
		String fen = FENConstants.KING_NORMAL_MOVES;
		Square e4 = Square.E4;

		// king can go to any of its valid squares
		assertMovesFromSquare(fen, e4,
				Square.E5,
				Square.F5,
				Square.D5,
				Square.F3,
				Square.D3,
				Square.E3,
				Square.F4,
				Square.D4
		);
	}

	@Test
	public void testKingMoveOnRayAttackedSquare() {
		String fen = FENConstants.KING_MOVE_TO_ATTACKED_SQUARE;

		Square e4 = Square.E4;

		// no square should be found on white king's right since this file is attacked by the black rook
		assertMovesFromSquare(fen, e4,
				Square.E5,
				Square.D5,
				Square.D3,
				Square.E3,
				Square.D4
		);
	}

	/**
	 * Tests a special case when the king cannot move into check. Pawns are special regarding this issue, because
	 * the diagonal capture move is not available when there is no enemy at those squares. However here we do want to
	 * detect that the pawn does "attack" the square (or more correctly: it would attack the square if our king moved to it)
	 * while there is no piece in it (yet)
	 * The rest of the pieces can capture wherever they can move, while pawns move (diagonally) whenever they can capture.
	 */
	@Test
	public void testKingMoveOnPawnAttackedSquare() {
		String fen = FENConstants.KING_MOVE_TO_PAWN_ATTACKED_SQUARE;

		Square e4 = Square.E4;

		// white king should not be able to move UP_RIGHT
		assertMovesFromSquare(fen, e4,
				Square.E3,
				Square.E5,
				Square.D3,
				Square.D4,
				Square.D5,
				Square.F3,
				Square.F4
		);
	}

	@Test
	public void testGetPawnSquareAttackers() {
		board.fromFEN(FENConstants.KING_MOVE_TO_PAWN_ATTACKED_SQUARE);

		List<Square> attacks = MoveGenerator.getAllSquareAttacks(board, Square.F5, PlayerColor.BLACK);
		assertExpected(Collections.singletonList(Square.G6), attacks);
	}

	@Test
	public void testPawnAttacksSquareWhenSquareEmpty() {
		// black pawn on G6 should attack F5
		assertAttackSquares(FENConstants.KING_MOVE_TO_PAWN_ATTACKED_SQUARE, PlayerColor.BLACK, Square.F5, Square.G6);
	}

	@Test
	public void testPawnAttacksSquareWhenSquareOccupied() {
		// black pawn on G6 should attack F5 even if it contains an ally
		assertAttackSquares(FENConstants.ATTACKED_SQUARES_PAWN_OCCUPIED, PlayerColor.BLACK, Square.F5, Square.G6);
	}

	@Test
	public void testKingAgainstKingAttackInBetweenSquareWhenEmpty() {
		// white king attacks the square between the kings
		assertAttackSquares(FENConstants.KING_AGAINST_OPPOSING_KING, PlayerColor.WHITE, Square.E5, Square.E4);
	}

	@Test
	public void testKingAgainstKingAttackInBetweenSquareWhenOccupied() {
		// black king's attacks the square between the kings even if it contains an ally
		assertAttackSquares(FENConstants.ATTACKED_SQUARES_KING_OCCUPIED, PlayerColor.BLACK, Square.E5, Square.E6);
	}

	/**
	 * Tests that the Move generator correctly identifies that there is a check from the promoted pawn
	 * which happens to be checkmate since there are no other legal moves
	 */
	@Test
	public void testKingStateIsCheckMateDeliveredByPawnPromotedToQueen() {
		board.fromFEN(FENConstants.KING_STATE_WHEN_CHECK_IS_CHECKMATE_BY_PAWN_PROMOTED_TO_QUEEN);

		// the g-pawn promoted to queen and delivered checkmate
		KingState state = MoveGenerator.getKingState(board, PlayerColor.BLACK, Move.of(Piece.PAWN_WHITE, Square.G7, Square.G8, PieceType.QUEEN));
		assertTrue(state.isInCheck());
		assertTrue(state.hasNoMoves());
	}

	/**
	 * Tests that the Move generator correctly identifies that there is a check from the promoted pawn
	 * which is not checkmate since it can be blocked
	 */
	@Test
	public void testKingStateIsCheckThatCanBeBlockedDeliveredByPawnPromotedToQueen() {
		board.fromFEN(FENConstants.KING_STATE_WHEN_CHECK_IS_NOT_CHECKMATE_BY_PAWN_PROMOTED_TO_QUEEN);

		// the g-pawn promoted to queen and delivered check (not checkmate though, black rook can block the check)
		KingState state = MoveGenerator.getKingState(board, PlayerColor.BLACK, Move.of(Piece.PAWN_WHITE, Square.G7, Square.G8, PieceType.QUEEN));
		assertTrue(state.isInCheck());
		assertFalse(state.hasNoMoves());
	}

	/**
	 * Tests a special case when the king cannot move into check. If the move is along the ray of the attack
	 * (e.g. a rank) the square where the king moves along the rank is not found as an attacked square because the
	 * king itself blocks the ray!
	 */
	@Test
	public void testKingMoveAlongRayAttackedSquare() {
		String fen = FENConstants.KING_MOVE_TO_RAY_ATTACKED_SQUARE;

		Square e4 = Square.E4;

		// no square should be found on white king's rank (i.e. f4 and d4) since this rank
		// is attacked by the black rook
		assertMovesFromSquare(fen, e4,
				Square.E5,
				Square.F5,
				Square.D5,
				Square.F3,
				Square.D3,
				Square.E3
		);
	}

	@Test
	public void testKingMovesWhenOpposingKings() {
		String fen = FENConstants.KING_AGAINST_OPPOSING_KING;
		Square e4 = Square.E4;

		// white king should not be able to move in any UP related directions since all the squares are attacked
		// by black king
		assertMovesFromSquare(fen, e4,
				// downward squares
				Square.E3,
				Square.D3,
				Square.F3,

				// side squares
				Square.D4,
				Square.F4
		);
	}

	@Test
	public void testKingKingSideCastlingWhite() {
		// when castling is available
		String fen = FENConstants.KINGSIDE_CASTLING_AVAILABLE;
		Square e1 = Square.E1;
		assertMovesFromSquare(fen, e1, Square.E2, Square.F1, Square.G1);

		// when castling is not available
		fen = fen.replace("KQkq", "-");
		assertMovesFromSquare(fen, e1, Square.E2, Square.F1);
	}

	@Test
	public void testKingKingCastlingRookMustExist() {
		// note that this is a actually an invalid FEN since the king-side castling right exists
		// but the rook has been captured so this should not be possible.
		// however there are FEN strings out there where they can have such relaxed rules. Here we check that
		// we take into account the position on the board
		String fen = FENConstants.KINGSIDE_CASTLING_AVAILABLE.replaceAll("K2R", "K3");

		Square e1 = Square.E1;
		assertMovesFromSquare(fen, e1, Square.E2, Square.F1);
	}


	@Test
	public void testKingKingSideCastlingBlack() {
		// when castling is available (make it black's turn)
		String fen = FENConstants.KINGSIDE_CASTLING_AVAILABLE.replace('w', 'b');
		Square e8 = Square.E8;
		assertMovesFromSquare(fen, e8, Square.E7, Square.F8, Square.G8);

		// when castling is not available
		fen = fen.replace("KQkq", "-");
		assertMovesFromSquare(fen, e8, Square.E7, Square.F8);
	}

	@Test
	public void testKingQueenSideCastling() {
		Square e1 = Square.E1;

		// when castling is available
		String fen = FENConstants.QUEENSIDE_CASTLING_AVAILABLE;
		assertMovesFromSquare(fen, e1, Square.D1, Square.C1);

		// when castling is not available
		fen = fen.replace("KQkq", "-");
		assertMovesFromSquare(fen, e1, Square.D1);
	}

	@Test
	public void testKingQueenSideCastlingWhenRookPathIsOccupied() {
		Square e1 = Square.E1;

		// when castling is available but note that while the king's path towards the queen's rook is clear
		// the rook path towards the king is not
		String fen = FENConstants.QUEENSIDE_CASTLING_ROOK_PATH_OCCUPIED;
		assertMovesFromSquare(fen, e1, Square.D1);
	}

	@Test
	public void testCheckEvasionRejectsKingMovesAlongTheCheckRay() {
		String fen = FENConstants.PETER_ELLIS_JONES_POSITION_1;
		assertMovesFromSquare(fen, Square.E7,
				Square.E8, Square.F8, // not d8, it still leaves king in check
				Square.F7, Square.D7,
				Square.E6, Square.D6 // not f6, it still leaves king in check
		);
	}

	@Test
	public void testCheckEvasionRejectsPawnPromotionNotCapturingAttacker() {
		// This is the PETER_ELLIS_JONES_POSITION_17 position after Kd7 Rd8+
		String fen = FENConstants.CHECK_EVASION_REJECTS_PAWN_PROMOTION_NOT_CAPTURES_ATTACKER;
		board.fromFEN(fen);

		Square[] kingExpectedSquares = { Square.D8, Square.C7, Square.C6, Square.E6 };
		List<Move> expectedMoves = listOf(Arrays.asList(
					Move.of(Piece.PAWN_WHITE, Square.E7, Square.D8, PieceType.QUEEN),
					Move.of(Piece.PAWN_WHITE, Square.E7, Square.D8, PieceType.ROOK),
					Move.of(Piece.PAWN_WHITE, Square.E7, Square.D8, PieceType.KNIGHT),
					Move.of(Piece.PAWN_WHITE, Square.E7, Square.D8, PieceType.BISHOP)
				),
				toMoves( square -> Move.of(Piece.KING_WHITE, Square.D7, square), kingExpectedSquares)
		);
		assertMoves(() -> MoveGenerator.allLegalMoves(board), expectedMoves);
	}

	@Test
	public void testCheckEvasionRejectsPawnMovesNotCapturingAttacker() {
		String fen = FENConstants.CHECK_EVASION_REJECTS_PAWN_CAPTURE_NOT_CAPTURES_ATTACKER;
		board.fromFEN(fen);

		Square[] kingExpectedSquares = {
				Square.F3, Square.F4, // not F2 - leaves king still in check
				Square.G2, Square.G4,
				Square.H2, Square.H3  // not H$ - leaves king still in check
		};

		List<Move> expectedMoves = toMoves( square -> Move.of(Piece.KING_WHITE, Square.G3, square), kingExpectedSquares);
		assertMoves(() -> MoveGenerator.allLegalMoves(board), expectedMoves);
	}

	@Test
	public void testCheckEvasionRejectsPinnedMove() {
		String fen = FENConstants.CHECK_EVASION_REJECTS_PINNED_PIECE_MOVE;
		board.fromFEN(fen);

		Square[] kingExpectedSquares = {
				Square.E6, Square.E7, Square.E8,
				Square.G8, Square.G6
		};

		List<Move> expected = listOf(
				Arrays.asList(
						Move.of(Piece.BISHOP_BLACK, Square.G7, Square.F6),	// bishop blocks the check
						Move.of(Piece.QUEEN_BLACK, Square.H7, Square.F5)	// queen blocks the check
				),
				toMoves( square -> Move.of(Piece.KING_WHITE, Square.F7, square), kingExpectedSquares)	// king moves
		);
		assertMoves(() -> MoveGenerator.allLegalMoves(board), expected);
	}


	@Test
	public void testCheckEvasionAcceptsEnPassantMoveThatCapturesAttacker() {
		String fen = FENConstants.PETER_ELLIS_JONES_POSITION_2;
		board.fromFEN(fen);

		Square[] kingExpectedSquares = { Square.B6, Square.C6, Square.D6, Square.B5, Square.D5, Square.B4, Square.D4 };

		List<Move> expectedMoves = listOf(
				Move.of(Piece.PAWN_BLACK, Square.C4, Square.D3),	// en-passant capture
				toMoves( square -> Move.of(Piece.KING_BLACK, Square.C5, square), kingExpectedSquares)
		);
		assertMoves( ()-> MoveGenerator.allLegalMoves(board), expectedMoves );
	}

	@Test
	public void testCheckEvasionRejectsEnPassantMoveThatNotCapturesAttacker() {
		String fen = FENConstants.CHECK_EVASION_REJECTS_EN_PASSANT_CAPTURE_NOT_CAPTURES_ATTACKER;
		board.fromFEN(fen);

		// black pawn has just moved two squares and caused a discovered check by the rook at H2
		// the white pawn at C5 can take the black pawn en-passant but this will not remove the check,
		// so this move is not a check evasion move and should not be generated
		Square[] kingExpectedSquares = { Square.A8, Square.B8, Square.A6, Square.B6 };
		assertMoves( ()-> MoveGenerator.allLegalMoves(board), toMoves(square -> Move.of(Piece.KING_BLACK, Square.A7, square), kingExpectedSquares) );
	}

	@Test
	public void testCheckEvasionAcceptsOnlyKingMovesWhenDoubleCheck() {
		// Important note: this tests depends on the order we iterate the pieces when we look for check evasion moves.
		// Assuming we don't check for at least two attack moves in move generator and by slightly change the position
		// so that the queen is attacked instead of the knight, the test would pass (!)
		String fen = FENConstants.CHECK_EVASION_KING_IN_DOYBLE_CHECK;
		board.fromFEN(fen);

		Square[] kingExpectedSquares = { Square.D1 };
		assertMoves( ()-> MoveGenerator.allLegalMoves(board), toMoves(square -> Move.of(Piece.KING_WHITE, Square.E1, square), kingExpectedSquares) );
	}

	private void assertMovesFromSquare(String fen, Square from, Square... expected) {
		board.fromFEN(fen);
		Piece piece = board.getPieceAt(from);
		List<Move> expectedMoves = toMoves(square -> Move.of(piece, from, square), expected);
		assertMovesFromSquare(from, expectedMoves);
	}

	@FunctionalInterface
	private interface MoveSupplier {
		Move create(Square square);
	}

	private void assertAttackSquares(String fen, PlayerColor attackColor, Square target, Square... expected) {
		board.fromFEN(fen);
		List<Square> attacksToSquare = MoveGenerator.getAllSquareAttacks(board, target, attackColor);
		assertExpected(attacksToSquare, Arrays.asList(expected));
	}

	private void assertMovesFromSquare(Square from, List<Move> expected) {
		assertMoves( ()-> MoveGenerator.getAllLegalMovesFromSquare(board, board.getPlayingColor(), from), expected);
	}

	private static List<Move> listOf(Move first, List<Move> rest) {
		List<Move> moves = new ArrayList<>();
		moves.add(first);
		moves.addAll(rest);
		return moves;
	}

	private static List<Move> listOf(List<Move> first, List<Move> second) {
		return Stream.of(first, second).flatMap(Collection::stream).collect(toList());
	}

	private static void assertMoves(Supplier<List<Move>> supplier, List<Move> expected) {
		List<Move> moves = supplier.get();
		assertExpected(moves, expected);
	}

	private static List<Move> toMoves(MoveSupplier supplier, Square... expected) {
		List<Move> expectedMoves = new ArrayList<>();
		for (Square p : expected) {
			expectedMoves.add( supplier.create(p) );
		}
		return expectedMoves;
	}


	private static <T> void assertExpected(List<T> generated, List<T> expected) {
		assertEquals("Actual: " + generated + ", Expected: " + expected, expected.size(), generated.size());
		for (T item : expected) {
			assertTrue("Expected " + item.toString() + " not found. Generated: " + generated + ", expected: " + expected, generated.contains(item));
		}
	}
}
