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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.*;

import com.lowbudget.chess.front.app.model.BoardModel;
import com.lowbudget.chess.front.app.UIApplication;
import com.lowbudget.chess.model.PlayerColor;
import com.lowbudget.chess.model.board.Board;

import com.lowbudget.chess.front.swing.components.painter.BoardPainter;
import com.lowbudget.chess.model.Piece;
import com.lowbudget.chess.model.Square;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * View responsible for drawing a board's square.
 */
public class SquareView extends AbstractView {
	private static final Logger log = LoggerFactory.getLogger(SquareView.class);

	/**
	 * Index that helps mapping a square view to its corresponding square and backwards. The index follows the same
	 * semantics as {@link Square#index(int, int)} and is used by the parent {@link BoardView} to correctly draw
	 * the board by taking into account the board's flipped status
	 * @see #getSquare()
	 */
	private final int squareIndex;

	/**
	 * The board model used by this component
	 */
	private final BoardModel model;

	public BoardModel getModel() {
		return model;
	}

	@SuppressWarnings("WeakerAccess")
	public SquareView(UIApplication application, BoardModel model, BoardPainter painter, int squareIndex) {
		super(application, painter);
		this.squareIndex = squareIndex;
		this.model = model;

		// assign a mouse listener and an appropriate transfer handler to be enable dragging operations
		setTransferHandler(new SquareViewDragHandler(application));
		addMouseListener(new SquareViewMouseListener());
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		tmpArea.setBounds(0, 0, getWidth(), getHeight());

		Square square = getSquare();
		PlayerColor color = Board.getSquareColor(square);

		// first draw the square ...
		painter.drawSquare(g, color, tmpArea, squareIndex);

		// ... then the piece at the square (if any)
		Piece piece = model.getPieceAtSquare(square);
		if (piece != null) {
			painter.drawPiece(g, tmpArea, piece);
		}

		// ... and finally draw the selection if this square is selected
		if (model.isSquareSelected(square)) {
			painter.drawSquareSelection(g, tmpArea);
		}
	}

	/**
	 * <p>Returns the board's square that this view's {@link #squareIndex} represents by taking into account the
	 * flipped status of the board.</p>
	 * @return the {@link Square} represented by this view
	 */
	public Square getSquare() {
		Square p = Square.of(squareIndex);
		return application.isBoardFlipped() ? p.flip() : p;
	}

	private static class SquareViewMouseListener extends MouseAdapter {
		@Override
		public void mousePressed(MouseEvent e) {
			SquareView squareView = (SquareView) e.getSource();
			Square square = squareView.getSquare();
			BoardModel model = squareView.model;

			model.setMouseRightButtonClicked( SwingUtilities.isRightMouseButton(e) );

			if (model.isDragAllowed(square)) {
				model.onPieceDragStarted(square);
				squareView.getTransferHandler().exportAsDrag(squareView, e, TransferHandler.MOVE);
			}
		}

		/* Important note: this method is never called if a drag operation is started in mousePressed() */
		@Override
		public void mouseReleased(MouseEvent event) {
			SquareView squareView = (SquareView) event.getSource();
			Square square = squareView.getSquare();
			BoardModel model = squareView.model;

			if (log.isDebugEnabled()) {
				log.debug("Mouse released at the same square view? {}", squareView.contains(event.getPoint()));
			}
			// make sure that the point where the mouse was released still falls inside the original square
			if (squareView.contains(event.getPoint()) && model.isClickAllowed(square)) {
				model.onSquareClick(square);
			}
		}
	}
}
