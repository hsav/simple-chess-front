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

import com.lowbudget.chess.front.app.Game;
import com.lowbudget.chess.front.app.Player;
import com.lowbudget.chess.model.PlayerColor;
import com.lowbudget.chess.model.board.MoveList;
import com.lowbudget.chess.model.board.TabularMoveList;
import com.lowbudget.chess.model.notation.pgn.PgnGame;
import com.lowbudget.chess.model.notation.pgn.PgnNotation.GameResult;

/**
 * An extension of the {@link BoardModel} interface that adds game related functionality
 * Additionally this model extends the {@link TabularMoveList} that describes a move list in a two-column tabular format
 */
public interface GameModel extends BoardModel, TabularMoveList {

	interface GameModelListener extends BoardModelListener {
		void onGameStarted(Game game);

		void onGameStopped(Game game);

		void onGamePaused(Game game);

		void onGameResumed(Game game);

		void onGameClosed(Game game);

		void onGameSaved(Game game);

		void onGameLoaded(Game game);

		void onTimeControlChanged(long whiteRemainingTime, long blackRemainingTime);

		void onTimeControlElapsed(String playerName);

		void onGameInformationChanged(String whiteName, String blackName);

		void onGameError(Exception exception);
	}

	class GameModelAdapter extends BoardModelAdapter implements GameModelListener {
		@Override
		public void onGameStarted(Game game) {}

		@Override
		public void onGameStopped(Game game) {}

		@Override
		public void onGamePaused(Game game) {}

		@Override
		public void onGameResumed(Game game) {}

		@Override
		public void onGameClosed(Game game) {}

		@Override
		public void onGameSaved(Game game) {}

		@Override
		public void onGameLoaded(Game game) {}

		@Override
		public void onTimeControlChanged(long whiteRemainingTime, long blackRemainingTime) {}

		@Override
		public void onTimeControlElapsed(String playerName) {}

		@Override
		public void onGameInformationChanged(String whiteName, String blackName) {}

		@Override
		public void onGameError(Exception exception) {}
	}

	void addGameModelListener(GameModelListener listener);

	@SuppressWarnings("unused")
	void removeGameModelListener(GameModelListener listener);

	void setGame(Game game);

	/**
	 * @return {@code true} if the model has a valid game that is not stopped, {@code false} otherwise
	 */
	boolean hasPlayingGame();

	/**
	 * @return {@code true} if the model has a valid game but it is currently stopped, {@code false} otherwise
	 */
	boolean hasStoppedGame();

	boolean hasUnsavedGame();

	void startGame();

	void pauseGame();

	void resumeGame();

	void stopGame();

	void closeGame();

	void loadGame(PgnGame pgnGame);

	void saveGame();

	GameResult getGameResult();

	void publishOnGameInformationChanged(String whiteName, String blackName);

	void publishOnTimeControlChanged(long whiteTime, long blackTime);

	void publishOnTimeControlElapsed(String playerName);

	void publishOnGameError(Exception exception);

	Player getPlayerByColor(PlayerColor color);

	boolean hasMovesInMoveList();

	void browseMoveList(MoveList.BrowseType browseType);

	void setEnginesShouldAlwaysRegisterLater(boolean value);
}
