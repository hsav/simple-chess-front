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

package com.lowbudget.chess.model;

import com.lowbudget.chess.model.notation.SANNotation;

import java.util.EnumSet;
import java.util.Objects;

public class Move {

	public enum MoveType {
		CAPTURE,
		EN_PASSANT_CAPTURE,
		CHECK,
		CHECKMATE,
		KINGSIDE_CASTLING,
		QUEENSIDE_CASTLING,
	}

	public enum Disambiguation {
		NONE, FILE, RANK, BOTH
	}

	private static final EnumSet<MoveType> QUIET_MOVE_TYPE = EnumSet.noneOf(MoveType.class);

	public static final Move NONE = new Move(Square.NONE, Square.NONE);

	public static final Move WHITE_KING_SIDE_CASTLING = Move.of(Piece.KING_WHITE, Square.E1, Square.G1);
	public static final Move BLACK_KING_SIDE_CASTLING = Move.of(Piece.KING_BLACK, Square.E8, Square.G8);
	public static final Move WHITE_QUEEN_SIDE_CASTLING = Move.of(Piece.KING_WHITE, Square.E1, Square.C1);
	public static final Move BLACK_QUEEN_SIDE_CASTLING = Move.of(Piece.KING_BLACK, Square.E8, Square.C8);

	private final Piece piece;
	private final Square from;
	private final Square to;
	private final Piece captured;
	private final PieceType promotionType;
	private final EnumSet<MoveType> moveType;
	private final Disambiguation disambiguation;

	private Move(Square from, Square to) {
		this(null, from, to, null, null, QUIET_MOVE_TYPE);
	}

	private Move(Square from, Square to, PieceType promotionType) {
		this(null, from, to, null, promotionType, QUIET_MOVE_TYPE, Disambiguation.NONE);
	}

	private Move(Piece piece, Square from, Square to) {
		this(piece, from, to, null, null, QUIET_MOVE_TYPE);
	}

	private Move(Piece piece, Square from, Square to, PieceType promotionType) {
		this(piece, from, to, null, promotionType, QUIET_MOVE_TYPE, Disambiguation.NONE);
	}

	public Move(Piece piece, Square from, Square to, Piece captured, PieceType promotionType, EnumSet<MoveType> moveType) {
		this(piece, from, to, captured, promotionType, moveType, Disambiguation.NONE);
	}

	public Move(Piece piece, Square from, Square to, Piece captured, PieceType promotionType, EnumSet<MoveType> moveType, Disambiguation disambiguation) {
		this.piece = piece;
		this.from = from;
		this.to = to;
		this.captured = captured;
		this.promotionType = promotionType;
		this.moveType = moveType;
		this.disambiguation = disambiguation;
	}

	public Piece getPiece() {
		return piece;
	}

	public Square getTo() {
		return to;
	}

	public Square getFrom() {
		return from;
	}

	public Piece getCaptured() {
		return captured;
	}

	public PieceType getPromotionType() {
		return promotionType;
	}

	public Disambiguation getDisambiguation() {
		return disambiguation;
	}

	public boolean isEnPassantCapture() {
		return moveType.contains(MoveType.EN_PASSANT_CAPTURE);
	}

	public boolean isCheck() {
		return moveType.contains(MoveType.CHECK);
	}

	public boolean isCheckmate() {
		return moveType.contains(MoveType.CHECKMATE);
	}

	public boolean isCastling() {
		return isKingSideCastling() || isQueenSideCastling();
	}

	public boolean isKingSideCastling() {
		return moveType.contains(MoveType.KINGSIDE_CASTLING);
	}

	private boolean isQueenSideCastling() {
		return moveType.contains(MoveType.QUEENSIDE_CASTLING);
	}

	public boolean isValid() {
		return from.isValid() && to.isValid();
	}

	public static Move of(Square from, Square to) {
		return new Move(from, to, null);
	}
	public static Move of(Square from, Square to, PieceType promotion) {
		return new Move(from, to, promotion);
	}
	public static Move of(Piece piece, Square from, Square to) {
		return new Move(piece, from, to);
	}
	public static Move of(Piece piece, Square from, Square to, PieceType promotionType) {
		return new Move(piece, from, to, promotionType);
	}

	public String toMoveString() {
		return SANNotation.convertToSAN(this);
	}

	@Override
	public String toString() {
		return toMoveString();
	}

	public String toUCIString() {
		return SANNotation.convertToUCIString(this);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Move move = (Move) o;
		return from == move.from &&
				to == move.to &&
				promotionType == move.promotionType;
	}

	@Override
	public int hashCode() {
		return Objects.hash(from, to, promotionType);
	}
}
