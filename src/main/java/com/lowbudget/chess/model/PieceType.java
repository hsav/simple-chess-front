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

import java.util.EnumSet;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

import static com.lowbudget.chess.model.Direction.*;
import static java.util.stream.Collectors.toMap;

public enum PieceType {

	/*
	 * Note that we directly use package-private constants declared in Direction class
	 * Because those enum sets are meant to be used as read-only constants we don't allow them to escape this class
	 * as {@code EnumSet}s but as {@code Iterable}s instead. This is done to avoid writing to them accidentally
	 */
	PAWN(1, PAWN_UP_DIRECTIONS, PAWN_DOWN_DIRECTIONS, 'P'),
	KNIGHT(3, KNIGHT_DIRECTIONS, 'N'),
	BISHOP(3, DIAGONAL_DIRECTIONS, 'B'),
	ROOK(5, ROOK_DIRECTIONS, 'R'),
	QUEEN(9, ALL_DIRECTIONS, 'Q'),
	KING(100, ALL_DIRECTIONS, 'K');

	private static final Map<Character, PieceType> PIECE_TYPES_BY_SHORT_NAME = Stream.of(values()).collect(toMap(PieceType::shortName, Function.identity()));

	PieceType(int value, EnumSet<Direction> directions, char shortName) {
		this(value, directions, directions, shortName);
	}

	PieceType(int value, EnumSet<Direction> whiteDirections, EnumSet<Direction> blackDirections, char shortName) {
		this.whiteDirections = whiteDirections;
		this.blackDirections = blackDirections;
		this.value = value;
		this.shortName = shortName;
	}

	private final EnumSet<Direction> whiteDirections;
	private final EnumSet<Direction> blackDirections;

	private final int value;

	private final char shortName;

	public int value() {
		return value;
	}

	public Iterable<Direction> getMoveDirections(PlayerColor color) {
		return color.isWhite() ? whiteDirections : blackDirections;
	}

	public boolean hasDirection(PlayerColor color, Direction direction) {
		return color.isWhite() ? whiteDirections.contains(direction) : blackDirections.contains(direction);
	}

	public boolean isSlider() {
		return this == PieceType.QUEEN || this == PieceType.BISHOP || this == PieceType.ROOK;
	}

	public boolean isPromotable() {
		return this == PieceType.QUEEN || this == PieceType.KNIGHT || this == PieceType.BISHOP || this == PieceType.ROOK;
	}

	public char shortName() {
		return this.shortName;
	}

	public static Optional<PieceType> of(char shortName) {
		PieceType result = PIECE_TYPES_BY_SHORT_NAME.get(Character.toUpperCase(shortName));
		return Optional.ofNullable(result);
	}

}
