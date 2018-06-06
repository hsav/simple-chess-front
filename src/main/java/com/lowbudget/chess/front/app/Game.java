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

import com.lowbudget.chess.model.PlayerColor;
import com.lowbudget.chess.model.notation.pgn.PgnNotation.GameResult;

/**
 * Describes a chess game along with its life-cycle events
 */
public interface Game {

	/**
	 * Sets the game configuration option that controls if engine players should always send the "register later"
	 * message automatically and don't request registration information by the user
	 * @param value {@code true} if engine players should automatically postpone registration for later, {@code false} otherwise
	 */
	void setEnginesShouldAlwaysRegisterLater(boolean value);

	/**
	 * Starts the game.
	 * The game should perform any initialization required including starting both of the players
	 */
	void start();

	/**
	 * Stops the game.
	 * The players should stop thinking and in any other way interact with the game, however the game is still available
	 * so the user can browse between the moves played
	 */
	void stop();

	/**
	 * Pauses the game and lets the players know that they should stop thinking until the game is resumed
	 */
	void pause();

	/**
	 * Resumes a paused game and lets the players know that they should start thinking again
	 */
	void resume();

	/**
	 * Closes a game and cleans-up all related resources. The game should not be accessed again after this operation
	 */
	void close();

	/**
	 * @return the white player's remaining time in millis
	 */
	long getWhiteTime();

	/**
	 * @return the black player's remaining time in millis
	 */
	long getBlackTime();

	/**
	 * @return the result of the game
	 */
	GameResult getGameResult();

	/**
	 * @return the white player's name
	 */
	String getWhiteName();

	/**
	 * @return the black player's name
	 */
	String getBlackName();

	/**
	 * @return {@code true} if the game has been stopped, {@code false} otherwise
	 */
	boolean isStopped();

	/**
	 * @return the player whose turn it is to play
	 */
	Player getCurrentPlayer();

	/**
	 * Returns the game's player with the specified color
	 * @param color the color of the player
	 * @return the player with the specified color
	 */
	Player getPlayerByColor(PlayerColor color);

}
