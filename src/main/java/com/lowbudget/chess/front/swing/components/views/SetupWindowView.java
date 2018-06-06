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

package com.lowbudget.chess.front.swing.components.views;

import com.lowbudget.chess.front.app.UIApplication;
import com.lowbudget.chess.front.swing.ComponentNames;
import com.lowbudget.chess.front.swing.actions.setup.SetupPieceAction;
import com.lowbudget.chess.front.swing.actions.setup.SetupFenAction;
import com.lowbudget.chess.front.swing.common.UIUtils;
import com.lowbudget.chess.front.swing.components.painter.BoardPainter;
import com.lowbudget.chess.front.app.model.BoardModel;
import com.lowbudget.chess.model.ChessConstants;
import com.lowbudget.chess.model.Piece;
import com.lowbudget.chess.front.swing.components.views.board.BoardBackgroundView;
import com.lowbudget.chess.front.swing.components.views.board.BoardView;

import javax.swing.*;
import java.awt.*;

import static com.lowbudget.chess.front.swing.ComponentNames.setupPieceButtonName;

/**
 * <p>Panel responsible to display the setup position screen.</p>
 * <p>This panel displays a board and a float-able toolbar that contains buttons for the pieces to set on the board.</p>
 */
class SetupWindowView extends JPanel {

	private final BoardModel boardModel;

	private final UIApplication application;

	SetupWindowView(UIApplication application, BoardPainter painter) {
		this.application = application;
		this.boardModel = application.getSetupBoardModel();
		setLayout(new BorderLayout());

		// the initial toolbar orientation depends on the initial position in the layout.
		// We use EAST so we need a vertical toolbar
		JToolBar toolBar = createToolBar(SwingConstants.VERTICAL);
		add(toolBar, BorderLayout.EAST);

		BoardBackgroundView setupPane = new BoardBackgroundView(application, painter);
		BoardView boardView = new BoardView(application, ComponentNames.BOARD_SETUP_VIEW, this.boardModel, painter, new Dimension(500, 500));
		setupPane.add(boardView);
		add( new JScrollPane(setupPane), BorderLayout.CENTER);
	}

	@SuppressWarnings("SameParameterValue")
	private JToolBar createToolBar(int orientation) {
		JToolBar toolBar = new JToolBar();
		toolBar.setRollover(true);
		toolBar.setFocusable(false);
		toolBar.setOrientation(orientation);

		// use a ButtonGroup to allow only a single button to be selected
		ButtonGroup group = new ButtonGroup();

		// move piece button
		toolBar.add( moveButton(group) );
		toolBar.addSeparator();

		// piece buttons
		for (Piece piece : Piece.values()) {
			if (Piece.PAWN_BLACK == piece) {
				toolBar.addSeparator();
			}
			if (!piece.isKing()) {
				toolBar.add(pieceButton(piece, group));
			}
		}

		// general purpose buttons
		toolBar.addSeparator();
		toolBar.add( setupButton(ComponentNames.SETUP_INITIAL_POSITION_BUTTON, "board-initial-icon.png", ChessConstants.INITIAL_FEN) );
		toolBar.add( setupButton(ComponentNames.SETUP_EMPTY_POSITION_BUTTON, "board-empty-icon.png", ChessConstants.KINGS_ONLY_FEN) );

		// select the first button
		group.getElements().nextElement().setSelected(true);

		return toolBar;
	}

	private AbstractButton moveButton(ButtonGroup group) {
		return setButtonProperties(ComponentNames.SETUP_MOVE_BUTTON, new JToggleButton(new SetupPieceAction(application, UIUtils.getImage("cursor-drag-icon.png"), boardModel)), group);
	}

	private AbstractButton pieceButton(Piece piece, ButtonGroup group) {
		return setButtonProperties( setupPieceButtonName(piece), new JToggleButton(new SetupPieceAction(application, piece, boardModel)), group);
	}

	private AbstractButton setupButton(String name, String image, String fen) {
		return setButtonProperties(name, new JButton(new SetupFenAction(boardModel, UIUtils.getImage(image), fen)), null);
	}

	private static AbstractButton setButtonProperties(String name, AbstractButton button, ButtonGroup buttonGroup) {
		button.setFocusable(false);
		if (buttonGroup != null) {
			buttonGroup.add(button);
		}
		button.setName(name);
		return button;
	}
}
