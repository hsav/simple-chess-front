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

import com.lowbudget.chess.model.Move;
import com.lowbudget.chess.model.uci.engine.options.*;
import com.lowbudget.chess.model.uci.protocol.message.GoMessage;
import com.lowbudget.chess.model.uci.protocol.message.InfoMessage;

/**
 * <p>Defines a delegate where a {@link UCIClient} propagates the message processing.</p>
 * <p>Essentially the combination of this interface and the {@link UCIClient} transforms a series of text messages
 * received asynchronously, to method calls of this class that handle each message on a separate method.</p>
 * <p>This way we can process well-defined objects instead of text and we do not have to worry
 * about any threading issues which are taken care of by the {@link UCIClient}.</p>
 * <p>An implementation of this interface would use the {@link UCIClient} only to send messages to the engine
 * since the receiving part will be handled by the methods declared here.</p>
 *
 * @see UCIClient
 */
@SuppressWarnings("SpellCheckingInspection")
public interface UCIClientDelegate {

	/**
	 * The engine's name sent via the "id name" message
	 * @param name the name of the engine
	 */
	void onIdName(String name);

	/**
	 * The engine's author sent via the "id author" message
	 * @param author the name of the engine's author
	 */
	void onIdAuthor(String author);

	/**
	 * <p>Error handler for unexpected errors that can occur during communication.</p>
	 * <p>If an error occurs than you should disconnect the client (by calling {@link UCIClient#disconnect()} and you
	 * should not use it anymore, otherwise its behaviour will be undefined.</p>
	 * @param exception the error encountered
	 */
	void onError(Exception exception);

	/**
	 * <p>Event handler for when the client is disconnected.</p>
	 * @param requested {@code true} when disconnection was requested (i.e. by calling {@link UCIClient#disconnect()}),
	 *                  {@code false} when the disconnection was unexpected
	 */
	void onDisconnect(boolean requested);

	/**
	 * <p>The engine sent a "copyprotection error" message declaring that this engine is copy-protected and cannot be
	 * used.</p>
	 * <p>However the UCI protocol mentions that such engines should not quit automatically but they should wait for
	 * the "quit" message which is sent automatically when you call {@link UCIClient#disconnect()}</p>
	 */
	void onCopyProtectionError();

	/**
	 * The engine sent the "registration ok" message indicating that registration was successful
	 */
	void onRegistrationOk();

	/**
	 * <p>The engine sent the "registration error" message indicating that registration is required.</p>
	 * <p>The same message can also be received as a response to incorrect registration details.</p>
	 */
	void onRegistrationError();

	/**
	 * <p>The engine sent the "bestmove" message.</p>
	 * <p>This message is received as a response to {@link UCIClient#sendGo(GoMessage.Builder)} command.</p>
	 * @param bestMove the best move at the current position for the current player
	 * @param ponderMove the "ponder" move the engine would like to ponder on (i.e. the move it thinks it the best
	 *                   opponent's response)
	 */
	void onBestMove(Move bestMove, Move ponderMove);

	/**
	 * <p>The engine sent the "readyok" message indicating that it is ready to receive commands related to a game</p>
	 * <p>This message is received as a response to {@link UCIClient#sendIsReady()} command.</p>
	 */
	void onReadyOk();

	/**
	 * <p>The engine sent the "uciok" message indicating that it has finished sending the uci related information</p>
	 * <p>This message is received as a response to {@link UCIClient#sendUci()} command.</p>
	 */
	void onUciOk();

	/**
	 * <p>The engine sent the "info" message with information about the engine's view of the current position</p>
	 * <p>This message is received multiple times as a response to a previously sent
	 * {@link UCIClient#sendGo(GoMessage.Builder)} command.</p>
	 * @param message the info message containing information about the current line
	 */
	void onInfoAvailable(InfoMessage message);

	/**
	 * <p>The engine sent an "option" message for an option it supports</p>
	 * <p>This message is received as a part of series of messages that are the response to {@link UCIClient#sendUci()} command.</p>
	 * @param option the option supported by the engine
	 */
	void onOption(UCIOption option);


}
