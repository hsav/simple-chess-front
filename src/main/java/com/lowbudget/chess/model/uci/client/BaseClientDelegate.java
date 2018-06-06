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

package com.lowbudget.chess.model.uci.client;

import com.lowbudget.chess.model.Move;
import com.lowbudget.chess.model.uci.engine.UCIEngineConfig;
import com.lowbudget.chess.model.uci.engine.options.*;
import com.lowbudget.chess.model.uci.protocol.message.InfoMessage;

import java.util.Objects;

/**
 * <p>A basic implementation of a {@link UCIClientDelegate}.</p>
 * <p>It handles common functionality that any client delegate will probably have to support like initiating the
 * UCI protocol and reading the engine's config (i.e. its name, author and supported options)</p>
 * <p>It also stores an internal state so we can query at which stage of the protocol we are currently at.</p>
 */
public abstract class BaseClientDelegate implements UCIClientDelegate {

	/** The underlying client being used */
	protected final UCIClient client;

	/** The engine's configuration read during the protocol's initialization phase */
	protected final UCIEngineConfig engineConfig;

	/** Available states of the delegate */
	public enum State {
		NOT_STARTED(true),
		WAITING_UCI_OK,
		WAITING_READY_OK,
		PLAYING,
		PAUSED,
		STOPPED(true);

		/** Denotes if this state is a "stopped" state */
		private final boolean stopped;

		State() {
			this(false);
		}
		State(boolean isStop) {
			this.stopped = isStop;
		}

		public boolean isStopState() {
			return stopped;
		}
	}

	private State state = State.NOT_STARTED;

	protected BaseClientDelegate(UCIClient client) {
		this.client = Objects.requireNonNull(client, "Client cannot be null");
		this.engineConfig = new UCIEngineConfig(client.getParams());
	}

	protected State getState() {
		return state;
	}

	protected void setState(State state) {
		this.state = state;
	}

	public UCIEngineConfig getEngineConfig() {
		return engineConfig;
	}

	public void start() {
		if (state != State.NOT_STARTED) {
			throw new IllegalStateException("The client is already started!");
		}
		client.setDelegate(this);
		client.connect();

		// start initialization of the UCI protocol
		client.sendUci();
		state = State.WAITING_UCI_OK;
	}

	public void stop() {
		client.disconnect();
		state = State.STOPPED;
	}

	@Override
	public void onIdName(String name) {
		if (state == State.WAITING_UCI_OK) {
			engineConfig.setName(name);
		}
	}

	@Override
	public void onIdAuthor(String author) {
		if (state == State.WAITING_UCI_OK) {
			engineConfig.setAuthor(author);
		}
	}

	@Override
	public void onOption(UCIOption option) {
		if (state == State.WAITING_UCI_OK) {
			engineConfig.addOption(option);
		}
	}

	@Override
	public void onInfoAvailable(InfoMessage message) {}

	@Override
	public void onCopyProtectionError() {}

	@Override
	public void onRegistrationOk() {}

	@Override
	public void onRegistrationError() {}

	@Override
	public void onBestMove(Move bestMove, Move ponderMove) {}

	@Override
	public void onReadyOk() {
		state = State.PLAYING;
	}

	@Override
	public void onUciOk() {
		state = State.WAITING_READY_OK;
	}
}
