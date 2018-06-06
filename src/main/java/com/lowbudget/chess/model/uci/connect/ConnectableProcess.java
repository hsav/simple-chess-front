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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.TimeUnit;

/**
 * <p>A {@link Connectable} implementation that connects to a local {@link Process}.</p>
 * <p>This object is thread-safe</p>
 */
public class ConnectableProcess implements Connectable {
	private static final Logger log = LoggerFactory.getLogger(ConnectableProcess.class);

	/**
	 * Guards the state of this object
	 */
	private final Object lock = new Object();

	/**
	 * The external process that we are connected to
	 */
	private Process externalProcess;

	/**
	 * The external process' executable file
	 */
	private final File executable;

	/**
	 * Keeps track if {@link #close()} has been called
	 */
	private boolean closed;

	/**
	 * Creates the connectable using the file specified
	 * @param executable the {@link File} that represents the process' executable
	 */
	/*package*/ ConnectableProcess(File executable) {
		this.executable = executable;
	}

	@Override
	public InputStream getInputStream() {
		synchronized (lock) {
			return externalProcess.getInputStream();
		}
	}

	@Override
	public OutputStream getOutputStream() {
		synchronized (lock) {
			return externalProcess.getOutputStream();
		}
	}

	@Override
	public void connect() throws IOException {
		synchronized (lock) {
			if (!closed) {
				this.externalProcess = startProcess(this.executable);
			}
		}
	}

	@Override
	public void close() {
		synchronized (lock) {
			if (!closed) {
				// it is expected that there are different threads that read/write the process' input and output streams
				// we wait a little in case stopping those threads allow the process to exit gracefully
				if (externalProcess != null) {

					boolean exited = false;
					try {
						exited = externalProcess.waitFor(300, TimeUnit.MILLISECONDS);
					} catch (InterruptedException e) {
						// let the thread know that it was interrupted
						// however we will still try to close the process forcefully
						Thread.currentThread().interrupt();
					}
					if (!exited) {
						stopProcess(externalProcess);
					} else {
						if (log.isDebugEnabled()) {
							log.debug("Process exited gracefully with exit code: " + externalProcess.exitValue());
						}
					}
				}
			}
			closed = true;
		}
	}

	@Override
	public String toString() {
		return "ConnectableProcess{" +
				executable.getName() +
				", closed=" + closed +
				'}';
	}

	/**
	 * Invokes an external process from the executable specified.
	 * @param executable the executable file that needs to be executed in the OS
	 * @return the {@link Process} that represents the external process
	 * @throws IOException if the process cannot be started
	 */
	private static Process startProcess(File executable) throws IOException {
		File workingDirectory = executable.getParentFile();
		if (log.isDebugEnabled()) {
			log.debug("Starting executable: '{}' in working directory: '{}'", executable, workingDirectory);
		}
		ProcessBuilder builder = new ProcessBuilder()
				.command(executable.getAbsolutePath())
				.directory(workingDirectory)
				.redirectErrorStream(true);

		// start the external process
		return builder.start();
	}

	/**
	 * <p>Stops the external process specified.</p>
	 * <p>This method will first check if the process is alive and if it is, it will {@link Process#destroy() destroy} it.</p>
	 * @param externalProcess the {@link Process} to stop. If the specified process is {@code null}, nothing happens.
	 */
	private void stopProcess(Process externalProcess) {
		if (externalProcess != null) {
			// if the process is not done until now, kill it
			if (externalProcess.isAlive()) {
				if (log.isDebugEnabled()) {
					log.debug("Process ({}) is still alive, killing it ...", executable.getName());
				}
				externalProcess.destroy();
			} else {
				if (log.isDebugEnabled()) {
					log.debug("Process ({}) is not alive (graceful exit).", executable.getName());
				}
			}
		}
	}
}
