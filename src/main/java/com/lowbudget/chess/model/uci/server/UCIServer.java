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
import com.lowbudget.chess.model.uci.connect.Connectable.Params;
import com.lowbudget.chess.model.uci.session.Session;
import com.lowbudget.chess.model.uci.session.SessionListener;
import com.lowbudget.util.Exceptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

/**
 * <p>A UCI server that acts as a facade between a UCI chess engine and a connected client.</p>
 * <p>The UCI Engine can be either a local executable file or even a remote chess engine host (i.e in this case the
 * server provides a "tunneling" functionality)</p>
 * <p>This server allows for a single client at a time. It also allows to set an external {@link SessionListener} that
 * will be set to each new {@link Session} created.</p>
 */
public class UCIServer {

	private static final Logger log = LoggerFactory.getLogger(UCIServer.class);

	@FunctionalInterface
	public interface Factory {
		/**
		 * Creates a new server
		 * @param params the {@link Params} for the connection to the chess engine
		 * @param serverPort the port the server is listening to
		 * @return a newly created {@link UCIServer} instance
		 */
		UCIServer create(Params params, int serverPort);
	}

	public interface ServerListener {
		void onServerStarted(int port);
		void onServerStopped();
		void onClientConnected(int port);
		void onClientDisconnected(int port);
	}

	/**
	 * Default factory to create a UCI server
	 */
	public static final Factory DEFAULT_FACTORY = UCIServer::new;

	/**
	 * Factory to create a dummy UCI server used for testing purposes
	 */
	public static final Factory TEST_FACTORY = (params, serverPort) -> new UCIServer(params, serverPort) {
		public void start() {}

		public void stop() {}

		public void setSessionListener(SessionListener sessionListener) {}
	};

	/**
	 * Listening thread of our server
	 */
	private final ListeningThread listeningThread;

	@SuppressWarnings("WeakerAccess")
	public UCIServer(Params engineParams, int serverPort) {
		listeningThread = new ListeningThread(serverPort, engineParams);
	}

	/**
	 * <p>Starts the server listening to the specified port for new incoming connections</p>
	 * @throws IllegalStateException if the server is already running or if no session listener is assigned or if no
	 * server listener is assigned
	 */
	public void start() {
		if (log.isDebugEnabled()) {
			log.debug("Starting UCI server...");
		}
		if (listeningThread.isRunning()) {
			throw new IllegalStateException("Server is already started");
		}

		if (listeningThread.getSessionListener() == null) {
			throw new IllegalStateException("No session listener has been assigned");
		}

		if (listeningThread.getServerListener() == null) {
			throw new IllegalStateException("No server listener has been assigned");
		}

		listeningThread.start();
	}

	/**
	 * Checks the current status of the server
	 * @return {@code true} if the server can accept new connections or {@code false} otherwise. Since this server
	 * accepts only one client at a time, a result of {@code true} means that there is currently no client connected
	 */
	public boolean canAcceptNewConnection() {
		return listeningThread.canAcceptNewConnection();
	}

	/**
	 * <p>Stops the server</p>
	 * <p>If any client is currently connected it will be forcefully disconnected</p>
	 * <p>This method will block until the listening thread has finished running</p>
	 */
	public void stop() {
		listeningThread.shutdown();

		try {
			listeningThread.join();
		} catch (InterruptedException e) {
			// should never happen
			Exceptions.rethrowAsRuntimeException(e);
		}
		if (log.isInfoEnabled()) {
			log.info("UCI server stopped.");
		}
	}

	/**
	 * <p>Sets the session listener that should be notified for sessions' events.</p>
	 * <p>The listener must be set before the server has been started.</p>
	 * <p>Note: the same listener will be assigned to all server's sessions (i.e. to all the clients connected to
	 * this server).</p>
	 * @param sessionListener the listener to set
	 * @throws IllegalStateException if the listening thread is already running
	 */
	public void setSessionListener(SessionListener sessionListener) {
		if (listeningThread.isRunning()) {
			throw new IllegalStateException("Session listener must be set before the server is started");
		}
		this.listeningThread.setSessionListener(sessionListener);
	}

	/**
	 * <p>Sets the server listener that should be notified for server's events.</p>
	 * <p>The listener must be set before the server has been started.</p>
	 * @param serverListener the listener to set
	 * @throws IllegalStateException if the listening thread is already running
	 */
	public void setServerListener(ServerListener serverListener) {
		if (listeningThread.isRunning()) {
			throw new IllegalStateException("Server listener must be set before the server is started");
		}
		this.listeningThread.setServerListener(serverListener);
	}

	@SuppressWarnings("SpellCheckingInspection")
	public static void main(String[] args) {
		//File executable = new File("D:\\temp\\chess\\sc8\\Windows\\stockfish_8_x64.exe");
		File executable = new File("D:\\temp\\chess\\Shredder Classic 5\\EngineClassic5UCIx64.exe");
		UCIServer server = new UCIServer(Connectable.Params.of(executable), 5000);
		server.start();
		log.debug("Main thread finished");
	}

}
