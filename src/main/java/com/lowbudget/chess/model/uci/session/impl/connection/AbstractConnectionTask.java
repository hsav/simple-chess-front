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
import com.lowbudget.chess.model.uci.session.impl.SessionTask;
import com.lowbudget.chess.model.uci.session.impl.message.MessageWriterTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.function.Supplier;

/**
 * <p>A base task class that initiates the engine connection and provides the necessary methods for descendants to
 * launch more child tasks each in its own thread.</p>
 */
public abstract class AbstractConnectionTask extends SessionTask {

	private static final Logger log = LoggerFactory.getLogger(AbstractConnectionTask.class);

	/**
	 * Represents the engine. It can either be a local process or a remote host or anything else that can provide
	 * input/output streams to communicate with it.
	 */
	private final Connectable engineConnectable;

	/**
	 * <p>Stores messages sent by the client (i.e. the GUI) to the chess engine in a thread safe manner.</p>
	 */
	final BlockingQueue<String> engineMessages = new LinkedBlockingDeque<>();

	/**
	 * Creates a new connection with the thread name and the session context specified
	 * @param threadName the name of the thread that should be used while this task is running (used for debugging purposes)
	 * @param sessionContext the session context to use
	 */
	AbstractConnectionTask(String threadName, SessionContext sessionContext, Connectable engineConnectable) {
		super(threadName, sessionContext);
		this.engineConnectable = engineConnectable;
	}

	@Override
	public void sendEngineMessage(String message) {
		enqueueMessage(engineMessages, message);
	}

	@Override
	protected void doRun() throws IOException, InterruptedException {

		// connect to the engine
		this.engineConnectable.connect();

		// create child tasks to be started
		List<SessionTask> tasks = createChildTasks(engineConnectable);

		if (log.isDebugEnabled()) {
			log.debug("Starting connection's sub-tasks and waiting for completion...");
		}

		// invoke all child tasks and block the current thread until all the tasks are completed.
		sessionContext.executeAndWait(tasks);
	}

	/**
	 * @return a {@link List} with the all the child tasks of this task.
	 * @param engineConnectable a {@link Connectable} representing the engine. It is expected that the connection is
	 *                          already established
	 * @throws IOException if an I/O error occurs
	 */
	protected abstract List<SessionTask> createChildTasks(Connectable engineConnectable) throws IOException;

	/**
	 * <p>Dequeue a message from the engine's message queue that is waiting to be delivered to the engine.</p>
	 * <p>This method acts as a {@link Supplier} for a {@link MessageWriterTask}
	 * that sends messages to the engine.</p>
	 * <p>This method is thread-safe.</p>
	 * @return the message to send to the engine or {@code null} in case the thread requested this operation is interrupted
	 * @see #dequeueMessage(BlockingQueue)
	 */
	String dequeueEngineMessage() {
		return dequeueMessage(engineMessages);
	}

	/**
	 * <p>Enqueues a message to the message queue specified.</p>
	 * <p>This method is thread-safe.</p>
	 * @param queue the {@link BlockingQueue} where the message should be put
	 * @param message the message to enqueue
	 */
	void enqueueMessage(BlockingQueue<String> queue, String message) {
		try {
			queue.put(message);
		} catch (InterruptedException e) {
			sessionContext.handleException(e, running);
		}
	}

	/**
	 * <p>Dequeue a message from the message queue specified.</p>
	 * <p>This method blocks until there is a message available in the queue and it is thread-safe.</p>
	 * @return the next message from the queue or {@code null} in case the thread requested this operation is interrupted
	 */
	String dequeueMessage(BlockingQueue<String> queue) {
		String result = null;
		try {
			//log.debug("Waiting for message...");
			result = queue.take();
		} catch (InterruptedException e) {
			sessionContext.handleException(e, running);
		}
		return result;
	}
}
