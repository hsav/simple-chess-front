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
import com.lowbudget.common.ListenerList;
import com.lowbudget.chess.model.*;
import com.lowbudget.chess.model.board.MoveList;
import com.lowbudget.chess.model.notation.pgn.PgnGame;
import com.lowbudget.chess.model.notation.pgn.PgnNotation.GameResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultGameModel extends AbstractBoardModel implements GameModel {

	private static final Logger log = LoggerFactory.getLogger(DefaultGameModel.class);

	/** Current game (if any) */
	private Game game;

	/** Denotes if the current game is saved */
	private boolean gameSaved;

	/** The registered game model listeners */
	private final ListenerList<GameModelListener> gameListeners = new ListenerList<>();

	@Override
	public void addGameModelListener(GameModelListener listener) {
		gameListeners.add(listener);
	}

	@Override
	public void removeGameModelListener(GameModelListener listener) {
		gameListeners.remove(listener);
	}

	@Override
	public void makeMove(Move move) {
		if (isMoveAllowed()) {
			this.gameSaved = false;
		}
		super.makeMove(move);
	}

	@Override
	public void setGame(Game game) {
		if (this.game != null && game != null) {
			throw new IllegalStateException("There is already a game set");
		}
		this.game = game;
		this.gameSaved = true;
	}

	@Override
	public boolean hasMovesInMoveList() {
		return hasBoard() && board.getMoveList().getTotalMoves() > 0;
	}

	@Override
	public boolean hasUnsavedGame() {
		return !gameSaved && hasMovesInMoveList();
	}

	@Override
	public void browseMoveList(MoveList.BrowseType browseType) {
		board.browseMoveList(browseType);
		fireBoardChanged();
	}

	@Override
	public Move getMove(int row, int column) {
		return board.getMoveList().getMove(row, column);
	}

	@Override
	public int getTotalMoves() {
		return board.getMoveList().getTotalMoves();
	}

	@Override
	public int getRowForCurrentMove() {
		return board.getMoveList().getRowForCurrentMove();
	}

	@Override
	public boolean isCellAfterCurrentMove(int row, int column) {
		return board.getMoveList().isCellAfterCurrentMove(row, column);
	}

	@Override
	public boolean isCellAtCurrentMove(int row, int column) {
		return board.getMoveList().isCellAtCurrentMove(row, column);
	}

	@Override
	public boolean hasBoard() {
		return game != null;
	}

	@Override
	public boolean hasPlayingGame() {
		return hasBoard() && !game.isStopped();
	}
	@Override
	public boolean hasStoppedGame() {
		return hasBoard() && game.isStopped();
	}

	@Override
	public void stopGame() {
		checkValidGame();
		game.stop();
		gameListeners.notifyAllListeners( listener -> listener.onGameStopped(game) );
	}

	@Override
	public void startGame() {
		checkValidGame();
		game.start();
		gameListeners.notifyAllListeners( listener -> listener.onGameStarted(game) );
	}

	@Override
	public void pauseGame() {
		checkValidGame();
		game.pause();
		gameListeners.notifyAllListeners( listener -> listener.onGamePaused(game) );
	}

	@Override
	public void resumeGame() {
		checkValidGame();
		game.resume();
		gameListeners.notifyAllListeners( listener -> listener.onGameResumed(game) );
	}

	@Override
	public void closeGame() {
		checkValidGame();
		game.close();
		setSelectedSquare(Square.NONE);
		setFenPosition(ChessConstants.EMPTY_BOARD_FEN);
		Game closedGame = this.game;
		game = null;
		gameListeners.notifyAllListeners(gameModelListener -> gameModelListener.onGameClosed(closedGame));
	}

	@Override
	public void saveGame() {
		checkValidGame();
		if (!hasUnsavedGame()) {
			throw new IllegalStateException("Game is already saved");
		}
		this.gameSaved = true;
		gameListeners.notifyAllListeners(gameModelListener -> gameModelListener.onGameSaved(game));
	}

	@Override
	public void loadGame(PgnGame pgnGame) {
		checkValidGame();
		this.gameSaved = true;
		board.fromPGN(pgnGame);
		gameListeners.notifyAllListeners(gameModelListener -> gameModelListener.onGameLoaded(game));
	}

	@Override
	public GameResult getGameResult() {
		checkValidGame();
		return game.getGameResult();
	}

	@Override
	public void setEnginesShouldAlwaysRegisterLater(boolean value) {
		if (hasBoard()) {
			game.setEnginesShouldAlwaysRegisterLater(value);
		}
	}

	@Override
	public void publishOnGameInformationChanged(String whiteName, String blackName) {
		gameListeners.notifyAllListeners(listener -> listener.onGameInformationChanged(whiteName, blackName));
	}

	@Override
	public void publishOnTimeControlChanged(long whiteRemainingTime, long blackRemainingTime) {
		gameListeners.notifyAllListeners(listener -> listener.onTimeControlChanged(whiteRemainingTime, blackRemainingTime));
	}

	@Override
	public void publishOnTimeControlElapsed(String playerName) {
		gameListeners.notifyAllListeners(listener -> listener.onTimeControlElapsed(playerName));
	}

	@Override
	public void publishOnGameError(Exception exception) {
		gameListeners.notifyAllListeners(listener -> listener.onGameError(exception));
	}

	@Override
	public Player getPlayerByColor(PlayerColor color) {
		checkValidGame();
		return game.getPlayerByColor(color);
	}

	@Override
	public void setSelectedPiece(Piece piece) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isDragAllowed(Square square) {
		if (!hasBoard() || game.isStopped()) {
			return false;
		}
		Piece piece = getPieceAtSquare(square);

		// drag only pieces of the player's color
		return piece != null && piece.hasColor(board.getPlayingColor()) && game.getCurrentPlayer().isHuman();
	}

	@Override
	protected void doDrag(Move move) {
		log.debug("Drag square ({}) is different than drop square ({}). Normal drag", move.getFrom(), move.getTo());
		checkValidGame();
		makeMove(move);
	}

	@Override
	protected boolean isMoveAllowed() {
		return hasBoard() && !game.isStopped();
	}

	@Override
	protected void doClick(Square square) {
		log.debug("This is not a drag. Toggling selection for square at: {}", square);
		setSelectedSquare(square);
	}

	private void checkValidGame() {
		if (!hasBoard()) {
			throw new IllegalStateException("Game is null!");
		}
	}
}
