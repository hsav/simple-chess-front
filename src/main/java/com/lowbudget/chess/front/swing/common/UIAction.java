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

package com.lowbudget.chess.front.swing.common;

import com.lowbudget.common.Messages;

import java.awt.*;
import java.awt.event.ActionEvent;

import javax.swing.*;

/**
 * <p>Skeletal implementation of an {@link AbstractAction} that can be used as a base for the actions of an application.</p>
 * <p>The out of the box functionality provided by this class is:</p>
 * <ul>
 *     <li>Support for internationalization of the action's title.</li>
 *     <li>Use of a waiting cursor: in case the action takes some time to complete, a descendant can display a waiting
 *     cursor during execution. The cursor will be restored after the action is completed. Descendants should opt-in
 *     for this functionality by calling {@link #setUseWaitingCursor(boolean) setUseWaitingCursor(true)}</li>
 *     <li>Global exception handling: In case an unexpected exception occurs this action will catch it and display
 *     an error dialog that can display an exception's stack trace by calling {@link UIUtils#showExceptionMessage(Window, String, Exception)}.
 *     The resulting dialog using an {@link ErrorPanel} to display the error.</li>
 * </ul>
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public abstract class UIAction extends AbstractAction {

	private boolean useWaitingCursor = false;

	public UIAction(String titleKey) {
		this(titleKey, null, null);
	}

	public UIAction(String titleKey, Image icon) {
		this(titleKey, icon, null);
	}

	public UIAction(String titleKey, Image icon, String tooltipKey) {
		super(titleKey != null ? Messages.get(titleKey) : "");
		if (icon != null) {
			putValue(Action.SMALL_ICON, new ImageIcon(icon));
		}
		if (tooltipKey != null) {
			putValue(Action.SHORT_DESCRIPTION, Messages.get(tooltipKey));
		}
	}


	@Override
	public void actionPerformed(ActionEvent event) {
		Window window = UIUtils.findWindowAncestor(event);
		try {
			// if this action uses the waiting cursor feature change the cursor to waiting
			if (useWaitingCursor) {
				setWaitingCursor(window);
			}

			doAction(event);
		} catch (Exception e) {
			UIUtils.showExceptionMessage(window,"app.error", e);
		} finally {
			// restore cursor to normal
			if (useWaitingCursor) {
				setNormalCursor(window);
			}
		}
	}

	public boolean isUseWaitingCursor() {
		return useWaitingCursor;
	}

	public void setUseWaitingCursor(boolean useWaitingCursor) {
		this.useWaitingCursor = useWaitingCursor;
	}

	protected abstract void doAction(ActionEvent e);

	protected void setWaitingCursor(Window window) {
		if (window != null) {
			window.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		}
	}

	protected void setNormalCursor(Window window) {
		if (window != null) {
			window.setCursor(Cursor.getDefaultCursor());
		}
	}

}
