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

package com.lowbudget.chess.front.app.controller.game;

import com.lowbudget.chess.front.app.*;
import com.lowbudget.chess.front.app.UIApplication.UITimer;
import com.lowbudget.chess.front.app.controller.player.HumanPlayer;
import com.lowbudget.chess.front.app.model.BoardModel;
import com.lowbudget.chess.front.app.model.DefaultGameModel;
import com.lowbudget.chess.front.app.model.GameModel;
import com.lowbudget.chess.model.ChessConstants;
import com.lowbudget.chess.model.PlayerColor;
import com.lowbudget.chess.model.Square;
import com.lowbudget.chess.model.Move;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class NormalGameTest {

	private static final String PLAYER_1 = "player1";
	private static final String PLAYER_2 = "player2";
	private static final int PLAYER_1_TIME = 11;
	private static final int PLAYER_2_TIME = 12;

	private Player whitePlayer;
	private Player blackPlayer;
	private GameModel gameModel;
	private NormalGame normalGame;
	private final UITimer timer = new TestTimer(); // use a timer that remembers if it is started

	@Before
	public void setUp() {
		// we spy to the created players
		whitePlayer = spy(new HumanPlayer(PLAYER_1, PlayerColor.WHITE));
		blackPlayer = spy(new HumanPlayer(PLAYER_2, PlayerColor.BLACK));

		gameModel = spy( new DefaultGameModel() );
		gameModel.setFenPosition(ChessConstants.INITIAL_FEN);

		TimeControl timeControl = new TimeControl(timer);
		timeControl.setup(PLAYER_1_TIME, PLAYER_2_TIME, true);

		normalGame = new NormalGame(gameModel, whitePlayer, blackPlayer, timeControl);
		gameModel.setGame(normalGame);
	}

	@Test
	public void testGetCurrentPlayer() {
		assertSame(whitePlayer, normalGame.getCurrentPlayer());
	}

	@Test
	public void testGetPlayerNames() {
		assertEquals(PLAYER_1, normalGame.getWhiteName());
		assertEquals(PLAYER_2, normalGame.getBlackName());
	}

	@Test
	public void testGetPlayerTimingControls() {
		assertEquals(PLAYER_1_TIME, normalGame.getWhiteTime());
		assertEquals(PLAYER_2_TIME, normalGame.getBlackTime());
	}

	@Test
	public void testGetPlayerByColor() {
		assertSame(whitePlayer, normalGame.getPlayerByColor(PlayerColor.WHITE));
		assertSame(blackPlayer, normalGame.getPlayerByColor(PlayerColor.BLACK));
	}

	@Test(expected = IllegalStateException.class)
	public void testStartGameTwiceThrowsException() {
		normalGame.start();
		normalGame.start();
	}

	@Test
	public void testStopGameCanBeSafelyCalledMultipleTimes() {
		normalGame.start();
		normalGame.stop();

		reset(whitePlayer, blackPlayer);

		normalGame.stop();

		verifyZeroInteractions(whitePlayer, blackPlayer);
	}

	@Test
	public void testIsStopped() {
		assertTrue(normalGame.isStopped());
		normalGame.start();
		assertFalse(normalGame.isStopped());
		normalGame.stop();
		assertTrue(normalGame.isStopped());
	}

	@Test(expected = IllegalStateException.class)
	public void testCannotPauseGameWhenNotStarted() {
		normalGame.pause();
	}

	@Test(expected = IllegalStateException.class)
	public void testCannotPauseGameWhenPaused() {
		normalGame.start();
		normalGame.pause();
		normalGame.pause();
	}

	@Test(expected = IllegalStateException.class)
	public void testCannotPauseGameWhenStopped() {
		normalGame.start();
		normalGame.stop();
		normalGame.pause();
	}

	@Test(expected = IllegalStateException.class)
	public void testCannotResumeGameWhenNotStarted() {
		normalGame.resume();
	}

	@Test(expected = IllegalStateException.class)
	public void testCannotResumeGameWhenStopped() {
		normalGame.start();
		normalGame.stop();
		normalGame.resume();
	}
	@Test(expected = IllegalStateException.class)
	public void testCannotResumeGameWhenNotPaused() {
		normalGame.start();
		normalGame.resume();
	}

	@Test
	public void testResumeGameRestartsTimer() {
		normalGame.start();
		normalGame.pause();
		normalGame.resume();
		assertTrue(timer.isRunning());
	}

	@Test
	public void testStartGameStartsTimer() {
		normalGame.start();
		assertTrue(timer.isRunning());
	}

	@Test
	public void testStartGameStartsPlayers() {
		normalGame.start();

		verify(whitePlayer).start();
		verify(blackPlayer).start();
	}

	@Test
	public void testPauseGameStopsTimer() {
		normalGame.start();
		normalGame.pause();
		assertFalse(timer.isRunning());
	}

	@Test
	public void testPauseGamePausesPlayers() {
		normalGame.start();
		normalGame.pause();
		verify(whitePlayer).pause();
		verify(blackPlayer).pause();
	}

	@Test
	public void testMakeMoveNotifiesGameModelWhenStarted() {
		normalGame.start();
		Move move = Move.of(Square.E2, Square.E4);
		normalGame.makeMove(move);
		verify(gameModel).makeMove(move);
	}

	@Test
	public void testMakeMoveIsIgnoredWhenStopped() {
		reset(gameModel);
		normalGame.makeMove(Move.NONE);
		verifyZeroInteractions(gameModel);
	}

	@Test
	public void testListensForPlayerReady() {
		// We expect the following series of events:
		// - Starting the game will cause the players to start.
		// - Because both players are humans, when started, they immediately notify the game that they are ready
		// - Since both players will become ready the clock will be started and the game should initiate the handshake
		//   between the players and also should cause the white player to start thinking
		normalGame.start();

		// if the game does not listen for player events, none of this will ever be called
		verify(whitePlayer).handShake(blackPlayer);
		verify(blackPlayer).handShake(whitePlayer);
		verify(whitePlayer).startThinking(ChessConstants.INITIAL_FEN, Move.NONE, PLAYER_1_TIME, PLAYER_2_TIME);
	}

	@Test
	public void testListensForBoardMoves() {
		normalGame.start();

		// make the first move as white and verify the black player starts thinking
		assertOpponentIsNotifiedForMove(gameModel, whitePlayer, Move.of(Square.E2, Square.E4));

		// now make the second move as black and verify the white player starts thinking
		assertOpponentIsNotifiedForMove(gameModel, blackPlayer, Move.of(Square.E7, Square.E5));
	}

	private void assertOpponentIsNotifiedForMove(BoardModel gameModel, Player currentPlayer, Move move) {
		String currentFen = gameModel.getFenPosition();
		gameModel.makeMove(move);

		Player opponent = (currentPlayer == whitePlayer) ? blackPlayer : whitePlayer;

		// the opponent should be notified to start thinking
		verify(opponent).startThinking(currentFen, move, PLAYER_1_TIME, PLAYER_2_TIME);
	}

	/**
	 * A UITimer implementation to use for this test that remembers if the timer is running or not.
	 * We do not use the implementation from the app.impl package since this introduces a cyclic package dependency
	 */
	private static class TestTimer implements UITimer {

		private boolean running;

		@Override
		public void start() { running = true; }

		@Override
		public void stop() { running = false; }

		@Override
		public boolean isRunning() { return running; }

		@Override
		public void addUITimerListener(UIApplication.UITimerListener listener) {}
	}
}
