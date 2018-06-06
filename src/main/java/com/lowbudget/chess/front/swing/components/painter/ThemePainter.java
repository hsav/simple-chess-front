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

package com.lowbudget.chess.front.swing.components.painter;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import com.lowbudget.chess.front.swing.SwingUIApplication;
import com.lowbudget.chess.model.ChessConstants;
import com.lowbudget.chess.model.Piece;
import com.lowbudget.chess.model.Square;
import com.lowbudget.chess.front.swing.common.UIUtils;
import com.lowbudget.chess.model.PlayerColor;

import static com.lowbudget.chess.model.ChessConstants.MAX_FILES;
import static com.lowbudget.chess.model.ChessConstants.MAX_RANKS;

public class ThemePainter implements BoardPainter {

	private static final int BORDER_ARC = 20;

	private Font subscriptFont;
	private Theme theme;

	private final BasicStroke twoPixelStroke = new BasicStroke(2);

	private final AffineTransform arrow_TX;
	private final Polygon arrowHead;
	private final int arrowHeadRadius = 5;

	public ThemePainter() {
		// create an AffineTransform 
		// and a triangle centered on (0,0) and pointing downward
		arrow_TX = new AffineTransform();

		arrowHead = new Polygon();
		arrowHead.addPoint(0, arrowHeadRadius);
		arrowHead.addPoint(-arrowHeadRadius, -arrowHeadRadius);
		arrowHead.addPoint(arrowHeadRadius, -arrowHeadRadius);
	}

	@Override
	public void loadTheme(String theme) {
		this.theme = new Theme(theme);
		this.theme.load(SwingUIApplication.getThemeProperties(theme));
		subscriptFont = new Font("Arial", Font.PLAIN, 10);
	}

	@Override
	public void drawBackground(Graphics g, Rectangle area) {
		if (area.isEmpty()) {
			return;
		}
		if (theme.hasBackgroundImage()) {
			drawBackgroundWithImage(g, area);
		} else {
			drawBackgroundWithColors(g, area);
		}
	}

	@Override
	public void drawPiece(Graphics g, Rectangle area, Piece piece) {
		if (area.isEmpty()) {
			return;
		}

		int radius = area.width;
		Graphics2D g2d = (Graphics2D) g;

		String name = SwingUIApplication.getPieceImageName(theme.getName(), piece);
		BufferedImage image = UIUtils.getImage(name);

		// scale the image if the cell is smaller else use image in full size (note: this approach does not enlarge the image in bigger window sizes)
		int iw = radius < image.getWidth() ? radius : image.getWidth();
		int ih = radius < image.getHeight() ? radius : image.getHeight();

		int x = (radius - iw) / 2;
		int y = (radius - ih) / 2;

		//log.debug("Drawing piece: " + piece + " at x,y=" + x + "," + y);

		// Bi-cubic provides a little better result but Bilinear is faster
		g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.drawImage(image, x, y, iw, ih, null);
	}

	@Override
	public void drawSubscripts(Graphics g, Rectangle area, int squareRadius, int borderWidth, boolean flipped) {
		if (area.isEmpty()) {
			return;
		}

		//g.setColor(Color.red);
		//g.drawRect(squareArea.x, squareArea.y, squareArea.width-1, squareArea.height-1);

		g.setColor(theme.getSubscriptFontColor());
		g.setFont(subscriptFont);

		drawFileNamesAtTheBottom(g, area, borderWidth, flipped, squareRadius);

		drawRankNamesOnTheLeft(g, area, borderWidth, flipped, squareRadius);
	}

	@Override
	public void drawSquareSelection(Graphics g, Rectangle area) {
		int radius = area.width;
		Graphics2D g2d = (Graphics2D) g;
		g.setColor(theme.getSquareSelectedColor());
		Stroke old = g2d.getStroke();
		g2d.setStroke(twoPixelStroke);
		g2d.drawRect(area.x, area.y, radius - 1, radius - 1);
		g2d.setStroke(old);
	}

	@Override
	public int calculateBorderWidth(float scale, Rectangle area) {
		if (theme.hasBorderImage()) {
			String resource = SwingUIApplication.getThemeImageName(theme.getName(), theme.getBorderImage());
			BufferedImage bg = UIUtils.getImage(resource);
			return Math.round((float) theme.getBorderWidth() / (float) bg.getWidth() * area.width);
		}
		return Math.round(area.width * 0.12f / 2f);
	}

	@Override
	public void highlightLegalMove(Graphics g, Rectangle r) {
		Graphics2D g2d = (Graphics2D) g;
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		int w = Math.round(r.width * 3f / 4f);
		int x = r.x + (r.width - w) / 2;
		int y = r.y + (r.height - w) / 2;

		g.setColor(theme.getSquareHighlightLegalMoveColor());

		Stroke old = g2d.getStroke();
		g2d.setStroke(twoPixelStroke);
		g.drawOval(x, y, w, w);
		g2d.setStroke(old);

	}

	@Override
	public void highlightSquare(Graphics g, Rectangle r) {
		Graphics2D g2d = (Graphics2D) g;
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		int w = r.width - 2 * Math.round(twoPixelStroke.getLineWidth());
		int x = r.x + (r.width - w) / 2;
		int y = r.y + (r.height - w) / 2;

		g.setColor(theme.getSquareHighlightColor());

		Stroke old = g2d.getStroke();
		g2d.setStroke(twoPixelStroke);
		g.drawRect(x, y, w - 1, w - 1);
		g2d.setStroke(old);

	}

	@Override
	public void highlightAttack(Graphics g, Rectangle selectedSquare, Rectangle attackSquare) {
		Graphics2D g2d = (Graphics2D) g;
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		g.setColor(theme.getSquareHighlightAttackColor());
		int x1 = xCenterOf(attackSquare);
		int y1 = yCenterOf(attackSquare);
		int x2 = xCenterOf(selectedSquare);
		int y2 = yCenterOf(selectedSquare);

		// treat the values as a vector to shorten the length a bit
		float vx = x2 - x1;
		float vy = y2 - y1;
		float vLength = (float) (Math.sqrt(vx * vx + vy * vy));
		vx = vx / vLength;
		vy = vy / vLength;

		vx *= vLength - arrowHeadRadius * 2;
		vy *= vLength - arrowHeadRadius * 2;
		x2 = Math.round(vx + x1);
		y2 = Math.round(vy + y1);

		Stroke old = g2d.getStroke();
		g2d.setStroke(twoPixelStroke);
		g.drawLine(x1, y1, x2, y2);
		g2d.setStroke(old);

		double angle = Math.atan2(y2 - y1, x2 - x1);

		arrow_TX.setToIdentity();
		arrow_TX.translate(x2, y2);
		arrow_TX.rotate((angle - Math.PI / 2.0));

		Graphics2D tg = (Graphics2D) g2d.create();
		try {
			tg.transform(arrow_TX);
			tg.fill(arrowHead);
		} finally {
			tg.dispose();
		}
	}

	@Override
	public void drawBorder(Graphics g, Rectangle area) {
		if (area.isEmpty()) {
			return;
		}

		if (theme.hasBorderImage()) {
			drawBorderWithImage(g, area);
		} else {
			drawBorderWithColors(g, area);
		}
	}

	@Override
	public void drawSquare(Graphics g, PlayerColor color, Rectangle area, int squareIndex) {
		if (area.isEmpty()) {
			return;
		}

		if (theme.hasSquareImages()) {
			BufferedImage img = UIUtils.getImage(SwingUIApplication.getThemeImageName(theme.getName(), color.isWhite() ? theme.getSquareWhiteImage() : theme.getSquareBlackImage()));
			g.drawImage(img, area.x, area.y, area.width, area.height, null);
//            // for debug: enable to draw the square index
//            g.setColor(Color.red);
//            g.drawString(""  + squareIndex, area.x + 10, area.y + 12 );
		} else {
			if (color.isBlack()) {
				g.setColor(theme.getSquareBlackColor());
			} else {
				g.setColor(theme.getSquareWhiteColor());
			}
			g.fillRect(area.x, area.y, area.width, area.height);

			//g.setColor(Color.red);
			//g.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
			//g.drawString(square.toString(), area.x + area.width / 2, area.y + area.height / 2);
			//g.drawRect(area.x,  area.y, area.width, area.height);
		}
	}

//	private void drawBackgroundWithTexture(Graphics g, Rectangle area) {
//		// draw texture
//		Graphics2D g2 = (Graphics2D) g;
//		Paint old = g2.getPaint();
//		g2.setPaint(texturePaint);
//		g.fillRect(area.x, area.y, area.width, area.height);
//		g2.setPaint(old);
//	}

	private void drawBackgroundWithImage(Graphics g, Rectangle area) {
		String imageName = SwingUIApplication.getThemeImageName(theme.getName(), theme.getBackgroundImage());
		BufferedImage image = UIUtils.getImage(imageName);
		g.drawImage(image, area.x, area.y, area.width, area.height, null);
	}

	private void drawBackgroundWithColors(Graphics g, Rectangle area) {
		g.setColor(theme.getBackgroundColor());
		g.fillRect(area.x, area.y, area.width, area.height);
	}

	private void drawBorderWithImage(Graphics g, Rectangle area) {
		String resource = SwingUIApplication.getThemeImageName(theme.getName(), theme.getBorderImage());
		BufferedImage img = UIUtils.getImage(resource);
		g.drawImage(img, area.x, area.y, area.width, area.height, null);
	}

	private void drawBorderWithColors(Graphics g, Rectangle area) {
		Graphics2D g2d = (Graphics2D) g;
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		g.setColor(theme.getBorderColor());
		g.fillRoundRect(area.x, area.y, area.width, area.height, BORDER_ARC, BORDER_ARC);

		// draw 1px border on the outside of the border itself
		g.setColor(theme.getBorderBorderColor());
		g.drawRoundRect(area.x, area.y, area.width - 1, area.height - 1, BORDER_ARC, BORDER_ARC);
	}

	private void drawRankNamesOnTheLeft(Graphics g, Rectangle area, int borderWidth, boolean flipped, int radius) {
		// draw rank names on the left
		for (int rank = 0; rank < MAX_RANKS; rank++) {
			String value = Square.rankName(rank);
			FontMetrics fm = g.getFontMetrics();
			Rectangle2D r = fm.getStringBounds(value, g);

			int adjustedRank = flipped ? rank : ChessConstants.Rank.inverted(rank);

			int x1 = area.x + (int) Math.round((borderWidth - r.getWidth()) * 0.5);
			int y1 = area.y + borderWidth + adjustedRank * radius + (int) Math.round((radius - r.getHeight()) * 0.5);

			int x = x1 + (int) Math.round(-r.getX());
			int y = y1 + (int) Math.round(-r.getY());

			g.drawString(value, x, y);
		}
	}

	private void drawFileNamesAtTheBottom(Graphics g, Rectangle area, int borderWidth, boolean flipped, int radius) {
		// draw file names on the bottom
		for (int file = 0; file < MAX_FILES; file++) {
			String value = Square.fileName(file).toUpperCase();
			FontMetrics fm = g.getFontMetrics();
			Rectangle2D r = fm.getStringBounds(value, g);

			int adjustedFile = flipped ? ChessConstants.File.inverted(file) : file;

			int x = area.x + borderWidth + adjustedFile * radius + (int) Math.round(-r.getX() + (radius - r.getWidth()) * 0.5);
			int y = area.y + borderWidth + MAX_RANKS * radius + (int) Math.round(-r.getY() + (borderWidth - r.getHeight()) * 0.5);
			g.drawString(value, x, y);
		}
	}

	private static int xCenterOf(Rectangle r) {
		return r.x + r.width / 2;
	}
	private static int yCenterOf(Rectangle r) {
		return r.y + r.height / 2;
	}

}


