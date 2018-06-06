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

import com.lowbudget.chess.front.app.Helper.Locator;
import com.lowbudget.chess.front.app.impl.EmptyUITimer;
import com.lowbudget.chess.front.app.model.GameModel;
import com.lowbudget.chess.front.app.model.GameModel.GameModelListener;
import com.lowbudget.chess.model.Piece;
import com.lowbudget.chess.model.Square;
import com.lowbudget.chess.model.Move;
import com.lowbudget.chess.front.swing.ComponentNames.DialogNewGame;
import org.assertj.swing.fixture.DialogFixture;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.swing.*;

import java.awt.*;

import static com.lowbudget.chess.front.swing.SwingHelper.*;
import static org.assertj.swing.edt.GuiActionRunner.execute;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

public class SwingBoardGameTest extends BaseSwingTest {

	// kings at D3, F6, two pawns at E4, E5
	private static final String SETUP_BOARD_FEN = "8/8/5k2/4p3/4P3/3K4/8/8 w - - 0 1";

	private GameModelListener mockGameListener;

	@Override
	@Before
	public void setUp() {
		super.setUp();

		mockGameListener = mock(GameModelListener.class);
		application.getGameModel().addGameModelListener(mockGameListener);
	}

	@Override
	@After
	public void tearDown() {
		// we are cleaning up our test, we are not really interested to save any unsaved information here
		// (this is necessary since there are tests in this class (i.e. like close game action) that do leave unsaved
		// information on purpose. The application will re-ask to save them if we don't disable this flag
		execute( () -> application.setCheckUnsavedGame(false));
		super.tearDown();
	}

	@Test
	public void testLastMoveIsNotHighlightedAfterGameCloses() {
		click(newGameAction);
		clickDialogOk();

		move(Square.E2, Square.E4);

		GameModel gameModel = application.getGameModel();

		execute(() -> assertEquals(Move.of(Square.E2, Square.E4), gameModel.getCurrentMove()));

		click(closeGameAction);

		execute( () -> assertFalse(gameModel.getCurrentMove().isValid()));
	}

	@Test
	public void testDisplaysDialogOnIllegalMove() {
		click(newGameAction);
		clickDialogOk();

		move(Square.E2, Square.D4); // illegal move

		assertAndDismissOptionPane();
	}

	@Test
	public void testDisplaysSaveDialogWhenUnsavedGameIsClosedWhileStopped() {
		execute( () -> application.setCheckUnsavedGame(true));
		startNewGamePlayOneMoveAndStop();
		click(closeGameAction);
		assertDialogExists(SAVE_DIALOG_TITLE);
	}

	@Test
	public void testDisplaysSaveDialogWhenUnsavedGameIsClosedWhilePlaying() {
		execute( () -> application.setCheckUnsavedGame(true));
		startNewGameAndPlayOneMove();
		click(closeGameAction);
		assertDialogExists(SAVE_DIALOG_TITLE);
	}

	@Test
	public void testCloseGameIsCancelledWhenSaveDialogIsCancelled() {
		verifyActionIsCancelledWhenSaveDialogIsCancelledAfter(closeGameAction);
	}

	@Test
	public void testCloseGameNotDisplaysSaveDialogWhenGameIsSaved() {
		verifyNotDisplaysSaveDialogWhenGameIsSavedAfter(closeGameAction);
	}

	@Test
	public void testCloseGameNotSavesGameWhenSaveDialogIsAnsweredWithNo() {
		verifyNotSavesGameWhenSaveDialogIsAnsweredWithNoAfter(closeGameAction);
	}

	@Test
	public void testCloseGameSavesGameWhenSaveDialogIsAnsweredWithYes() {
		verifySavesGameWhenSaveDialogIsAnsweredWithYesAfter(closeGameAction);
	}

	@Test
	public void testExitIsCancelledOnExitActionWhenSaveDialogIsCancelled() {
		verifyActionIsCancelledWhenSaveDialogIsCancelledAfter(exitAction);
		Frame w = execute( () -> ((SwingUIApplication) application).getMainWindow());
		assertTrue(w.isDisplayable());
	}

	@Test
	public void testExitNotDisplaysSaveDialogWhenGameIsSaved() {
		verifyNotDisplaysSaveDialogWhenGameIsSavedAfter(exitAction);
	}

	@Test
	public void testExitNotSavesGameWhenSaveDialogIsAnsweredWithNo() {
		verifyNotSavesGameWhenSaveDialogIsAnsweredWithNoAfter(exitAction);
	}

	@Test
	public void testExitSavesGameWhenSaveDialogIsAnsweredWithYes() {
		verifySavesGameWhenSaveDialogIsAnsweredWithYesAfter(exitAction);
	}

	@Test
	public void testDisplaysDialogWhenPaused() {
		click(newGameAction);
		clickDialogOk();

		move(Square.E2, Square.E4);

		click(pauseGameAction);
		assertAndDismissOptionPane();
	}

	@Test
	public void testDisplaysDialogOnFinalMove() {
		click(newGameAction);
		clickDialogOk();

		// Fool's mate
		move(Square.F2, Square.F4);
		move(Square.E7, Square.E6);
		move(Square.G2, Square.G4);
		move(Square.D8, Square.H4);

		assertAndDismissOptionPane();
	}

	@Test
	public void testDisplaysDialogWhenTimeElapses() {
		EmptyUITimer testTimer = new EmptyUITimer();
		application.setUITimerFactory( delay -> testTimer );

		click(newGameAction);
		clickDialogOk();

		execute( () -> {
			// the game that is "remembered"  as the last game is a human-human game with 120 seconds each
			// each timer tick is a second so we need to fire the timer 120 times for the time control to elapse
			for (int i = 0; i < 119; i++) {
				testTimer.fire();
			}

			// fire the last event later otherwise the test will wait at this point for the dialog to close
			SwingUtilities.invokeLater( testTimer::fire );
		});
		assertAndDismissOptionPane();
	}

	@Test
	public void testNewGameWithSetupBoardPositionWhenBoardIsEmpty() {
		setupBoard();
		assertNewGameUsesFen(DialogNewGame.USE_SETUP_POSITION, SETUP_BOARD_FEN);
		assertEquals(SETUP_BOARD_FEN, application.getGameModel().getFenPosition());
		assertSetupBoardSquares();
	}

	@Test
	public void testNewGameWithSetupBoardPositionWhenBoardHasExistingGame() {
		startNewGamePlayOneMoveAndStop();

		setupBoard();
		assertNewGameUsesFen(DialogNewGame.USE_SETUP_POSITION, SETUP_BOARD_FEN);
		assertEquals(SETUP_BOARD_FEN, application.getGameModel().getFenPosition());
		assertSetupBoardSquares();
	}

	@Test
	public void testNewGameWithExistingGamePositionWhenBoardIsEmpty() {
		click(newGameAction);
		DialogFixture dialog = window.dialog();
		dialog.radioButton(DialogNewGame.USE_GAME_POSITION).requireDisabled();
	}

	@Test
	public void testNewGameWithExistingGamePositionWhenBoardHasExistingGame() {
		startNewGamePlayOneMoveAndStop();

		String fen = application.getGameModel().getFenPosition();
		assertNewGameUsesFen(DialogNewGame.USE_GAME_POSITION, fen);
	}


	private void verifyActionIsCancelledWhenSaveDialogIsCancelledAfter(Locator action) {
		execute( () -> application.setCheckUnsavedGame(true));
		startGameThenPlayMoveAndFinishWith(action, BUTTON_CANCEL);
		assertEnabled(closeGameAction);
		verify(mockGameListener, never()).onGameClosed(any());
	}

	private void verifyNotDisplaysSaveDialogWhenGameIsSavedAfter(Locator action) {
		execute( () -> application.setCheckUnsavedGame(true));

		startNewGamePlayOneMoveAndStop();

		click(saveGameAction);
		approveSaveDialog();

		reset(mockGameListener);
		click(action);
		assertDisabled(closeGameAction);
		verify(mockGameListener, never()).onGameSaved(any());
	}

	private void verifyNotSavesGameWhenSaveDialogIsAnsweredWithNoAfter(Locator action) {
		execute( () -> application.setCheckUnsavedGame(true));
		startGameThenPlayMoveAndFinishWith(action, BUTTON_NO);
		assertDisabled(closeGameAction);
		verify(mockGameListener).onGameClosed(any());
		verify(mockGameListener, never()).onGameSaved(any());
	}


	private void verifySavesGameWhenSaveDialogIsAnsweredWithYesAfter(Locator action) {
		execute( () -> application.setCheckUnsavedGame(true));
		startGameThenPlayMoveAndFinishWith(action, BUTTON_YES);

		// this will cause save dialog
		approveSaveDialog();

		assertDisabled(closeGameAction);
		verify(mockGameListener).onGameClosed(any());
		verify(mockGameListener).onGameSaved(any());
	}

	private void startGameThenPlayMoveAndFinishWith(Locator action, String saveDialogButton) {
		startNewGamePlayOneMoveAndStop();
		click(action);
		assertAndDismissDialogByTitle(SAVE_DIALOG_TITLE, saveDialogButton);
	}

	private void assertNewGameUsesFen(String radioName, String expectedFen) {
		click(newGameAction);

		DialogFixture dialog = window.dialog();

		dialog.radioButton(radioName).click();

		clickOk(dialog);
		assertEquals(expectedFen, application.getGameModel().getFenPosition());
	}

	private void setupBoard() {
		click(enterSetupModeAction);
		application.getSetupBoardModel().setFenPosition(SETUP_BOARD_FEN);
	}

	private void assertSetupBoardSquares() {
		square(Square.D3).requirePiece(Piece.KING_WHITE);
		square(Square.F6).requirePiece(Piece.KING_BLACK);
		square(Square.E4).requirePiece(Piece.PAWN_WHITE);
		square(Square.E5).requirePiece(Piece.PAWN_BLACK);
	}

}
