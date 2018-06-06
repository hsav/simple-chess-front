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

package com.lowbudget.chess.model.uci.session.impl;

import com.lowbudget.chess.model.uci.connect.Connectable;
import com.lowbudget.chess.model.uci.session.Session;
import com.lowbudget.chess.model.uci.session.SessionListener;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.*;

public class SessionImplTest {

	private SessionListener mockListener;

	private SessionTask mockTask;

	private String message;

	private Session session;

	@Before
	public void beforeEachTest() {
		mockListener = mock(SessionListener.class);
		mockTask = mock(SessionTask.class);
		message = "uci";
	}

	@After
	public void afterEachTest() {
		if (session != null) {
			session.stop();
			// this is not required but can help when watching the log
			//ThreadUtils.sleep(200);
		}
	}

	@Test(expected = IllegalStateException.class)
	public void testSessionRequiresSessionIsNotAlreadyStarted() {
		session = newStartedSession();

		// we are trying to start an already started session
		session.start();
	}

	@Test(expected = IllegalStateException.class)
	public void testSessionDoesNotAllowReuse() {
		session = newStartedSession();

		session.stop();

		// we are trying to reuse a session
		session.start();
	}


	@Test(expected = IllegalStateException.class)
	public void testSessionRequiresSessionListener() {
		session = newSessionWithoutListener();
		// we haven't set a session listener
		session.start();
	}

	@Test
	public void testSessionIgnoresMessagesBeforeStart() {
		session = newNotStartedSession();

		// session is not started when we send the message
		session.sendMessage(message);

		// the message should be ignored  (i.e. never reach the task)
		verify(mockTask, never()).sendEngineMessage(message);
	}

	@Test
	public void testSessionPropagatesMessagesToTaskAfterStart() {
		session = newStartedSession();

		// session is started, normal message sending
		session.sendMessage(message);

		session.stop();

		// the message should propagate to task as expected
		verify(mockTask).sendEngineMessage(message);
	}

	@Test
	public void testSessionIgnoresMessagesAfterStop() {
		session = newStartedSession();

		// we stop the session and we directly send a message
		session.stop();
		session.sendMessage(message);

		// the message should be ignored (i.e. never reach the task)
		verify(mockTask, never()).sendEngineMessage(message);
	}

	private Session newStartedSession() {
		return newSession(true);
	}

	private Session newNotStartedSession() {
		return newSession(false);
	}

	private Session newSessionWithoutListener() {
		return new SessionImpl("test", mock(Connectable.class), context -> mockTask);
	}

	private Session newSession(boolean start) {
		Session session = newSessionWithoutListener();
		session.setSessionListener(mockListener);
		if (start) {
			session.start();
		}
		return session;
	}
}
