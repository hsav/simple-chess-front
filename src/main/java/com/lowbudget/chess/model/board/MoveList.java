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
import com.lowbudget.chess.model.notation.pgn.PgnNotation;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * <p>Utility class to store the moves played during a game.</p>
 * <p>This class allows us to access the moves in convenient ways not only as a list but as a tabular format that is
 * appropriate to display them in UI table.</p>
 */
public class MoveList implements TabularMoveList {

	/**
	 * Defines how the move list can be browsed. There are four possibilities defined that correspond to "next move",
	 * "previous move", "go to move list start" and "go to move list end" operations.
	 */
	public enum BrowseType {
		PREVIOUS, NEXT, FIRST, LAST
	}

	private final List<Move> moves = new ArrayList<>();

	/**
	 * Index of last current move. The value of {@code -1} is interpreted as "before the first move".
	 * It always points to the last move added unless {@link #browse(BrowseType)} is called
	 */
	private int currentMoveIndex = -1;

	/** Denotes if the first move added to the list is for black */
	private boolean firstMoveBlack = false;

	@SuppressWarnings("WeakerAccess")
	public MoveList() {}

	public Move getMove(int index) {
		return inRange(index) ? moves.get(index) : Move.NONE;
	}

	public int size() {
		return moves.size();
	}

	@Override
	public Move getMove(int row, int column) {
		int index = getMoveIndex(row, column);
		return inRange(index) ? moves.get(index) : Move.NONE;
	}

	@Override
	public int getTotalMoves() {
		int size = moves.size() + (firstMoveBlack ? 1 : 0);
		return size / 2 + (size % 2 != 0 ? 1 : 0);
	}

	@Override
	public boolean isCellAfterCurrentMove(int row, int column) {
		if (column < 0 || column > 1) {
			throw new IllegalArgumentException("Invalid column specified: " + column + ", must be either 0 or 1");
		}

		int index = getCurrentMoveIndex();
		return index == -1 || getMoveIndex(row, column) > index;
	}

	@Override
	public boolean isCellAtCurrentMove(int row, int column) {
		if (column < 0 || column > 1) {
			throw new IllegalArgumentException("Invalid column specified: " + column + ", must be either 0 or 1");
		}

		int index = getCurrentMoveIndex();
		return index != -1 && getMoveIndex(row, column) == index;
	}

	@Override
	public int getRowForCurrentMove() {
		int index = getCurrentMoveIndex();
		if (!inRange(index)) {
			return 0;
		}
		return (index + (firstMoveBlack ? 1 : 0)) / 2;
	}

	public void clear() {
		this.moves.clear();
		firstMoveBlack = false;
		this.currentMoveIndex = -1;
	}

	public void addMove(Move move) {
		if (size() == 0 && move.getPiece().isBlack()) {
			firstMoveBlack = true;
		}
		moves.add(move);
		this.currentMoveIndex++;
	}

	public Move getCurrentMove() {
		if (moves.size() == 0) {
			return Move.NONE;
		}
		int last = getCurrentMoveIndex();
		return inRange(last) ? moves.get(last) : Move.NONE;
	}

	public void removeLast() {
		int last = getValidLastIndex();
		moves.remove(last);
		this.currentMoveIndex--;
		if (moves.isEmpty()) {
			firstMoveBlack = false;
		}
	}

	public int getCurrentMoveIndex() {
		return currentMoveIndex;
	}

	public void browse(BrowseType browseType) {
		Objects.requireNonNull(browseType);
		int size = size();
		switch (browseType) {
			case PREVIOUS:
				if (currentMoveIndex > -1) {
					this.currentMoveIndex--;
				}
				break;
			case NEXT:
				if (currentMoveIndex < size - 1) {
					this.currentMoveIndex++;
				}
				break;
			case FIRST:
				this.currentMoveIndex = -1;
				break;
			case LAST:
				if (size > 0) {
					this.currentMoveIndex = size - 1;
				}
				break;
			default:
				throw new IllegalArgumentException("Cannot recognize browse type: " + browseType);
		}
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("MoveList[");
		PgnNotation.convertMoveTextToString(moves, sb);
		sb.append(']');
		return sb.toString();
	}

	private int getMoveIndex(int row, int column) {
		return (row * 2 + column) - (firstMoveBlack ? 1 : 0);
	}

	private boolean inRange(int index) {
		return index >= 0 && index < size();
	}

	private int getValidLastIndex() {
		int last = moves.size() - 1;
		if (last < 0) {
			throw new IndexOutOfBoundsException("Trying to access index: " + last + ", size: " + moves.size());
		}
		return last;
	}
}
