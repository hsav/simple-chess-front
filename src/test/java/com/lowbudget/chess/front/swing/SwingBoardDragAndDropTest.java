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

package com.lowbudget.chess.front.swing;

import com.lowbudget.chess.model.ChessConstants;
import com.lowbudget.chess.model.Piece;
import com.lowbudget.chess.model.Square;
import com.lowbudget.chess.front.swing.SwingHelper.SquareViewFixture;
import org.junit.*;

import static com.lowbudget.chess.front.swing.ComponentNames.promotionPopupMenuItemName;
import static com.lowbudget.chess.front.swing.SwingHelper.SquareViewFixture.assertBoardFen;
import static com.lowbudget.chess.front.swing.SwingHelper.enterSetupModeAction;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class SwingBoardDragAndDropTest extends BaseSwingTest {

	@Test
	public void testDragAndDropToSameSquareTogglesSquareSelection() {
		startNewGame();

		SquareViewFixture square = square(Square.E2).requireUnSelected();

		square.dragToSelf().requireSelected();
		square.dragToSelf().requireUnSelected();
	}

	@Test
	public void testNormalMove() {
		startNewGame();

		SquareViewFixture from = square(Square.D2).requirePiece(Piece.PAWN_WHITE);
		SquareViewFixture to = square(Square.D4).requireEmpty();

		from.dragToSquare(to).requireEmpty();
		to.requirePiece(Piece.PAWN_WHITE);
	}

	@Test
	public void testPawnAtLastRankActivatesPromotionPopup() {
		startNewGame();

		// play a series of moves so the white h-pawn promotes at H8 (black is helping along the way)
		move(Square.H2, Square.H4);
		move(Square.A7, Square.A6);
		move(Square.H4, Square.H5);
		move(Square.A6, Square.A5);
		move(Square.H5, Square.H6);
		move(Square.A5, Square.A4);
		move(Square.H6, Square.G7);
		move(Square.A4, Square.A3);
		move(Square.G7, Square.H8);

		assertEquals(ComponentNames.PROMOTION_POPUP, activePopupMenuName());

		clickMenuItem( promotionPopupMenuItemName(Piece.QUEEN_WHITE) );

		square(Square.H8).requirePiece(Piece.QUEEN_WHITE);
	}

	@Test
	public void testSetupDragAndDropToSameSquareDoesNotToggleSelection() {
		click(enterSetupModeAction);

		// when entering setup mode only kings should be available
		SquareViewFixture square = square(Square.E2).requireEmpty();

		// drag and drop to same square does not cause the square to be selected like in the normal game board
		square.dragToSelf().requireUnSelected();
	}

	@Test
	public void testSetupAllowsIllegalMoves() {
		click(enterSetupModeAction);

		// the white king
		SquareViewFixture square = square(Square.E1).requirePiece(Piece.KING_WHITE);
		SquareViewFixture targetSquare = square(Square.D5).requireEmpty();

		// make a move that would be illegal in a normal game
		square.dragToSquare(targetSquare);

		square.requireEmpty();
		targetSquare.requirePiece(Piece.KING_WHITE);
	}

	@Test
	public void testSetupInitialAndEmptyPositionButtons() {
		click(enterSetupModeAction);

		clickButton(ComponentNames.SETUP_INITIAL_POSITION_BUTTON);
		assertBoardFen(application.getSetupBoardModel(), ChessConstants.INITIAL_FEN);

		clickButton(ComponentNames.SETUP_EMPTY_POSITION_BUTTON);
		assertBoardFen(application.getSetupBoardModel(), ChessConstants.KINGS_ONLY_FEN);
	}

	@Test
	public void testSetupNotAllowsToDeleteKing() {
		click(enterSetupModeAction);

		// try to delete (i.e. right click) the white king
		square(Square.E1).rightClick();

		// a warning should appear
		assertNotNull(window.optionPane());
	}

	@Test
	public void testSetupNotAllowsKingToOverwriteKing() {
		click(enterSetupModeAction);

		// try to delete (i.e. right click) the white king
		SquareViewFixture whiteKing = square(Square.E1);
		SquareViewFixture blackKing = square(Square.E8);

		// try to place the white king at the square occupied by the black king (i.e. to overwrite it)
		whiteKing.dragToSquare(blackKing);

		// a warning should appear
		assertAndDismissOptionPane();
	}

	@Test
	public void testSetupNotAllowsPieceToOverwriteKingBySetting() {
		click(enterSetupModeAction);

		// select a white knight as the current piece
		clickToggleButton(ComponentNames.setupPieceButtonName(Piece.KNIGHT_WHITE));

		// try to set the white knight at a square with a king
		click(Square.E1);

		// a warning should appear
		assertAndDismissOptionPane();
	}

	@Test
	public void testSetupNotAllowsPieceToOverwriteKingByMoving() {
		click(enterSetupModeAction);

		// set the initial position
		clickButton(ComponentNames.SETUP_INITIAL_POSITION_BUTTON);

		// try to move the white knight at a square with a king
		move(Square.G1, Square.E1);

		// a warning should appear
		assertAndDismissOptionPane();
	}

	@Test
	public void testSetupPiece() {
		click(enterSetupModeAction);

		// select the piece to set
		clickToggleButton( ComponentNames.setupPieceButtonName(Piece.PAWN_BLACK) );

		// click at some random squares to set the piece
		click(Square.E5).requirePiece(Piece.PAWN_BLACK);
		click(Square.C2).requirePiece(Piece.PAWN_BLACK);
		click(Square.G6).requirePiece(Piece.PAWN_BLACK);
	}

	@Test
	public void testSetupNotAllowsPawnsAtFirstOrLastRanks() {
		click(enterSetupModeAction);

		// select the piece to set
		clickToggleButton( ComponentNames.setupPieceButtonName(Piece.PAWN_BLACK) );

		// try to set a pawn at the first rank - should display an error
		click(Square.C1);
		assertAndDismissOptionPane();

		// try to set a pawn at the last rank - should display an error
		click(Square.C8);
		assertAndDismissOptionPane();
	}

}
