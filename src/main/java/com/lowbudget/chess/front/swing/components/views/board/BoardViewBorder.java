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

import com.lowbudget.chess.front.swing.components.painter.BoardPainter;

import javax.swing.border.AbstractBorder;
import java.awt.*;
import java.awt.geom.Area;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;

/**
 * <p>A custom {@link javax.swing.border.Border} implementation responsible to draw the border around the board.</p>
 * <p>The actual drawing is still performed by a {@link BoardPainter}, this class only calculates and sets the
 * appropriate clip on the {@link Graphics} object to avoid drawing over the scrollbars or causing any other artifacts</p>
 */
class BoardViewBorder extends AbstractBorder {
	private final int borderWidth;
	private final BoardPainter painter;
	private final Rectangle tmpRect = new Rectangle();

	BoardViewBorder(int borderWidth, BoardPainter painter) {
		this.borderWidth = borderWidth;
		this.painter = painter;
	}

	private Shape createClip(int width, int height) {
		Rectangle r1 = new Rectangle(width, height);

		Rectangle2D r2 = new Rectangle2D.Double(r1.getMinX() + borderWidth,
				r1.getMinY() + borderWidth,
				r1.getWidth() - 2 * borderWidth,
				r1.getHeight() - 2 * borderWidth
		);

		Path2D p = new Path2D.Double(Path2D.WIND_EVEN_ODD);
		p.append(r1, false);
		p.append(r2, false);
		return p;
	}

	@Override
	public Insets getBorderInsets(Component c, Insets insets) {
		//noinspection SuspiciousNameCombination
		insets.left = insets.top = insets.right = insets.bottom = borderWidth;
		return insets;
	}

	@Override
	public boolean isBorderOpaque() {
		// Note: this stands for the themes shipped with the application. However if we add a theme that contains
		// an image with transparent areas (i.e. rounded corners) this might need to be changed
		return true;
	}

	@Override
	public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
		Shape oldClip = g.getClip();
		Shape borderClip = createClip(width, height);

		// we need to intersect with the current clip otherwise when the board is zoomed-in too much, the
		// border is painted over the scrollbars
		Area oldArea = new Area(oldClip);
		Area newArea = new Area(borderClip);
		newArea.intersect(oldArea);

		g.setClip(newArea.getBounds());

		tmpRect.setBounds(x, y, width, height);
		painter.drawBorder(g, tmpRect);
		g.setClip(oldClip);
	}
}
