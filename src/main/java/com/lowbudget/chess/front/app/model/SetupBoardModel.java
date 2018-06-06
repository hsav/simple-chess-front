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

import com.lowbudget.chess.model.ChessConstants;
import com.lowbudget.chess.model.Piece;
import com.lowbudget.chess.model.Square;
import com.lowbudget.chess.model.Move;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link BoardModel} used for the setup board position screen
 */
public class SetupBoardModel extends AbstractBoardModel {

	private static final Logger log = LoggerFactory.getLogger(SetupBoardModel.class);

	private Piece setupPiece;

	public SetupBoardModel() {
		this.board.fromFEN(ChessConstants.KINGS_ONLY_FEN);
		this.board.setSetupMode(true);
	}

	@Override
	public void setSelectedPiece(Piece piece) {
		this.setupPiece = piece;
	}

	@Override
	public boolean isDragAllowed(Square square) {
		return !mouseRightButtonClicked && setupPiece == null && board.getPieceAt(square) != null;
	}

	@Override
	protected void doDrag(Move move) {
		Square from = move.getFrom();
		Square to = move.getTo();
		log.debug("Drag square ({}) is different than drop square ({}). Normal drag", from, to);
		board.movePieceInSetupMode(from, to);
		fireBoardChanged();
	}

	@Override
	protected void doClick(Square square) {
		if (mouseRightButtonClicked) {
			deletePieceAtSquare(square);
		} else {
			setPieceAtSquare(square);
		}
	}

	private void deletePieceAtSquare(Square square) {
		Piece piece = board.getPieceAt(square);
		if (piece != null) {
			log.debug("Player square clicked. Deleting piece {} at square: {}", piece, square);
			board.setPieceInSetupMode(null, square);
			fireBoardChanged();
		}
	}

	private void setPieceAtSquare(Square square) {
		// if the current cursor is "move" there is nothing to do, otherwise we will set a piece
		// to whatever piece is selected by the user
		if (setupPiece != null) {
			log.debug("Player square clicked. Square: {}, Selected piece: {}", square, setupPiece);
			board.setPieceInSetupMode(setupPiece, square);
			fireBoardChanged();
		} else {
			log.debug("Current cursor is MOVE, cannot set a piece at square {}", square);
		}
	}
}
