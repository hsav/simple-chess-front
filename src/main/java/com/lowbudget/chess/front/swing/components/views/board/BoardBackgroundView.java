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

import com.lowbudget.chess.front.app.UIApplication;
import com.lowbudget.chess.front.swing.components.painter.BoardPainter;

/**
 * <p>The view that displays the application's background.</p>
 * <p>It also contains the {@link BoardView} that draws the board.</p>
 */
public class BoardBackgroundView extends AbstractView {

	public BoardBackgroundView(UIApplication application, BoardPainter boardPainter) {
		super(application, boardPainter);

		// the background view is added in a scroll pane and contains the board view
		// this layout causes the board view to be centered in the scroll pane
		setLayout(new GridBagLayout());

		application.addModelListener( new GameViewModelListener() );
	}

	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);

		tmpArea.setBounds(0, 0, getWidth(), getHeight());
		painter.drawBackground(g, tmpArea);
	}

	private class GameViewModelListener extends UIApplication.ModelAdapter {
		@Override
		public void onThemeChanged(String newTheme) {
			repaint();
		}
	}
}
