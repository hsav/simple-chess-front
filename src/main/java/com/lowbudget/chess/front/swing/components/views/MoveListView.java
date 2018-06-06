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
import com.lowbudget.chess.front.swing.ComponentNames.ViewMoveList;
import com.lowbudget.chess.front.swing.actions.game.BrowseMoveListAction;
import com.lowbudget.chess.model.Move;
import com.lowbudget.chess.model.board.MoveList.BrowseType;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

/**
 * Displays the moves played so far and provides the ability to browse the previously played moves.
 */
@SuppressWarnings("WeakerAccess")
public class MoveListView extends JPanel {

	/**
	 * 3-column table model to display the number of the move, the white move and the black move
	 */
	private final AbstractTableModel tableModel;

	/** The table displaying the moves */
	private final JTable table;

	/** The game model used by this component */
	private final GameModel gameModel;

	private final UIApplication application;

	public MoveListView(UIApplication application) {
		this.application = application;
		this.gameModel = application.getGameModel();
		setLayout(new BorderLayout());

		// create a toolbar with buttons that allow browsing the move list. This toolbar is placed on the top of the
		// component
		JToolBar toolBar = new JToolBar();
		toolBar.setBorderPainted(true);
		toolBar.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
		toolBar.setFloatable(false);
		toolBar.setFocusable(false);
		toolBar.setLayout(new GridLayout(1, 4));
		toolBar.add(newToolBarButton(ViewMoveList.FIRST_MOVE, "fast-backward-icon.png", "app.action.game.browse.first", BrowseType.FIRST));
		toolBar.add(newToolBarButton(ViewMoveList.PREVIOUS_MOVE, "backward-icon.png", "app.action.game.browse.previous", BrowseType.PREVIOUS));
		toolBar.add(newToolBarButton(ViewMoveList.NEXT_MOVE,"forward-icon.png", "app.action.game.browse.next", BrowseType.NEXT));
		toolBar.add(newToolBarButton(ViewMoveList.LAST_MOVE,"fast-forward-icon.png", "app.action.game.browse.last", BrowseType.LAST));

		add(toolBar, BorderLayout.NORTH);

		// a table with 3 columns to display moves for white and black
		this.tableModel = new MoveListTableModel();
		this.table = new JTable(tableModel);

		table.setShowGrid(false);

		// assign a custom renderer to be able to draw the table cells with different colors based on their state e.g. when
		// browsing, all moves after the current move are displayed in a gray color, the current move is displayed in
		// blue etc.
		table.setDefaultRenderer(Object.class, new DefaultRenderer());

		JScrollPane scroll = new JScrollPane(table);
		add(scroll, BorderLayout.CENTER);

		// listen for board changes
		this.gameModel.addBoardModelListener( new SingleActionBoardModelListener(this::refreshView) );

		// listen for game status changes
		this.gameModel.addGameModelListener( new SingleActionGameModelListener(this::refreshView) );
	}

	/**
	 * <p>Creates a new browse button in the toolbar with the specified icon name and type.</p>
	 * <p>Each button is assigned an action that modifies the current index of the move list.</p>
	 * @param name the component name - used in testing
	 * @param iconNameKey a key that specifies the icon name
	 * @param tooltipKey a key that specifies the button's tooltip
	 * @param browseType the type of browsing that should occur when clicking at this button
	 * @return the {@link JButton} configured with the appropriate {@link BrowseType}
	 */
	private JButton newToolBarButton(String name, String iconNameKey, String tooltipKey, BrowseType browseType) {
		JButton button = new JButton(new BrowseMoveListAction(application, iconNameKey, tooltipKey, browseType));
		// we don't want our buttons to be able to receive focus
		// if we need focus, we can probably disable painting the focus border
		//button.setFocusPainted(false);
		button.setFocusable(false);
		button.setName(name);
		return button;
	}

	private void refreshView() {
		// no internal state to update, the state is read from the UI model, we just need to repaint
		tableModel.fireTableDataChanged();

		// scroll as needed so the current move is visible
		int rowIndex = gameModel.getRowForCurrentMove();
		table.scrollRectToVisible( table.getCellRect(rowIndex, 0, true) );
	}

	/**
	 * Converts a column index used by the {@link #tableModel} to an index identifier by a {@link com.lowbudget.chess.model.board.TabularMoveList}
	 * which uses a two column scheme
	 * @param tableColumn the column index to convert
	 * @return the appropriate index to use to call methods of the {@link com.lowbudget.chess.model.board.TabularMoveList}
	 */
	@SuppressWarnings("SpellCheckingInspection")
	private static int toMoveListColumn(int tableColumn) {
		// minus one to compensate for the first column where we display the move number
		return tableColumn - 1;
	}

	private class MoveListTableModel extends AbstractTableModel {
		@Override
		public String getColumnName(int index) {
			return (index == 0) ? "#" : (index == 1 ? Messages.get("app.view.move.list.label.white") : Messages.get("app.view.move.list.label.black"));
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			String result = "";
			if (!gameModel.hasBoard()) {
				return result;
			}
			Move move;
			switch (columnIndex) {
				case 0:
					result = String.valueOf(rowIndex + 1);
					break;
				case 1:
					move = gameModel.getMove(rowIndex, toMoveListColumn(columnIndex));
					result = move.isValid() ? move.toMoveString() : "...";
					break;
				case 2:
					move = gameModel.getMove(rowIndex, toMoveListColumn(columnIndex));
					result = move.isValid() ? move.toMoveString() : "";
					break;
				default:
					break;
			}
			return result;
		}

		@Override
		public int getRowCount() {
			return gameModel.getTotalMoves();
		}

		@Override
		public int getColumnCount() {
			return 3;
		}
	}

	/**
	 * <p>Custom renderer for table rows that uses different colors to draw each move cell.</p>
	 * <p>The standard color used for moves is {@link Color#black}, the current move is highlighted with
	 * {@link Color#blue} and any lists after the current move (in case we have browsed to a previous move) is
	 * highlighted with {@link Color#lightGray}</p>
	 */
	private class DefaultRenderer extends DefaultTableCellRenderer {
		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {

			super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

			if (column == 0) {
				setForeground(Color.black);
				return this;
			}

			if (!gameModel.hasBoard()) {
				return this;
			}

			int moveListColumn = toMoveListColumn(column);
			if (gameModel.isCellAfterCurrentMove(row, moveListColumn)) {
				setForeground(Color.lightGray);
			} else if (gameModel.isCellAtCurrentMove(row, moveListColumn)) {
				setForeground(Color.blue);
			} else {
				setForeground(Color.black);
			}
			return this;
		}
	}
}
