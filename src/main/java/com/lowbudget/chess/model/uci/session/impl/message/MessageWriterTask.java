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

package com.lowbudget.chess.model.uci.session.impl.message;

import com.lowbudget.chess.model.uci.session.impl.SessionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.function.Supplier;

/**
 * <p>The writer task that sends commands to the engine.</p>
 * <p>This implementation uses an {@link OutputStream} to write commands to (this output stream is the engine's input).
 * The commands are provided by a {@link Supplier}.</p>
 */
public class MessageWriterTask extends AbstractMessageTask {
	private static final Logger log = LoggerFactory.getLogger(MessageWriterTask.class);

	/**
	 * Writer to write to the engine's input
	 */
	private final PrintWriter writer;

	/**
	 * A producer that provides the messages to send.
	 */
	private final Supplier<String> messageProducer;

	/**
	 * Creates a new writer task for the stream specified.
	 * @param outputStream the {@link OutputStream} that represents the engine's input
	 * @param sessionContext the current session context to use
	 * @param producer the supplier of messages that will be written to the output stream
	 */
	public MessageWriterTask(OutputStream outputStream, SessionContext sessionContext, Supplier<String> producer) {
		this(sessionContext.getSessionName() + "-writer", outputStream, sessionContext, producer);
	}

	/**
	 * Creates a new writer task for the stream specified.
	 * @param threadName the thread name that should be used while this task is running
	 * @param outputStream the {@link OutputStream} that represents the engine's input
	 * @param sessionContext the current session context to use
	 * @param producer the supplier of messages that will be written to the output stream
	 */
	public MessageWriterTask(String threadName, OutputStream outputStream, SessionContext sessionContext, Supplier<String> producer) {
		super(threadName, sessionContext);
		this.writer = new PrintWriter(outputStream, true);
		this.messageProducer = producer;
	}

	@Override
	protected boolean isWaitingTask() {
		return true;
	}

	@SuppressWarnings("SpellCheckingInspection")
	@Override
	protected boolean processMessage() {
		String command = messageProducer.get();
		if (command == null) {
			return false;
		}
		if (log.isDebugEnabled()) {
			log.debug("Sending command: {}", command);
		}
//		if ("isready".equals(command)) {
//			throw new IllegalStateException("Test exception");
//		}
		writer.println(command);
		return true;
	}
}
