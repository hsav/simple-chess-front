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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.*;
import java.net.Socket;
import java.util.Objects;

/**
 * <p>Represents a connection point that we can connect to.</p>
 * <p>This abstraction allows us to connect to an external {@link Process} or a remote {@link Socket} using
 * a unified approach.</p>
 * <p>A connectable needs to be first initialized by calling {@link #connect()}. It can then provide an
 * {@link InputStream} and an {@link OutputStream} for its standard input/output that are used during communication
 * (it is assumed that the streams can be read/written by different threads).</p>
 * <p>Since a connectable is also a {@link Closeable}, we should {@link #close()} it when we are done with it.</p>
 *
 * @see Process
 * @see Socket
 */
public interface Connectable extends Closeable {

	/**
	 * <p>Creates a {@link Connectable} from the parameters specified</p>
	 * @param params the parameters to use to obtain the connectable
	 * @return the newly created {@link Connectable}
	 */
	static Connectable of(Params params) {
		Connectable connectable;

		if (params instanceof LocalConnectionParams) {
			LocalConnectionParams localParams = (LocalConnectionParams) params;
			// a local process with the executable specified
			connectable = new ConnectableProcess(localParams.executable);
		} else if (params instanceof RemoteConnectionParams) {
			RemoteConnectionParams remoteParams = (RemoteConnectionParams) params;
			// a remote connection to the host and port specified
			connectable = new ConnectableSocket(remoteParams.host, remoteParams.port);
		} else {
			throw new UnsupportedOperationException("Cannot create a client from params of class: " + params.getClass().getName());
		}
		return connectable;
	}

	/**
	 * Static factory to create a connectable from an already connected socket
	 * @param connectedSocket the {@link Socket} created externally
	 * @return the newly created connectable
	 */
	static Connectable of(Socket connectedSocket) {
		return new ConnectableSocket(connectedSocket);
	}

	/**
	 * <p>Initiates the connection</p>
	 * <p>Any initialization required to be able to provide the input/output streams, should be done here</p>
	 * @throws IOException if an IO error occurs
	 */
	void connect() throws IOException;

	/**
	 * Returns the connectable's input that is used to read data sent to us
	 * @return the connectable's {@link InputStream}
	 * @throws IOException if an IO error occurs
	 */
	InputStream getInputStream() throws IOException;

	/**
	 * Returns the connectable's output that is used to write data to be sent to the connection point
	 * @return the connectable's {@link OutputStream}
	 * @throws IOException if an IO error occurs
	 */
	OutputStream getOutputStream() throws IOException;

	/**
	 * Marker interface that only provide static factories to create the different types of params
	 */
	interface Params {

		Params NONE = new Params() {}; // sentinel value, useful only in testing

		/**
		 * Static factory to create params for a connectable external process invoked by a local executable file
		 * @param executable the {@link File} to run
		 * @return the newly created connectable
		 */
		static Params of(File executable) {
			return new LocalConnectionParams(executable);
		}

		/**
		 * Static factory to create the params for a connectable remote host
		 * @param host the remote host
		 * @param port the remote port
		 * @return the newly created connectable
		 */
		static Params of(String host, int port) {
			return new RemoteConnectionParams(host, port);
		}
	}

	/**
	 * Client parameters required to create a client from a local executable file
	 */
	@XmlRootElement(name = "local-client")
	@XmlAccessorType(XmlAccessType.FIELD)
	class LocalConnectionParams implements Params {
		@XmlAttribute
		private final File executable;

		// for JAXB
		@SuppressWarnings("unused")
		private LocalConnectionParams() {
			executable = null;
		}

		private LocalConnectionParams(File executable) {
			Objects.requireNonNull(executable);
			if (executable.isDirectory() || !executable.exists()) {
				throw new IllegalArgumentException("Invalid executable: " + executable.getAbsolutePath());
			}
			this.executable = executable;
		}

		public File getExecutable() {
			return executable;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			LocalConnectionParams that = (LocalConnectionParams) o;
			return Objects.equals(executable, that.executable);
		}

		@Override
		public int hashCode() {
			return Objects.hash(executable);
		}

		@Override
		public String toString() {
			return '[' + executable.getName() + ']';
		}
	}

	/**
	 * Client parameters required to create a client to a remote host and port
	 */
	@XmlRootElement(name = "remote-client")
	@XmlAccessorType(XmlAccessType.FIELD)
	class RemoteConnectionParams implements Params {
		@XmlAttribute
		private final String host;

		@XmlAttribute
		private final int port;

		// for JAXB
		@SuppressWarnings("unused")
		private RemoteConnectionParams() {
			host = null;
			port = 0;
		}

		private RemoteConnectionParams(String host, int port) {
			this.host = Objects.requireNonNull(host);
			this.port = port;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			RemoteConnectionParams that = (RemoteConnectionParams) o;
			return port == that.port &&
					Objects.equals(host, that.host);
		}

		@Override
		public int hashCode() {
			return Objects.hash(host, port);
		}

		@Override
		public String toString() {
			return '[' + host + ':' + port + ']';
		}
	}
}
