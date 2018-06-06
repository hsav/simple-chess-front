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
import com.lowbudget.chess.front.app.Player.PlayerListener;
import com.lowbudget.chess.front.app.model.BoardModel.BoardModelAdapter;
import com.lowbudget.chess.front.app.model.BoardModel.BoardModelListener;
import com.lowbudget.chess.front.app.model.GameModel;
import com.lowbudget.chess.model.PlayerColor;
import com.lowbudget.chess.model.board.GameState;
import com.lowbudget.chess.model.Move;
import com.lowbudget.chess.model.notation.pgn.PgnNotation.GameResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/**
 * <p>Normal game implementation between two players (human or engines).</p>
 * <p>This implementation respects the correct side to move and time controls and enforces all the normal rules of
 * game (i.e. it does not allow moves of the player of the wrong side to move, it will stop the
 * game when the time elapses for one of two players etc)</p>
 */
public class NormalGame implements Game {

	private static final Logger log = LoggerFactory.getLogger(NormalGame.class);

	/** Game states */
	private enum State {
		NOT_STARTED, STARTED, PAUSED, STOPPED
	}

	/*package*/ final TimeControl timeControl;

	/*package*/ final Player whitePlayer;

	/*package*/ final Player blackPlayer;

	/*package*/ final GameModel gameModel;

	/** Listens to board changes to respond to moves and the final game state */
	private final BoardModelListener boardModelListener;

	/** Listens for player events */
	private final PlayerListener playerListener;

	/** Current state of the game */
	private State state = State.NOT_STARTED;

	/** Current game result */
	private GameResult gameResult = GameResult.UNDEFINED;

	private boolean allowReconnect = false;

	public NormalGame(GameModel gameModel, Player whitePlayer, Player blackPlayer, TimeControl timeControl) {
		this.gameModel = Objects.requireNonNull(gameModel, "Game model cannot be null");
		this.whitePlayer = Objects.requireNonNull(whitePlayer, "White player cannot be null");
		this.blackPlayer = Objects.requireNonNull(blackPlayer, "Black player cannot be null");
		this.timeControl = Objects.requireNonNull(timeControl, "Time control cannot be null");

		checkColor(PlayerColor.WHITE, whitePlayer);
		checkColor(PlayerColor.BLACK, blackPlayer);

		this.boardModelListener = new NormalGameBoardModelListener();
		this.playerListener = new NormalGamePlayerListener();

		gameModel.addBoardModelListener(boardModelListener);
		whitePlayer.addPlayerListener(playerListener);
		blackPlayer.addPlayerListener(playerListener);

		timeControl.setListener(new NormalGameTimeControlListener());
	}

	@Override
	public GameResult getGameResult() {
		return gameResult;
	}

	@Override
	public String getWhiteName() {
		return whitePlayer.getName();
	}

	@Override
	public String getBlackName() {
		return blackPlayer.getName();
	}

	@Override
	public Player getPlayerByColor(PlayerColor color) {
		return color.isWhite() ? whitePlayer : blackPlayer;
	}

	@Override
	public void setEnginesShouldAlwaysRegisterLater(boolean value) {
		Player[] players = new Player[]{whitePlayer, blackPlayer};
		for (Player player : players) {
			if (player.isEngine()) {
				Engine engine = (Engine) player;
				engine.setAlwaysRegisterLater(value);
			}
		}
	}

	@Override
	public void start() {
		if (!allowReconnect && (state != State.NOT_STARTED)) {
			throw new IllegalStateException("Cannot start game, state=" + state);
		}
		if (allowReconnect && state != State.NOT_STARTED && state != State.STOPPED) {
			throw new IllegalStateException("Cannot start game, state=" + state);
		}

		if (log.isDebugEnabled()) {
			log.debug("Starting new game: {} - {}", whitePlayer.getName(), blackPlayer.getName());
		}
		state = State.STARTED;

		// Important note: When players are engines the application (that listens for player events too) display
		// dialogs when a player is started
		// It is important that those dialogs are scheduled to run later with a mechanism similar to invokeLater() of
		// SwingUtilities otherwise the expected behaviour will be disrupted.
		whitePlayer.start();
		blackPlayer.start();

		if (log.isDebugEnabled()) {
			log.debug("Game started");
		}
	}

	@Override
	public void stop() {
		if (state == State.STOPPED || state == State.NOT_STARTED) {
			return;
		}
		if (log.isDebugEnabled()) {
			log.debug("Stopping game ...");
		}
		state = State.STOPPED;
		timeControl.stop();
		whitePlayer.stop();
		blackPlayer.stop();
		if (log.isDebugEnabled()) {
			log.debug("Game stopped");
		}
	}

	@Override
	public void pause() {
		if (state != State.STARTED) {
			throw new IllegalStateException("Cannot pause game, state=" + state);
		}
		timeControl.stop();
		whitePlayer.pause();
		blackPlayer.pause();
		state = State.PAUSED;
	}

	@Override
	public void resume() {
		if (state != State.PAUSED) {
			throw new IllegalStateException("Cannot resume game, state=" + state);
		}
		state = State.STARTED;
		whitePlayer.resume();
		blackPlayer.resume();
		timeControl.start();
	}

	@Override
	public void close() {
		stop();
		gameModel.removeBoardModelListener(boardModelListener);
		whitePlayer.removePlayerListener(playerListener);
		blackPlayer.removePlayerListener(playerListener);
	}

	@Override
	public long getWhiteTime() {
		return timeControl.getWhiteRemainingTime();
	}

	@Override
	public long getBlackTime() {
		return timeControl.getBlackRemainingTime();
	}

	@Override
	public boolean isStopped() {
		return state == State.STOPPED || state == State.NOT_STARTED;
	}

	@Override
	public Player getCurrentPlayer() {
		return gameModel.getPlayingColor().isWhite() ? whitePlayer : blackPlayer;
	}

	/*package*/ void makeMove(Move move) {
		if (state == State.STARTED) {
			gameModel.makeMove(move);
		}
	}

	/*package*/ void setAllowReconnect(boolean allowReconnect) {
		this.allowReconnect = allowReconnect;
	}

	private void startClockIfPlayersAreReady() {
		if (isStopped()) {
			return;
		}
		if (!timeControl.isStarted() && whitePlayer.isReady() && blackPlayer.isReady()) {
			whitePlayer.handShake(blackPlayer);
			blackPlayer.handShake(whitePlayer);

			timeControl.start();
			getCurrentPlayer().startThinking(gameModel.getFenPosition(), Move.NONE, getWhiteTime(), getBlackTime());
		}
	}

	private void checkColor(PlayerColor expectedColor, Player player) {
		if (player.getColor() != expectedColor) {
			throw new IllegalArgumentException("Player: " + player.getName() + " expected to be " + expectedColor + " but it is " + player.getColor());
		}
	}

	/**
	 * Listens for events from both players
	 */
	private class NormalGamePlayerListener extends Player.PlayerListenerAdapter {

		@Override
		public void onPlayerReady(Player player) {
			startClockIfPlayersAreReady();
		}

		@Override
		public void onPlayerMove(Move move) {
			makeMove(move);
		}

		@Override
		public void onPlayerError(Exception exception) {
			gameModel.stopGame();
		}

		@Override
		public void onPlayerUnexpectedDisconnection() {
			gameModel.stopGame();
		}

		@Override
		public void onPlayerCopyProtectionError(String name, PlayerColor color) {
			gameModel.stopGame();
		}
	}

	/**
	 * Listens for events of the {@link #gameModel}
	 */
	private class NormalGameBoardModelListener extends BoardModelAdapter {
		@Override
		public void onMoveMade(String previousFenPosition, Move lastMove) {
			// toggle the color of the time control
			timeControl.setCurrentPlayer(gameModel.getPlayingColor().isWhite());

			// after the move the current player will be the opponent of the one that moved so notify the player to
			// start thinking its move
			getCurrentPlayer().startThinking(previousFenPosition, lastMove, getWhiteTime(), getBlackTime());
		}

		@Override
		public void onFinalMoveMade(GameState gameState, Move lastMove) {
			gameModel.stopGame();
			switch (gameState) {
				case CHECKMATE:
					gameResult = lastMove.getPiece().isWhite() ? GameResult.WHITE_WINS : GameResult.BLACK_WINS;
					break;
				case STALEMATE:
				case DRAW_THREEFOLD_REPETITION:
				case DRAW_INSUFFICIENT_MATERIAL:
				case DRAW_FIFTY_MOVES:
					gameResult = GameResult.DRAW;
					break;
				default:
					throw new IllegalStateException("Did not handle switch case for game state: " + gameState);
			}
		}
	}

	/**
	 * Listens for events from {@link #timeControl}
	 */
	private class NormalGameTimeControlListener implements TimeControl.TimeControlListener {
		@Override
		public void onTimeControlChanged(long whiteTime, long blackTime) {
			gameModel.publishOnTimeControlChanged(whiteTime, blackTime);
		}

		@Override
		public void onTimeControlElapsed(boolean isWhite) {
			gameModel.stopGame();
			Player playerLost = isWhite ? whitePlayer : blackPlayer;
			gameModel.publishOnTimeControlElapsed(playerLost.getName());
		}
	}
}
