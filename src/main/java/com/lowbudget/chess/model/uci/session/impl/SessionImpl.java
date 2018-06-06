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

import com.lowbudget.chess.model.uci.connect.Connectable;
import com.lowbudget.chess.model.uci.session.*;
import com.lowbudget.util.Streams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/**
 * <p>A UCI session implementation that handles a connection to a UCI chess engine.</p>
 * <p>In order to achieve that the session works closely with a {@link SessionTask} which is provided by the
 * specified {@link SessionTaskFactory}.</p>
 * <p>This class has the following responsibilities:</p>
 * <ul>
 *     <li>Manages connection life-cycle i.e. connecting and disconnecting.</li>
 *     <li>Provides global exception handling for all the tasks involved.</li>
 *     <li>Sends {@code String} commands to the engine.</li>
 *     <li>Provides a {@link SessionMonitor} service for executing tasks in new threads, that helps to
 *     keep track of the thread lifecycle and takes care of shutting them down.</li>
 * </ul>
 * <p>The class utilises a {@link SessionContext} to pass any necessary objects to the tasks
 * that execute code in this class i.e. see {@link #handleException(Exception, boolean)} that is passed
 * as the default exception handler.</p>
 * <p>This object is thread-safe. All of its public methods can be called by any thread without any external synchronization.</p>
 * <p>Note however that the notifications to the assigned {@link SessionListener} occur in the tasks' threads. In case
 * it is required to respond to events inside the UI thread, external synchronization is required (i.e. for a swing
 * application you would wrap your listener code with a call to {@link javax.swing.SwingUtilities#invokeLater(Runnable)}).</p>
 *
 * @see SessionListener
 * @see Session
 * @see SessionContext
 * @see SessionTask
 */
public class SessionImpl implements Session {

	private static final Logger log = LoggerFactory.getLogger(SessionImpl.class);

	/**
	 * A factory responsible for creating the starting {@link SessionTask} that will initiate the connection
	 */
	@FunctionalInterface
	public interface SessionTaskFactory {

		/**
		 * Creates the starting task to connect to a chess engine
		 * @param sessionContext the session context to use
		 * @return the newly created connection task
		 */
		SessionTask createConnectionTask(SessionContext sessionContext);
	}

	private enum State {
		NOT_STARTED, STARTED, CLOSED
	}

	/**
	 * Guards the internal state of this object (used to avoid locking on this object's monitor)
	 */
	private final Object stateLock = new Object();

	/**
	 * The task factory to use to create the starting {@link SessionTask}
	 */
	private final SessionTaskFactory taskFactory;

	/**
	 * The session's name. This is used purely for debugging purposes
	 */
	private final String sessionName;

	/** A connectable representing the chess engine */
	private final Connectable engineConnectable;

	/**
	 * The session monitor that keeps track of all the sub-tasks created by the starting task and manages their execution
	 */
	private SessionMonitorImpl sessionMonitor;

	/**
	 * <p>The session listener to notify for received messages. Currently a single listener is supported for simplicity's shake.</p>
	 */
	private SessionListener sessionListener;

	/**
	 * The starting session task used to establish the connection to the engine.
	 */
	private SessionTask engineTask;

	/**
	 * State variable with the current status of the session.
	 */
	private State state = State.NOT_STARTED;

	/**
	 * Creates a new session with the session name and the connection factory specified
	 * @param sessionName the name of this session
	 * @param engineConnectable a {@link Connectable} representing the engine
	 * @param taskFactory the factory to use to create the starting {@link SessionTask}
	 * @throws NullPointerException if any of the arguments is {@code null}
	 */
	public SessionImpl(String sessionName, Connectable engineConnectable, SessionTaskFactory taskFactory) {
		this.taskFactory = Objects.requireNonNull(taskFactory, "Connection factory cannot be null");
		this.sessionName = Objects.requireNonNull(sessionName, "Session name cannot be null");
		this.engineConnectable = Objects.requireNonNull(engineConnectable, "Engine connectable cannot be null");
	}

	@Override
	public void setSessionListener(SessionListener listener) {
		synchronized (stateLock) {
			if (state == State.NOT_STARTED) {
				this.sessionListener = listener;
			} else {
				throw new IllegalStateException("Listener must be set before the session is started");
			}
		}
	}

	@Override
	public void start() {
		synchronized (stateLock) {
			startSession();
		}
	}

//	/**
//	 * Waits until all tasks submitted in the executor are finished.
//	 * @implNote Current implementation counts on the fact that the session will be auto-stopped. However this is the
//	 * case only if at least one thread exits abnormally (i.e. due to an exception). If all threads exit gracefully
//	 * {@link #closeSession()} will never have been called when this method exits. Moreover, if there exists a
//	 * scenario where all tasks are stopped but not all of them are finished, then this method <strong>will block forever</strong>.
//	 * Currently we only use this method to test the {@link ServerConnectionTask} with a stand-alone server which exits
//	 * abnormally thus calling {@link #closeSession()} however it worth keeping it in mind. If such a scenario becomes
//	 * possible in the future then {@link SessionMonitorImpl} should be modified to release the latch when all
//	 * threads are stopped not when they are finished (and possibly call {@link #stop()} on the session}
//	 * @throws InterruptedException if the current thread is interrupted (should never happen)
//	 */
//	@SuppressWarnings("unused")
//	public void waitForTerminationOfAllTasks() throws InterruptedException {
//		//start();
//		sessionMonitor.awaitTaskTermination();
//	}

	@Override
	public void stop() {
		closeSessionAndNotifyListener(true);
	}

	@Override
	public void sendMessage(String message) {
		synchronized (stateLock) {
			if (state == State.STARTED) {
				engineTask.sendEngineMessage(message);
			}
		}
	}

	/**
	 * <p>Starts the session and initializes any threads involved.</p>
	 * <p>Guarded by {@link #stateLock}.</p>
	 */
	private void startSession() {
		if (state != State.NOT_STARTED) {
			throw new IllegalStateException("The session is not in the initial state: " + state);
		}
		if (sessionListener == null) {
			throw new IllegalStateException("No session listener has been set!");
		}

		if (log.isDebugEnabled()) {
			log.debug("Starting session '{}' ...", sessionName);
		}

		state = State.STARTED;

		this.sessionMonitor = new SessionMonitorImpl(sessionName, this::onAutomaticStopDetected);

		// create the new session context. The default context provides implicit access to methods of this class via method references
		SessionContext sessionContext = new SessionContext(sessionName,
				this::handleException,
				sessionListener,
				sessionMonitor);

		// create the starting task that connects to the engine. The type of the task created is decided by the factory
		engineTask = taskFactory.createConnectionTask(sessionContext);

		// connect to the engine. This will create and start any threads involved.
		sessionMonitor.executeStartingTask(engineTask);

		if (log.isDebugEnabled()) {
			log.debug("Session '{}' started", sessionName);
		}
	}

	private void onAutomaticStopDetected() {
		closeSessionAndNotifyListener(false);
	}

	/**
	 * <p>Stops the session and makes sure that the involved threads exit gracefully if possible.</p>
	 * <p><strong>Implementation note </strong> - You should test this method along with {@link #handleException(Exception, boolean)},
	 * at least for the following scenarios in both cases when we play nice and send a quit message and when we don't do that
	 * (resulting in at least 8 scenarios):</p>
	 * <ul>
	 *     <li>When you start the connection from the gui and then quit normally.</li>
	 *     <li>When an unexpected error occurs during connection when reader and writer threads have not been created yet.
	 *     This is the case when a connection is attempted to a non-existent file, unknown host, wrong port etc.</li>
	 *     <li>When the reader thread throws an un-expected exception somewhere.</li>
	 *     <li>When the writer thread throws an un-expected exception somewhere.</li>
	 * </ul>
	 * Guarded by {@link #stateLock}
	 */
	private void closeSession() {
		if (state != State.STARTED) {
			return;
		}

		if (log.isDebugEnabled()) {
			log.debug("closeSession - Closing session: '{}' by thread: {}", sessionName, Thread.currentThread().getName());
		}

		if (log.isDebugEnabled()) {
			log.debug("closeSession - Stopping connection");
		}

		// stop the connection task and its sub-tasks.
		engineTask.stop();

		// close the connectable representing the engine. This can cause the child tasks to throw errors
		// (i.e. like stream closed etc). These exceptions are ignored though when handling the exception
		Streams.close(engineConnectable);

		// stop the monitor. This will start a new thread that will shutdown the monitor's executor
		sessionMonitor.shutdownGracefully();

		state = State.CLOSED;
		if (log.isDebugEnabled()) {
			log.debug("closeSession - Session '{}' closed", sessionName);
		}
	}

	/**
	 * <p>Handles errors that can occur in the various session tasks.</p>
	 * <p>This method is called in complicated scenarios since it handles all exceptions thrown from connection and
	 * task threads.</p>
	 * @param exception the error occurred
	 * @param running if the task calling this method was running or not when the error occurred
	 */
	private void handleException(Exception exception, boolean running) {

		boolean isInterrupted = exception instanceof InterruptedException;

		// unexpected interruption when running (possibly due to session shutdown)
		if (isInterrupted) {
			// be nice and let whatever thread we are running in, to know it has been interrupted
			Thread.currentThread().interrupt();
		}

		boolean notifyListenerForStop;
		boolean notifyListenerForError;

		SessionListener listener;
		synchronized (stateLock) {
			listener = sessionListener;

			if (log.isDebugEnabled()) {
				log.debug("handleException - Thread {}, running: {}, session state: {}, exception: ({}: {})",
						Thread.currentThread().getName(), running, state, exception.getClass().getName(), exception.getMessage());
			}

			boolean isSessionClosed = state != State.STARTED;

			if (isSessionClosed) {
				log.debug("handleException - Ignoring exception, session is not started ({}: {})", exception.getClass().getName(), exception.getMessage());
			} else if (!running) {
				log.debug("handleException - Ignoring exception, task is not running ({}: {})", exception.getClass().getName(), exception.getMessage());
			} else {
				log.error("handleException - Unexpected error occurred", exception);
			}

			// decide if we should notify the session listener which is desirable only in case of an unexpected error
			// if the task in the current thread has been stopped or if the thread was interrupted this is due to the
			// shutdown process so there is no need to notify the listener in that case
			notifyListenerForError = running && !isInterrupted && !isSessionClosed;
			log.debug("handleException - Will notify listener for error? {}", notifyListenerForError);

			notifyListenerForStop = !isSessionClosed;

			// close the session
			closeSession();
		}
		if (notifyListenerForError) {
			listener.onError(exception);
		}
		if (notifyListenerForStop) {
			// if we notify the listener about the error, we consider this as if a stop has been requested
			// otherwise the client will have to react to both a session error and a session unexpected stop
			notifyListenerForDisconnectEvent(listener, notifyListenerForError);
		}
	}

	private void closeSessionAndNotifyListener(boolean stopRequested) {
		boolean notifyListenerForStop;
		SessionListener listener;
		synchronized (stateLock) {
			notifyListenerForStop = (state == State.STARTED);
			listener = sessionListener;
			closeSession();
		}
		if (notifyListenerForStop) {
			notifyListenerForDisconnectEvent(listener, stopRequested);
		}
	}

	private void notifyListenerForDisconnectEvent(SessionListener listener, boolean stopRequested) {
		if (log.isDebugEnabled()) {
			log.debug("Firing session onDisconnect(), stopRequested? {}", stopRequested);
		}
		listener.onDisconnect(stopRequested);
	}
}
