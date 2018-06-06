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

package com.lowbudget.chess.front.swing.components.views.board;

import java.awt.*;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

import com.lowbudget.chess.front.app.UIApplication;
import com.lowbudget.chess.front.swing.components.painter.BoardPainter;

/**
 * <p>Base class for the board related views that perform custom drawing.</p>
 * <p>All views that extend this class use a {@link BoardPainter} to propagate any required custom drawing.</p>
 */
@SuppressWarnings("WeakerAccess")
public abstract class AbstractView extends JPanel {

	/** The painter to propagate the drawing to */
	protected final BoardPainter painter;

	/**
	 * A rectangle to be temporarily used for drawing operations. The life-cycle of this variable is expected to
	 * start and end during a single method call to avoid creating {@link Rectangle} objects for each call of the
	 * {@link java.awt.Component#paint(Graphics)} method.
	 */
	protected final Rectangle tmpArea = new Rectangle();

	/**
	 * The global application object
	 */
	protected final UIApplication application;

	public AbstractView(UIApplication application, BoardPainter painter) {
		this.painter = painter;
		this.application = application;
		setBorder(BorderFactory.createEmptyBorder());
	}
}
