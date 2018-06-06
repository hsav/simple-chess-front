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

package com.lowbudget.chess.front.app.impl;

import com.lowbudget.chess.front.app.Helper;
import com.lowbudget.chess.front.app.UIApplication;
import com.lowbudget.chess.front.app.config.GameSetup;
import com.lowbudget.chess.front.app.config.PlayerSetup;
import com.lowbudget.chess.front.app.Helper.CallableAction;
import com.lowbudget.chess.model.PlayerColor;
import com.lowbudget.chess.model.Square;
import com.lowbudget.chess.model.Move;
import com.lowbudget.chess.model.uci.server.UCIServer;
import com.lowbudget.chess.model.uci.client.UCIClient;
import com.lowbudget.chess.model.uci.connect.Connectable.Params;
import com.lowbudget.chess.model.uci.engine.UCIEngineConfig;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class HeadlessApplicationActionStateTest {

	private UIApplication application;

	private final CallableAction newGameAction = CallableAction.of(() -> application.isNewGameActionAllowed());
	private final CallableAction enterSetupModeAction = CallableAction.of(() -> application.isEnterSetupModeActionAllowed());
	private final CallableAction exitSetupModeAction = CallableAction.of(() -> application.isExitSetupModeActionAllowed());
	private final CallableAction stopGameAction = CallableAction.of(() -> application.isStopGameActionAllowed());
	private final CallableAction pauseGameAction = CallableAction.of(() -> application.isPauseGameActionAllowed());
	private final CallableAction closeGameAction = CallableAction.of(() -> application.isCloseGameActionAllowed());
	private final CallableAction loadGameAction = CallableAction.of(() -> application.isLoadGameActionAllowed());
	private final CallableAction saveGameAction = CallableAction.of(() -> application.isSaveGameActionAllowed());
	private final CallableAction browseMoveListAction = CallableAction.of(() -> application.isBrowseMoveListActionAllowed());
	private final CallableAction copyFenAction = CallableAction.of(() -> application.isCopyFenActionAllowed());
	private final CallableAction pasteFenAction = CallableAction.of(() -> application.isPasteFenActionAllowed());
	private final CallableAction manageEnginesAction = CallableAction.of(() -> application.isManageEnginesActionAllowed());
	private final CallableAction runServerAction = CallableAction.of(() -> application.isRunServerActionAllowed());
	private final CallableAction stopServerAction = CallableAction.of(() -> application.isStopServerActionAllowed());
	private final CallableAction whiteSettingsAction = CallableAction.of(() -> application.isEngineSettingsActionAllowed(PlayerColor.WHITE));
	private final CallableAction blackSettingsAction = CallableAction.of(() -> application.isEngineSettingsActionAllowed(PlayerColor.BLACK));

	@Before
	public void setup() {
		application = new Helper.TestApplicationFactory().createApplication();
		application.setUCIClientFactory(UCIClient.TEST_FACTORY);
		application.setUCIServerFactory(UCIServer.TEST_FACTORY);
	}

	@After
	public void tearDown() {
		application.exit();
	}

	@Test
	public void testAllowedActionsWhenApplicationStarts() {
		assertEnabled(newGameAction);
		assertEnabled(loadGameAction);
		assertEnabled(enterSetupModeAction);
		assertEnabled(manageEnginesAction);
		assertEnabled(runServerAction);
	}

	@Test
	public void testNotAllowedActionsWhenApplicationStarts() {
		assertDisabled(pauseGameAction);
		assertDisabled(stopGameAction);
		assertDisabled(closeGameAction);
		assertDisabled(saveGameAction);
		assertDisabled(browseMoveListAction);
		assertDisabled(exitSetupModeAction);

		assertDisabled(copyFenAction);
		assertDisabled(pasteFenAction);

		assertDisabled(stopServerAction);
		assertDisabled(whiteSettingsAction);
		assertDisabled(blackSettingsAction);
	}

	@Test
	public void testEnterAndExitSetupModeActionsWhenInSetupMode() {
		application.enterSetupMode();
		assertEnabled(exitSetupModeAction);
		assertDisabled(enterSetupModeAction);

		application.exitSetupMode();
		assertEnabled(enterSetupModeAction);
		assertDisabled(exitSetupModeAction);
	}

	@Test
	public void testCopyFenEnabledWhenInSetupMode() {
		application.enterSetupMode();
		assertEnabled(copyFenAction);
	}

	@Test
	public void testPasteEnabledAfterCopyWhenInSetupMode() {
		application.enterSetupMode();
		assertEnabled(copyFenAction);
		assertDisabled(pasteFenAction);

		// now copy fen to clipboard - paste action should become enabled
		application.copyFenPosition();
		assertEnabled(copyFenAction);
		assertEnabled(pasteFenAction);
	}

	@Test
	public void testStatusOfGameActionsAfterGameStarts() {
		assertEnabled(loadGameAction);
		assertDisabled(pauseGameAction);
		assertDisabled(stopGameAction);
		assertDisabled(saveGameAction);
		assertDisabled(closeGameAction);
		assertDisabled(browseMoveListAction);

		startNewGame();

		assertDisabled(newGameAction);
		assertDisabled(loadGameAction);
		assertEnabled(stopGameAction);
		assertDisabled(saveGameAction);
		assertEnabled(closeGameAction);
		assertDisabled(browseMoveListAction);
	}

	@Test
	public void testStatusOfGameActionsAfterGameStops() {
		startNewGame();
		application.stopGame();
		assertEnabled(newGameAction);
		assertEnabled(loadGameAction);
		assertDisabled(pauseGameAction);
		assertDisabled(stopGameAction);
		assertEnabled(closeGameAction);

		assertDisabled(saveGameAction);			// disabled because no moves were made
		assertDisabled(browseMoveListAction);	// no moves were made so browse action should be disabled
	}

	@Test
	public void testStatusOfGameActionsAfterGameCloses() {
		startNewGame();
		application.closeGame();
		assertEnabled(newGameAction);
		assertEnabled(loadGameAction);
		assertDisabled(pauseGameAction);
		assertDisabled(stopGameAction);
		assertDisabled(saveGameAction);
		assertDisabled(closeGameAction);
		assertDisabled(browseMoveListAction);
	}

	@Test
	public void testWhiteSettingsActionIsEnabledIfWhitePlayerIsAnEngine() {
		startNewGameWithWhiteEnginePlayer(true);
		assertEnabled(whiteSettingsAction);
		assertDisabled(blackSettingsAction);
	}

	@Test
	public void testBlackSettingsActionIsEnabledIfBlackPlayerIsAnEngine() {
		startNewGameWithWhiteEnginePlayer(false);
		assertDisabled(whiteSettingsAction);
		assertEnabled(blackSettingsAction);
	}

	@Test
	public void testAllNonServerActionsAreDisabledAfterServerStarts() {
		startServer();

		assertDisabled(newGameAction);
		assertDisabled(loadGameAction);
		assertDisabled(pauseGameAction);
		assertDisabled(stopGameAction);
		assertDisabled(closeGameAction);
		assertDisabled(saveGameAction);
		assertDisabled(browseMoveListAction);

		assertDisabled(enterSetupModeAction);
		assertDisabled(exitSetupModeAction);
		assertDisabled(pasteFenAction);
		assertDisabled(whiteSettingsAction);
		assertDisabled(blackSettingsAction);

		// these actions should be enabled
		assertEnabled(copyFenAction);
		assertEnabled(stopServerAction);
	}

	@Test
	public void testSaveIsNotAllowedWhenNoMovesPlayed() {
		startNewGame();
		application.stopGame();
		// no moves were made so save should still be unavailable
		assertDisabled(saveGameAction);
	}

	@Test
	public void testSaveIsNotAllowedWhenGameIsStillPlaying() {
		startNewGame();
		application.getGameModel().makeMove(Move.of(Square.E2, Square.E4));
		assertDisabled(saveGameAction);
	}

	@Test
	public void testSaveIsAllowedWhenGameIsStoppedAndMovesWerePlayed() {
		startNewGame();
		application.getGameModel().makeMove(Move.of(Square.E2, Square.E4));
		application.stopGame();

		// after stop and since there are moves played we should be able to save the game
		assertEnabled(saveGameAction);
	}

	private void startServer() {
		// note that this will not actually start a server since our test server factory creates a dummy server
		// so its ok to pass dummy values as arguments
		UCIEngineConfig engineConfig = new UCIEngineConfig(Params.NONE);
		application.startServer(engineConfig, application.getDefaultServerPort());
	}

	private void startNewGame() {
		GameSetup gameSetup = new GameSetup(new PlayerSetup(PlayerColor.WHITE), new PlayerSetup(PlayerColor.BLACK));
		application.startNewGame(gameSetup);
	}

	private void startNewGameWithWhiteEnginePlayer(boolean white) {
		PlayerSetup whiteSetup = white ? dummyEnginePlayerSetup(PlayerColor.WHITE) : new PlayerSetup(PlayerColor.WHITE);
		PlayerSetup blackSetup = white ? new PlayerSetup(PlayerColor.BLACK) : dummyEnginePlayerSetup(PlayerColor.BLACK);
		GameSetup gameSetup = new GameSetup(whiteSetup, blackSetup);
		application.startNewGame(gameSetup);
	}

	private PlayerSetup dummyEnginePlayerSetup(PlayerColor color) {
		return new PlayerSetup("", color, Params.NONE, 0);
	}

	private static void assertEnabled(CallableAction action) {
		assertTrue( action.isAllowed() );
	}

	private static void assertDisabled(CallableAction action) {
		assertFalse( action.isAllowed() );
	}

}
