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

package com.lowbudget.chess.front.app.impl;

import com.lowbudget.chess.front.app.Game;
import com.lowbudget.chess.front.app.model.GameModel.GameModelAdapter;
import com.lowbudget.chess.model.Move;
import com.lowbudget.chess.model.board.GameState;
import com.lowbudget.chess.model.board.IllegalMoveException;

/**
 * A listener for game model changes used by the application which in most cases will display an appropriate dialog
 * like an informative message or a warning.
 * Important note: when displaying dialogs in this listener it is important to use {@link HeadlessApplication#runInUIThread(Runnable)}
 * to allow the rest of the game model's listeners to be executed. Failure to do that will most probably result in
 * undesired behaviour
 */
public class ApplicationGameListener extends GameModelAdapter {

	private final HeadlessApplication application;

	ApplicationGameListener(HeadlessApplication application) {
		this.application = application;
	}

	@Override
	public void onFinalMoveMade(GameState gameState, Move lastMove) {
		application.runInUIThread( () -> application.showGameFinalMoveInfoMessage(gameState) );
	}

	@Override
	public void onTimeControlElapsed(String playerName) {
		application.runInUIThread( () -> application.showGameTimeElapsedWarningMessage(playerName) );
	}

	@Override
	public void onGamePaused(Game game) {
		application.runInUIThread( () -> {
			application.showGamePauseDialog();
			application.getGameModel().resumeGame();
		});
	}

	@Override
	public void onGameError(Exception exception) {
		application.runInUIThread( () -> application.showExceptionMessage(exception) );
	}

	@Override
	public void onBoardError(Exception exception) {
		if (exception instanceof IllegalMoveException) {
			application.runInUIThread( () -> application.showIllegalMoveInfoMessage((IllegalMoveException) exception) );
		} else {
			application.runInUIThread( () -> application.showExceptionMessage(exception) );
		}
	}
}
