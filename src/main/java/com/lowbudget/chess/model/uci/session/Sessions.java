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

import com.lowbudget.chess.model.uci.connect.Connectable;
import com.lowbudget.chess.model.uci.connect.Connectable.Params;
import com.lowbudget.chess.model.uci.session.impl.*;
import com.lowbudget.chess.model.uci.session.impl.connection.*;

import java.net.Socket;

/**
 * <p>Provides static factories to create various {@link Session} types without exposing any implementation specific
 * classes.</p>
 */
public final class Sessions {
	// do not allow instantiation
	private Sessions() {}

	/**
	 * Creates a new server session
	 * @param name the name of the new session
	 * @param engineParams the connection {@link Params} for the engine that will answer client requests
	 * @param clientSocket the socket of the connected client
	 * @return a new {@link Session}
	 */
	public static Session newServerSession(String name, Params engineParams, Socket clientSocket) {
		Connectable engine = Connectable.of(engineParams);
		return new SessionImpl(name, engine, sessionContext -> new ServerConnectionTask(sessionContext, engine, Connectable.of(clientSocket)));
	}

	/**
	 * Creates a new session using the client parameters specified
	 * @param name the name of the new session
	 * @param params the connection {@link Params} of the engine to connect to
	 * @return a new {@link Session}
	 */
	public static Session newSession(String name, Params params) {
		Connectable engine = Connectable.of(params);
		return new SessionImpl(name, engine, sessionContext -> new ClientConnectionTask(sessionContext, engine));
	}
}
