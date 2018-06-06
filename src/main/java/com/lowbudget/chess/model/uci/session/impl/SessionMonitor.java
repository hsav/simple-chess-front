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

import com.lowbudget.chess.model.uci.session.Session;

import java.io.InputStream;
import java.util.List;

/**
 * <p>Manages the execution of {@link Session} tasks that run in their own thread.</p>
 * <p>The executed tasks, are expected to work with this class to let it know when a task is stopped and/or
 * finished, so it can keep track of their life-cycle. This allows this class to monitor the tasks and detect any
 * inconsistencies during the tasks' life-cycle.</p>
 * <p>A session task is expected to be in one of three ({@code 3}) possible states:</p>
 * <ul>
 *     <li>{@code STARTED}: the task has been started i.e. its thread has entered its {@link Thread#run()} method.</li>
 *     <li>{@code STOPPED}: the task has been requested to stop but it might still be running. We expect such a task
 *     to finish soon but this may not be possible if the task is executing a blocking operation i.e. like
 *     {@link InputStream#read()}. By notifying this class that the task has been stopped (by calling
 *     {@link #onTaskStopped(SessionTask)}), it is possible to identify and report all such tasks or even take an action
 *     trying to fix the problem (i.e. by closing the blocking stream).</li>
 *     <li>{@code FINISHED}: the task has been completed i.e. it is about to exit its {@link Runnable#run()} method. In that
 *     case the task needs to call {@link #onTaskFinished(SessionTask)}.</li>
 * </ul>
 * <p>The expected task state transition is: {@code STARTED -> STOPPED -> FINISHED}. If a task is
 * blocked and cannot make further progress it might not be able to reach the last state on its own.</p>
 * <p>Implementations should make sure that they are thread-safe.</p>
 */
interface SessionMonitor {

	/**
	 * Listens for events in a {@link SessionMonitor}
	 */
	@FunctionalInterface
	interface Listener {
		/**
		 * Event that fires when the monitor detects that the session should be automatically stop.
		 * This is the case when a child task is stopped before the starting task (aka the parent task) has been stopped
		 * This can occur when a connection is lost for reasons external to this system
		 */
		void onAutomaticStopDetected();
	}

	/**
	 * <p>Schedules a list of tasks for execution, each in its own thread and waits (blocking the current thread)
	 * until all tasks are finished.</p>
	 * <p>All the tasks in this list are expected to be child tasks of the session's starting task</p>
	 * @param tasks the list of tasks to run
	 * @throws InterruptedException if interrupted while waiting
	 * @throws IllegalStateException if there is no parent task started yet
	 */
	void executeAndWait(List<SessionTask> tasks) throws InterruptedException;

	/**
	 * <p>Notifies this class that a task has been started.</p>
	 * @param task the task that has been started
	 */
	void onTaskStarted(Runnable task);

	/**
	 * <p>Notifies this class that a task has finished.</p>
	 * @param task the task that has been finished
	 */
	void onTaskFinished(SessionTask task);

	/**
	 * <p>Notifies this class that a task has been stopped.</p>
	 * @param task the task that has been stopped
	 */
	void onTaskStopped(SessionTask task);
}
