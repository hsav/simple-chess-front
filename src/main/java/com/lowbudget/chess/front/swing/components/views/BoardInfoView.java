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

import com.lowbudget.chess.front.app.model.SingleActionBoardModelListener;
import com.lowbudget.chess.front.app.model.SingleActionGameModelListener;
import com.lowbudget.chess.front.app.model.GameModel;
import com.lowbudget.chess.front.app.UIApplication;
import com.lowbudget.common.Messages;
import com.lowbudget.chess.model.Castling.CastlingRight;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;

/**
 * <p>Displays some general board information.</p>
 * <p>This is a component that is mostly useful during debugging.</p>
 */
@SuppressWarnings("WeakerAccess")
public class BoardInfoView extends JPanel {

	/**
	 * Two column table model that displays board info in a property-value approach
	 */
	private final AbstractTableModel tableModel;

	/**
	 * The game model this component uses
	 */
	private final GameModel gameModel;

	public BoardInfoView(UIApplication application) {

		setLayout(new GridLayout());

		this.gameModel = application.getGameModel();
		this.tableModel = new BoardInfoTableModel();

		JTable table = new JTable(tableModel);

		JScrollPane scroll = new JScrollPane();
		scroll.getViewport().add(table);

		add(scroll);

		// listen for board changes
		this.gameModel.addBoardModelListener( new SingleActionBoardModelListener(this::refreshView) );

		// listen for game status changes
		this.gameModel.addGameModelListener( new SingleActionGameModelListener(this::refreshView) );
	}

	private class BoardInfoTableModel extends AbstractTableModel {

		/**
		 * Labels for the first column
		 */
		private final String[] labels = {
				Messages.get("app.view.board.info.label.property.turn"),
				Messages.get("app.view.board.info.label.property.en_passant"),
				Messages.get("app.view.board.info.label.property.half_move"),
				Messages.get("app.view.board.info.label.property.move"),
				Messages.get("app.view.board.info.label.property.white.kingside"),
				Messages.get("app.view.board.info.label.property.white.queenside"),
				Messages.get("app.view.board.info.label.property.black.kingside"),
				Messages.get("app.view.board.info.label.property.black.queenside"),
		};

		@Override
		public String getColumnName(int index) {
			return (index == 0) ? Messages.get("app.view.board.info.label.property") : Messages.get("app.view.board.info.label.value");
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			return columnIndex == 0 ? labels[rowIndex] : getSecondColumnValueAtRow(rowIndex);
		}

		@Override
		public int getRowCount() {
			return labels.length;
		}

		@Override
		public int getColumnCount() {
			return 2;
		}

		/**
		 * Displays the value of the property represented by each row.
		 * @param rowIndex the row index to display
		 * @return the object that represents the value of the row's property
		 */
		private Object getSecondColumnValueAtRow(int rowIndex) {
			if (!gameModel.hasBoard()) {
				return null;
			}

			Object result = null;
			switch (rowIndex) {
				case 0:
					result = gameModel.getPlayingColor();
					break;
				case 1:
					result = gameModel.getEnPassantSquare();
					break;
				case 2:
					result = gameModel.getHalfMoveClock();
					break;
				case 3:
					result = gameModel.getMoveNumber();
					break;
				case 4:
					result = gameModel.getCastling().hasRight(CastlingRight.WhiteKingSide);
					break;
				case 5:
					result = gameModel.getCastling().hasRight(CastlingRight.WhiteQueenSide);
					break;
				case 6:
					result = gameModel.getCastling().hasRight(CastlingRight.BlackKingSide);
					break;
				case 7:
					result = gameModel.getCastling().hasRight(CastlingRight.BlackQueenSide);
					break;
				default:
					break;
			}
			return result;
		}
	}

	private void refreshView() {
		// no internal state to update, all state is read from the game model, we just need the table model to
		// cause a repaint
		tableModel.fireTableDataChanged();
	}
}
