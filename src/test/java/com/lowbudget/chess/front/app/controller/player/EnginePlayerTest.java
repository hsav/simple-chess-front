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

package com.lowbudget.chess.front.app.controller.player;

import com.lowbudget.chess.front.app.*;
import com.lowbudget.chess.model.PlayerColor;
import com.lowbudget.chess.model.Square;
import com.lowbudget.chess.model.Move;
import com.lowbudget.chess.model.uci.client.UCIClient;
import com.lowbudget.chess.model.uci.connect.Connectable;
import com.lowbudget.chess.model.uci.engine.UCIEngineConfig;
import com.lowbudget.chess.model.uci.engine.options.StringOption;
import com.lowbudget.chess.model.uci.protocol.message.InfoMessage;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class EnginePlayerTest {

	private static final String TEST_NAME = "test-name";
	private static final String TEST_AUTHOR = "test-author";

	private Player.PlayerListener mockPlayerListener;
	private UCIClient mockClient;

	private EnginePlayer enginePlayer;

	@Before
	public void setup() {
		mockClient = mock(UCIClient.class);
		mockPlayerListener = mock(Player.PlayerListener.class);

		when(mockClient.getParams()).thenReturn(Connectable.Params.NONE);
		enginePlayer = new EnginePlayer("test", PlayerColor.WHITE, mockClient);
		enginePlayer.addPlayerListener(mockPlayerListener);
	}

	@Test
	public void testNotifiesListenerWhenStarted() {
		enginePlayer.start();
		verify(mockPlayerListener).onPlayerStarted(enginePlayer);
	}

	@Test
	public void testNotifiesListenerWhenStopped() {
		enginePlayer.start();
		enginePlayer.stop();

		verify(mockPlayerListener).onPlayerStopped(enginePlayer);
	}

	@Test
	public void testNotifiesListenerWhenReady() {
		enginePlayer.start();

		enginePlayer.onUciOk();
		enginePlayer.onReadyOk();

		verify(mockPlayerListener).onPlayerReady(enginePlayer);
	}

	@Test
	public void testIgnoresAllMessagesBeforeStarted(){
		verifyAllMessagesAreIgnored();
	}

	@Test
	public void testIgnoresAllMessagesAfterStoppedImmediately(){
		enginePlayer.start();
		enginePlayer.stop();

		verifyAllMessagesAreIgnored();
	}

	@Test
	public void testIgnoresAllMessagesAfterStoppedInReadyOkState(){
		proceedToReadyOkState();
		enginePlayer.stop();

		verifyAllMessagesAreIgnored();
	}


	@Test(expected = IllegalStateException.class)
	public void testPlayerCannotBeStartedTwice() {
		enginePlayer.start();
		enginePlayer.start();
	}

	@Test
	public void testPlayerCanSafelyBeStoppedEvenIfNotStarted() {
		enginePlayer.stop();
		verify(mockPlayerListener, never()).onPlayerStopped(enginePlayer);
	}

	@Test
	public void testPlayerCanSafelyBeStoppedTwice() {
		enginePlayer.start();
		enginePlayer.stop();
		enginePlayer.stop();

		// only one invocation should occur
		verify(mockPlayerListener).onPlayerStopped(enginePlayer);
	}

	@Test(expected = IllegalStateException.class)
	public void testCannotBePausedWhenNotStarted() {
		enginePlayer.pause();
	}

	@Test(expected = IllegalStateException.class)
	public void testCannotBePausedWhileWaitingForUciOk() {
		enginePlayer.start();
		enginePlayer.pause();
	}

	@Test(expected = IllegalStateException.class)
	public void testCannotBePausedWhileWaitingForReadyOk() {
		proceedToUciOkState();
		enginePlayer.pause();
	}

	@Test(expected = IllegalStateException.class)
	public void testCannotBePausedWhileStopped() {
		proceedToReadyOkState();
		enginePlayer.stop();
		enginePlayer.pause();
	}

	@Test
	public void testWhilePausedIgnoresAllMessages() {
		proceedToReadyOkState();
		enginePlayer.pause();
		verifyAllMessagesAreIgnored();
	}

	@Test(expected = IllegalStateException.class)
	public void testCannotBeResumedWhenNotStarted() {
		enginePlayer.pause();
	}

	@Test(expected = IllegalStateException.class)
	public void testCannotBeResumedWhileWaitingForUciOk() {
		enginePlayer.start();
		enginePlayer.resume();
	}

	@Test(expected = IllegalStateException.class)
	public void testCannotBeResumedWhileWaitingForReadyOk() {
		proceedToUciOkState();
		enginePlayer.resume();
	}

	@Test(expected = IllegalStateException.class)
	public void testCannotBeResumedWhilePlaying() {
		proceedToReadyOkState();
		enginePlayer.resume();
	}

	@Test(expected = IllegalStateException.class)
	public void testCannotBeResumedWhileStopped() {
		proceedToReadyOkState();
		enginePlayer.stop();
		enginePlayer.resume();
	}

	@Test
	public void testResumeRestoresPlayingState() {
		proceedToReadyOkState();
		enginePlayer.pause();
		enginePlayer.resume();

		Move move = Move.of(Square.E2, Square.E4);
		enginePlayer.onBestMove(move, Move.NONE);

		verify(mockPlayerListener).onPlayerMove(move);
	}

	@Test
	public void testSavesBestMoveWhenPausedAndNotifiesOnResume() {
		proceedToReadyOkState();

		Move bestMove = Move.of(Square.E2, Square.E4);

		enginePlayer.pause();
		// assume a move arrived just after the player is paused
		enginePlayer.onBestMove(bestMove, Move.NONE);

		// resume should have saved the move and publish it after resume
		enginePlayer.resume();
		verify(mockPlayerListener).onPlayerMove(bestMove);
	}

	@Test(expected = IllegalStateException.class)
	public void testCanSavesOnlyOneBestMoveWhenPaused() {
		proceedToReadyOkState();

		Move bestMove = Move.of(Square.E2, Square.E4);

		enginePlayer.pause();

		// assume a move arrived just after the player is paused
		enginePlayer.onBestMove(bestMove, Move.NONE);

		// assume a second move arrived (this should never happen but we test it anyway)
		enginePlayer.onBestMove(bestMove, Move.NONE);
	}


	@Test
	public void testWhileWaitingForUciOkIgnoresUnrelatedMessages(){
		proceedToStartState();

		enginePlayer.onBestMove(Move.NONE, Move.NONE);
		enginePlayer.onReadyOk();
		enginePlayer.onRegistrationOk();
		enginePlayer.onRegistrationError();
		enginePlayer.onCopyProtectionError();
		enginePlayer.onInfoAvailable( testInfoMessage() );

		verifyZeroInteractionsForAllMocks();
	}

	@Test
	public void testWhileWaitingForUciOkAcceptsIdNameMessage(){
		proceedToStartState();

		enginePlayer.onIdName(TEST_NAME);

		UCIEngineConfig engineConfig = enginePlayer.getEngineConfig();
		assertEquals(TEST_NAME, engineConfig.getName());
	}

	@Test
	public void testWhileWaitingForUciOkAcceptsIdAuthorMessage(){
		proceedToStartState();

		enginePlayer.onIdAuthor(TEST_AUTHOR);

		UCIEngineConfig engineConfig = enginePlayer.getEngineConfig();
		assertEquals(TEST_AUTHOR, engineConfig.getAuthor());
	}

	@Test
	public void testWhileWaitingForUciOkAcceptsOptionMessage(){
		proceedToStartState();

		StringOption option = testStringOption();
		enginePlayer.onOption(option);

		UCIEngineConfig engineConfig = enginePlayer.getEngineConfig();
		assertTrue(engineConfig.getOptions().contains(option));
	}

	@Test
	public void testWhileWaitingForReadyOkIgnoresUnrelatedMessages(){

		StringOption option = testStringOption();

		proceedToUciOkState();

		enginePlayer.onIdName(TEST_NAME);
		enginePlayer.onIdAuthor(TEST_AUTHOR);
		enginePlayer.onOption(option);
		enginePlayer.onBestMove(Move.NONE, Move.NONE);
		enginePlayer.onInfoAvailable( testInfoMessage() );

		verifyZeroInteractionsForAllMocks();

		assertEngineNotModified(option);
	}

	@Test
	public void testWhileWaitingForReadyOkAcceptsCopyProtectionErrorMessage(){
		proceedToUciOkState();

		enginePlayer.onCopyProtectionError();
		verify(mockPlayerListener).onPlayerCopyProtectionError(anyString(), any());
	}

	@Test
	public void testWhileWaitingForReadyOkAcceptsRegistrationErrorMessage(){
		proceedToUciOkState();

		enginePlayer.onRegistrationError();
		verify(mockPlayerListener).onPlayerRegistrationRequired(any(), any(), any());
	}

	@Test
	public void testWhilePlayingAcceptsRegistrationOkMessage(){
		proceedToUciOkState();

		enginePlayer.onRegistrationError();

		// we first receive ready ok and then registration ok
		enginePlayer.onReadyOk();

		assertFalse(enginePlayer.isReady());

		enginePlayer.onRegistrationOk();

		assertTrue(enginePlayer.isReady());

		verify(mockPlayerListener).onPlayerReady(enginePlayer);
	}

	@Test
	public void testWhileWaitingForReadyOkAcceptsRegistrationOkMessage(){
		proceedToUciOkState();

		enginePlayer.onRegistrationError();

		// we first receive registration ok and then ready ok
		enginePlayer.onRegistrationOk();

		assertFalse(enginePlayer.isReady());

		enginePlayer.onReadyOk();

		assertTrue(enginePlayer.isReady());

		verify(mockPlayerListener).onPlayerReady(enginePlayer);
	}

	@Test
	public void testWhilePlayingIgnoresUnrelatedMessages(){

		StringOption option = testStringOption();

		proceedToReadyOkState();

		enginePlayer.onUciOk();
		enginePlayer.onReadyOk();
		enginePlayer.onRegistrationOk();
		enginePlayer.onRegistrationError();
		enginePlayer.onCopyProtectionError();
		enginePlayer.onIdName(TEST_NAME);
		enginePlayer.onIdAuthor(TEST_AUTHOR);
		enginePlayer.onOption( option );

		verifyZeroInteractionsForAllMocks();

		assertEngineNotModified(option);
	}

	@Test
	public void testWhilePlayingAcceptsInfoMessage(){
		proceedToReadyOkState();

		enginePlayer.onInfoAvailable( testInfoMessage() );

		verify(mockPlayerListener).onEngineInfoAvailable(anyString());
	}

	@Test
	public void testWhilePlayingAcceptsBestMoveMessage(){
		proceedToReadyOkState();

		enginePlayer.onBestMove(Move.NONE, Move.NONE);

		verify(mockPlayerListener).onPlayerMove(Move.NONE);
	}


	private void assertEngineNotModified(StringOption option) {
		UCIEngineConfig engineConfig = enginePlayer.getEngineConfig();
		assertNull(TEST_NAME, engineConfig.getName());
		assertNull(TEST_AUTHOR, engineConfig.getAuthor());
		assertFalse(engineConfig.getOptions().contains(option));
	}

	private void verifyAllMessagesAreIgnored() {
		StringOption option = testStringOption();
		resetAllMocks();

		enginePlayer.onUciOk();
		enginePlayer.onReadyOk();
		enginePlayer.onRegistrationOk();
		enginePlayer.onRegistrationError();
		enginePlayer.onCopyProtectionError();
		enginePlayer.onIdName(TEST_NAME);
		enginePlayer.onIdAuthor(TEST_AUTHOR);
		enginePlayer.onOption( option );
		enginePlayer.onInfoAvailable( testInfoMessage() );
		enginePlayer.onBestMove(Move.NONE, Move.NONE);

		verifyZeroInteractionsForAllMocks();
		assertEngineNotModified(option);
	}

	private static StringOption testStringOption() {
		return new StringOption("option", "value");
	}

	private static InfoMessage testInfoMessage() {
		return InfoMessage.builder().nodes(1).depth(3).build();
	}

	private void proceedToStartState() {
		enginePlayer.start();
		resetAllMocks();
	}

	private void proceedToUciOkState() {
		enginePlayer.start();
		enginePlayer.onUciOk();
		resetAllMocks();
	}

	private void verifyZeroInteractionsForAllMocks() {
		verifyZeroInteractions(mockPlayerListener, mockClient);
	}

	private void resetAllMocks() {
		reset(mockPlayerListener, mockClient);
	}

	private void proceedToReadyOkState() {
		enginePlayer.start();
		enginePlayer.onUciOk();
		enginePlayer.onReadyOk();
		resetAllMocks();
	}

}
