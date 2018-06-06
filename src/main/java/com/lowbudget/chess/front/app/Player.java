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

package com.lowbudget.chess.front.app;

import com.lowbudget.chess.front.app.UIApplication.DialogCancelListener;
import com.lowbudget.chess.front.app.UIApplication.RegistrationDialogSuccessListener;
import com.lowbudget.chess.model.PlayerColor;
import com.lowbudget.chess.model.Move;
import com.lowbudget.chess.model.uci.engine.UCIEngineConfig;
import com.lowbudget.chess.model.uci.engine.options.UCIOption;

import java.util.Collections;
import java.util.List;

/**
 * <p>Describes a player of a chess game</p>
 * <p>This abstraction allows us to interact with human and engine players alike.</p>
 */
public interface Player {

	/**
	 * <p>Listener that can be registered with a player and can be notified for various events during the player's
	 * life-cycle.</p>
	 * <p>This interface is created mostly for engine players, so the application can respond to player related events
	 * (i.e. like displaying a registration dialog). For human players either most events do not apply or
	 * they have trivial implementation (e.g. a human player is "ready" as soon as it is started)</p>
	 */
	interface PlayerListener {
		/**
		 * Event fired when a player has been started.
		 * Staring a player does not necessary mean that the player is ready to make moves since engine players
		 * must perform some initialization first
		 * @param player the player that has been started
		 */
		void onPlayerStarted(Player player);

		/**
		 * Event fired when a player has been stopped
		 * @param player the player that has been stopped
		 */
		void onPlayerStopped(Player player);

		/**
		 * Event fired when a player has performed any initialization required upon startup and is now ready
		 * @param player the player that is ready
		 */
		void onPlayerReady(Player player);

		/**
		 * Event fired when a player has decided the best move to play
		 * @param move the move decided by the player
		 */
		void onPlayerMove(Move move);

		/**
		 * Event fired when an unexpected error has occurred
		 * @param exception the error occurred
		 */
		void onPlayerError(Exception exception);

		/**
		 * Event fired when a connection to an (engine) player has been lost
		 */
		void onPlayerUnexpectedDisconnection();

		/**
		 * Event fired when the engine player is copy-protected and cannot be used
		 * @param name the player's name
		 * @param color the color of the player
		 */
		void onPlayerCopyProtectionError(String name, PlayerColor color);

		/**
		 * Event fired when the player's engine requires registration. In such a case it is assumed that the application
		 * will prompt the user for the registration details
		 * @param player the player representing the engine that needs registration
		 * @param successListener a listener that is notified if the registration is successful
		 * @param cancelListener a listener that is notified if the registration is cancelled
		 */
		void onPlayerRegistrationRequired(Player player, RegistrationDialogSuccessListener successListener, DialogCancelListener cancelListener);

		/**
		 * Event fired when the engine sends information about its current thinking lines
		 * @param info the engine information available as a simple string
		 */
		void onEngineInfoAvailable(String info);

		/**
		 * Event fired when an engine player has performed the initialization required by the UCI protocol and has received
		 * the engine's uci options available (i.e. the engine's config) just before the {@link #onPlayerReady(Player)}
		 * event. This event gives the application a chance to set some values for the engine options other than the default ones.
		 * @param engineConfig the engine's config received by the uci engine
		 * @return a list of options modified by the listener (if any)
		 */
		List<UCIOption> onEngineConfigAvailable(UCIEngineConfig engineConfig);
	}

	/**
	 * Empty implementation that allows to override only the methods of interest
	 */
	class PlayerListenerAdapter implements PlayerListener {
		@Override
		public void onPlayerStopped(Player player) {}

		@Override
		public void onPlayerStarted(Player player) {}

		@Override
		public void onPlayerReady(Player player) {}

		@Override
		public void onPlayerMove(Move move) {}

		@Override
		public void onPlayerError(Exception exception) {}

		@Override
		public void onPlayerUnexpectedDisconnection() {}

		@Override
		public void onPlayerCopyProtectionError(String name, PlayerColor color) {}

		@Override
		public void onPlayerRegistrationRequired(Player player, RegistrationDialogSuccessListener successListener, DialogCancelListener cancelListener) {}

		@Override
		public void onEngineInfoAvailable(String info) {}

		@Override
		public List<UCIOption> onEngineConfigAvailable(UCIEngineConfig engineConfig) {
			return Collections.emptyList();
		}
	}

	/**
	 * Returns the player's name
	 * @return the player's name. This value should be used wherever the player's name is displayed
	 */
	String getName();

	/**
	 * Sets the player's name
	 * @param name the new name to set
	 */
	void setName(String name);

	/**
	 * Returns the player's color
	 * @return the {@link PlayerColor} of this player (i.e. if it plays white or black pieces)
	 */
	PlayerColor getColor();

	/**
	 * Checks if this player is an engine
	 * @return {@code true} if this player is an engine player, {@code false} otherwise
	 */
	boolean isEngine();

	/**
	 * Chesks if this player is a human
	 * @return {@code true} if this player is a human player, {@code false} otherwise
	 */
	boolean isHuman();

	/**
	 * Adds a listener to this player to be notified for player events
	 * @param playerListener the listener to add
	 */
	void addPlayerListener(PlayerListener playerListener);

	/**
	 * Removes the player listener specified
	 * @param playerListener the listener to remove
	 */
	void removePlayerListener(PlayerListener playerListener);

	/**
	 * <p>Asks the player to start thinking for its next move.</p>
	 * <p>This operation applies only to engine players since they usually need to send some information in order
	 * for the engine to play</p>
	 * @param lastFenPosition the FEN position of the board <strong>before</strong> the {@code move} was played
	 * @param lastMove the last move made (if any). The current board position is defined by the {@code lastFenPosition}
	 *                 after playing the {@code lastMove}
	 * @param whiteRemainingTime the white player's remaining time (in millis)
	 * @param blackRemainingTime the black player's remaining time (in millis)
	 */
	void startThinking(String lastFenPosition, Move lastMove, long whiteRemainingTime, long blackRemainingTime);

	/**
	 * <p>Informs the player about its opponent.</p>
	 * <p>This is useful for engine players that can send the "UCI_Opponent" option with the opponent's information</p>
	 * @param opponent the opponent player
	 */
	void handShake(Player opponent);

	/**
	 * <p>Checks if a player is ready so the time controls can be started.</p>
	 * <p>Human players will probably return {@code true} immediately here as long as they have been started, engine
	 * players however will probably need some time until they are connected with an engine.</p>
	 * @return {@code true} if this player is ready, {@code false} otherwise
	 */
	boolean isReady();

	/**
	 * Performs any initialization required for the player to be able to start thinking for moves (i.e. this is the
	 * place where engine players will initiate the connection to the engine)
	 */
	void start();

	/**
	 * Performs any cleanup required after the game is stopped (i.e. this is the place where engine players will
	 * disconnect from the engine)
	 */
	void stop();

	/**
	 * Lets the player know that the game is paused. For engine players this means that they should not process any
	 * more messages coming from the engine until the player is resumed
	 */
	void pause();

	/**
	 * Resumes a paused player. Engine players that have received any messages (i.e. an engine move) after pause,
	 * should go on and process them now
	 */
	void resume();
}
