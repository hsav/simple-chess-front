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

package com.lowbudget.chess.front.app.controller;

import com.lowbudget.chess.front.app.UIApplication.WaitDialogHolder;
import com.lowbudget.chess.front.app.UIApplication.UITimer;
import com.lowbudget.chess.model.uci.client.BaseClientDelegate;
import com.lowbudget.chess.model.uci.client.UCIClient;

/**
 * Tests a connection to a chess engine
 */
public class EngineConnectionTester extends BaseClientDelegate {

	/** Timer to be able to time out if the connection is taking too long */
	private final UITimer timer;

	/** Wait dialog that is being displayed during testing */
	private final WaitDialogHolder waitDialogHolder;

	/** Stores the exception occurred (if any) */
	private Exception error;

	/** Denotes if the test was successful */
	private boolean successful = false;

	/** Denotes if the test has timed out */
	private boolean timedOut = false;

	public EngineConnectionTester(UCIClient client, UITimer timer, WaitDialogHolder dialogHolder) {
		super(client);
		this.timer = timer;

		// listen for timer events to be able to know if the connection has timed out
		// the time out interval has been configured externally
		this.timer.addUITimerListener( this::onTimer );
		this.waitDialogHolder = dialogHolder;
	}

	@Override
	public void start() {
		super.start();

		timer.start();
		waitDialogHolder.openDialog(this::stop);
	}

	@Override
	public void stop() {
		timer.stop();
		waitDialogHolder.close();

		super.stop();
	}

	public boolean isSuccessful() {
		return successful;
	}

	public boolean isTimedOut() {
		return timedOut;
	}

	public Exception getError() {
		return error;
	}

	@Override
	public void onError(Exception exception) {
		this.error = exception;
		stop();
	}

	@Override
	public void onDisconnect(boolean requested) {
		if (!requested) {
			this.error = new Exception("Unexpected session termination");
			stop();
		}
	}

	@Override
	public void onUciOk() {
		successful = true;
		stop();
	}

	/**
	 * Conforms to {@link Runnable} so it can be used as a callback with {@link #timer} via a method reference
	 */
	private void onTimer() {
		timedOut = true;
		stop();
	}
}
