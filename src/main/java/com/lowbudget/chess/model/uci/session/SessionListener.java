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

package com.lowbudget.chess.model.uci.session;

/**
 * <p>Listens for sent/received messages during the communication with a UCI chess engine via a {@link Session}.</p>
 * <p>All methods in this interface are not thread-safe and it should be assumed that they are being executed in an
 * arbitrary thread, so external synchronization is required.</p>
 *
 * @see Session
 */
public interface SessionListener {

	/**
	 * <p>Called when a message is available from the chess engine.</p>
	 * @param message the message sent by the chess engine.
	 */
	void onEngineMessage(String message);

	/**
	 * <p>Called when a message is available from the client.</p>
	 * <p>If the client is also a session listener, then this method is of no use to it (since any notifications
	 * through this method will involve messages the client itself has sent to the engine).</p>
	 * <p>However this is useful in a server-client scenario where the client is external and we want to
	 * listen to both sides of the communication</p>
	 * @param message the message sent by the client.
	 */
	void onClientMessage(String message);

	/**
	 * <p>Called when an unexpected error has occurred during the connection with the chess engine.</p>
	 * @param exception the exception occurred
	 */
	void onError(Exception exception);

	/**
	 * <p>Called when the session is closed.</p>
	 * @param stopRequested a flag that indicates if the event of closing the session was triggered by the client.
	 *                      This value will be {@code true} if the client has requested the session to stop (i.e. by
	 *                      calling its {@link Session#stop()} method), or {@code false} if the shutdown was caused by
	 *                      an unknown factor (e.g. lost connection to a remote host)
	 */
	void onDisconnect(boolean stopRequested);
}
