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

package com.lowbudget.chess.model.uci.session.impl.connection;

import com.lowbudget.chess.model.uci.connect.Connectable;
import com.lowbudget.chess.model.uci.session.impl.SessionContext;
import com.lowbudget.chess.model.uci.session.SessionListener;
import com.lowbudget.chess.model.uci.session.impl.SessionTask;
import com.lowbudget.chess.model.uci.session.impl.message.MessageReaderTask;
import com.lowbudget.chess.model.uci.session.impl.message.MessageWriterTask;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * <p>A task that exposes a chess engine as a server.</p>
 * <p>This class listens for connections to a specified port and when a client is connected, it establishes a connection
 * to a chess engine and acts as an intermediate between the engine and the client.</p>
 * <p>Both the engine and end the client are represented by {@link Connectable}s that can provide input/output streams
 * for communication.</p>
 * <p>This task will invoke 4 new tasks: a {@link MessageReaderTask} and a {@link MessageWriterTask} for each side of
 * communication. The reader/writers are wired together so the traffic is re-directed from the engine to the client and
 * vice-versa.</p>
 * <p>Note that this task will handle a single client and it will exit when the communication is completed.</p>
 */
public class ServerConnectionTask extends AbstractConnectionTask {

	/**
	 * The client that is already connected to our server
	 */
	private final Connectable clientConnectable;

	/**
	 * <p>Queue to store messages sent by the engine to the client in a thread safe manner.</p>
	 */
	private final BlockingQueue<String> clientMessages = new LinkedBlockingDeque<>();

	/**
	 * Create a new connection task that will act as a server between the engine specified and the external client.
	 * @param sessionContext the session context to use
	 * @param engineConnectable the engine to connect to
	 * @param clientConnectable the client that is already connected to the UCI server
	 */
	public ServerConnectionTask(SessionContext sessionContext, Connectable engineConnectable, Connectable clientConnectable) {
		super(sessionContext.getSessionName() + "-connection", sessionContext, engineConnectable);
		this.clientConnectable = clientConnectable;
	}

	@Override
	protected List<SessionTask> createChildTasks(Connectable engineConnectable) throws IOException {
		// create one reader and one writer tasks for each side of the communication
		// note how the method references representing the message consumers re-direct traffic from the engine to the client and vice-versa

		return Arrays.asList(
			// what the engine sends to us: onEngineMessage() notifies the session listener and enqueues the message to be sent to the client
			new MessageReaderTask("engine-reader", engineConnectable.getInputStream(), sessionContext, this::onEngineMessage),

			// what we send to the engine: the writer writes the message to the output of the engine as normal
			new MessageWriterTask("engine-writer", engineConnectable.getOutputStream(), sessionContext, this::dequeueEngineMessage),

			// what the client sends to us: onClientMessage() notifies the session listener and enqueues the message to be sent to the engine
			new MessageReaderTask("client-reader", clientConnectable.getInputStream(), sessionContext, this::onClientMessage),

			// what we send to the client: The writer writes the message to the output of the client as normal
			new MessageWriterTask("client-writer", clientConnectable.getOutputStream(), sessionContext, this::dequeueClientMessage)
		);
	}

	/**
	 * <p>Method that acts as a {@link Consumer} for any messages coming from the engine.</p>
	 * <p>This consumer notifies the {@link SessionListener#onEngineMessage(String)} and additionally enqueues the
	 * message to the client's queue, so the message will be sent to the client.</p>
	 * @param message the message sent by the engine and needs to be sent to the client
	 */
	private void onEngineMessage(String message) {
		enqueueMessage(clientMessages, message);
		sessionContext.onEngineMessage(message);
	}

	/**
	 * <p>Method that acts as a {@link Consumer} for any messages coming from the client.</p>
	 * <p>This consumer notifies the {@link SessionListener#onClientMessage(String)} and additionally enqueues the
	 * message to the engine's queue, so the message will be sent to the engine.</p>
	 * @param message the message sent by the client and needs to be sent to the engine
	 */
	private void onClientMessage(String message) {
		enqueueMessage(engineMessages, message);
		sessionContext.onClientMessage(message);
	}

	/**
	 * <p>Dequeue a message from the client's message queue that waits to be delivered to the client.</p>
	 * <p>This method acts as a {@link Supplier} for a writer that sends messages to the client.</p>
	 * <p>This method is thread-safe.</p>
	 * @return the next message to send to the client or {@code null} in case the thread requested this operation is interrupted
	 */
	private String dequeueClientMessage() {
		return dequeueMessage(clientMessages);
	}
}
