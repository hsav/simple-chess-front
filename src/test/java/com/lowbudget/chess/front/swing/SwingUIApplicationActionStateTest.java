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
import com.lowbudget.chess.model.Move;
import com.lowbudget.chess.model.Square;
import com.lowbudget.chess.model.uci.client.UCIClient;
import com.lowbudget.chess.model.uci.server.UCIServer;
import org.junit.*;

import java.util.Collections;

import static com.lowbudget.chess.front.swing.SwingHelper.*;
import static org.assertj.swing.edt.GuiActionRunner.execute;

public class SwingUIApplicationActionStateTest extends BaseSwingTest {

	@Test
	public void testWindowTitle() {
		window.requireTitle(APP_TITLE);
	}

	@Test
	public void testAllowedActionsWhenApplicationStarts() {
		assertEnabled(
				newGameAction,
				loadGameAction,
				enterSetupModeAction,
				manageEnginesAction,
				runServerAction
		);
	}

	@Test
	public void testNotAllowedActionsWhenApplicationStarts() {
		assertDisabled(
				saveGameAction,
				pauseGameAction,
				stopGameAction,
				closeGameAction,
				browseMoveListAction,
				exitSetupModeAction,
				copyFenAction,
				pasteFenAction,
				stopServerAction,
				whiteSettingsAction,
				blackSettingsAction
		);
	}

	@Test
	public void testEnterAndExitSetupModeActionsWhenInSetupMode() {
		click(enterSetupModeAction);
		assertEnabled(exitSetupModeAction);
		assertDisabled(enterSetupModeAction);

		click(exitSetupModeAction);
		assertEnabled(enterSetupModeAction);
		assertDisabled(exitSetupModeAction);
	}

	@Test
	public void testCopyFenEnabledWhenInSetupMode() {
		click(enterSetupModeAction);
		assertEnabled(copyFenAction);
	}

	@Test
	public void testPasteEnabledAfterCopyWhenInSetupMode() {
		// ensure there is no text currently in clipboard from some other application otherwise paste action can be enabled
		clearClipboardTextByCopyingSomethingElse();

		click(enterSetupModeAction);
		assertEnabled(copyFenAction);
		assertDisabled(pasteFenAction);

		// now copy fen to clipboard - paste action should become enabled
		click(copyFenAction);
		assertEnabled(copyFenAction);
		assertEnabled(pasteFenAction);
	}

	@Test
	public void testStatusOfGameActionsAfterGameStarts() {
		assertEnabled(loadGameAction);
		assertDisabled(
				pauseGameAction,
				stopGameAction,
				saveGameAction,
				closeGameAction,
				browseMoveListAction
		);

		startNewGame();

		assertDisabled(newGameAction, loadGameAction, saveGameAction, browseMoveListAction);
		assertEnabled(pauseGameAction, stopGameAction, closeGameAction);
	}

	@Test
	public void testStatusOfGameActionsAfterGameStops() {
		startNewGame();
		click(stopGameAction);
		assertEnabled(
				newGameAction,
				loadGameAction,
				closeGameAction
		);

		assertDisabled(
				pauseGameAction,
				stopGameAction,
				// note: below actions should be disabled because no moves were made
				saveGameAction,
				browseMoveListAction
		);
	}

	@Test
	public void testStatusOfGameActionsAfterGameCloses() {
		startNewGame();
		click(closeGameAction);
		assertEnabled(newGameAction, loadGameAction);
		assertDisabled(
				pauseGameAction,
				stopGameAction,
				saveGameAction,
				closeGameAction,
				browseMoveListAction
		);
	}

	@Test
	public void testWhiteSettingsActionIsEnabledIfWhitePlayerIsAnEngine() {
		startNewGameWithEnginePlayers(true, false);
		assertEnabled(whiteSettingsAction);
		assertDisabled(blackSettingsAction);
	}

	@Test
	public void testBlackSettingsActionIsEnabledIfBlackPlayerIsAnEngine() {
		startNewGameWithEnginePlayers(false, true);
		assertDisabled(whiteSettingsAction);
		assertEnabled(blackSettingsAction);
	}

	@Test
	public void testAllNonServerActionsAreDisabledAfterServerStarts() {
		// note that this will start the server's listening thread a new thread however it will be shutdown immediately
		execute (() -> application.setUCIServerFactory(UCIServer.DEFAULT_FACTORY));

		click(runServerAction);
		clickDialogOk();

		assertDisabled(
				newGameAction,
				loadGameAction,
				pauseGameAction,
				stopGameAction,
				closeGameAction,
				saveGameAction,
				browseMoveListAction,
				enterSetupModeAction,
				exitSetupModeAction,
				pasteFenAction,
				whiteSettingsAction,
				blackSettingsAction
		);

		// these are enabled
		assertEnabled(copyFenAction, stopServerAction);
	}

	@Test
	public void testAllNonServerActionsAreDisabledAfterServerGameStarts() {
		// add a game move listener - we will need this to wait until a move is made
		GameMoveListener gameListener = new GameMoveListener();

		execute (() -> {
			// start an actual server
			application.setUCIServerFactory(UCIServer.DEFAULT_FACTORY);

			application.getGameModel().addBoardModelListener(gameListener);
		});

		click(runServerAction);
		clickDialogOk();

		// create an actual client and connect to the server
		UCIClient client = connectClientAndWaitUntilReady(application.getDefaultServerPort());

		execute( () -> client.sendFenPosition(ChessConstants.INITIAL_FEN, Collections.singletonList(Move.of(Square.E2, Square.E4))) );

		// wait until a move is made. This would change the state of the buttons in a normal game.
		// For a server game though, the buttons should remain disabled
		waitUntilMoveMade(gameListener);

		assertDisabled(
				newGameAction,
				loadGameAction,
				pauseGameAction,
				stopGameAction,
				closeGameAction,
				saveGameAction,
				browseMoveListAction,
				enterSetupModeAction,
				exitSetupModeAction,
				pasteFenAction,
				whiteSettingsAction,
				blackSettingsAction
		);

		// these are enabled
		assertEnabled(copyFenAction, stopServerAction);
	}

	@Test
	public void testSaveIsNotAllowedWhenNoMovesPlayed() {
		click(newGameAction);
		clickOk(window.dialog());
		click(stopGameAction);
		// no moves were made so save should still be unavailable
		assertDisabled(saveGameAction);
	}

	@Test
	public void testSaveIsNotAllowedWhenGameIsStillPlaying() {
		click(newGameAction);
		clickOk(window.dialog());

		move(Square.E2, Square.E4);
		assertDisabled(saveGameAction);
	}

	@Test
	public void testSaveIsAllowedWhenGameIsStoppedAndMovesWerePlayed() {
		startNewGamePlayOneMoveAndStop();

		// after stop and since there are moves played we should be able to save the game
		assertEnabled(saveGameAction);
	}

	@Test
	public void testSaveIsNotAllowedAfterGameIsSaved() {
		startNewGamePlayOneMoveAndStop();
		click(saveGameAction);
		approveSaveDialog();
		assertDisabled(saveGameAction);
	}

}
