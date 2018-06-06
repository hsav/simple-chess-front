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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Objects;

/**
 * <p>A base {@link Runnable} class that can be used for all tasks that are related to a specific {@link Session}.</p>
 * <p>This class is appropriate to be used in combination with an {@link SessionMonitor} and
 * tries to stop gracefully if possible.</p>
 * <p>Each task is provided with a {@link SessionContext} that encapsulates the session it is related with and
 * makes sure that the assigned {@link SessionContext#sessionMonitor} is notified for the task's life-cycle events.</p>
 */
public abstract class SessionTask implements Runnable {

	private static final Logger log = LoggerFactory.getLogger(SessionTask.class);

	/**
	 * Flag that indicates if we are inside the {@link #run()} method.
	 */
	protected volatile boolean running;

	/**
	 * The thread name that this task should use while running. Used for debugging purposes
	 */
	private final String threadName;

	/**
	 * The session context to use
	 */
	protected final SessionContext sessionContext;

	/**
	 * The thread we are currently running in
	 */
	private Thread runningThread;

	/**
	 * Creates a new task with the specified thread name and session context.
	 * @param threadName the thread name that this task should use while running
	 * @param sessionContext the session context to use
	 */
	protected SessionTask(String threadName, SessionContext sessionContext) {
		this.sessionContext = Objects.requireNonNull(sessionContext);
		this.threadName = Objects.requireNonNull(threadName);
	}

	@Override
	public void run() {
		Thread.currentThread().setName(threadName);
		runningThread = Thread.currentThread();
		running = true;

		if (log.isDebugEnabled()) {
			log.debug("Thread '{}' is now running", threadName);
		}

		sessionContext.onTaskStarted(this);

		try {
			// invoke the descendant's task
			doRun();
		} catch(Exception e) {
			sessionContext.handleException(e, running);
		}
		stop();
		log.debug("Thread '{}' finished execution", threadName);
		sessionContext.onTaskFinished(this);
	}

	/**
	 * <p>Marks this task as stopped.</p>
	 * <p>Descendants that perform a loop should check the {@link #running} flag in each iteration, so
	 * they can stop gracefully (note however this is not always possible e.g. when a descendant is blocked waiting
	 * on a stream read operation).</p>
	 */
	public void stop() {
		this.running = false;
		sessionContext.onTaskStopped(this);

		// if this task is a waiting one, it might not stop until we interrupt its thread
		if (isWaitingTask() && runningThread != null && !runningThread.isInterrupted()) {
			log.debug("Task {} is a waiting task. Interrupting its thread", this);
			runningThread.interrupt();
		}
	}

	/**
	 * <p>Denotes if this task is a waiting task, in which case we should interrupt its thread when calling {@link #stop()}
	 * @return {@code true} if the task is a waiting task, {@code false} otherwise.</p>
	 * <p>Descendants are expected to override this method and return the value appropriate for their implementation
	 * of the {@link #doRun()} method</p>
	 */
	protected boolean isWaitingTask() {
		return false;
	}

	/**
	 * <p>Sends a message to the UCI engine.</p>
	 * <p>Since a connection can involve more than one threads, it should be expected that the message will be sent
	 * asynchronously though this is up to the implementation.</p>
	 * <p>This method must be thread-safe.</p>
	 * <p>The connection can choose to ignore the message if it is shutting down or an error has occurred.</p>
	 * @param message the message to send.
	 */
	public void sendEngineMessage(String message) {}

	@Override
	public String toString() {
		return "[" + getClass().getSimpleName() + " (" + threadName + ")]";
	}

	/**
	 * Carries out the actual work of this task
	 * @throws IOException if an error occurs while IO is being executed
	 * @throws InterruptedException if the thread we are running in, is interrupted
	 */
	protected abstract void doRun() throws IOException, InterruptedException;

}
