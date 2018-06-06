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

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toMap;

public enum Piece {

	PAWN_WHITE(PieceType.PAWN, PlayerColor.WHITE, 'P'),
	KNIGHT_WHITE(PieceType.KNIGHT, PlayerColor.WHITE, 'N'),
	BISHOP_WHITE(PieceType.BISHOP, PlayerColor.WHITE, 'B'),
	ROOK_WHITE(PieceType.ROOK, PlayerColor.WHITE, 'R'),
	QUEEN_WHITE(PieceType.QUEEN, PlayerColor.WHITE, 'Q'),
	KING_WHITE(PieceType.KING, PlayerColor.WHITE, 'K'),
	PAWN_BLACK(PieceType.PAWN, PlayerColor.BLACK, 'p'),
	KNIGHT_BLACK(PieceType.KNIGHT, PlayerColor.BLACK, 'n'),
	BISHOP_BLACK(PieceType.BISHOP, PlayerColor.BLACK, 'b'),
	ROOK_BLACK(PieceType.ROOK, PlayerColor.BLACK, 'r'),
	QUEEN_BLACK(PieceType.QUEEN, PlayerColor.BLACK, 'q'),
	KING_BLACK(PieceType.KING, PlayerColor.BLACK, 'k');

	/**
	 * <p>Convenient mapping to be able to find the corresponding enemy piece (i.e. the piece with the same type and
	 * opposite color).</p>
	 * <p>Note: a previous version of this mapping was using an array being accessed by {@code Piece.ordinal()}. However
	 * following the advice of Effective Java 3rd edition Item 37, an {@link EnumMap} is more appropriate for this
	 * purpose. Furthermore our perft tests proved that there is really no remarkable performance benefit by using the
	 * array ourselves.</p>
	 * <p>Note that we can define the mapping directly (which piece is the opposite of which piece) however
	 * the map of maps approach does not depend on referencing specific enum constants.</p>
	 */
	private static final Map<PlayerColor, Map<PieceType, Piece>> PIECES_MAP_BY_COLOR_AND_TYPE =
			// this is somewhat complicated: we want to create a Map of Maps. The first map, maps a piece color to the
			// piece type and the second one, maps the type to a piece
			Stream.of(values()).collect(groupingBy(
					piece -> piece.color,						// the key of the map produced by the groupingBy()
					() -> new EnumMap<>(PlayerColor.class),		// the new map to be used to store values
					// the value of each key is also a map
					toMap(
						piece -> piece.type,					// the key of the inner map
						piece -> piece,							// the value of the inner map
						(x, y) -> y,							// a merge function to resolve collisions of values that map to the same key
																// (does not apply in our case and it is not used, we only specify this
																// because we need to specify the 4th parameter that creates the map)
						() -> new EnumMap<>(PieceType.class)	// the new map to be used for the inner map
					)
			));

	/**
	 * Mapping pieces by their short name (i.e. their representative character)
	 */
	private static final Map<Character, Piece> PIECES_MAP_BY_SHORT_NAME =
			Stream.of(values()).collect( toMap( piece -> piece.shortName, Function.identity() ));

	public static class PieceValueComparator implements Comparator<Piece> {
		@Override
		public int compare(Piece o1, Piece o2) {
			return o1.getType().value() - o2.getType().value();
		}
	}

	private static final List<Piece> WHITE_PIECES = Collections.unmodifiableList(
			Arrays.stream(values()).filter(Piece::isWhite).collect(Collectors.toList())
	);

	private static final List<Piece> WHITE_SLIDER_PIECES = Collections.unmodifiableList(
			WHITE_PIECES.stream().filter(Piece::isSlider).collect(Collectors.toList())
	);

	private static final List<Piece> BLACK_PIECES = Collections.unmodifiableList(
			Arrays.stream(values()).filter(Piece::isBlack).collect(Collectors.toList())
	);

	private static final List<Piece> BLACK_SLIDER_PIECES = Collections.unmodifiableList(
			BLACK_PIECES.stream().filter(Piece::isSlider).collect(Collectors.toList())
	);


	private final PieceType type;
	private final PlayerColor color;
	private final char shortName;

	Piece(PieceType type, PlayerColor color, char shortName) {
		this.type = type;
		this.color = color;
		this.shortName = shortName;
	}

	public char shortName() {
		return this.shortName;
	}

	public boolean isPawn() {
		return type == PieceType.PAWN;
	}

	public static Iterable<Piece> getPieces(PlayerColor color) {
		return color.isWhite() ? WHITE_PIECES : BLACK_PIECES;
	}

	public static Piece of(PlayerColor color, PieceType type) {
		return PIECES_MAP_BY_COLOR_AND_TYPE.get(color).get(type);
	}

	public static Piece of(char c) {
		return PIECES_MAP_BY_SHORT_NAME.get(c);
	}

	public Piece asType(PieceType type) {
		return PIECES_MAP_BY_COLOR_AND_TYPE.get(color).get(type);
	}

	public boolean isAtLastRank(Square square) {
		return isAtLastRank(this, square);
	}

	public boolean isAtFirstRank(Square square) {
		return isAtFirstRank(this, square);
	}


	public static Iterable<Piece> getSliderPieces(PlayerColor color) {
		return color.isWhite() ? WHITE_SLIDER_PIECES : BLACK_SLIDER_PIECES;
	}

	public static boolean isAtLastRank(Piece piece, Square from) {
		return piece.isWhite() ? from.rank() == ChessConstants.Rank._8 : from.rank() == ChessConstants.Rank._1;
	}

	public static boolean isPawnAtLastRank(Piece piece, Square from) {
		return piece.isPawn() && isAtLastRank(piece, from);
	}

	public static boolean isAtFirstRank(Piece piece, Square from) {
		return piece.isWhite() ? from.rank() == ChessConstants.Rank._1 : from.rank() == ChessConstants.Rank._8;
	}

	public static boolean isKingAtStartingSquare(Piece piece, Square p) {
		return PieceType.KING == piece.type
				&& (p.file() == ChessConstants.File.E)
				&& (p.rank() == (piece.isWhite() ? ChessConstants.Rank._1 : ChessConstants.Rank._8));

	}

	public static boolean isPawnAtStartingSquare(Piece piece, Square p) {
		return PieceType.PAWN == piece.type
				&& (p.rank() == (piece.isWhite() ? ChessConstants.PAWN_RANK_WHITE : ChessConstants.PAWN_RANK_BLACK));
	}

	public static boolean isPawnAtFifthRank(Piece piece, Square from) {
		return piece.isPawn() && isAtFifthRank(piece.color, from);
	}

	public static boolean isAtFifthRank(PlayerColor color, Square from) {
		return from != null && (from.rank() == (color.isWhite() ? ChessConstants.Rank._5 : ChessConstants.Rank._4));
	}

	public static boolean isPawnTwoSquareDistance(Square from, Square to) {
		return Math.abs(from.rankDistance(to)) == ChessConstants.PAWN_TWO_SQUARE_ADVANCE;
	}

	public static Square getNewEnPassantSquare(Piece piece, Square from, Square to) {
		int enPassantRank = from.rank() + (piece.isBlack() ? ChessConstants.PAWN_CAPTURE_RANK_DISTANCE_BLACK : ChessConstants.PAWN_CAPTURE_RANK_DISTANCE_WHITE);
		return Square.of(enPassantRank, to.file());
	}

	public Piece enemy() {
		return PIECES_MAP_BY_COLOR_AND_TYPE.get(color.opposite()).get(type);
	}

	public boolean isKnight() {
		return type == PieceType.KNIGHT;
	}

	@SuppressWarnings("unused")
	public boolean isBishop() {
		return type == PieceType.BISHOP;
	}

	public boolean isRook() {
		return type == PieceType.ROOK;
	}

	public boolean isQueen() {
		return type == PieceType.QUEEN;
	}

	public boolean isKing() {
		return type == PieceType.KING;
	}

	public boolean isPromotable() {
		return this.type.isPromotable();
	}

	public boolean isBlack() {
		return this.color.isBlack();
	}

	public boolean isWhite() {
		return this.color.isWhite();
	}

	public PlayerColor color() {
		return color;
	}

	public PieceType getType() {
		return type;
	}

	public Iterable<Direction> getMoveDirections() {
		return type.getMoveDirections(color);
	}

	public boolean canMoveAlong(Direction direction) {
		return type.hasDirection(color, direction);
	}

	public boolean hasColor(PlayerColor other) {
		return this.color == other;
	}

	public boolean hasOppositeColor(Piece other) {
		return this.color != other.color;
	}

	public boolean hasOppositeColor(PlayerColor other) {
		return this.color != other;
	}

	public boolean isSlider() {
		return type.isSlider();
	}

//	@Override
//	public String toString() {
//		return "[" + color.shortName() + shortName() + "]";
//	}

}
