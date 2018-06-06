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

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toCollection;

/**
 * Represents the movement directions of the chess pieces
 */
public enum Direction {

	UP					(+1,  0),
	KNIGHT_UP_RIGHT		(+2, +1),
	UP_RIGHT			(+1, +1),
	KNIGHT_RIGHT_UP		(+1, +2),
	RIGHT				(0 , +1),
	KNIGHT_RIGHT_DOWN	(-1, +2),
	DOWN_RIGHT			(-1, +1),
	KNIGHT_DOWN_RIGHT	(-2, +1),
	DOWN				(-1,  0),
	KNIGHT_DOWN_LEFT	(-2, -1),
	DOWN_LEFT			(-1, -1),
	KNIGHT_LEFT_DOWN	(-1, -2),
	LEFT				(0,  -1),
	KNIGHT_LEFT_UP		(+1, -2),
	UP_LEFT				(+1, -1),
	KNIGHT_UP_LEFT		(+2, -1),
	NONE				(0,   0);

	/**
	 * Package private constants, declared here specifically for {@link PieceType} class' benefit so these values
	 * can be referenced directly in the enum's constructor.
	 * Unfortunately the sets are modifiable and we cannot wrap them with an unmodifiable collection because we loose
	 * the benefits of using an {@link EnumSet} instead of a regular {@link java.util.Set}.
	 * Thus treat these values as constants for read-only operations
	 */
	static final EnumSet<Direction> PAWN_UP_DIRECTIONS = Stream.of(values())
			.filter(d -> d.rankIncrease == 1 && !d.isKnight() )
			.collect( toCollection( () -> EnumSet.noneOf(Direction.class) ) );

	static final EnumSet<Direction> PAWN_DOWN_DIRECTIONS = Stream.of(values())
			.filter(d -> d.rankIncrease == -1 && !d.isKnight() )
			.collect( toCollection( () -> EnumSet.noneOf(Direction.class) ) );

	static final EnumSet<Direction> KNIGHT_DIRECTIONS = Stream.of(values())
			.filter(Direction::isKnight)
			.collect( toCollection( () -> EnumSet.noneOf(Direction.class) ) );

	static final EnumSet<Direction> DIAGONAL_DIRECTIONS = Stream.of(values())
			.filter(Direction::isDiagonal)
			.collect( toCollection( () -> EnumSet.noneOf(Direction.class) ) );

	static final EnumSet<Direction> ROOK_DIRECTIONS = Stream.of(values())
			.filter(d -> d.isVertical() || d.isHorizontal())
			.collect( toCollection( () -> EnumSet.noneOf(Direction.class) ) );

	static final EnumSet<Direction> ALL_DIRECTIONS = Stream.of(values())
			.filter(Direction::isVerticalOrHorizontalOrDiagonal)
			.collect( toCollection( () -> EnumSet.noneOf(Direction.class) ) );


	/**
	 * Cache used exclusively from {@link #between(Square, Square)}. All possible 65 x 65
	 * {@code from - to} combinations are pre-computed (including {@link Square#NONE})
	 */
	private static final EnumMap<Square, EnumMap<Square, Direction>> DIRECTION_BETWEEN_CACHE = calculateAllDirections();

	/** How much should we add to the rank to get the next square along this direction */
	private final int rankIncrease;

	/** How much should we add to the file to get the next square along this direction */
	private final int fileIncrease;

	/** Denotes if this is one of the knight directions */
	private final boolean knight;

	Direction(int rankIncrease, int fileIncrease) {
		this.rankIncrease = rankIncrease;
		this.fileIncrease = fileIncrease;
		this.knight = Math.abs(rankIncrease) + Math.abs(fileIncrease) > 2;
	}

	public boolean isInvalid() {
		return this == NONE;
	}

	/**
	 * Returns the next square along this direction starting from the square specified
	 * @param from the square to start from
	 * @return the next square along this direction
	 */
	public Square next(Square from) {
		int newRank = from.rank() + rankIncrease;
		int newFile = from.file() + fileIncrease;
		return Square.of(newRank, newFile);
	}

	public boolean isReverse(Direction other) {
		return (-1 * other.rankIncrease == this.rankIncrease) && (-1 * other.fileIncrease == this.fileIncrease);
	}

	/**
	 * Traverses the direction and returns all the squares found, starting from the square specified (exclusive) and
	 * ending when it reaches the end of the board
	 * @param from the square to start from (exclusive)
	 * @return an {@link Iterable} to iterate over the returned squares so the result can be used in a
	 * {@code for-each} loop
	 */
	public Iterable<Square> openPath(final Square from) {
		return openPath(from, Square.NONE);
	}

	/**
	 * Traverses the direction and returns all the squares found, that are strictly between the squares specified
	 * @param from the square to start from (exclusive)
	 * @param to the square to end to (exclusive)
	 * @return an {@link Iterable} to iterate over the returned squares so the result can be used in a
	 * {@code for-each} loop
	 */
	public Iterable<Square> openPath(final Square from, final Square to) {
		return () -> new OpenPathIterator(this, from , to);
	}

	/**
	 * Traverses the squares that lie along this direction starting from the specified square {@code from}
	 * (exclusive) and ending to the specified square {@code to} (inclusive).
	 * @param from the square to start from (exclusive)
	 * @param to the square to end to (inclusive)
	 * @return an {@code Iterable} to iterate over all the returned squares.
	 * <p>Special cases:</p>
	 * <ul>
	 *     <li>if {@code Square.NONE} is specified for {@code from} then no squares are returned.</li>
	 *     <li>If {@code Square.NONE} is specified for {@code to} then the traversal will return all the squares
	 *     along the direction until it reaches the end of the board. In that case the call is equivalent to
	 *     {@link #openPath(Square)}</li>
	 * </ul>
	 */
	public Iterable<Square> closedPath(final Square from, final Square to) {
		return () -> new ClosedPathIterator(this, from, to);
	}

	/**
	 * Returns the direction along which both squares lie if such a direction exists
	 * @param from the starting square
	 * @param to the ending square
	 * @return the direction represented by the two squares. If no such direction exists then {@link #NONE} is returned
	 */
	public static Direction between(Square from, Square to) {
		return DIRECTION_BETWEEN_CACHE.get(from).get(to);
	}

	public boolean isVertical() {
		return this == UP || this == DOWN;
	}

	public boolean isHorizontal() {
		return this == LEFT || this == RIGHT;
	}

	public boolean isDiagonal() {
		return this == UP_LEFT || this == UP_RIGHT || this == DOWN_LEFT || this == DOWN_RIGHT;
	}

	public boolean isVerticalOrHorizontalOrDiagonal() {
		return
				// vertical
				this == UP || this == DOWN ||
				// horizontal
				this == LEFT || this == RIGHT ||
				// diagonal
				this == UP_LEFT || this == UP_RIGHT || this == DOWN_LEFT || this == DOWN_RIGHT;
	}

	public boolean isKnight() {
		return this.knight;
	}

	private static class ClosedPathIterator implements Iterator<Square> {

		final Direction direction;
		Square nextSquare;
		final Square to;

		ClosedPathIterator(Direction direction, Square from, Square to) {
			this.direction = direction;
			this.nextSquare = direction.next(from);
			this.to = to;
			if (from.equals(to)) {
				nextSquare = Square.NONE;
			}
		}

		@Override
		public boolean hasNext() {
			return nextSquare.isValid();
		}

		@Override
		public Square next() {
			Square result = nextSquare;
			nextSquare = direction.next(nextSquare);
			if (isFinished(result, nextSquare)) {
				nextSquare = Square.NONE;
			}
			return result;
		}

		boolean isFinished(Square currentNext, Square nextNext) {
			return currentNext.equals(to);
		}
	}

	private static class OpenPathIterator extends ClosedPathIterator {

		OpenPathIterator(Direction direction, Square from, Square to) {
			super(direction, from, to);
			if (nextSquare.equals(to)) {
				nextSquare = Square.NONE;
			}
		}

		@Override
		boolean isFinished(Square currentNext, Square nextNext) {
			return nextNext.equals(to);
		}
	}

	/**
	 * Static method that is used to initialize the {@link #DIRECTION_BETWEEN_CACHE} field
	 * Pre-computes all the possible results that would be returned from {@link #between(Square, Square)} method for
	 * all {@code from - to} combinations that can be specified as arguments
	 */
	private static EnumMap<Square, EnumMap<Square, Direction>> calculateAllDirections() {
		EnumMap<Square, EnumMap<Square, Direction>> cache = new EnumMap<>(Square.class);

		Square[] allSquares = Square.values();
		for (Square from : allSquares) {
			for (Square to : allSquares) {
				Direction direction = calculateDirection(from, to);
				EnumMap<Square, Direction> squareCache = cache.get(from);
				if (squareCache == null) {
					squareCache = new EnumMap<>(Square.class);
					cache.put(from, squareCache);
				}
				squareCache.put(to, direction);
			}
		}
		return cache;
	}

	private static Direction calculateDirection(Square from, Square to) {
		int ranks = to.rankDistance(from);
		int files = to.fileDistance(from);

		// horizontal
		if (ranks == 0) {
			return (files == 0) ? NONE : (files > 0 ? RIGHT : LEFT);
		}

		// vertical (note: case for ranks = 0 && files == 0 is already handled)
		if (files == 0) {
			return (ranks > 0) ? UP : DOWN;
		}

		// diagonals
		if (Math.abs(ranks) == Math.abs(files)) {
			if (ranks == files) { 	// both positive or both negative
				return (ranks > 0) ? UP_RIGHT : DOWN_LEFT;
			} else { 				// one positive the other negative
				return (ranks > 0) ? UP_LEFT : DOWN_RIGHT;
			}
		}

		// knight cases
		if (ranks == 1 && files == 2) {
			return KNIGHT_RIGHT_UP;
		} else if (ranks == 2 && files == 1) {
			return KNIGHT_UP_RIGHT;
		} else if (ranks == 1 && files == -2) {
			return KNIGHT_LEFT_UP;
		} else if (ranks == 2 && files == -1) {
			return KNIGHT_UP_LEFT;
		} else if (ranks == -1 && files == 2) {
			return KNIGHT_RIGHT_DOWN;
		} else if (ranks == -2 && files == 1) {
			return KNIGHT_DOWN_RIGHT;
		} else if (ranks == -1 && files == -2) {
			return KNIGHT_LEFT_DOWN;
		} else if (ranks == -2 && files == -1) {
			return KNIGHT_DOWN_LEFT;
		}
		return NONE;
	}
}
