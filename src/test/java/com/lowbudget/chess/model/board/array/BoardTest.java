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
import com.lowbudget.chess.model.Castling.CastlingRight;
import com.lowbudget.chess.model.board.*;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class BoardTest {

	private interface PieceAssertion {
		boolean is(Piece piece);
	}

	private Board chessBoard;

	@Before
	public void setup() {
		this.chessBoard = new ArrayBoard();
		chessBoard.fromFEN(ChessConstants.INITIAL_FEN);
	}

	@Test
	public void testGetPiece() {
		assertPieceNotNull(Square.E2, Piece::isPawn);
	}

	@Test
	public void testCanSetEmptyFen() {
		chessBoard.fromFEN(ChessConstants.EMPTY_BOARD_FEN);
	}

	@Test
	public void testConvertToFenNotation() {
		assertEquals(ChessConstants.INITIAL_FEN, chessBoard.toFEN());
	}

	@Test
	public void testHalfMoveClock() {
		assertEquals(0, chessBoard.getHalfMoveClock());
		assertEquals(1, chessBoard.getMoveNumber());

		move(Square.E2, Square.E4);
		assertEquals(0, chessBoard.getHalfMoveClock());
		assertEquals(1, chessBoard.getMoveNumber());

		move(Square.C7, Square.C5);
		assertEquals(0, chessBoard.getHalfMoveClock());
		assertEquals(2, chessBoard.getMoveNumber());

		move(Square.G1, Square.F3);
		assertEquals(1, chessBoard.getHalfMoveClock());
		assertEquals(2, chessBoard.getMoveNumber());
	}

	@Test
	public void testPawnOneSquareMove() {
		chessBoard.fromFEN(FENConstants.FEN_PAWN_ONE_SQUARE_ADVANCE);
		move(Square.E2, Square.E3);
		assertPieceNotNull(Square.E3, Piece::isPawn);
		assertPieceNull(Square.E2);
	}

	@Test
	public void testPawnTwoSquareMoveAtStart() {
		chessBoard.fromFEN(ChessConstants.INITIAL_FEN);
		move(Square.E2, Square.E4);
		assertPieceNotNull(Square.E4, Piece::isPawn);
		assertPieceNull(Square.E2);
		assertPieceNull(Square.E3);
	}

	@Test(expected = IllegalMoveException.class)
	public void testPawnTwoSquareMoveNotAllowedWhenNotAtStart() {
		chessBoard.fromFEN(ChessConstants.INITIAL_FEN);
		move(Square.E2, Square.E4);

		move(Square.A7, Square.A6);

		// white pawn tries to move two squares again
		move(Square.E4, Square.E6);
	}

	@Test
	public void testPawnNormalCapture() {
		chessBoard.fromFEN(FENConstants.FEN_PAWN_NORMAL_CAPTURE);

		move(Square.D4, Square.E5);

		assertPieceNotNull(Square.E5, Piece::isPawn);
		assertPieceNull(Square.D4);
	}

	@Test
	public void testPawnEnPassantCapture() {
		chessBoard.fromFEN(FENConstants.FEN_PAWN_EN_PASSANT_CAPTURE);

		move(Square.D5, Square.E6);

		assertPieceNotNull(Square.E6, Piece::isPawn);
		assertPieceNull(Square.D5);
		assertPieceNull(Square.E5);
	}

	@Test
	public void testPawnPromotion() {
		chessBoard.fromFEN(FENConstants.FEN_PAWN_PROMOTION);

		move(Square.H7, Square.H8, PieceType.QUEEN);

		assertPieceNotNull(Square.H8, Piece::isQueen);
		assertPieceNull(Square.H7);
	}

	@Test
	public void testWhiteKingSideCastlingIsIgnoredIfNotPossible() {
		chessBoard.fromFEN(ChessConstants.KINGS_ONLY_FEN.replaceFirst("-", "K"));
		Castling castling = chessBoard.getCastling();
		assertFalse("No castling rights expected, but found: " + castling, castling.hasAnyCastlingRights());
	}

	@Test
	public void testWhiteQueenSideCastlingIsIgnoredIfNotPossible() {
		chessBoard.fromFEN(ChessConstants.KINGS_ONLY_FEN.replaceFirst("-", "Q"));
		Castling castling = chessBoard.getCastling();
		assertFalse("No castling rights expected, but found: " + castling, castling.hasAnyCastlingRights());
	}

	@Test
	public void testBlackKingSideCastlingIsIgnoredIfNotPossible() {
		chessBoard.fromFEN(ChessConstants.KINGS_ONLY_FEN.replaceFirst("-", "k"));
		Castling castling = chessBoard.getCastling();
		assertFalse("No castling rights expected, but found: " + castling, castling.hasAnyCastlingRights());
	}

	@Test
	public void testBlackQueenSideCastlingIsIgnoredIfNotPossible() {
		chessBoard.fromFEN(ChessConstants.KINGS_ONLY_FEN.replaceFirst("-", "q"));
		Castling castling = chessBoard.getCastling();
		assertFalse("No castling rights expected, but found: " + castling, castling.hasAnyCastlingRights());
	}

	@Test
	public void testCastlingRightsAreLostOnKingMove() {
		chessBoard.fromFEN(FENConstants.KINGSIDE_CASTLING_AVAILABLE);

		assertTrue(chessBoard.getCastling().hasRight(CastlingRight.WhiteKingSide));

		// move the king
		move(Square.E1, Square.F1);

		// castling rights should be lost now
		assertFalse(chessBoard.getCastling().hasRight(CastlingRight.WhiteKingSide));
		assertFalse(chessBoard.getCastling().hasRight(CastlingRight.WhiteQueenSide));
	}

	@Test
	public void testCastlingRightsAreLostOnKingCaptureMove() {

		chessBoard.fromFEN(FENConstants.KING_CAN_CAPTURE);

		assertTrue(chessBoard.getCastling().hasRight(CastlingRight.WhiteKingSide));

		// capture the black knight with the king
		move(Square.E1, Square.D1);

		assertFalse(chessBoard.getCastling().hasRight(CastlingRight.WhiteKingSide));
		assertFalse(chessBoard.getCastling().hasRight(CastlingRight.WhiteQueenSide));
	}

	@Test
	public void testCastlingRightsAreLostOnKingRookMove() {

		chessBoard.fromFEN(FENConstants.KINGSIDE_CASTLING_AVAILABLE);

		assertTrue(chessBoard.getCastling().hasRight(CastlingRight.WhiteKingSide));

		// moving king rook should loose the right to castle king-side
		move(Square.H1, Square.G1);

		assertFalse(chessBoard.getCastling().hasRight(CastlingRight.WhiteKingSide));
	}

	@Test
	public void testCastlingRightsAreLostOnQueenRookMove() {

		chessBoard.fromFEN(FENConstants.QUEENSIDE_CASTLING_AVAILABLE);

		assertTrue(chessBoard.getCastling().hasRight(CastlingRight.WhiteQueenSide));

		// moving queen rook should loose the right to castle queen-side
		move(Square.A1, Square.B1);

		assertFalse(chessBoard.getCastling().hasRight(CastlingRight.WhiteQueenSide));
	}

	@Test
	public void testCastlingRightsAreLostOnQueenRookCaptureMove() {

		chessBoard.fromFEN(FENConstants.QUEENSIDE_CASTLING_AVAILABLE);

		assertTrue(chessBoard.getCastling().hasRight(CastlingRight.WhiteQueenSide));

		// move the leftmost white pawn
		move(Square.A2, Square.A3);

		// capture with the black pawn
		move(Square.B4, Square.A3);

		// re-capture black pawn with the white queen rook
		move(Square.A1, Square.A3);

		assertFalse(chessBoard.getCastling().hasRight(CastlingRight.WhiteQueenSide));
	}

	@Test
	public void testCastlingRightsAreLostOnEnemyRookCapture() {
		chessBoard.fromFEN(FENConstants.CASTLING_AVAILABLE_UNTIL_ROOK_CAPTURE);

		assertTrue(chessBoard.getCastling().hasRight(CastlingRight.WhiteKingSide));

		// black queen captures white rook. white should loose the right to castle king-side
		move(Square.H5, Square.H1);

		assertFalse(chessBoard.getCastling().hasRight(CastlingRight.WhiteKingSide));
	}

	@Test
	public void testCastlingRightsAreNotLostOnWrongRookCapture() {
		chessBoard.fromFEN(FENConstants.CASTLING_AVAILABLE_AFTER_WRONG_ROOK_CAPTURE);

		assertTrue(chessBoard.getCastling().hasRight(CastlingRight.WhiteKingSide));

		// black queen captures a white rook on the black's territory. this should not affect the white's right to
		// castle king-side sine the rook on h1 still sits in its original square without moving
		// (Note: this was a bug that was discovered during perft tests)
		move(Square.H5, Square.H8);

		assertTrue(chessBoard.getCastling().hasRight(CastlingRight.WhiteKingSide));
	}


	/**
	 * Tests a special case when a piece is pinned to the king and thus cannot move.
	 * In this scenario the en-passant capture removes two pawns and leaves king in check. Thus the black pawn
	 * is actually pinned, so it should not be able to capture en-passant.
	 */
	@Test(expected = IllegalMoveException.class)
	public void testPawnIsPinnedForEnPassantDiscoveredCheckMoveNotAllowed() {
		chessBoard.fromFEN(FENConstants.PAWN_EN_PASSANT_CAPTURE_PINNED);

		move(Square.E4, Square.D3);
//		GameState result = move(Square.E4, Square.D3);
//		assertEquals(GameState.INVALID_MOVE_KING_IN_CHECK, result);
	}

	/**
	 * Tests the same scenario for the special case of the en-passant discovered check as above but by calling
	 * the {@link Board#isPinned(Piece, Square, Square)} directly. We should probably leave one of the two
	 */
	@Test
	public void testIsPinnedForEnPassantDiscoveredCheck() {
		chessBoard.fromFEN(FENConstants.PAWN_EN_PASSANT_CAPTURE_PINNED);
		assertTrue(chessBoard.isPinned(Piece.PAWN_BLACK, Square.E4, Square.D3));
	}

	@Test
	public void testIsPinnedForEnemyPlayer() {
		// the Ruy Lopez: black last played d6 leaving the c6 knight pinned
		chessBoard.fromFEN(FENConstants.RUY_LOPEZ_6_MOVES);
		assertTrue(chessBoard.isPinned(Piece.KNIGHT_BLACK, Square.C6, Square.E7));
	}

	/**
	 * Tests a bug we discovered in isPinned(). The method used the king's position of the current player and not
	 * the king with the same color with the piece specified (i.e. in this test it was testing if the black knight
	 * in c6 is pinned to the white king(!)
	 */
	@Test
	public void testIsPinnedForCurrentPlayer() {
		// Ruy Lopez: black last played d6 leaving the c6 knight pinned - change the position to be black's turn
		chessBoard.fromFEN(FENConstants.RUY_LOPEZ_6_MOVES.replace('w', 'b'));
		assertTrue(chessBoard.isPinned(Piece.KNIGHT_BLACK, Square.C6, Square.E7));
	}

	@Test
	public void testPawnIsNotPinnedWhenSliderDoesNotAttackKing() {
		chessBoard.fromFEN(FENConstants.PAWN_NOT_PINNED_WHEN_SLIDER_NOT_ATTACKS_KING);
		assertFalse(chessBoard.isPinned(Piece.PAWN_BLACK, Square.E5, Square.D4));
	}

	@Test
	public void testIsPinnedAllowsMoveAlongThePinDirection() {
		chessBoard.fromFEN(FENConstants.PIECE_NOT_PINNED_ALONG_PIN_DIRECTION);

		// outside pin ray
		assertTrue(chessBoard.isPinned(Piece.ROOK_WHITE, Square.E3, Square.F3));
		assertTrue(chessBoard.isPinned(Piece.ROOK_WHITE, Square.E3, Square.D3));

		// along the pin ray (forward and backward)
		assertFalse(chessBoard.isPinned(Piece.ROOK_WHITE, Square.E3, Square.E4));
		assertFalse(chessBoard.isPinned(Piece.ROOK_WHITE, Square.E3, Square.E2));
	}

	@Test
	public void testCapturedPiecesRestoredWhenBrowsing() {
		chessBoard.fromFEN(FENConstants.CAPTURES_WHEN_BROWSING);

		assertTrue(chessBoard.getCapturedPieces().isEmpty());

		move(Square.E4, Square.D5); // white pawn captures black pawn
		assertCapturedPieces(1, Piece.PAWN_BLACK);

		move(Square.D8, Square.D5); // black pawn captures white pawn
		assertCapturedPieces(2, Piece.PAWN_BLACK, Piece.PAWN_WHITE);

		chessBoard.browseMoveList(MoveList.BrowseType.FIRST);
		assertCapturedPieces(0);

		chessBoard.browseMoveList(MoveList.BrowseType.LAST);
		assertCapturedPieces(2, Piece.PAWN_BLACK, Piece.PAWN_WHITE);
	}

	@Test
	public void testUndoMoveRestoresRookIfCastling() {

		chessBoard.fromFEN(FENConstants.KINGSIDE_CASTLING_AVAILABLE);

		// king castles kingside
		move(Square.E1, Square.G1);

		Move move = chessBoard.getCurrentMove();

		assertTrue(move.isKingSideCastling());

		chessBoard.undoPlayerMove();

		assertPieceNotNull(Square.E1, Piece::isKing);   // after undo, king square should be restored
		assertPieceNotNull(Square.H1, Piece::isRook);   // rook square should be also restored
		assertPieceNull(Square.F1);
	}

	@Test
	public void testUndoMoveRestoresEnPassantSquare() {
		chessBoard.fromFEN(ChessConstants.INITIAL_FEN);
		// white pawn e2-e4
		move(Square.E2, Square.E4);

		// en-passant square should now be available
		assertEquals(Square.E3, chessBoard.getEnPassant());

		chessBoard.undoPlayerMove();

		// after undo, en-passant square should be null again
		assertFalse(chessBoard.getEnPassant().isValid());

		// the white pawn should be restored to its initial square
		assertPieceNotNull(Square.E2);
		assertPieceNull(Square.E3);
		assertPieceNull(Square.E4);
	}

	@Test
	public void testUndoMoveRestoresPawnsAfterEnPassantCapture() {
		chessBoard.fromFEN(FENConstants.FEN_PAWN_EN_PASSANT_CAPTURE);

		// white pawn captures en-passant
		move(Square.D5, Square.E6);

		chessBoard.undoPlayerMove();

		assertPieceNotNull(Square.D5, Piece::isWhite);  // the white pawn should be restored to its initial square
		assertPieceNotNull(Square.E5, Piece::isBlack);  // the black pawn should be restored to its initial square too
		assertPieceNull(Square.E6);                     // the en-passant square should not have a pawn
	}


	@Test
	public void testUndoMoveRestoresLostCastlingRights() {
		// perform the same testing actions as when testing for lost castling rights due to king move
		testCastlingRightsAreLostOnKingMove();

		chessBoard.undoPlayerMove();

		// after undo, castling rights should be restored
		assertTrue(chessBoard.getCastling().hasRight(CastlingRight.WhiteKingSide));
	}

	@Test
	public void testUndoMoveRestoresPromotingPawn() {
		chessBoard.fromFEN(FENConstants.FEN_PAWN_PROMOTION);

		move(Square.H7, Square.H8, PieceType.QUEEN);

		chessBoard.undoPlayerMove();

		assertPieceNotNull(Square.H7, Piece::isPawn);
		assertPieceNull(Square.H8);
	}

	@Test
	public void testCheckMateQueen() {
		chessBoard.fromFEN(FENConstants.CHECKMATE_QUEEN);
		GameState state = move(Square.H7, Square.E7);
		assertEquals(GameState.CHECKMATE, state);
	}

	@Test
	public void testCheckMateFoolsMate() {
		chessBoard.fromFEN(FENConstants.CHECKMATE_FOOLS_MATE);
		GameState state = move(Square.D8, Square.H4);
		assertEquals(GameState.CHECKMATE, state);
	}

	@Test
	public void testStaleMateQueen() {
		chessBoard.fromFEN(FENConstants.STALEMATE_QUEEN);
		GameState state = move(Square.H6, Square.F6);
		assertEquals(GameState.STALEMATE, state);
	}

	@Test
	public void testThreeFoldRepetition() {
		chessBoard.fromFEN(FENConstants.STALEMATE_QUEEN);
		move(Square.H6, Square.H1);
		move(Square.E8, Square.D8);

		move(Square.H1, Square.H6);
		move(Square.D8, Square.E8);

		move(Square.H6, Square.H1);
		move(Square.E8, Square.D8);

		move(Square.H1, Square.H6);
		GameState state = move(Square.D8, Square.E8);

		assertEquals(GameState.DRAW_THREEFOLD_REPETITION, state);
	}

	@Test
	public void testPawnCanDefendCheckByBlock() {
		chessBoard.fromFEN(FENConstants.PAWN_CAN_DEFEND_CHECK_BY_BLOCK);

		// give check with the black queen
		GameState state = move(Square.D8, Square.A5);

		// the result should be check, not check-mate because the white c-pawn can block the diagonal
		assertEquals(GameState.CHECK, state);
	}

	@Test
	public void testPawnCannotDefendCheckByBlockWhenBlockedItself() {
		chessBoard.fromFEN(FENConstants.PAWN_CANNOT_DEFEND_CHECK_BY_BLOCK_WHEN_BLOCKED);

		// give check with the black queen
		GameState state = move(Square.D8, Square.A5);

		// the result should be check-mate because the white c-pawn is blocked (otherwise a two square push would block the check)
		assertEquals(GameState.CHECKMATE, state);
	}

	@Test
	public void testPawnCanDefendCheckByCapture() {
		chessBoard.fromFEN(FENConstants.PAWN_CAN_DEFEND_CHECK_BY_CAPTURE);

		// give check with the black queen
		GameState state = move(Square.C8, Square.C3);

		// the result should be check, not check-mate because the white b-pawn can capture the attacker
		assertEquals(GameState.CHECK, state);
	}

	@Test
	public void testKingCanDefendCheckByCapture() {
		chessBoard.fromFEN(FENConstants.PAWN_CAN_DEFEND_CHECK_BY_BLOCK);

		// give check with the black queen
		GameState state = move(Square.D8, Square.D2);

		// the result should be check, not check-mate because the white king can capture the attacker
		assertEquals(GameState.CHECK, state);
	}

	@Test
	public void testRookCanDefendCheckByBlock() {
		chessBoard.fromFEN(FENConstants.ROOK_CAN_DEFEND_CHECK_BY_BLOCK);

		// give check with the black queen
		GameState state = move(Square.D8, Square.A5);

		// the result should be check, not check-mate because the white rook can block the attacker
		assertEquals(GameState.CHECK, state);
	}

	@Test
	public void testRookCanDefendCheckByCapture() {
		chessBoard.fromFEN(FENConstants.ROOK_CAN_DEFEND_CHECK_BY_CAPTURE);

		// give check with the black queen
		GameState state = move(Square.D8, Square.A5);

		// the result should be check, not check-mate because the white rook can capture the attacker
		assertEquals(GameState.CHECK, state);
	}

	@Test
	public void testNeedsFileDisambiguationWhenOtherPieceHasSameRank() {
		chessBoard.fromFEN(FENConstants.DISAMBIGUATION_NEEDS_FILE_SAME_RANK);
		move(Square.D1, Square.E3);
		String move = chessBoard.getCurrentMove().toMoveString();
		assertEquals("Nde3", move);
	}

	@Test
	public void testNeedsFileDisambiguationFileWhenOtherPieceHasDifferentFileAndRank() {
		chessBoard.fromFEN(FENConstants.DISAMBIGUATION_NEEDS_FILE_DIFFERENT_RANK_AND_FILE);
		move(Square.G1, Square.E2);
		String move = chessBoard.getCurrentMove().toMoveString();
		assertEquals("Nge2", move);
	}

	@Test
	public void testNotNeedsFileDisambiguationWhenOtherPieceIsPinned() {
		chessBoard.fromFEN(FENConstants.NO_DISAMBIGUATION_OTHER_PIECE_PINNED);
		move(Square.F1, Square.E3);
		String move = chessBoard.getCurrentMove().toMoveString();
		assertEquals("Ne3", move);
	}

	@Test
	public void testNeedsRankDisambiguationWhenOtherPieceHasSameFile() {
		chessBoard.fromFEN(FENConstants.DISAMBIGUATION_NEEDS_RANK_SAME_FILE);
		move(Square.E3, Square.G4);
		String move = chessBoard.getCurrentMove().toMoveString();
		assertEquals("N3g4", move);
	}

	@Test
	public void testNeedsRankAndFileDisambiguationWhenOtherPiecesHaveSameRankAndFile() {
		chessBoard.fromFEN(FENConstants.DISAMBIGUATION_NEEDS_BOTH_RANK_ANDE_FILE);
		move(Square.D1, Square.E3);
		String move = chessBoard.getCurrentMove().toMoveString();
		assertEquals("Nd1e3", move);
	}

	private GameState move(Square from, Square to) {
		return move(from, to, null);
	}

	private GameState move(Square from, Square to, PieceType promotionType) {
		Piece piece = chessBoard.getPieceAt(from);

		assertNotNull(piece);
		assertEquals("Incorrect player's color", chessBoard.getPlayingColor(), piece.color());

		return chessBoard.makePlayerMove(from, to, promotionType);
	}

	private void assertCapturedPieces(int expectedCaptures, Piece... capturedPieces) {
		List<Piece> captures = chessBoard.getCapturedPieces();
		assertEquals(expectedCaptures, captures.size());

		for (Piece captured: capturedPieces) {
			assertTrue(captures.contains(captured));
		}
	}


	private void assertPieceNull(Square p) {
		assertNull(chessBoard.getPieceAt(p));
	}

	@SuppressWarnings("SameParameterValue")
	private void assertPieceNotNull(Square p) {
		assertPieceNotNull(p, null);
	}

	private void assertPieceNotNull(Square p, PieceAssertion typeAssertion) {
		Piece piece = chessBoard.getPieceAt(p);
		assertNotNull(piece);
		if (typeAssertion != null) {
			assertTrue(typeAssertion.is(piece));
		}
	}


}
