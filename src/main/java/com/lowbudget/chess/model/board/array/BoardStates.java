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

package com.lowbudget.chess.model.board.array;

import com.lowbudget.chess.model.Castling;
import com.lowbudget.chess.model.Piece;
import com.lowbudget.chess.model.PlayerColor;
import com.lowbudget.chess.model.Square;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>Stores a list of board states that arise after each move.</p>
 * <p>Storing all the board states allow us to support browsing operations inside the move list of the board</p>
 */
class BoardStates {

	static class BoardState {
		List<Piece> capturedPieces;
		PiecesArray pieces;
		final Square enPassant;
		final Castling castling;
		final int halfMoveClock;
		final int moveNumber;
		final PlayerColor color;

		BoardState(Square enPassant, Castling castling, PlayerColor color, int halfMoveClock, int moveNumber) {
			this.color = color;
			this.enPassant = enPassant;
			this.castling = Castling.allOf(castling);
			this.halfMoveClock = halfMoveClock;
			this.moveNumber = moveNumber;
		}
	}

	private final List<BoardState> boardStates = new ArrayList<>();

	private BoardState currentBoardState;

	public void add(BoardState state) {
		boardStates.add(state);
	}

	BoardState removeLastBoardState() {
		return boardStates.remove( checkIndex(boardStates.size() - 1) );
	}

	void clear() {
		boardStates.clear();
	}

	BoardState getState(int stateIndex) {
		checkIndex(stateIndex);
		BoardState state;
		if (stateIndex == boardStates.size()) {
			state = currentBoardState;
		} else {
			state = boardStates.get(stateIndex);
		}
		return state;
	}

	void setCurrentState(BoardState state, PiecesArray pieces, List<Piece> capturedPieces) {
		currentBoardState = state;
		currentBoardState.pieces = pieces;
		currentBoardState.capturedPieces = capturedPieces;
	}

	private int checkIndex(int stateIndex) {
		if (stateIndex < 0 || stateIndex > boardStates.size()) {
			throw new IndexOutOfBoundsException("Invalid state index: " + stateIndex + ", state list size: " + boardStates.size());
		}
		return stateIndex;
	}
}
