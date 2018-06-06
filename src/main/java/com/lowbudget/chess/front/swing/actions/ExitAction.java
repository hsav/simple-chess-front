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

package com.lowbudget.chess.front.swing.actions;

import com.lowbudget.chess.front.app.UIApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.awt.event.ActionEvent;

public class ExitAction extends ApplicationAction {

	private static final Logger log = LoggerFactory.getLogger(ExitAction.class);

	ExitAction(UIApplication application, String title) {
		super(application, title);
	}

	@Override
	protected void doAction(ActionEvent e) {

		if (log.isDebugEnabled()) {
			log.debug("Exit action called");
		}

		// main window should respond to this event by disposing itself so the application exits
		application.exit();

		// For testing in case our app does not exit, we can inspect the top level containers
		Window[] windows = Window.getWindows();
		for (Window w : windows) {
			if (w.isDisplayable()) {
				log.warn("Found opened window, name={}, displayable: {}, class: {}", w.getName(), w.isDisplayable(), w.getClass().getSimpleName());
			}
		}
	}

}
