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

package com.lowbudget.chess.model.board;

import com.lowbudget.chess.model.Move;

/**
 * <p>Defines the minimum interface that should be supported by a chess move list to be able to display the moves in a
 * tabular two-column layout (i.e. for a UI table).</p>
 * <p>A move list records the moves played and has the ability to browse between the moves in the directions defined by
 * {@link MoveList.BrowseType}</p>
 * <p>In all the methods of this class the term "current move" always denotes the last move played unless the move list is
 * browsed to some other position in which case the current move is the move at that position. Exception to this rule is
 * when the move list is browsed at the beginning where there is no "current move" (i.e. the move list's index is
 * considered to be before the first move in that case).</p>
 */
public interface TabularMoveList {

	/**
	 * Returns the move that corresponds to a table cell
	 * @param row the cell's row
	 * @param column the cell's column (it must be either {@code 0} or {@code 1})
	 * @return the corresponding {@code Move}. Note that in case the row and/or column are not valid then
	 * {@link Move#NONE} will be returned
	 */
	Move getMove(int row, int column);

	/**
	 * <p>Returns the total number of moves in the move list.</p>
	 * <p>A move here is considered to be a row in the table which consists of two actual moves (i.e. one move by each player) </p>
	 * @return the total number of moves which is equal to the rows of a table that would be needed to display the move list
	 */
	int getTotalMoves();

	/**
	 * @return The table row's index that corresponds to the current move
	 */
	int getRowForCurrentMove();

	/**
	 * <p>Checks if the cell specified by the row and column correspond to a move after the current move.</p>
	 * <p>Note that there are two special cases: a) when the move list is browsed at the beginning then this method
	 * always return {@code true} for any cell and b) Specifying a cell that corresponds to a move after the total
	 * moves again will return {@code true} even though there is no move to display in such a cell (in such a case
	 * {@link #getMove(int, int)} would return {@link Move#NONE}).</p>
	 * @param row the cell's row
	 * @param column the cell's column (it must be either {@code 0} or {@code 1})
	 * @return {@code true} if the cell corresponds to a move strictly after the current move or {@code false} otherwise
	 */
	boolean isCellAfterCurrentMove(int row, int column);

	/**
	 * <p>Checks if the cell specified by the row and column correspond to the current move.</p>
	 * <p>Note that when the move list is browsed at the beginning, then this method always return {@code false}.</p>
	 * @param row the cell's row
	 * @param column the cell's column (it must be either {@code 0} or {@code 1})
	 * @return {@code true} if the cell corresponds to the current move or {@code false} otherwise
	 */
	boolean isCellAtCurrentMove(int row, int column);
}
