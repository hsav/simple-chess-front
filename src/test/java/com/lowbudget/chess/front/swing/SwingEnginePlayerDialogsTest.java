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

import com.lowbudget.chess.front.app.controller.player.EnginePlayer;
import com.lowbudget.chess.model.PlayerColor;
import com.lowbudget.chess.model.uci.client.UCIClient;
import com.lowbudget.chess.model.uci.connect.Connectable;
import com.lowbudget.chess.front.swing.ComponentNames.DialogRegistration;
import com.lowbudget.chess.front.swing.SwingUIApplication.SwingWaitDialogHolder;
import org.assertj.swing.fixture.DialogFixture;
import org.junit.Before;
import org.junit.Test;

import javax.swing.*;

import java.util.ArrayList;
import java.util.List;

import static com.lowbudget.chess.front.app.UIApplication.*;
import static com.lowbudget.chess.front.swing.SwingHelper.*;
import static org.assertj.swing.edt.GuiActionRunner.execute;
import static org.mockito.Mockito.*;

public class SwingEnginePlayerDialogsTest extends BaseSwingTest {

	private UCIClient mockClient;

	private final List<WaitDialogHolder> waitDialogHolders = new ArrayList<>();

	@Before
	public void setUp() {
		super.setUp();
		mockClient = mock(UCIClient.class);
		when(mockClient.getParams()).thenReturn(Connectable.Params.NONE);
		application.setUCIClientFactory((sessionName, params, threadContext) -> mockClient );

		// we wrap the same factory used by the application to return our spied objects
		WaitDialogHolderFactory waitDialogHolderFactory = SwingWaitDialogHolder::new;
		application.setWaitDialogHolderFactory( () -> {
			WaitDialogHolder holder = spy(waitDialogHolderFactory.createWaitDialogHolder());
			waitDialogHolders.add(holder);
			return holder;
		});

		application.setEnginesAlwaysRegisterLater(false);
	}

	@Test
	public void testClosesWaitDialogWhenReadyWithOneEnginePlayer() {
		startNewGameWithEnginePlayers(true, false);
		sendUciOk(PlayerColor.WHITE);
		sendReadyOk(PlayerColor.WHITE);

		// need to execute in EDT to be sure that the player has processed the ready ok message
		execute ( () -> waitDialogHolders.forEach( holder -> verify(holder).close() ) );
	}

	@Test
	public void testClosesWaitDialogWhenReadyWithTwoEnginePlayers() {
		startNewGameWithEnginePlayers(true, true);
		sendUciOk(PlayerColor.WHITE);
		sendReadyOk(PlayerColor.WHITE);

		sendUciOk(PlayerColor.BLACK);
		sendReadyOk(PlayerColor.BLACK);

		// need to execute in EDT to be sure that the players have processed the ready ok message
		execute( () -> waitDialogHolders.forEach(holder -> verify(holder).close() ));
	}

	@Test
	public void testCopyProtectionErrorWithOneEnginePlayer() {
		startNewGameWithEnginePlayers(true, false);

		sendUciOk(PlayerColor.WHITE);
		sendCopyProtectionError(PlayerColor.WHITE);
		assertAndDismissOptionPane();
	}

	@Test
	public void testCopyProtectionErrorWithTwoEnginePlayers() {
		startNewGameWithEnginePlayers(true, true);

		sendUciOk(PlayerColor.WHITE);
		sendUciOk(PlayerColor.BLACK);

		sendCopyProtectionError(PlayerColor.WHITE);
		sendCopyProtectionError(PlayerColor.BLACK);

		// a copy protection error stops the game so only the white's copy protection error will be displayed
		assertAndDismissDialogByTitle( COPY_PROTECTION_WHITE_DIALOG_TITLE, BUTTON_OK );
	}

	@Test
	public void testRegistrationErrorDisplayDialog() {
		startNewGameWithEnginePlayers(true, false);

		sendUciOk(PlayerColor.WHITE);
		sendRegistrationError(PlayerColor.WHITE);

		assertAndDismissDialogByTitle( REGISTRATION_WHITE_DIALOG_TITLE, BUTTON_LATER );
	}

	@Test
	public void testRegistrationErrorIsHandledAutomaticallyWhenOptionIsSet() {
		// we need this option to be true for this test case
		application.setEnginesAlwaysRegisterLater(true);

		startNewGameWithEnginePlayers(true, false);

		sendUciOk(PlayerColor.WHITE);
		sendRegistrationError(PlayerColor.WHITE);

		// note: we execute this in EDT too, otherwise a happens-before relationship is not guaranteed
		// i.e. sendRegistrationError() might not have been executed in EDT when verification is invoked and this test
		// can fail at random times (this behaviour has been observed but might not be easily reproducible)
		execute( () -> {
			// the "register later" message should have been sent automatically without prompting the user
			verify(mockClient).sendRegisterLater();
		});
	}

	@Test
	public void testRegistrationBadCredentialsRedisplayDialog() {
		startNewGameWithEnginePlayers(true, false);

		String name = "test-name";
		String code = "test-code";

		sendUciOk(PlayerColor.WHITE);
		sendRegistrationError(PlayerColor.WHITE);

		DialogFixture dialog = assertDialogExists(REGISTRATION_WHITE_DIALOG_TITLE);
		dialog.textBox(DialogRegistration.NAME).setText(name);
		dialog.textBox(DialogRegistration.CODE).setText(code);
		clickOk(dialog);

		// note: should execute in EDT, see explanation in testRegistrationErrorIsHandledAutomaticallyWhenOptionIsSet()
		execute( () -> {
			// player should send the registration information to client
			verify(mockClient).sendRegisterInformation(name, code);
		});

		// re-send the registration error message as a response to bad credentials were given - the dialog should be re-displayed
		sendRegistrationError(PlayerColor.WHITE);
		assertAndDismissDialogByTitle( REGISTRATION_WHITE_DIALOG_TITLE, BUTTON_LATER );
	}

	@Test
	public void testRegistrationErrorWithTwoEnginePlayers() {
		startNewGameWithEnginePlayers(true, true);

		sendUciOk(PlayerColor.WHITE);
		sendUciOk(PlayerColor.BLACK);
		sendRegistrationError(PlayerColor.WHITE);
		sendRegistrationError(PlayerColor.BLACK);

		// we assert in the opposite order of display. Black dialog was displayed last and needs to be dismissed first
		assertAndDismissDialogByTitle( REGISTRATION_BLACK_DIALOG_TITLE, BUTTON_LATER );
		assertAndDismissDialogByTitle( REGISTRATION_WHITE_DIALOG_TITLE, BUTTON_LATER );
	}

	@Test
	public void testDisplaysDialogOnPause() {
		startNewGame();
		click(pauseGameAction);
		assertAndDismissOptionPane();
	}

	@Test
	public void testDisplaysDialogOnError() {
		startNewGameWithEnginePlayers(true, false);
		sendUciOk(PlayerColor.WHITE);
		sendError(PlayerColor.WHITE, new Exception("Test exception"));
		assertAndDismissOptionPane();
	}

	@Test
	public void testDisplaysDialogOnUnexpectedDisconnection() {
		startNewGameWithEnginePlayers(true, false);
		sendUciOk(PlayerColor.WHITE);
		sendDisconnected(PlayerColor.WHITE);
		assertAndDismissOptionPane();
	}

	private EnginePlayer engineFor(PlayerColor color) {
		return execute( () -> (EnginePlayer) application.getGameModel().getPlayerByColor(color) );
	}

	private void sendCopyProtectionError(PlayerColor color) {
		sendPlayerMessage( engineFor(color)::onCopyProtectionError );
	}
	private void sendRegistrationError(PlayerColor color) {
		sendPlayerMessage( engineFor(color)::onRegistrationError );
	}
	private void sendUciOk(PlayerColor color) {
		sendPlayerMessage( engineFor(color)::onUciOk );
	}
	@SuppressWarnings("SameParameterValue")
	private void sendError(PlayerColor color, Exception e) {
		sendPlayerMessage( () -> engineFor(color).onError(e) );
	}
	@SuppressWarnings("SameParameterValue")
	private void sendDisconnected(PlayerColor color) {
		sendPlayerMessage( () -> engineFor(color).onDisconnect(false) );
	}

	private void sendReadyOk(PlayerColor color) {
		sendPlayerMessage( engineFor(color)::onReadyOk );
	}

	private void sendPlayerMessage(Runnable action) {
		// Using GuiActionRunner.execute() here wont work. execute() uses locking to serialize execution to force
		// the test thread to wait until the gui action is completed. However the action here will display
		// a modal option pane thus the execute() (and as a result the test thread) will wait until the dialog is
		// closed not allowing us to assert the dialog presence.
		// What we need instead, is to just schedule the execution in EDT and then wait to assert the dialog
		SwingUtilities.invokeLater(action);
	}
}
