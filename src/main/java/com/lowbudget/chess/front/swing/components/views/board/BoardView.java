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

import com.lowbudget.chess.front.app.model.SingleActionBoardModelListener;
import com.lowbudget.chess.front.app.model.SingleActionGameModelListener;
import com.lowbudget.chess.front.app.model.BoardModel;
import com.lowbudget.chess.front.app.model.GameModel;
import com.lowbudget.chess.front.app.UIApplication;
import com.lowbudget.chess.front.app.UIApplication.ModelAdapter;
import com.lowbudget.chess.front.swing.ComponentNames;
import com.lowbudget.chess.front.swing.components.painter.BoardPainter;
import com.lowbudget.chess.model.Square;
import com.lowbudget.chess.model.Move;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseWheelEvent;
import java.util.Collection;
import java.util.List;

import static com.lowbudget.chess.model.ChessConstants.*;

/**
 * <p>View responsible to display a chess board.</p>
 * <p>This view supports custom drawing operations like scaling the board and drawing additional board information
 * like highlighting a square, displaying the enemy pieces that attack the currently selected square, the available
 * legal moves etc.</p>
 */
public class BoardView extends AbstractView {

	private static final Logger log = LoggerFactory.getLogger(BoardView.class);

	/**
	 * The border width of the component that is appropriate for its current size/scale.
	 */
	private int borderWidth;

	/**
	 * The board model of this view
	 */
	private final BoardModel boardModel;

	/**
	 * <p>Creates the components and initializes the layout by also creating the children {@link SquareView}s that are
	 * responsible to display the squares of the chess board</p>
	 *
	 * <p>A note worth mentioning is the way each board's square is assigned to each child square view.</p>
	 * <p>More specifically we want the first child view to correspond to the the top left square of our board
	 * (i.e. {@code a8}), the second child view to correspond to {@code b8} and so on until the last bottom-right
	 * child view that corresponds to the square {@code h1}.</p>
	 * <p>This is a top-down, left-right coordinate system (the same used by swing) which compared with the coordinate
	 * system used by {@link Square#index(int, int)} has the <strong>rank inverted</strong>.</p>
	 * <p>This property is utilized in {@link #getSquareView(Square)} to perform the opposite operation i.e. get a
	 * reference to the square view that displays a specific square</p>
	 *
	 * @param application the current application object
	 * @param boardName the name of the board component - required for our swing tests
	 * @param boardModel the model used by this view
	 * @param boardPainter the painter used by this view to propagate any custom drawing operations
	 * @param size the desired preferred size
	 */
	@SuppressWarnings("WeakerAccess")
	public BoardView(UIApplication application, String boardName, BoardModel boardModel, BoardPainter boardPainter, Dimension size) {
		super(application, boardPainter);
		setPreferredSize(size);
		setMinimumSize(new Dimension(300, 300));
		setMaximumSize(new Dimension(1200, 1200));

		setOpaque(false);
		setName(boardName);

		this.boardModel = boardModel;

		calculateDimensions(size.width, size.height);

		// the grid layout is really convenient in our case since by creating the appropriate border around this view
		// the layout will correctly position and set the size of all the children square views
		setLayout(new GridLayout(MAX_RANKS, MAX_FILES, 0, 0));

		// create the children views. Note the order in which the views are added: left to right and top to bottom.
		// Compared with the coordinate implied by the Square.index() this system has the rank inverted
		// Example:
		// - the first child (index 0) is assigned the square with rank = 7, file = 0 (square index 56 i.e. a8)
		// - the second child (index 1) is assigned the square with rank = 7, file = 1 (square index 57 i.e. b8)
		// - and so on
		for (int rank = MAX_RANKS - 1; rank >= 0; rank--) {
			for (int file = 0; file < MAX_FILES; file++) {
				int squareIndex = Square.index(rank, file);
				SquareView sv = new SquareView(application, boardModel, painter, squareIndex);
				sv.setName(ComponentNames.boardSquareName(boardName, sv.getSquare()));
				add(sv);
			}
		}

		// listen for mouse wheel events to adjust the scale
		addMouseWheelListener(new MouseWheelListener());

		// listen to model events in case we need to repaint ourselves
		application.addModelListener(new ModelListener());

		this.boardModel.addBoardModelListener( new SingleActionBoardModelListener(this::repaint) );

		if (!this.boardModel.isBoardInSetupMode()) {
			GameModel gameModel = (GameModel) boardModel;
			gameModel.addGameModelListener( new SingleActionGameModelListener(this::repaint));
		}
	}

	@Override
	public void paint(Graphics g) {
		super.paint(g);

		drawSubscripts(g);

		if (boardModel.isBoardInSetupMode()) {
			return;	// for the board of the setup we do not draw any extra information
		}
		boolean viewLegalMoves = application.isViewLegalMovesEnabled();
		boolean viewAttacks = application.isViewAttacksEnabled();
		boolean viewLastMove = application.isViewLastMoveEnabled();
		Square selectedSquare = boardModel.getSelectedSquare();
		Move lastMove = boardModel.getCurrentMove();

		if (selectedSquare.isValid()) {
			if (viewLegalMoves) {
				drawLegalMoves(g);
			}
			if (viewAttacks) {
				drawAttacks(g);
			}
		}

		if (viewLastMove && lastMove.isValid()) {
			drawLastMove(g, lastMove);
		}
	}

	private void drawSubscripts(Graphics g) {
		Graphics2D g2d = (Graphics2D) g;
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

		// to decide the square's width just use the width of the first child (since we use a grid layout all children
		// will have the same width and height)
		int squareWidth = getComponent(0).getWidth();

		tmpArea.setBounds(0, 0, getWidth(), getHeight());
		painter.drawSubscripts(g, tmpArea, squareWidth, borderWidth, application.isBoardFlipped());
	}

	private void drawLastMove(Graphics g, Move lastMove) {
		drawSquare(lastMove.getFrom(), () -> painter.highlightSquare(g, tmpArea));
		drawSquare(lastMove.getTo(), () -> painter.highlightSquare(g, tmpArea));
	}

	private void drawLegalMoves(Graphics g) {
		Collection<Move> legalMoves = boardModel.getLegalMovesForSelectedSquare();
		for (Move move : legalMoves) {
			drawSquare(move.getTo(), () -> painter.highlightLegalMove(g, tmpArea));
		}
	}

	private void drawAttacks(Graphics g) {

		List<Square> attackerMoves = boardModel.getAttacksToSelectedSquare();

		// selected square
		Square selectedSquare = boardModel.getSelectedSquare();
		SquareView selectedView = getSquareView(selectedSquare);
		Rectangle selectedSquareBounds = selectedView.getBounds();

		for (Square square : attackerMoves) {
			// direction of the arrow is from the attacker square to the selected square
			drawSquare(square, () -> painter.highlightAttack(g, selectedSquareBounds, tmpArea));
		}
	}

	private void drawSquare(Square square, Runnable paintAction) {
		SquareView sv = getSquareView(square);
		tmpArea.setBounds(sv.getX(), sv.getY(), sv.getWidth(), sv.getHeight());
		paintAction.run();
	}

	private void scaleBoard(float ratio) {
		float scale = (ratio > 0) ? 1.1f * Math.abs(ratio) : (ratio < 0) ? Math.abs(ratio) / 1.1f : 1.f;

		int w = getWidth();
		int h = getHeight();

		int r = w < h ? w : h;
		int boardRadius = Math.round(scale * r);

		if (boardRadius < getMinimumSize().width) {
			boardRadius = getMinimumSize().width;
		} else if (boardRadius > getMaximumSize().width) {
			boardRadius = getMaximumSize().width;
		}

		// calculate new border width
		calculateDimensions(boardRadius, boardRadius);

		// new square size
		int squareRadius = (boardRadius - 2 * borderWidth) / MAX_RANKS;

		// new board size
		boardRadius = squareRadius * MAX_RANKS + 2 * borderWidth;

		Dimension size = new Dimension(boardRadius, boardRadius);
		setPreferredSize(size);
		revalidate();
	}

	private void calculateDimensions(int w, int h) {
		tmpArea.setBounds(0, 0, w, h);
		int newBorderWidth = painter.calculateBorderWidth(1.f, tmpArea);

		int squareAreaWidth = w - 2 * newBorderWidth; // area left for squares
		if (squareAreaWidth % MAX_RANKS != 0) {
			squareAreaWidth = (squareAreaWidth + MAX_RANKS - 1) / MAX_RANKS * MAX_RANKS; // round up to next multiple of MAX_RANKS
		}

		borderWidth = (w - squareAreaWidth) / 2;
		//log.debug("Calculated dimensions [w, h] = [{}x{}], border width: {}, inner area: {}", w, h, borderWidth, squareAreaWidth);
		setBorder( new BoardViewBorder(borderWidth, painter) );
	}

	/**
	 * <p>Returns the square view that corresponds to the square specified.</p>
	 * <p>This is possible due to the way the children square views were constructed. As explained in the
	 * {@link #BoardView(UIApplication, String, BoardModel, BoardPainter, Dimension) constructor} the square
	 * views coordinate system has the rank inverted compared to the coordinate system implied
	 * by {@link Square#index(int, int)}.</p>
	 * <p>To find the view that corresponds to a specific square we need to calculate the index of the
	 * square that has the rank inverted and use that value as the argument of {@link Container#getComponent(int)}
	 * to get the desired view.</p>
	 * <p>If the board is flipped we need to invert both the square's rank and file, thus ending with an index that
	 * corresponds to the original non-inverted rank and the file inverted.</p>
	 * <p>Summarizing, to find the component array index of the square view that corresponds to a specific square we
	 * need to transform the square's rank and file and then use the transformed values with
	 * {@link Square#index(int, int)} to get the component array index. The transformations needed to be done are the following:</p>
	 * <ul>
	 *     <li>if the board is not flipped, then invert the rank</li>
	 *     <li>if the board is flipped, then invert the file</li>
	 * </ul>
	 *
	 * @param square the board square
	 * @return the square view that displays the value of that square
	 * @see BoardView#BoardView(UIApplication, String, BoardModel, BoardPainter, Dimension)
	 */
	private SquareView getSquareView(Square square) {
		int rank, file;
		if (!application.isBoardFlipped()) {
			rank = Rank.inverted(square.rank());
			file = square.file();
		} else {
			rank = square.rank();
			file = File.inverted(square.file());
		}
		return (SquareView) getComponent(Square.index(rank, file));
	}

	private class MouseWheelListener extends MouseAdapter {
		@Override
		public void mouseWheelMoved(MouseWheelEvent e) {
			double clicks = e.getPreciseWheelRotation();
			if (log.isDebugEnabled()) {
				log.debug("Wheel rotated, clicks: {}",  clicks);
			}
			scaleBoard((float) clicks);
		}
	}

	private class ModelListener extends ModelAdapter {
		@Override
		public void onThemeChanged(String newTheme) {
			calculateDimensions(getWidth(), getHeight());
			repaint();
		}

		@Override
		public void onOptionChanged() {
			repaint();
		}

		@Override
		public void onBoardFlipped(boolean isFlipped) {
			repaint();
		}
	}
}
