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

package com.lowbudget.chess.front.app.controller.game;

import com.lowbudget.chess.front.app.UIApplication.UITimer;
import com.lowbudget.chess.front.app.UIApplication.UITimerListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>Keeps track of the remaining time for both players</p>
 * <p>This class utilizes a {@link UITimer} to tick the clock of the current player by one second each time.</p>
 */
public class TimeControl implements UITimerListener {
	private static final Logger log = LoggerFactory.getLogger(TimeControl.class);

	/** Interval used for the {@link UITimer} to fire every second */
	public static final int TIMER_INTERVAL = 1000; // in millis

	/**
	 * Utility class to store the total and remaining time for each player
	 */
	private static class TimeCounter {
		long total;
		long remaining;

		void reset(long total) {
			this.total = total;
			this.remaining = total;
		}

		void sync(long timeRemaining) {
			this.remaining = timeRemaining;
			if (this.total < this.remaining) {
				this.total = this.remaining;
			}
		}

		boolean elapsed() {
			remaining -= TIMER_INTERVAL;
			if (remaining <= 0) {
				remaining = 0;
				return true;
			}
			return false;
		}

		@Override
		public String toString() {
			return "TimeCounter{" +
					"total=" + total +
					", remaining=" + remaining +
					'}';
		}
	}

	public interface TimeControlListener {
		void onTimeControlChanged(long whiteTime, long blackTime);

		void onTimeControlElapsed(boolean isWhite);
	}

	/** The counter for the white player **/
	private final TimeCounter whiteTimeCounter = new TimeCounter();

	/** The counter for the black player **/
	private final TimeCounter blackTimeCounter = new TimeCounter();

	/**
	 * Timer that fires every second and notifies this class by calling {@link #onTimer()}.
	 * We always use the same timer which we start/stop according to the game state
	 */
	private final UITimer timer;

	/**
	 * Indicates if the current player whose time will be ticked every time the timer fires, is the white player or not.
	 */
	private boolean isWhite;

	/**
	 * A single listener that is notified for ticks of the time control and when the time control of a player
	 * has elapsed
	 */
	private TimeControlListener listener;

	public TimeControl(UITimer timer) {
		this.timer = timer;
		timer.addUITimerListener(this);
	}

	public void setListener(TimeControlListener listener) {
		this.listener = listener;
	}

	/**
	 * Returns the remaining time of white player (in millis)
	 */
	long getWhiteRemainingTime() {
		return whiteTimeCounter.remaining;
	}

	/**
	 * Returns the remaining time of black player (in millis)
	 */
	long getBlackRemainingTime() {
		return blackTimeCounter.remaining;
	}

	/**
	 * @return {@code true} if the time control is started (i.e. the timer is running) or {@code false} otherwise.
	 */
	@SuppressWarnings("BooleanMethodIsAlwaysInverted")
	public boolean isStarted() {
		return timer.isRunning();
	}

	/**
	 * Sets the current player to the color specified
	 * @param isWhite the color of the player for whom we should tick the time from now and on
	 */
	void setCurrentPlayer(boolean isWhite) {
		this.isWhite = isWhite;
	}

	/**
	 * Resets the total time and the current player. This action will stop any previously running timer but will not
	 * automatically start a new one
	 * @param whiteTotalTime the total time for white player (in millis)
	 * @param blackTotalTime the total time for black player (in millis)
	 * @param isWhite {@code true} if the player we will tick the time for, the next time the timer fires, is the
	 *                            white player, {@code false} otherwise
	 */
	public void setup(long whiteTotalTime, long blackTotalTime, boolean isWhite) {
		this.whiteTimeCounter.reset(whiteTotalTime);
		this.blackTimeCounter.reset(blackTotalTime);
		setCurrentPlayer(isWhite);
		stop();
	}

	/**
	 * Synchronizes the remaining time for both players
	 * This is used when the timing controls are managed externally (i.e. when watching a server game)
	 * @param whiteRemainingTime white's remaining time (in millis)
	 * @param blackRemainingTime black's remaining time (in millis)
	 */
	void sync(long whiteRemainingTime, long blackRemainingTime) {
		this.whiteTimeCounter.sync(whiteRemainingTime);
		this.blackTimeCounter.sync(blackRemainingTime);
	}

	/**
	 * <p>Starts the time controls.</p>
	 * <p>The timer will be scheduled to fire every second. If the timer was already running it is stopped and then
	 * re-started</p>
	 */
	public void start() {
		if (log.isDebugEnabled()) {
			log.debug("Timer started");
		}
		stop();
		timer.start();
		listener.onTimeControlChanged(getWhiteRemainingTime(), getBlackRemainingTime());
	}

	/**
	 * <p>Stops the time controls by stopping the UI timer.</p>
	 */
	public void stop() {
		if (timer.isRunning()) {
			if (log.isDebugEnabled()) {
				log.debug("Timer stopped");
			}
			timer.stop();
		}
	}

	@Override
	public String toString() {
		return "TimeControl{" +
				"whiteTimeCounter=" + whiteTimeCounter +
				", blackTimeCounter=" + blackTimeCounter +
				", isWhite=" + isWhite +
				'}';
	}


	/**
	 * <p>Method that conforms to {@link UITimerListener} so it can be used as a listener to the {@link #timer}.</p>
	 */
	@Override
	public void onTimer() {
		boolean finished = this.isWhite ? whiteTimeCounter.elapsed() : blackTimeCounter.elapsed();
		listener.onTimeControlChanged(getWhiteRemainingTime(), getBlackRemainingTime());

		if (finished) {
			stop();
			listener.onTimeControlElapsed(this.isWhite);
		}
	}
}
