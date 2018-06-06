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

package com.lowbudget.chess.front.swing;

import com.lowbudget.chess.front.app.UIApplication;
import com.lowbudget.chess.front.app.UIApplication.UITimerListener;

import javax.swing.*;

/**
 * Swing implementation of a {@link com.lowbudget.chess.front.app.UIApplication.UITimer} which is basically a wrapper around
 * Swing's {@link Timer}
 */
public class SwingUITimer implements UIApplication.UITimer {

	private final Timer timer;

	SwingUITimer(int delay) {
		this.timer = new Timer(delay, null);
	}

	@Override
	public void addUITimerListener(UITimerListener listener) {
		timer.addActionListener( event -> listener.onTimer() );
	}

	@Override
	public void start() {
		timer.start();
	}

	@Override
	public void stop() {
		timer.stop();
	}

	@Override
	public boolean isRunning() {
		return timer.isRunning();
	}
}
