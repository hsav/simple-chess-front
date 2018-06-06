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

package com.lowbudget.chess.model.notation.fen;

import com.lowbudget.chess.model.Castling;
import com.lowbudget.chess.model.Piece;
import com.lowbudget.chess.model.PlayerColor;
import com.lowbudget.chess.model.Square;

import java.util.EnumMap;
import java.util.Map;

/**
 * A standard implementation of a {@link FenBoard}
 * This class is used from {@link FenNotation#convertFromString(String)} to return a board that was converted from a
 * {@code String}
 */
class SimpleFenBoard implements FenBoard {

	private final Map<Square, Piece> pieces = new EnumMap<>(Square.class);
	private final Castling castling = Castling.empty();

	private PlayerColor playingColor;

	private Square enPassant = Square.NONE;

	private int halfMoveClock = 0;

	private int moveNumber = 1;

	@Override
	public Iterable<Square> getAllOccupiedSquares() {
		return pieces.keySet();
	}

	@Override
	public Piece getPieceAt(int rank, int file) {
		return pieces.get(Square.of(rank, file));
	}

	@Override
	public Piece getPieceAt(Square square) {
		return pieces.get(square);
	}

	@Override
	public Castling getCastling() {
		return castling;
	}

	@Override
	public PlayerColor getPlayingColor() {
		return playingColor;
	}

	@Override
	public Square getEnPassant() {
		return enPassant;
	}

	@Override
	public int getHalfMoveClock() {
		return halfMoveClock;
	}

	@Override
	public int getMoveNumber() {
		return moveNumber;
	}

	void setPieceAt(int rank, int file, Piece piece) {
		pieces.put(Square.of(rank, file), piece);
	}

	void setPlayingColor(PlayerColor playingColor) {
		this.playingColor = playingColor;
	}

	void setEnPassant(Square enPassant) {
		this.enPassant = enPassant;
	}

	void setHalfMoveClock(int halfMoveClock) {
		this.halfMoveClock = halfMoveClock;
	}

	void setMoveNumber(int moveNumber) {
		this.moveNumber = moveNumber;
	}
}
