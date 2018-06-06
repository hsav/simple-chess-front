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
import com.lowbudget.chess.model.uci.session.impl.message.MessageReaderTask;
import com.lowbudget.chess.model.uci.session.impl.message.MessageWriterTask;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * <p>A task that establishes a client connection to a chess engine.</p>
 * <p>The engine is represented by a {@link Connectable} that can provide input/output streams to communicate with it.</p>
 * <p>This class will invoke two new tasks, a {@link MessageReaderTask} and a {@link MessageWriterTask} that read the
 * engine's {@link java.io.InputStream} and {@link java.io.OutputStream} respectively).</p>
 *
 * @see MessageReaderTask
 * @see MessageWriterTask
 */
public class ClientConnectionTask extends AbstractConnectionTask {

	/**
	 * Create a new connection task that will establish a connection to an engine using the details specified.
	 * @param sessionContext the session context to use
	 * @param engineConnectable the engine's connection details
	 */
	public ClientConnectionTask(SessionContext sessionContext, Connectable engineConnectable) {
		super(sessionContext.getSessionName() + "-engine", sessionContext, engineConnectable);
	}

	@Override
	protected List<SessionTask> createChildTasks(Connectable engineConnectable) throws IOException {
		return Arrays.asList(
			new MessageReaderTask(engineConnectable.getInputStream(), sessionContext, sessionContext::onEngineMessage),
			new MessageWriterTask(engineConnectable.getOutputStream(), sessionContext, this::dequeueEngineMessage)
		);
	}
}
