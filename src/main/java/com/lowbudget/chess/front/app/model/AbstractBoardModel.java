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

import com.lowbudget.common.ListenerList;
import com.lowbudget.chess.model.*;
import com.lowbudget.chess.model.board.*;
import com.lowbudget.chess.model.board.array.ArrayBoard;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Implements common functionality for {@link DefaultGameModel} and {@link SetupBoardModel}.
 */
public abstract class AbstractBoardModel implements BoardModel {

	private static final Logger log = LoggerFactory.getLogger(AbstractBoardModel.class);

	/** Manages the list of registered board listeners */
	private final ListenerList<BoardModelListener> listenerList = new ListenerList<>();

	/**
	 * The squares that represent legal moves for the piece standing at the selected square. This list is updated
	 * after each move
	 */
	private final List<Move> selectedSquareLegalMoves = new ArrayList<>();

	/**
	 * The squares which attack the selected square. This list is updated after each move
	 */
	private final List<Square> selectedSquareAttacks = new ArrayList<>();

	/**
	 * The currently selected square (if any). The selected square utilizes a toggle behaviour each time the user clicks at it
	 */
	private Square selectedSquare = Square.NONE;

	/**
	 * The square where the dragging operation started from. This field has a meaningful value only during a drag operation
	 */
	private Square dragSquare = Square.NONE;

	/**
	 * If the current mouse click has been performed with the right mouse button.
	 */
	/*package*/ boolean mouseRightButtonClicked;

	/**
	 * The underlying board whose state is represented by this model
	 */
	protected final Board board;

	/*package*/ AbstractBoardModel() {
		this.board = new ArrayBoard();
	}

	@Override
	public void addBoardModelListener(BoardModelListener listener) {
		this.listenerList.add(listener);
	}

	@Override
	public void removeBoardModelListener(BoardModelListener listener) {
		this.listenerList.remove(listener);
	}

	@Override
	public Piece getPieceAtSquare(Square square) {
		return hasBoard() ? board.getPieceAt(square) : null;
	}

	@Override
	public boolean isSquareSelected(Square square) {
		return square.equals(getSelectedSquare());
	}

	@Override
	public Square getSelectedSquare() {
		return selectedSquare;
	}

	@Override
	public void setSelectedSquare(Square selected) {
		// toggle selection if we are selecting the same square
		this.selectedSquare = (selectedSquare == selected ? Square.NONE : selected);
		updateHighlightedSquaresAndNotifyBoardChanged();
	}

	@Override
	public List<Move> getLegalMovesForSelectedSquare() {
		return selectedSquareLegalMoves;
	}

	@Override
	public List<Square> getAttacksToSelectedSquare() {
		return selectedSquareAttacks;
	}

	@Override
	public void setMouseRightButtonClicked(boolean value) {
		mouseRightButtonClicked = value;
	}

	@Override
	public boolean isDragAllowed(Square square) {
		return hasBoard();
	}

	@Override
	public boolean isClickAllowed(Square square) {
		return hasBoard();
	}

	@Override
	public boolean isBoardInSetupMode() {
		return board.isInSetupMode();
	}

	@Override
	public void onPieceDragStarted(Square square) {
		dragSquare = square;
	}

	@Override
	public void onPieceDragFinished(Square dropSquare, PieceType promotionType) {
		if (dragSquare.equals(dropSquare)) {
			doGuardedClick(dragSquare);
		} else {
			doGuardedDrag(Move.of(dragSquare, dropSquare, promotionType));
		}
		mouseRightButtonClicked = false;
		dragSquare = Square.NONE;
	}

	@Override
	public void onSquareClick(Square square) {
		doGuardedClick(square);
		mouseRightButtonClicked = false;
	}

	@Override
	public void setFenPosition(String fen) {
		board.fromFEN(fen);
		fireBoardChanged();
	}

	@Override
	public String getFenPosition() {
		return board.toFEN();
	}

	@Override
	public PlayerColor getPlayingColor() {
		return board.getPlayingColor();
	}

	@Override
	public Square getEnPassantSquare() {
		return board.getEnPassant();
	}

	@Override
	public int getHalfMoveClock() {
		return board.getHalfMoveClock();
	}

	@Override
	public int getMoveNumber() {
		return board.getMoveNumber();
	}

	@Override
	public Castling getCastling() {
		return board.getCastling();
	}

	@Override
	public List<Piece> getCapturedPieces() {
		return board.getCapturedPieces();
	}

	@Override
	public void makeMove(Move move) {
		if (!isMoveAllowed()) {
			return;
		}
		Square from = move.getFrom();
		Square to = move.getTo();

		// the position in fen before playing the move. In case of engine players this is the value we need to
		// send with the "position" command
		String previousFenPosition = getFenPosition();

		GameState state = board.makePlayerMove(from, to, move.getPromotionType());

		// if the square where the drag started was selected, then select the new square (where the drag ended) too
		// so the selected square is "transferred" along with the piece
		if (from.equals(selectedSquare)) {
			// this fires the board changed event
			setSelectedSquare(to);
		} else {
			// otherwise we fire the event ourselves after we update the highlighted information
			updateHighlightedSquaresAndNotifyBoardChanged();
		}

		if (state.isFinished()) {
			onFinalMove(state, board.getCurrentMove());
		} else {
			onAfterMove(previousFenPosition, board.getCurrentMove());
		}
	}

	@Override
	public Move getCurrentMove() {
		return board.getCurrentMove();
	}

	@Override
	public boolean hasBoard() {
		return true;
	}

	@Override
	public boolean isPromotionMove(Square to) {
		if (!dragSquare.isValid()) {
			return false;
		}
		Piece pawn = getPieceAtSquare(dragSquare);

		// Note the last check, it enables us to avoid the popup when the pawn move is illegal (i.e. the user drags a
		// pawn diagonally but there is no enemy to capture at the 8th rank)
		// For the check we arbitrarily use a queen promotion since legality does not depend on
		// the type we promote to
		return Piece.isPawnAtLastRank(pawn, to) && board.isLegalMove(pawn, dragSquare, to, PieceType.QUEEN);
	}

	/*package*/ void fireBoardChanged() {
		listenerList.notifyAllListeners( listener -> listener.onBoardModelChanged(this) );
	}

	protected boolean isMoveAllowed() {
		return true;
	}

	private void updateHighlightedSquaresAndNotifyBoardChanged() {
		updateLegalMovesForSelectedSquare();
		updateAttacksToSelectedSquare();
		fireBoardChanged();
	}

	/**
	 * Notifies the listeners for the move played on the board
	 * @param previousFenPosition the board's position before the move is played
	 * @param lastMove the last move played
	 */
	private void onAfterMove(String previousFenPosition, Move lastMove) {
		listenerList.notifyAllListeners( listener -> listener.onMoveMade(previousFenPosition, lastMove) );
	}

	/**
	 * Notifies the listeners for the final move that resulted in ending the game.
	 * @param state the final {@link GameState} that indicates how the game is ended (i.e. check mate, draw etc)
	 * @param lastMove the last move made that caused the game to finish
	 */
	private void onFinalMove(GameState state, Move lastMove) {
		listenerList.notifyAllListeners( listener -> listener.onFinalMoveMade(state, lastMove) );
	}

	private void doGuardedClick(Square square) {
		try {
			doClick(square);
		} catch (Exception e) {
			listenerList.notifyAllListeners(listener -> listener.onBoardError(e));
		}
	}

	private void doGuardedDrag(Move move) {
		try {
			doDrag(move);
		} catch (Exception e) {
			listenerList.notifyAllListeners(listener -> listener.onBoardError(e));
		}
	}

	protected void doClick(Square square) {
		// hook for descendants to handle the click
	}

	protected void doDrag(Move move) {
		// hook for descendants to handle the drag
	}

	private void updateLegalMovesForSelectedSquare() {
		selectedSquareLegalMoves.clear();
		Square selectedSquare = getSelectedSquare();
		if (!hasBoard() || !selectedSquare.isValid()) {
			return;
		}
		Piece piece = board.getPieceAt(selectedSquare);
		if (piece != null) {
			log.debug("Getting all legal moves for piece: {}", piece);
			selectedSquareLegalMoves.addAll( board.findLegalMoves(piece, selectedSquare) );
		}
	}

	private void updateAttacksToSelectedSquare() {
		selectedSquareAttacks.clear();
		Square selectedSquare = getSelectedSquare();

		if (!hasBoard() || !selectedSquare.isValid()) {
			return;
		}
		Piece piece = board.getPieceAt(selectedSquare);
		PlayerColor color = board.getPlayingColor();
		if (piece != null) {
			color = piece.color();
		}
		log.debug("Getting all attackers for piece: {} at square: {} by color: {}", piece, selectedSquare, color.opposite());
		selectedSquareAttacks.addAll(board.getAllSquareAttacks(selectedSquare, color.opposite()));
	}
}
