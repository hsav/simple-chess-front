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
import com.lowbudget.chess.model.Piece;
import com.lowbudget.chess.model.Piece.PieceValueComparator;
import com.lowbudget.chess.model.PlayerColor;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * A panel responsible for displaying which pieces were captured so far in the game
 */
@SuppressWarnings("WeakerAccess")
public class CapturedView extends JPanel {

	/**
	 * A custom table model appropriate for displaying the captured pieces in two table columns, the first column
	 * displaying the white captured pieces and the second column the black ones.
	 */
	private final AbstractTableModel tableModel;

	/**
	 * Mapping from piece color to a list with the captured pieces for that color
	 */
	private final Map<PlayerColor, List<Piece>> pieces = new EnumMap<>(PlayerColor.class);

	/**
	 * The maximum number of rows in the table (each time {@link #refreshView()} is called, this value is set to the
	 * maximum of the sizes of the two lists in the {@link #pieces} map)
	 */
	private int maxRows;

	/**
	 * The game model used by this component
	 */
	private final GameModel gameModel;

	public CapturedView(UIApplication application) {

		setLayout(new GridLayout());

		this.gameModel = application.getGameModel();
		this.tableModel = new CapturedTableModel();

		JTable table = new JTable(tableModel);

		JScrollPane scroll = new JScrollPane();
		scroll.getViewport().add(table);
		add(scroll);

		// initialize our lists
		pieces.put(PlayerColor.WHITE, new ArrayList<>());
		pieces.put(PlayerColor.BLACK, new ArrayList<>());

		// listen for board changes
		this.gameModel.addBoardModelListener( new SingleActionBoardModelListener(this::refreshView) );

		// listen for game status changes
		this.gameModel.addGameModelListener( new SingleActionGameModelListener(this::refreshView) );
	}

	/**
	 * Refresh our internal state i.e. the captured pieces of both colors.
	 */
	private void refreshView() {
		List<Piece> whitePieces = pieces.get(PlayerColor.WHITE);
		List<Piece> blackPieces = pieces.get(PlayerColor.BLACK);

		whitePieces.clear();
		blackPieces.clear();

		for (Piece piece : gameModel.getCapturedPieces()) {
			pieces.get(piece.color()).add(piece);
		}

		// sort the two lists according to the value of each piece
		PieceValueComparator pieceValueComparator = new PieceValueComparator();
		whitePieces.sort(pieceValueComparator);
		blackPieces.sort(pieceValueComparator);

		maxRows = Math.max(whitePieces.size(), blackPieces.size());

		tableModel.fireTableDataChanged();
	}

	private class CapturedTableModel extends AbstractTableModel {
		@Override
		public String getColumnName(int index) {
			return (index == 0) ? Messages.get("app.view.captured.label.captured.white") : Messages.get("app.view.captured.label.captured.black");
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			PlayerColor color = columnIndex == 0 ? PlayerColor.WHITE : PlayerColor.BLACK;
			List<Piece> list = pieces.get(color);
			if (rowIndex >= list.size()) {
				return "";
			}
			Piece p = list.get(rowIndex);
			return p.getType().toString();
		}

		@Override
		public int getRowCount() {
			return maxRows;
		}

		@Override
		public int getColumnCount() {
			return 2;
		}
	}
}
