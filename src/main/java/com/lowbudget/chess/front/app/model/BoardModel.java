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

import com.lowbudget.chess.model.*;
import com.lowbudget.chess.model.board.*;

import java.util.List;

/**
 * Represents the state of a chess board
 */
public interface BoardModel {

	/**
	 * Listens for changes in the board state
	 */
	interface BoardModelListener {
		/**
		 * Fires when the board has generally changed in a way other than a move (i.e. the selected square has changed)
		 * @param boardModel the board model that fired the event
		 */
		void onBoardModelChanged(BoardModel boardModel);

		/**
		 * Fires when a move has been made on the board
		 * @param previousFenPosition the board's position in FEN notation before the move was made
		 * @param lastMove the move made
		 */
		void onMoveMade(String previousFenPosition, Move lastMove);

		/**
		 * Fires when a final move has been made on the board. Final moves are the moves that result in the game to finish
		 * @param gameState the game state resulted from the last move (i.e. checkmate, three-fold repetition etc)
		 * @param lastMove the last move made that finished the game
		 */
		void onFinalMoveMade(GameState gameState, Move lastMove);

		/**
		 * Fires when a board error has occurred
		 * @param exception the exception occurred
		 */
		void onBoardError(Exception exception);
	}

	/**
	 * Empty implementation to allow us to override only the methods of interest
	 */
	class BoardModelAdapter implements BoardModelListener {
		@Override
		public void onBoardModelChanged(BoardModel boardModel) {}

		@Override
		public void onMoveMade(String previousFenPosition, Move lastMove) {}

		@Override
		public void onFinalMoveMade(GameState gameState, Move lastMove) {}

		@Override
		public void onBoardError(Exception exception) {}
	}

	void addBoardModelListener(BoardModelListener listener);

	@SuppressWarnings("unused")
	void removeBoardModelListener(BoardModelListener listener);

	Piece getPieceAtSquare(Square square);

	boolean isSquareSelected(Square square);

	Square getSelectedSquare();

	void setSelectedSquare(Square selected);

	List<Move> getLegalMovesForSelectedSquare();

	List<Square> getAttacksToSelectedSquare();

	/**
	 * Notifies the model that the current click of the mouse is the right button.
	 * This information can be used by some implementations during {@link #isDragAllowed(Square)} or {@link #isClickAllowed(Square)}
	 * @param value {@code true} if the current mouse button is the right mouse button, {@code false} otherwise
	 */
	void setMouseRightButtonClicked(boolean value);

	/**
	 * <p>Indicates if a drag operation is allowed.</p>
	 * @param square the square where the drag operation starts from.
	 * @return {@code true} if a drag is allowed, {@code false} otherwise
	 */
	boolean isDragAllowed(Square square);

	/**
	 * Indicates if mouse clicks are allowed by this model
	 * @return {@code true} if a click is allowed, {@code false} otherwise
	 * @param square the square for which we check if it is allowed to be clicked
	 */
	boolean isClickAllowed(Square square);

	boolean isBoardInSetupMode();

	boolean isPromotionMove(Square to);

	/**
	 * <p>Notifies the model that a drag operation was just started from the specified square.</p>
	 * <p>This operation will never be called if {@link #isDragAllowed(Square)} returns {@code false} for the
	 * specified square</p>
	 * @param square the square the drag operation has started from
	 */
	void onPieceDragStarted(Square square);

	/**
	 * <p>Notifies the model that a drag operation was finished at the drop square specified.</p>
	 * @param dropSquare the square where the drag operation ended
	 * @param promotionType the {@link PieceType} the dragging piece should promote to in case it is a pawn reaching
	 *                      the 8-th rank, {@code null} otherwise
	 */
	void onPieceDragFinished(Square dropSquare, PieceType promotionType);

	/**
	 * <p>Performs a click at the square specified.</p>
	 * <p>This operation will never be called if {@link #isClickAllowed(Square)} returns {@code false} for the
	 * specified square</p>
	 * @param square the square clicked with the mouse
	 */
	void onSquareClick(Square square);

	void setFenPosition(String fen);

	String getFenPosition();

	PlayerColor getPlayingColor();

	/**
	 * @return {@code true} if the model has a valid board, {@code false} otherwise
	 */
	boolean hasBoard();

	void setSelectedPiece(Piece piece);

	Square getEnPassantSquare();

	int getHalfMoveClock();

	int getMoveNumber();

	Castling getCastling();

	List<Piece> getCapturedPieces();

	void makeMove(Move move);

	Move getCurrentMove();

}
