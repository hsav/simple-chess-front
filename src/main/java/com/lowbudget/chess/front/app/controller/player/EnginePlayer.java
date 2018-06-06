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

import com.lowbudget.chess.front.app.Player;
import com.lowbudget.common.ListenerList;
import com.lowbudget.chess.model.PlayerColor;
import com.lowbudget.chess.model.Move;
import com.lowbudget.chess.model.uci.client.BaseClientDelegate;
import com.lowbudget.chess.model.uci.client.UCIClient;
import com.lowbudget.chess.front.app.Engine;
import com.lowbudget.chess.model.uci.engine.options.*;
import com.lowbudget.chess.model.uci.protocol.message.GoMessage;
import com.lowbudget.chess.model.uci.protocol.message.InfoMessage;
import com.lowbudget.chess.model.uci.protocol.message.UCIMessage.Token.OpponentTitle;
import com.lowbudget.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * <p>Engine player implementation of the {@link Player} interface.</p>
 * <p>This implementation uses a {@link UCIClient} to play moves decided by a chess engine and interact
 * with it as needed to simulate an automated {@link Player}.</p>
 */
public class EnginePlayer extends BaseClientDelegate implements Player, Engine {
	private static final Logger log = LoggerFactory.getLogger(EnginePlayer.class);

	/** Manages player listeners registered with this player */
	private final ListenerList<PlayerListener> playerListeners;

	/** The color of this player */
	private final PlayerColor color;

	/** The name of this player */
	private final String name;

	/** Denotes if the engine interacting with this player requires registration */
	private boolean registrationRequired = false;

	/**
	 * Stores the best move received by the engine while the player was in the paused state.
	 * Due to the asynchronous communication with the engine it is possible to receive a move even though we have been
	 * paused e.g. in a scenario where: a) we send a request to the engine to play b) the user pauses the game c)
	 * the engine responds with the best move found. Note however that it is not possible to receive more than one such
	 * moves.
	 * If such a move is received then the player will fire the appropriate event to inform the listeners about the
	 * move, after the player is resumed.
	 */
	private Move bestMoveWhilePaused = Move.NONE;

	/** Denotes if the engine should postpone registration for later automatically and avoid displaying a registration dialog */
	private boolean alwaysRegisterLater;

	public EnginePlayer(String name, PlayerColor color, UCIClient client) {
		super(client);
		this.color = color;
		this.name = name;
		this.playerListeners = new ListenerList<>();
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(String name) {
		throw new UnsupportedOperationException();
	}

	@Override
	public PlayerColor getColor() {
		return color;
	}

	@Override
	public boolean isHuman() {
		return false;
	}

	@Override
	public boolean isEngine() {
		return true;
	}

	@Override
	public boolean isReady() {
		return !registrationRequired && getState() == State.PLAYING;
	}

	@Override
	public void addPlayerListener(PlayerListener playerListener) {
		this.playerListeners.add(Objects.requireNonNull(playerListener, "Player listener cannot be null"));
	}

	@Override
	public void removePlayerListener(PlayerListener playerListener) {
		this.playerListeners.remove(playerListener);
	}

	@Override
	public void start() {
		if (log.isDebugEnabled()) {
			log.debug("Starting player {} ({})", this.name, this.color);
		}
		super.start();

		if (log.isDebugEnabled()) {
			log.debug("Player {} ({}) started. Opening dialog and waiting...", this.name, this.color);
		}
		playerListeners.notifyAllListeners(listener -> listener.onPlayerStarted(this));
	}

	@Override
	public void stop() {
		if (!getState().isStopState()) {
			if (log.isDebugEnabled()) {
				log.debug("Stopping player {} ({})", this.name, this.color);
			}
			playerListeners.notifyAllListeners(listener -> listener.onPlayerStopped(this));
			super.stop();
			if (log.isDebugEnabled()) {
				log.debug("Player stopped");
			}
		}
	}

	@Override
	public void pause() {
		if (getState() != State.PLAYING) {
			throw new IllegalStateException("Player cannot be paused if not playing");
		}
		setState(State.PAUSED);
	}

	@Override
	public void resume() {
		if (getState() != State.PAUSED) {
			throw new IllegalStateException("Player cannot be resumed because it is not paused. Current state: " + getState());
		}
		if (bestMoveWhilePaused.isValid()) {
			if (log.isDebugEnabled()) {
				log.debug("Notifying about saved move {}", bestMoveWhilePaused);
			}
			playerListeners.notifyAllListeners(listener -> listener.onPlayerMove(bestMoveWhilePaused));
			bestMoveWhilePaused = Move.NONE;
		}
		setState(State.PLAYING);
	}

	@Override
	public void startThinking(String lastFenPosition, Move lastMove, long whiteRemainingTime, long blackRemainingTime) {

		if (log.isDebugEnabled()) {
			log.debug("{} ({}) - Thinking, after move: {}", getName(), getColor(), (lastMove.isValid() ? lastMove : "<none - playing first move>"));
		}

		List<Move> moves = lastMove.isValid() ? Collections.singletonList(lastMove) : Collections.emptyList();

		client.sendFenPosition(lastFenPosition, moves);
		client.sendGo(GoMessage.builder()
				.whiteTime(whiteRemainingTime)
				.blackTime(blackRemainingTime)
		);
	}

	@Override
	public void handShake(Player opponent) {
		client.sendOpponentName(OpponentTitle.NONE, -1, opponent.isEngine(), opponent.getName());
	}

	@Override
	public void onBestMove(Move bestMove, Move ponderMove) {
		if (getState() == State.PLAYING) {
			if (log.isDebugEnabled()) {
				log.debug("{} - best move: {}{}{}, ponder: {}", getName(), bestMove.getFrom(), bestMove.getTo(), (bestMove.getPromotionType() != null ? bestMove.getPromotionType() : ""), ponderMove);
			}
			playerListeners.notifyAllListeners(listener -> listener.onPlayerMove(bestMove));

			// if we decide to support ponder move this is the place to send information to engine to start pondering
		} else if (getState() == State.PAUSED){
			saveBestMove(bestMove);
		} else {
			log.warn("Received best move {} while the player state is {}", bestMove, getState());
		}
	}

	@Override
	public void onError(Exception exception) {
		playerListeners.notifyAllListeners(listener -> listener.onPlayerError(exception));
	}

	@Override
	public void onDisconnect(boolean requested) {
		if (!requested) {
			playerListeners.notifyAllListeners(PlayerListener::onPlayerUnexpectedDisconnection);
		}
	}

	@Override
	public void onCopyProtectionError() {
		if (getState() == State.WAITING_READY_OK) {
			playerListeners.notifyAllListeners(listener -> listener.onPlayerCopyProtectionError(this.name, this.color));
		}
	}

	@Override
	public void onRegistrationOk() {
		State state = getState();
		if ((state == State.WAITING_READY_OK  || state == State.PLAYING) && registrationRequired) {
			registrationRequired = false;
			fireOnPlayerReadyIfReady();
		}
	}

	@Override
	public void onRegistrationError() {
		if (getState() == State.WAITING_READY_OK) {
			if (this.alwaysRegisterLater) {
				registerLater();
			} else {
				// as long as the registration dialog is displayed registration is required
				registrationRequired = true;
				playerListeners.notifyAllListeners(listener -> listener.onPlayerRegistrationRequired(this, this::register, this::registerLater));
			}
		}
	}

	@Override
	public void onReadyOk() {
		if (getState() == State.WAITING_READY_OK) {
			setState(State.PLAYING);
			log.debug("{}: Engine is ready - {}", getName(), engineConfig);

			fireOnPlayerReadyIfReady();

			client.sendNewUciGame();
		}
	}

	@Override
	public void onUciOk() {
		if (getState() == State.WAITING_UCI_OK) {

			playerListeners.notifyAllListeners(listener -> {
				List<UCIOption> modifiedOptions = listener.onEngineConfigAvailable(engineConfig);

				if (!modifiedOptions.isEmpty()) {
					// if any modifications occurred, send messages to the uci engine to be aware of the new values
					client.sendSetOptions(modifiedOptions);
				}
			});

			client.sendIsReady();
			setState(State.WAITING_READY_OK);
		}
	}

	@Override
	public void onInfoAvailable(InfoMessage message) {
		if (getState() == State.PLAYING && message.getNodes() > 0) {
			StringBuilder sb = new StringBuilder();
			sb.append("Nodes: ").append(message.getNodes());

			if (message.getScore() != null) {
				sb.append(", Score: ").append(message.getScore());
			}
			sb
					.append(", Depth: ").append(message.getDepth())
					.append(", Nps: ").append(message.getNodesPerSecond());

			if (message.getPv() != null) {
				sb.append(", PV: ").append(Strings.join(message.getPv(), ", "));
			}

			playerListeners.notifyAllListeners(listener -> listener.onEngineInfoAvailable(sb.toString()));
		}
	}

	@Override
	public void sendModifiedOptions(List<UCIOption> modifiedOptions) {
		client.sendSetOptions(modifiedOptions);
	}

	@Override
	public void setAlwaysRegisterLater(boolean enginesAlwaysRegisterLater) {
		this.alwaysRegisterLater = enginesAlwaysRegisterLater;
	}

	private void register(String name, String code) {
		client.sendRegisterInformation(name, code);
	}

	private void saveBestMove(Move move) {
		if (log.isDebugEnabled()) {
			log.debug("Move {} received when paused, will play it after resume", move);
		}

		if (bestMoveWhilePaused.isValid()) {
			throw new IllegalStateException("There is already a saved best move: " + bestMoveWhilePaused);
		}

		bestMoveWhilePaused = move;
	}

	private void registerLater() {
		client.sendRegisterLater();
		registrationRequired = false;
		fireOnPlayerReadyIfReady();
	}

	private void fireOnPlayerReadyIfReady() {
		if (isReady()) {
			playerListeners.notifyAllListeners(listener -> listener.onPlayerReady(this));
		}
	}
}
