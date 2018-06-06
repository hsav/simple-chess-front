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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.function.Consumer;

/**
 * <p>The reader task that processes commands coming from the engine.</p>
 * <p>This implementation uses an {@link InputStream} to read commands from and a {@link Consumer} to
 * send commands to.</p>
 */
public class MessageReaderTask extends AbstractMessageTask {
	private static final Logger log = LoggerFactory.getLogger(MessageReaderTask.class);

	/**
	 * Reader for reading the engine's output
	 */
	private final BufferedReader reader;

	/**
	 * A consumer that can process further any message read.
	 */
	private final Consumer<String> consumer;

	/**
	 * Creates a new reader task for the stream specified.
	 * @param inputStream the {@link InputStream} that represents the engine's output
	 * @param sessionContext the current session context to use
	 * @param consumer the consumer of the messages that were read from the input stream
	 */
	public MessageReaderTask(InputStream inputStream, SessionContext sessionContext, Consumer<String> consumer) {
		this(sessionContext.getSessionName() + "-reader", inputStream, sessionContext, consumer);
	}

	/**
	 * Creates a new reader task for the stream specified.
	 * @param threadName the thread name that should be used while this task is running
	 * @param inputStream the {@link InputStream} that represents the engine's output
	 * @param sessionContext the current session context to use
	 * @param consumer the consumer of the messages that were read from the input stream
	 */
	public MessageReaderTask(String threadName, InputStream inputStream, SessionContext sessionContext, Consumer<String> consumer) {
		super(threadName, sessionContext);
		this.reader = new BufferedReader(new InputStreamReader(inputStream));
		this.consumer = consumer;
	}

	@Override
	protected boolean processMessage() throws IOException {
		String command = reader.readLine();
		if (log.isDebugEnabled()) {
			log.debug("Read command: '{}'", command);
		}

		if (command == null) {
			return false;
		}
		this.consumer.accept(command);
		return true;
	}
}
