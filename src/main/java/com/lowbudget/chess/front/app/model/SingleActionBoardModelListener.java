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

package com.lowbudget.chess.front.app.model;

import com.lowbudget.chess.model.Move;
import com.lowbudget.chess.model.board.GameState;

/**
 * A {@link BoardModel.BoardModelListener} that handles all the events by running the same the same action.
 * This is useful for most of our board-related view components that need to perform a single action that does not
 * depend on the game state (i.e. to repaint themselves)
 */
public class SingleActionBoardModelListener implements BoardModel.BoardModelListener {

	private final Runnable action;

	public SingleActionBoardModelListener(Runnable action) {
		this.action = action;
	}

	@Override
	public void onBoardModelChanged(BoardModel boardModel) {
		action.run();
	}

	@Override
	public void onMoveMade(String previousFenPosition, Move lastMove) {
		action.run();
	}

	@Override
	public void onFinalMoveMade(GameState gameState, Move lastMove) {
		action.run();
	}

	@Override
	public void onBoardError(Exception exception) {
		// do nothing here. This listener is meant to be assigned to views which are not responsible to
		// catch or log the error
	}
}
