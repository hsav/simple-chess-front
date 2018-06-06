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
 * <p>Provides an abstraction of the communication between a GUI and a UCI compatible chess engine.</p>
 * <p>The session creates the necessary context required when establishing the communication while it also manages
 * any state required during that communication.</p>
 * <p>The communication is double-sided where the GUI and the engine can both send and receive messages
 * to/from each other asynchronously, either as a reply to a previously sent message or as a new message where one
 * side requires information from the other.</p>
 * <p>To send messages to the engine a client (i.e. the GUI) can use {@link #sendMessage(String)} while receiving
 * messages from the engine is achieved through a {@link SessionListener} that is assigned to the session.</p>
 */
public interface Session {

	/**
	 * <p>Sets the session listener that will be notified for messages sent from the engine to the GUI.</p>
	 * <p>The listener must be set before the session is started.</p>
	 * @param listener the new listener to be assigned.
	 * @throws IllegalStateException if an attempt is made to set a listener after the session has been started.
	 */
	void setSessionListener(SessionListener listener);

	/**
	 * <p>Starts a new session with the UCI engine.</p>
	 * <p>This method returns immediately and does not wait for the connection to be established.</p>
	 * @throws IllegalStateException if the session is not in the initial state or if no listener has been set.
	 */
	void start();

	/**
	 * <p>Stops the session with the UCI engine.</p>
	 * <p>If the engine is not started or is already stopped, this should be a no-op.</p>
	 */
	void stop();

	/**
	 * <p>Sends a message to the UCI engine.</p>
	 * <p>If the engine is not started or is already stopped this should be a no-op.</p>
	 * @param message the message to send to the UCI engine.
	 */
	void sendMessage(String message);

}
