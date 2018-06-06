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

import com.lowbudget.chess.model.uci.session.SessionListener;

/**
 * A {@link SessionListener} implementation that wraps another session listener and ensures that all of the calls
 * to the original session listener are executed in the desired thread provided by a {@link ThreadContext}
 */
public class ThreadContextSessionListener implements SessionListener {

	private final SessionListener originalSessionListener;
	private final ThreadContext threadContext;

	public ThreadContextSessionListener(SessionListener originalSessionListener, ThreadContext threadContext) {
		this.originalSessionListener = originalSessionListener;
		this.threadContext = threadContext;
	}

	@Override
	public void onEngineMessage(String message) {
		threadContext.runInThread( () -> originalSessionListener.onEngineMessage(message) );
	}

	@Override
	public void onClientMessage(String message) {
		threadContext.runInThread( () -> originalSessionListener.onClientMessage(message) );
	}

	@Override
	public void onError(Exception exception) {
		threadContext.runInThread( () -> originalSessionListener.onError(exception) );
	}

	@Override
	public void onDisconnect(boolean stopRequested) {
		threadContext.runInThread( () -> originalSessionListener.onDisconnect(stopRequested) );
	}
}
