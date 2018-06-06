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

import com.lowbudget.chess.front.app.UIApplication;
import com.lowbudget.chess.front.app.Helper;
import com.lowbudget.chess.front.app.Helper.Locator;
import com.lowbudget.chess.front.app.controller.player.EnginePlayer;
import com.lowbudget.chess.front.app.model.GameModel;
import com.lowbudget.chess.model.Move;
import com.lowbudget.chess.model.PlayerColor;
import com.lowbudget.chess.model.Square;
import com.lowbudget.chess.model.uci.connect.Connectable;
import com.lowbudget.chess.model.uci.server.UCIServer;
import com.lowbudget.chess.model.uci.client.UCIClient;
import com.lowbudget.chess.front.swing.ComponentNames.DialogNewGame;
import com.lowbudget.chess.front.swing.SwingHelper.SquareViewFixture;
import org.assertj.swing.core.EmergencyAbortListener;
import org.assertj.swing.core.GenericTypeMatcher;
import org.assertj.swing.edt.FailOnThreadViolationRepaintManager;
import org.assertj.swing.edt.GuiActionRunner;
import org.assertj.swing.fixture.*;
import org.assertj.swing.timing.Condition;
import org.assertj.swing.timing.Pause;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.concurrent.TimeUnit;

import static com.lowbudget.chess.front.swing.SwingHelper.BUTTON_OK;
import static com.lowbudget.chess.front.swing.SwingHelper.SquareViewExtension.squareWithName;
import static com.lowbudget.chess.front.swing.SwingHelper.newGameAction;
import static com.lowbudget.chess.front.swing.SwingHelper.stopGameAction;
import static org.assertj.swing.edt.GuiActionRunner.execute;
import static org.assertj.swing.timing.Timeout.timeout;
import static org.junit.Assert.assertNotNull;

public abstract class BaseSwingTest {

	FrameFixture window;

	UIApplication application;

	private EmergencyAbortListener listener;

	@BeforeClass
	public static void setUpOnce() {
		FailOnThreadViolationRepaintManager.install();
	}

	@AfterClass
	public static void afterClass() {
		File test = Helper.TEST_SAVE_GAME_FILE;
		if (test.exists()) {
			//noinspection ResultOfMethodCallIgnored
			test.delete();
		}
	}

	@Before
	public void setUp() {
		listener = EmergencyAbortListener.registerInToolkit();

		application = new SwingUIApplicationFactory().createApplication();
		application.initialize();

		// Note that the following code should be executed in EDT for all the changes in this block to be visible in EDT.
		// The SwingUIApplication creates the window in EDT, so we can't access it
		// safely from the test thread i.e. it is possible that it has not been fully created yet.
		// Furthermore the rest of the changes i.e. setting the client factory might not be visible in EDT if they are
		// executed outside of this block.
		// We have discovered that by accident in the Manage engine dialogs test where testing for time-out sometimes
		// failed randomly. The application was using the default client factory instead of the test factory that is
		// setup here proving that the statement "application.setUCIClientFactory(UCIClient.TEST_FACTORY);" was not
		// visible in EDT.
		// Executing in a GuiActionRunner block solves all those problems
		Frame frame = GuiActionRunner.execute(() -> {
			// by default do not check for unsaved data so we do not have to handle the save dialog in all of our tests
			// Test cases specific to unsaved data can re-enable this for their case only
			application.setCheckUnsavedGame(false);

			// Using the test config is required for most of the tests to succeed.
			// Note that this test config introduces dependencies on the default stock-fish engine's executable that
			// should reside in the folder referenced in the config file
			application.loadConfiguration(Helper.TEST_CONFIG_FILE);

			application.setUCIServerFactory(UCIServer.TEST_FACTORY);
			application.setUCIClientFactory(UCIClient.TEST_FACTORY);

			return ((SwingUIApplication) application).getMainWindow();
		});
		window = new FrameFixture(frame);
	}

	@After
	public void tearDown() {
		listener.unregister();
		window.cleanUp();
		GuiActionRunner.execute(application::exit);
	}

	void click(Locator locator) {
		locator.locate(window).click();
	}

	SquareViewFixture click(Square square) {
		return square(square).click();
	}

	SquareViewFixture square(Square square) {
		String boardName = application.isSetupMode() ? ComponentNames.BOARD_SETUP_VIEW : ComponentNames.BOARD_GAME_VIEW;
		return window.with( squareWithName( ComponentNames.boardSquareName(boardName, square)) );
	}

	void assertAndDismissOptionPane() {
		JOptionPaneFixture dialog = window.optionPane(timeout(2, TimeUnit.SECONDS));
		assertNotNull(dialog);
		dialog.buttonWithText(BUTTON_OK).click();
	}

	DialogFixture assertDialogExists(String title) {
		return assertDialogExists(title, 2);
	}

	DialogFixture assertDialogExists(String title, int timeOutInSeconds) {
		DialogFixture dialog = window.dialog(new DialogTitleMatcher(title), timeout(timeOutInSeconds, TimeUnit.SECONDS));
		assertNotNull(dialog);
		return dialog;
	}

	void assertAndDismissDialogByTitle(String title, String buttonText) {
		DialogFixture dialog = assertDialogExists(title);
		clickButtonWithText(dialog, buttonText);
	}

	@SuppressWarnings("WeakerAccess")
	void clickButtonWithText(AbstractWindowFixture window, String text) {
		window.button(new ButtonTextMatcher(text)).click();
	}

	void clickDialogOk() {
		clickOk(window.dialog());
	}

	static void clickOk(AbstractWindowFixture windowFixture) {
		windowFixture.button(ComponentNames.OK_BUTTON).click();
	}

	void startNewGame() {
		click(newGameAction);
		clickDialogOk();
	}

	void clickToggleButton(String name) {
		window.toggleButton(name).click();
	}

	void clickButton(String name) {
		window.button(name).click();
	}

	void clickMenuItem(String name) {
		window.menuItem(name).click();
	}

	String activePopupMenuName() {
		return window.robot().findActivePopupMenu().getName();
	}

	void startNewGameWithEnginePlayers(boolean isWhiteEngine, boolean isBlackEngine) {
		click(newGameAction);

		DialogFixture dialog = window.dialog();

		if (isWhiteEngine) {
			dialog.radioButton(DialogNewGame.FIRST_PLAYER_ENGINE_RADIO).click();
		}
		if (isBlackEngine) {
			dialog.radioButton(DialogNewGame.SECOND_PLAYER_ENGINE_RADIO).click();
		}
		clickOk(dialog);
	}

	void move(Square from, Square to) {
		square(from).dragToSquare(square(to));
	}

	void startNewGameAndPlayOneMove() {
		click(newGameAction);
		clickOk(window.dialog());
		move(Square.D2, Square.D4);
	}

	void startNewGamePlayOneMoveAndStop() {
		startNewGameAndPlayOneMove();
		click(stopGameAction);
	}

	void assertEnabled(Locator... locators) {
		for (Locator locator : locators) {
			assertEnabled(locator);
		}
	}

	void assertDisabled(Locator... locators) {
		for (Locator locator : locators) {
			assertDisabled(locator);
		}
	}

	void assertEnabled(Locator locator) {
		locator.locate(window).requireEnabled();
	}

	void assertDisabled(Locator locator) {
		locator.locate(window).requireDisabled();
	}

	void approveSaveDialog() {
		JFileChooserFixture fileChooserFixture = window.fileChooser();
		fileChooserFixture.fileNameTextBox().setText(Helper.TEST_SAVE_GAME_FILE.getAbsolutePath());
		fileChooserFixture.approve();
	}

	interface ConditionTest {
		boolean isSatisfied();
	}

	static Condition conditionOf(String description, ConditionTest test) {
		return new Condition(description) {
			@Override
			public boolean test() {
				return execute( test::isSatisfied );
			}
		};
	}

	static void waitUntil(Condition condition) {
		Pause.pause(condition, timeout(1, TimeUnit.SECONDS));
	}

	static void waitUntilMoveMade(GameMoveListener gameListener) {
		waitUntil(conditionOf("Waiting until UI is notified about the move", gameListener::isMoveMade));
	}

	static UCIClient connectClientAndWaitUntilReady(int port) {
		Connectable.Params params = Connectable.Params.of("localhost", port);
		UCIClient client = UCIClient.DEFAULT_FACTORY.create("test", params, SwingUtilities::invokeLater);

		// use an engine player as a client to connect to the server and handle UCI protocol initialization
		// until the player is ready
		// this saves us the trouble to manage the starting phase manually
		EnginePlayer engine = new EnginePlayer("Test", PlayerColor.WHITE, client);

		// needs to be executed in EDT because it shows a dialog. This could never happen through the GUI, however
		// it is the easiest way to test that the server watches the game
		execute(engine::start);

		waitUntil(conditionOf("Waiting engine player to become ready", engine::isReady));

		return client;
	}

	static class GameMoveListener extends GameModel.GameModelAdapter {
		private boolean moveMade = false;

		@Override
		public void onMoveMade(String previousFenPosition, Move lastMove) {
			moveMade = true;
		}

		void reset() {
			moveMade = false;
		}

		boolean isMoveMade() {
			return moveMade;
		}
	}

	private static class DialogTitleMatcher extends GenericTypeMatcher<Dialog> {

		private final String expectedTitle;

		private DialogTitleMatcher(String expectedTitle) {
			super(Dialog.class);
			this.expectedTitle = expectedTitle;
		}

		@Override
		protected boolean isMatching(Dialog dialog) {
			return expectedTitle.equals(dialog.getTitle()) && dialog.isVisible();
		}
	}

	private static class ButtonTextMatcher extends GenericTypeMatcher<JButton> {

		private final String expectedText;

		private ButtonTextMatcher(String expectedText) {
			super(JButton.class);
			this.expectedText = expectedText;
		}

		@Override
		protected boolean isMatching(JButton jButton) {
			return expectedText.equals(jButton.getText());
		}
	}
}
