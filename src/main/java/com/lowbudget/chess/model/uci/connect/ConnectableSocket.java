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

package com.lowbudget.chess.model.uci.connect;

import com.lowbudget.util.Streams;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Objects;

/**
 * <p>A {@link Connectable} implementation that connects to a remote socket.</p>
 * <p>This object is thread-safe</p>
 */
public class ConnectableSocket implements Connectable {

	/**
	 * Guards the state of this object
	 */
	private final Object lock = new Object();

	/**
	 * The remote host to connect to
	 */
	private final String host;

	/**
	 * The remote port to connect to
	 */
	private final int port;

	/**
	 * The socket to the remote host
	 */
	private Socket socket;

	/**
	 * Keeps track if {@link #close()} has been called
	 */
	private boolean closed;

	/**
	 * Creates the connectable using the host and port specified
	 * @param host the remote host
	 * @param port the remote port
	 */
	/*package*/ ConnectableSocket(String host, int port) {
		this.host = Objects.requireNonNull(host);
		this.port = port;
	}

	/**
	 * <p>Creates the connectable using the socket specified.</p>
	 * <p>Use this constructor when the socket has already been created externally (note that in this case
	 * {@link #connect()} is a no-op)
	 * @param socket a {@link Socket} where we are already connected to
	 */
	/*package*/ ConnectableSocket(Socket socket) {
		this.host = null;
		this.port = -1;
		this.socket = socket;
	}

	@Override
	public InputStream getInputStream() throws IOException {
		synchronized (lock) {
			return socket.getInputStream();
		}
	}

	@Override
	public OutputStream getOutputStream() throws IOException {
		synchronized (lock) {
			return socket.getOutputStream();
		}
	}

	@Override
	public void connect() throws IOException {
		synchronized (lock) {
			if (!closed && socket == null) {
				this.socket = new Socket(this.host, this.port);
			}
		}
	}

	@Override
	public void close() {
		synchronized (lock) {
			if (!closed) {
				Streams.close(socket);
			}
			closed = true;
		}
	}

	@Override
	public String toString() {
		return "ConnectableSocket{" +
				(host != null ? "host='" + host + '\'' + ", port=" + port : "<client socket>") +
				", closed=" + closed +
				'}';
	}
}
