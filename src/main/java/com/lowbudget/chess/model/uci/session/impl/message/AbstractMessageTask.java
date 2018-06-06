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
import com.lowbudget.chess.model.uci.session.impl.SessionTask;

import java.io.*;

/**
 * <p>Base task class to be used by both reader and writer threads that read a {@link com.lowbudget.chess.model.uci.connect.Connectable}'s
 * input and output.</p>
 * <p>Because both of those threads process messages, we use the term "Message tasks" for either of them.</p>
 * <p>This class follows the logic that each task "reads" the next command from some abstract source and then "writes"
 * it to some other source for further processing. For example the reader thread will read the next command from
 * an {@link InputStream} and then write (i.e. propagate) the command for further processing while a writer
 * will "read" the next message to be sent possibly from a queue and then write it to an {@link OutputStream}.</p>
 */
public abstract class AbstractMessageTask extends SessionTask {

	/**
	 * Creates a task with the specified thread name and session context.
	 * @param threadName the thread name that used be used while this task is running
	 * @param sessionContext the session context to use.
	 */
	@SuppressWarnings("WeakerAccess")
	protected AbstractMessageTask(String threadName, SessionContext sessionContext) {
		super(threadName, sessionContext);
	}

	@Override
	protected void doRun() throws IOException {
		// run while a) we are not stopped b) we can still process messages (i.e. we haven't encountered a null message)
		// and c) we are not interrupted
		while (running && !Thread.currentThread().isInterrupted()) {
			if (!processMessage()) {
				break;
			}
		}
	}

	/**
	 * Processes the next message
	 * @return {@code true} if the task should continue processing messages or {@code false} otherwise
	 * @throws IOException if an I/O error occurs while processing the message
	 */
	protected abstract boolean processMessage() throws IOException;
}
