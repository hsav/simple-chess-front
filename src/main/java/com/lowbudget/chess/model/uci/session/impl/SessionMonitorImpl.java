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
import com.lowbudget.chess.model.uci.session.impl.connection.ServerConnectionTask;
import com.lowbudget.util.Exceptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.*;

/**
 * <p>A straightforward implementation of a {@link SessionMonitor}.</p>
 * <p>This class uses a {@link Executors#newCachedThreadPool()} executor to invoke new tasks and stores 3 separate
 * {@link Set}s to keep track of started, stopped and finished tasks.</p>
 * <p>It additionally distinguishes between the starting task that initiated the connection (aka the parent task) and
 * the rest of the tasks that are considered to be the "child" tasks. Since all tasks notify this class about their
 * lifecycle events it is possible to detect inconsistencies like a child task being stopped while the parent is not
 * and act accordingly.</p>
 * <p>When the session is about to be closed a new clean-up thread is created that shuts down the underlying
 * {@link ExecutorService} that is being used.</p>
 */
public class SessionMonitorImpl implements SessionMonitor {

	private static final Logger log = LoggerFactory.getLogger(SessionMonitorImpl.class);

	/**
	 * The executor used to run the tasks
	 */
	private final ExecutorService executorService;

	/**
	 * Lock that guards the internal state of this object
	 */
	private final Object stateLock = new Object();

	/**
	 * The session name this executor is related with. (used for debugging purposes)
	 */
	private final String sessionName;

	/**
	 * The monitor listener (i.e. the session) this monitor is associated with. Note that caution should be exercised
	 * to not hold any locks while calling listener's methods or if this cannot be avoided to be careful for deadlocks.
	 */
	private final Listener monitorListener;

	/** Keeps track of started tasks */
	private final Set<Runnable> startedTasks = new HashSet<>();

	/** Keeps track of stopped tasks */
	private final Set<Runnable> stoppedTasks = new HashSet<>();

	/** Keeps track of finished tasks */
	private final Set<Runnable> finishedTasks = new HashSet<>();

	/**
	 * Indicates if at least one task was started (i.e. it has invoked {@link #onTaskStarted(Runnable)})
	 */
	private boolean started = false;

	/**
	 * <p>The latch we should count down if a call is made to {@link #awaitTaskTermination()}.</p>
	 * <p>This can be used if the session wishes to block until all tasks are finished.</p>
	 * <p>Normally the session is not supposed to wait however this is useful for testing the
	 * {@link ServerConnectionTask} with a stand-alone server.</p>
	 */
	private final CountDownLatch waitTerminationLatch;

	/**
	 * Indicates if a thread is waiting termination of the tasks in this executor (i.e. a call was made
	 * to {@link #awaitTaskTermination()})
	 * In this case we should notify the caller by counting down the {@link #waitTerminationLatch} when the last task
	 * is finished
	 * Default is {@code false}
	 */
	private boolean waitingTermination = false;

	/**
	 * The first task started by the {@link Session}. Only one parent task is allowed per session, the
	 * rest of the tasks are considered child tasks of this one.
	 */
	private SessionTask parentTask;

	/**
	 * Child tasks invoked by the {@link #parentTask}
	 */
	private final Set<SessionTask> childTasks = new LinkedHashSet<>();

	/**
	 * Creates a new session monitor
	 */
	SessionMonitorImpl(String sessionName, Listener monitorListener) {
		this.executorService = Executors.newCachedThreadPool();
		this.sessionName = Objects.requireNonNull(sessionName, "Session name is null");
		this.monitorListener = Objects.requireNonNull(monitorListener, "Monitor listener is null");
		this.waitTerminationLatch = new CountDownLatch(1);
	}

	/**
	 * <p>Executes a {@link Session}'s starting task in its own thread.</p>
	 * <p>This method is expected to be called once per session for the root task that starts the connection (and
	 * possibly invokes more sub-tasks).</p>
	 * @param task the task to run
	 * @throws IllegalStateException if the method is called more than once.
	 */
	void executeStartingTask(SessionTask task) {
		synchronized (stateLock) {
			if (parentTask != null) {
				// should never happen if we use this class correctly
				throw new IllegalStateException("Parent task has already been started!");
			}
			parentTask = task;
		}
		executorService.execute(task);
	}

	@Override
	public void executeAndWait(List<SessionTask> tasks) throws InterruptedException {
		synchronized (stateLock) {
			if (parentTask == null) {
				throw new IllegalStateException("There is no parent task!");
			}
			childTasks.addAll(tasks);
		}

		int count = tasks.size();

		CompletionService<Void> cs = new ExecutorCompletionService<>(this.executorService);

		if (log.isDebugEnabled()) {
			log.debug("Invoking {} tasks through completion service...", count);
		}

		for (Runnable task : tasks) {
			cs.submit( () -> { task.run(); return null; } );
		}
		if (log.isDebugEnabled()) {
			log.debug("Completion service: All tasks are submitted, waiting for completion...");
		}
		for (int i = 0; i < count; i++) {
			// blocks until a task (any task) is completed
			cs.take();
		}

		// all tasks should be done by now
		if (log.isDebugEnabled()) {
			log.debug("Completion service: all tasks are finished");
		}
	}

	@Override
	public void onTaskStarted(Runnable task) {
		synchronized (stateLock) {
			startedTasks.add(task);
			//log.debug("Started task: " + task);
			report("started");
			// we have at least one task, so we consider the executor "started"
			started = true;
		}
	}

	@Override
	public void onTaskFinished(SessionTask task) {
		synchronized (stateLock) {
			boolean finished = finishedTasks.add(task);
			if (finished) {
				log.debug("Finished task: " + task);
				report("finished");
			}

			if (waitingTermination && areAllTasksFinished()) {
				waitTerminationLatch.countDown();
				// make sure we don't count down again
				waitingTermination = false;
			}
		}
	}

	@Override
	public void onTaskStopped(SessionTask task) {
		boolean shouldTriggerAutomaticStop = false;
		synchronized (stateLock) {
			boolean stopped = stoppedTasks.add(task);
			// tasks can be stopped many times so report them only the first time
			if (stopped) {
				log.debug("Stopped task: " + task);
				if (task == parentTask) {
					// we need to stop all child tasks too
					stopChildTasks();
					report("stopped");
				} else if (!stoppedTasks.contains(parentTask)) {
					// this can happen only if we loose the connection for reasons out of our hand (i.e. during a remote
					// connection we reach the end of stream)
					log.debug(" Child task {} stopped while its parent is still running. Notifying the session to stop automatically", task);
					shouldTriggerAutomaticStop = true;
				}
			}
		}
		// note: no lock is held while calling the monitor listener's method
		if (shouldTriggerAutomaticStop) {
			monitorListener.onAutomaticStopDetected();
		}
	}

	/**
	 * Shuts down the executor. This is expected to be called by the session which has created this class
	 */
	/*package*/ void shutdownGracefully() {
		// stop our executor service. Note that we do need to use another thread, because the
		// thread closing the session might already be running in the executor. In that case calling shutdown() cannot succeed.
		CleanupTask.runInNewThread(sessionName, executorService);
	}

	/**
	 * <p>Waits (i.e. blocks the current thread) until all tasks submitted in this executor are finished.</p>
	 * <p><strong>Warning:</strong> do not call this from a task (i.e. a {@link SessionTask}) already submitted in
	 * the executor since it is guaranteed that the calling thread will be blocked forever.</p>
	 * <p>If you want to wait for a group of tasks to be finished inside a {@link SessionTask} you can use the
	 * {@link SessionMonitor#executeAndWait(List)} method that will wait only for the specific group of tasks to be finished.</p>
	 * @throws InterruptedException if the calling thread is interrupted while waiting
	 */
	@SuppressWarnings("unused")
	private void awaitTaskTermination() throws InterruptedException {
		synchronized (stateLock) {
			this.waitingTermination = true;
		}
		waitTerminationLatch.await();
	}

	/**
	 * Stops all the child tasks of the parent task.
	 * Guarded by {@link #stateLock}
	 */
	private void stopChildTasks() {
		if (childTasks.size() > 0) {
			log.debug("Stopping child tasks of parent {}", parentTask);
			for (SessionTask child : childTasks) {
				if (!stoppedTasks.contains(child)) {
					child.stop();
				}
			}
			log.debug("Child tasks stopped");
		} else {
			log.debug("Task {} has no child tasks", parentTask);
		}
	}

	/**
	 * Checks if all the tasks are finished.
	 * Guarded by {@link #stateLock}
	 */
	private boolean areAllTasksFinished() {
		return started && startedTasks.size() == finishedTasks.size();
	}

	/**
	 * Guarded by {@link #stateLock}
	 */
	private void report(String action) {
		log.debug("{}: Tasks available: started={}, stopped={}, finished={}", action, startedTasks.size(), stoppedTasks.size(), finishedTasks.size());
	}

	/**
	 * <p>A runnable task that stops the specified {@link ExecutorService}.</p>
	 * <p>The need to use another thread arises from the fact that the session can be closed by any thread (e.g.
	 * the reader or writer threads can attempt to close the session if they encounter an exception).</p>
	 * <p>In that case calling {@link ExecutorService#shutdown()} is guaranteed to fail and we will always need to
	 * call {@link ExecutorService#shutdownNow()}. However {@link ExecutorService#shutdownNow()} interrupts the
	 * threads which can result in interrupting the thread that is trying to shutdown the executor.</p>
	 * <p>This is not as bad as it sounds since at that particular time, the interrupted thread is not blocked/waiting
	 * (since it is handling the exception), however stopping the executor from a different thread seems a cleaner
	 * approach.</p>
	 */
	private static class CleanupTask implements Runnable {

		final ExecutorService executorService;
		final String sessionName;

		CleanupTask(String sessionName, ExecutorService executorService) {
			this.executorService = executorService;
			this.sessionName = sessionName;
		}

		/**
		 * Invokes a cleanup task in a new thread to shutdown the specified executor service.
		 * @param sessionName the name of the session that invoked the cleanup
		 * @param executorService the executor service to shutdown
		 */
		static void runInNewThread(String sessionName, ExecutorService executorService) {
			CleanupTask cleanupTask = new CleanupTask(sessionName, executorService);
			Thread cleanupThread = new Thread(cleanupTask);
			cleanupThread.start();
		}

		@Override
		public void run() {
			Thread.currentThread().setName(sessionName + "-cleanup");
			try {
				boolean finished = attemptToShutDownExecutorNormally();
				if (!finished) {
					// last attempt
					attemptToShutDownExecutorNow();
				}
			} catch (InterruptedException e) {
				Exceptions.rethrowAsRuntimeException(e); // should never happen
			}
		}

		private boolean attemptToShutDownExecutorNormally() throws InterruptedException {
			if (log.isDebugEnabled()) {
				log.debug("Shutting down executor...");
			}
			executorService.shutdown();
			return waitForExecutorTermination();
		}

		private void attemptToShutDownExecutorNow() throws InterruptedException {
			if (log.isDebugEnabled()) {
				log.debug("Executor is not shut down yet. Calling shutDownNow(). Threads might be interrupted");
			}
			executorService.shutdownNow();
			waitForExecutorTermination();
		}

		private boolean waitForExecutorTermination() throws InterruptedException {
			if (log.isDebugEnabled()) {
				log.debug("Waiting for executor to shutdown...");
			}
			boolean finished = executorService.awaitTermination(500, TimeUnit.MILLISECONDS);
			if (log.isDebugEnabled()) {
				log.debug("Executor is shut down: {}", finished);
			}
			return finished;
		}
	}
}
