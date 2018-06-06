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

import com.lowbudget.chess.front.app.Constants;
import com.lowbudget.chess.front.app.Player;
import com.lowbudget.common.ListenerList;
import com.lowbudget.chess.model.PlayerColor;
import com.lowbudget.chess.model.Move;

import java.util.Objects;

/**
 * A base implementation for a {@link Player} that stores the player's name and color and provide the infrastructure to
 * add and remove {@link com.lowbudget.chess.front.app.Player.PlayerListener}s.
 */
public abstract class AbstractPlayer implements Player {

	private String name;
	private final PlayerColor color;

	final ListenerList<PlayerListener> playerListeners;

	AbstractPlayer(PlayerColor color) {
		this(Constants.UNKNOWN_NAME, color);
	}

	AbstractPlayer(String name, PlayerColor color) {
		this.name = Objects.requireNonNull(name, "Player's name is required");
		this.color = Objects.requireNonNull(color, "Player's color is required");
		this.playerListeners = new ListenerList<>();
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public PlayerColor getColor() {
		return this.color;
	}

	@Override
	public void addPlayerListener(PlayerListener playerListener) {
		this.playerListeners.add(playerListener);
	}

	@Override
	public void removePlayerListener(PlayerListener playerListener) {
		playerListeners.remove(playerListener);
	}

	@Override
	public boolean isEngine() {
		return false;
	}

	@Override
	public boolean isHuman() {
		return false;
	}

	@Override
	public boolean isReady() {
		return true;
	}

	@Override
	public void startThinking(String lastFenPosition, Move lastMove, long whiteRemainingTime, long blackRemainingTime) {}

	@Override
	public void handShake(Player opponent) {}

	@Override
	public void start() {}

	@Override
	public void stop() {}

	@Override
	public void pause() {}

	@Override
	public void resume() {}

}
