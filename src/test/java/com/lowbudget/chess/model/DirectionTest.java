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

import static org.junit.Assert.*;

import org.junit.Test;

import java.util.stream.Stream;

public class DirectionTest {

	@Test
	public void testIsVertical() {
		assertTrue(Direction.UP.isVertical());
		assertTrue(Direction.DOWN.isVertical());
	}

	@Test
	public void testIsHorizontal() {
		assertTrue(Direction.LEFT.isHorizontal());
		assertTrue(Direction.RIGHT.isHorizontal());
	}

	@Test
	public void testIsDiagonal() {
		assertTrue(Direction.UP_LEFT.isDiagonal());
		assertTrue(Direction.UP_RIGHT.isDiagonal());
		assertTrue(Direction.DOWN_LEFT.isDiagonal());
		assertTrue(Direction.DOWN_RIGHT.isDiagonal());
	}

	@Test
	public void testIsKnight() {
		assertTrue(Direction.KNIGHT_UP_RIGHT.isKnight());
		assertTrue(Direction.KNIGHT_DOWN_LEFT.isKnight());
		assertTrue(Direction.KNIGHT_RIGHT_UP.isKnight());
		assertTrue(Direction.KNIGHT_LEFT_DOWN.isKnight());
		assertTrue(Direction.KNIGHT_RIGHT_DOWN.isKnight());
		assertTrue(Direction.KNIGHT_LEFT_UP.isKnight());
		assertTrue(Direction.KNIGHT_DOWN_RIGHT.isKnight());
		assertTrue(Direction.KNIGHT_UP_LEFT.isKnight());
	}

	@Test
	public void testIsReverse() {
		Direction[] directions = {
				Direction.NONE, Direction.UP, Direction.KNIGHT_UP_RIGHT, Direction.UP_RIGHT, Direction.KNIGHT_RIGHT_UP, Direction.RIGHT,
				Direction.KNIGHT_RIGHT_DOWN, Direction.DOWN_RIGHT, Direction.KNIGHT_DOWN_RIGHT, Direction.DOWN, Direction.KNIGHT_DOWN_LEFT,
				Direction.DOWN_LEFT, Direction.KNIGHT_LEFT_DOWN, Direction.LEFT, Direction.KNIGHT_LEFT_UP, Direction.UP_LEFT, Direction.KNIGHT_UP_LEFT
		};
		Direction[] reverses = {
				Direction.NONE, Direction.DOWN, Direction.KNIGHT_DOWN_LEFT, Direction.DOWN_LEFT, Direction.KNIGHT_LEFT_DOWN, Direction.LEFT,
				Direction.KNIGHT_LEFT_UP, Direction.UP_LEFT, Direction.KNIGHT_UP_LEFT, Direction.UP, Direction.KNIGHT_UP_RIGHT,
				Direction.UP_RIGHT, Direction.KNIGHT_RIGHT_UP, Direction.RIGHT, Direction.KNIGHT_RIGHT_DOWN, Direction.DOWN_RIGHT, Direction.KNIGHT_DOWN_RIGHT
		};

		for (int i = 0; i < directions.length; i++) {
			Direction d = directions[i];
			Direction r = reverses[i];
			assertTrue(d + " is not reverse of " + r, r.isReverse(d));
			assertTrue(d + " is not reverse of " + r, d.isReverse(r));
		}
	}

	@Test
	public void testBetween() {
		//vertical 
		assertEquals(Direction.UP, Direction.between(Square.D2, Square.D4));
		assertEquals(Direction.DOWN, Direction.between(Square.D4, Square.D2));

		//horizontal
		assertEquals(Direction.RIGHT, Direction.between(Square.D2, Square.F2));
		assertEquals(Direction.LEFT, Direction.between(Square.F2, Square.D2));

		//diagonal
		assertEquals(Direction.UP_RIGHT, Direction.between(Square.D2, Square.F4));
		assertEquals(Direction.UP_LEFT, Direction.between(Square.D2, Square.B4));
		assertEquals(Direction.DOWN_LEFT, Direction.between(Square.D3, Square.B1));
		assertEquals(Direction.DOWN_RIGHT, Direction.between(Square.D3, Square.F1));

		//knight
		assertEquals(Direction.KNIGHT_UP_RIGHT, Direction.between(Square.D4, Square.E6));
		assertEquals(Direction.KNIGHT_UP_LEFT, Direction.between(Square.D4, Square.C6));
		assertEquals(Direction.KNIGHT_RIGHT_UP, Direction.between(Square.D4, Square.F5));
		assertEquals(Direction.KNIGHT_RIGHT_DOWN, Direction.between(Square.D4, Square.F3));
		assertEquals(Direction.KNIGHT_DOWN_RIGHT, Direction.between(Square.D4, Square.E2));
		assertEquals(Direction.KNIGHT_DOWN_LEFT, Direction.between(Square.D4, Square.C2));
		assertEquals(Direction.KNIGHT_LEFT_UP, Direction.between(Square.D4, Square.B5));
		assertEquals(Direction.KNIGHT_LEFT_DOWN, Direction.between(Square.D4, Square.B3));

		//edge cases
		assertEquals(Direction.NONE, Direction.between(Square.D4, Square.D4));  // zero distance
		assertEquals(Direction.NONE, Direction.between(Square.D4, Square.F8));  // consecutive knight moves (e.g. d4-e6-f8)
		assertEquals(Direction.NONE, Direction.between(Square.D4, Square.H5));  // path not along a direction

		// some more illegal directions discovered during experimenting with the speed of the between() method
		assertEquals(Direction.NONE, Direction.between(Square.G8, Square.E1));  // path not along a direction
		assertEquals(Direction.DOWN, Direction.between(Square.F8, Square.F4));  // path not along a direction
	}

	@Test
	public void testOnlyNoneDirectionIsInvalid() {
		for (Direction d : Direction.values()) {
			if (d == Direction.NONE) {
				assertTrue(d.isInvalid());
			} else {
				assertFalse(d.isInvalid());
			}
		}
	}

	@Test
	public void testIsVerticalOrHorizontalOrDiagonal() {
		Direction[] valid = {
				Direction.UP, Direction.DOWN, Direction.LEFT, Direction.RIGHT,
				Direction.UP_LEFT, Direction.UP_RIGHT, Direction.DOWN_RIGHT, Direction.DOWN_LEFT,
		};

		for (Direction d : valid) {
			assertTrue(d.isVerticalOrHorizontalOrDiagonal());
		}

		Object[] invalid = Stream.of(Direction.values()).filter( d -> d.isKnight() || d == Direction.NONE).toArray();

		for (Object o : invalid) {
			Direction d = (Direction) o;
			assertFalse("Direction " + d + " should not be vertical/horizontal/diagonal", d.isVerticalOrHorizontalOrDiagonal());
		}
	}


	@Test
	public void testOpenLeftPath() {
		Square e4 = Square.E4;
		Square[] expected = {Square.F4, Square.G4, Square.H4};

		assertPath(Direction.RIGHT.openPath(e4), expected);
	}

	@Test
	public void testOpenPath() {
		Square[] expected = { Square.F4 };
		assertPath(Direction.RIGHT.openPath(Square.E4, Square.G4), expected);
	}

	@Test
	public void testOpenPathWithSquareNoneAtTheEnd() {
		Square[] expected = {Square.F4, Square.G4, Square.H4};
		// if no end is specified it behaves like the open path until it reaches the end of the board
		assertPath(Direction.RIGHT.openPath(Square.E4, Square.NONE), expected);
	}

	@Test
	public void testOpenPathWithSquareNoneAtStart() {
		// if no start is specified it does not return any squares
		assertFalse(Direction.RIGHT.openPath(Square.NONE, Square.E4).iterator().hasNext());
	}

	@Test
	public void testOpenPathWithTheSameStartAndEndReturnsEmptyIterator() {
		assertFalse(Direction.RIGHT.openPath(Square.E4, Square.E4).iterator().hasNext());
	}

	@Test
	public void testOpenPathWithZeroElement() {
		assertFalse(Direction.RIGHT.openPath(Square.E4, Square.F4).iterator().hasNext());
	}

	@Test
	public void testClosedPath() {
		Square e4 = Square.E4;
		Square g4 = Square.G4;
		Square[] expected = {Square.F4, g4};

		assertPath(Direction.RIGHT.closedPath(e4, g4), expected);
	}

	@Test
	public void testClosedPathWithSquareNoneAtTheEnd() {
		Square e4 = Square.E4;
		Square none = Square.NONE;
		Square[] expected = {Square.F4, Square.G4, Square.H4};
		// if no end is specified it behaves like the open path until it reaches the end of the board
		assertPath(Direction.RIGHT.closedPath(e4, none), expected);
	}

	@Test
	public void testClosedPathWithSquareNoneAtStart() {
		Square e4 = Square.E4;
		Square none = Square.NONE;
		// if no start is specified it does not return any squares
		assertFalse(Direction.RIGHT.closedPath(none, e4).iterator().hasNext());
	}


	@Test
	public void testClosedPathWithSingleElement() {
		Square e4 = Square.E4;
		Square f4 = Square.F4;
		Square[] expected = {f4};

		assertPath(Direction.RIGHT.closedPath(e4, f4), expected);
	}

	@Test
	public void testClosedPathWithTheSameStartAndEndReturnsEmptyIterator() {
		assertFalse(Direction.RIGHT.closedPath(Square.E4, Square.E4).iterator().hasNext());
	}

	private void assertPath(Iterable<Square> iterable, Square[] expected) {
		int index = 0;
		boolean loopExecuted = false;

		for (Square p : iterable) {
			assertEquals(expected[index++], p);
			loopExecuted = true;
		}
		assertTrue("Loop was not executed!", loopExecuted);
	}
}
