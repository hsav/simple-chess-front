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

package com.lowbudget.chess.model.uci.session.impl;

import com.lowbudget.chess.model.uci.session.Session;
import com.lowbudget.chess.model.uci.session.SessionListener;

import java.util.List;

/**
 * <p>Encapsulates the context created by a {@link Session}.</p>
 * <p>The {@link SessionTask}s that interact with the session require all of the objects in this class. To not pollute
 * the various constructor signatures with many arguments we use this object to pass session related information.</p>
 * <p>Note that this object does not provide any functionality itself. It merely delegates all calls to the appropriate
 * component, so any thread-safety issues should be handled by those components.</p>
 *
 * <p>This object is immutable.</p>
 */
public class SessionContext implements SessionExceptionHandler, SessionMonitor {

	/**
	 * The current session's name. This is used for debugging purposes
	 */
	private final String sessionName;

	/**
	 * Exception handler used by the {@link Session}. All tasks should propagate to the handler any exceptions they encounter.
	 */
	private final SessionExceptionHandler exceptionHandler;

	/**
	 * The {@link SessionMonitor} to use to invoke new tasks
	 */
	private final SessionMonitor sessionMonitor;

	/**
	 * The assigned session's listener that is notified for engine and client incoming messages
	 */
	private final SessionListener sessionListener;

	/**
	 * Creates a new session context
	 * @param sessionName the session's name
	 * @param exceptionHandler the {@link SessionExceptionHandler} that should be used by all tasks in case of an error
	 * @param sessionListener the {@link SessionListener} that should be notified for client/engine incoming messages
	 * @param sessionMonitor the {@link SessionMonitor} that should be used to invoke any new threads and should be
	 *                       notified for changes in tasks' status.
	 */
	SessionContext(String sessionName, SessionExceptionHandler exceptionHandler, SessionListener sessionListener, SessionMonitor sessionMonitor) {
		this.sessionName = sessionName;
		this.exceptionHandler = exceptionHandler;
		this.sessionMonitor = sessionMonitor;
		this.sessionListener = sessionListener;
	}

	public String getSessionName() {
		return sessionName;
	}

	@Override
	public void handleException(Exception exception, boolean running) {
		exceptionHandler.handleException(exception, running);
	}

	public void onEngineMessage(String message) {
		sessionListener.onEngineMessage(message);
	}

	public void onClientMessage(String message) {
		sessionListener.onClientMessage(message);
	}

	public void executeAndWait(List<SessionTask> tasks) throws InterruptedException {
		sessionMonitor.executeAndWait(tasks);
	}

	@Override
	public void onTaskStarted(Runnable task) {
		sessionMonitor.onTaskStarted(task);
	}

	@Override
	public void onTaskFinished(SessionTask task) {
		sessionMonitor.onTaskFinished(task);
	}

	@Override
	public void onTaskStopped(SessionTask task) {
		sessionMonitor.onTaskStopped(task);
	}
}
