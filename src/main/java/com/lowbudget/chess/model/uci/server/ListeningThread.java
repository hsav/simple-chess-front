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

package com.lowbudget.chess.model.uci.server;

import com.lowbudget.chess.model.uci.connect.Connectable;
import com.lowbudget.chess.model.uci.session.Session;
import com.lowbudget.chess.model.uci.session.SessionListener;
import com.lowbudget.chess.model.uci.session.Sessions;
import com.lowbudget.util.Streams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Server's listening thread
 */
class ListeningThread extends Thread {
	private static final Logger log = LoggerFactory.getLogger(ListeningThread.class);

	/** Guards the state of this object (the {@link #currentClient} and {@link #running} members) */
	private final Object stateLock = new Object();

	/**
	 * Latest connected client (if any). This member is guarded by {@link #stateLock}
	 */
	private ClientHandler currentClient;

	/**
	 * Flag indicating if the server is running. This member is guarded by {@link #stateLock}
	 */
	private boolean running;

	/**
	 * Session listener that is notified for all sessions' events.
	 * The listener should be set before the thread is started
	 */
	private SessionListener sessionListener;

	/**
	 * Server listener that is notified when the server is started/stopped and when a client is connected/disconnected.
	 * The listener should be set before the thread is started
	 */
	private UCIServer.ServerListener serverListener;

	/** Port where the server is listening to */
	private final int serverPort;

	/** Connection params for the engine that is being exposed as a server */
	private final Connectable.Params engineParams;

	/**
	 * An id that is incremented and assigned to each new session to distinguish between different session names
	 * This is mainly used for debugging purposes, it is not related to the server's functionality
	 */
	private int sessionId;

	/** This server listening socket */
	private ServerSocket serverSocket;

	ListeningThread(int serverPort, Connectable.Params engineParams) {
		this.serverPort = serverPort;
		this.engineParams = engineParams;
	}

	@Override
	public void run() {
		Thread.currentThread().setName("server-listening-thread");

		setRunning(true);

		try {
			serverSocket = new ServerSocket(serverPort);

			serverListener.onServerStarted(serverPort);

			if (log.isInfoEnabled()) {
				log.info("UCI server started. Listening to port: {}", serverPort);
			}

			while (isRunning()) {
				if (log.isDebugEnabled()) {
					log.debug("Waiting for new client...");
				}
				Socket clientSocket = serverSocket.accept();

				// create a new session and block until it is completed
				handleClient(clientSocket);
			}
		} catch (IOException | InterruptedException e) {
			if (!isRunning()) {
				log.debug("Server is already shutdown. Ignoring exception {}:{}", e.getClass().getName(), e.getMessage());
			} else {
				log.error("Server error", e);
			}
		}
		setRunning(false);
		serverListener.onServerStopped();
	}

	SessionListener getSessionListener() {
		return sessionListener;
	}

	void setSessionListener(SessionListener sessionListener) {
		this.sessionListener = sessionListener;
	}

	UCIServer.ServerListener getServerListener() {
		return serverListener;
	}

	void setServerListener(UCIServer.ServerListener serverListener) {
		this.serverListener = serverListener;
	}

	private void setRunning(boolean value) {
		synchronized (stateLock) {
			this.running = value;
		}
	}

	boolean isRunning() {
		synchronized (stateLock) {
			return this.running;
		}
	}

	boolean canAcceptNewConnection() {
		synchronized (stateLock) {
			return currentClient == null;
		}
	}

	void shutdown() {
		synchronized (stateLock) {
			running = false;

			if (currentClient != null) {
				currentClient.stopSession();
			}
			currentClient = null;
			Streams.close(serverSocket);
		}
	}

	private void handleClient(Socket clientSocket) throws InterruptedException {

		ClientHandler handler = null;
		synchronized (stateLock) {
			if (running) {
				if (log.isDebugEnabled()) {
					log.debug("New client connected, creating new session with id {}", sessionId);
				}

				// create a new session for this client
				Session newSession = Sessions.newServerSession("server-" + sessionId, engineParams, clientSocket);

				sessionId++;

				handler = new ClientHandler(newSession, clientSocket, sessionListener);
				currentClient = handler;
				handler.startSession();
			}
		}

		// Thread safety: note that in the highly unlikely case where the server is signaled to be stopped at this point
		// the currentClient class member will become null but only after stopping the handler (and thus its underlying session)
		// In that case we could just be waiting for the session to close or even not wait at all (theoretically, if the
		// session manages to stop fast enough, the method could return immediately)
		if (handler != null) {
			serverListener.onClientConnected(serverPort);
			handler.waitForCompletion();
			serverListener.onClientDisconnected(serverPort);
		}

		synchronized (stateLock) {
			currentClient = null;
		}
	}
}
