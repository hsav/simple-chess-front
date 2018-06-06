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

import com.lowbudget.chess.model.uci.protocol.message.SimpleMessage;
import com.lowbudget.chess.model.uci.session.Session;
import com.lowbudget.chess.model.uci.session.SessionListener;
import com.lowbudget.util.Streams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.Socket;
import java.util.concurrent.CountDownLatch;

/**
 * Manages the connection (i.e. the {@link Session} between a single client and the server.
 */
class ClientHandler {

	private static final Logger log = LoggerFactory.getLogger(ClientHandler.class);

	/** The server session for this client */
	private final Session session;

	/** A latch that is notified when the session is disconnected */
	private final CountDownLatch sessionDisconnectLatch = new CountDownLatch(1);

	ClientHandler(Session session, Socket socket, SessionListener sessionListener) {
		this.session = session;

		// wrap the session listener so we can close the client's socket when the session is closed
		this.session.setSessionListener(new SessionListenerWrapper(sessionListener) {
			@Override
			public void onDisconnect(boolean stopRequested) {
				super.onDisconnect(stopRequested);
				if (log.isDebugEnabled()) {
					log.debug("Client disconnected, client socket will be closed");
				}
				Streams.close(socket);

				// fire the latch
				sessionDisconnectLatch.countDown();
			}
		});
	}

	/**
	 * Starts the underlying session
	 */
	void startSession() {
		session.start();
	}

	/**
	 * <p>Causes the current thread to wait until the underlying session is disconnected.</p>
	 * <p>If the session is already stopped this method returns immediately</p>
	 * @throws InterruptedException if interrupted while waiting
	 */
	void waitForCompletion() throws InterruptedException {
		sessionDisconnectLatch.await();
	}

	/**
	 * <p>Stops the underlying session.</p>
	 * <p>If the underlying session is already stopped this method has no effect</p>
	 */
	void stopSession() {
		session.sendMessage(SimpleMessage.QUIT.toCommand());
		session.stop();
	}

	private static class SessionListenerWrapper implements SessionListener {
		private final SessionListener sessionListener;

		SessionListenerWrapper(SessionListener sessionListener) {
			this.sessionListener = sessionListener;
		}

		@Override
		public void onEngineMessage(String message) {
			sessionListener.onEngineMessage(message);
		}

		@Override
		public void onClientMessage(String message) {
			sessionListener.onClientMessage(message);
		}

		@Override
		public void onError(Exception exception) {
			sessionListener.onError(exception);
		}

		@Override
		public void onDisconnect(boolean stopRequested) {
			sessionListener.onDisconnect(stopRequested);
		}
	}
}
