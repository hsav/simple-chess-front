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

import com.lowbudget.chess.model.Castling.CastlingRight;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class CastlingTest {

	private Castling castling;

	@Before
	public void setup() {
		this.castling = Castling.empty();
	}

	@Test
	public void testCopyConstructor() {
		castling.addRight(CastlingRight.WhiteKingSide);
		castling.addRight(CastlingRight.BlackKingSide);

		Castling other = Castling.allOf(castling);
		assertHasRights(other, CastlingRight.WhiteKingSide, CastlingRight.BlackKingSide);
		assertNotHaveRights(other, CastlingRight.WhiteQueenSide, CastlingRight.BlackQueenSide);
	}

	@Test
	public void testIsKingSideDistance() {
		assertTrue(Castling.CastlingType.isKingSideDistance(Square.E1, Square.G1));
		assertFalse(Castling.CastlingType.isKingSideDistance(Square.E1, Square.H1));
	}

	@Test
	public void testIsQueenSideDistance() {
		assertTrue(Castling.CastlingType.isQueenSideDistance(Square.E1, Square.C1));
		assertFalse(Castling.CastlingType.isQueenSideDistance(Square.E1, Square.B1));
	}

	@Test
	public void testColor() {
		assertEquals(PlayerColor.WHITE, CastlingRight.WhiteKingSide.color());
		assertEquals(PlayerColor.WHITE, CastlingRight.WhiteQueenSide.color());
		assertEquals(PlayerColor.BLACK, CastlingRight.BlackKingSide.color());
		assertEquals(PlayerColor.BLACK, CastlingRight.BlackQueenSide.color());
	}

	@Test
	public void testDirection() {
		assertEquals(Direction.RIGHT, CastlingRight.WhiteKingSide.direction());
		assertEquals(Direction.LEFT, CastlingRight.WhiteQueenSide.direction());
		assertEquals(Direction.RIGHT, CastlingRight.BlackKingSide.direction());
		assertEquals(Direction.LEFT, CastlingRight.BlackQueenSide.direction());
	}

	@Test
	public void testSquares() {
		assertEquals(Square.H1, CastlingRight.WhiteKingSide.originalRookSquare());
		assertEquals(Square.F1, CastlingRight.WhiteKingSide.castledRookSquare());
		assertEquals(Square.G1, CastlingRight.WhiteKingSide.castledKingSquare());

		assertEquals(Square.A1, CastlingRight.WhiteQueenSide.originalRookSquare());
		assertEquals(Square.D1, CastlingRight.WhiteQueenSide.castledRookSquare());
		assertEquals(Square.C1, CastlingRight.WhiteQueenSide.castledKingSquare());

		assertEquals(Square.H8, CastlingRight.BlackKingSide.originalRookSquare());
		assertEquals(Square.F8, CastlingRight.BlackKingSide.castledRookSquare());
		assertEquals(Square.G8, CastlingRight.BlackKingSide.castledKingSquare());

		assertEquals(Square.A8, CastlingRight.BlackQueenSide.originalRookSquare());
		assertEquals(Square.D8, CastlingRight.BlackQueenSide.castledRookSquare());
		assertEquals(Square.C8, CastlingRight.BlackQueenSide.castledKingSquare());
	}

	@Test
	public void testIsKingRookAtStartSquare() {
		assertTrue(CastlingRight.isKingRookAtStartSquare(PlayerColor.WHITE, Square.H1));
		assertTrue(CastlingRight.isQueenRookAtStartSquare(PlayerColor.WHITE, Square.A1));

		assertTrue(CastlingRight.isKingRookAtStartSquare(PlayerColor.BLACK, Square.H8));
		assertTrue(CastlingRight.isQueenRookAtStartSquare(PlayerColor.BLACK, Square.A8));
	}

	@Test
	public void testCastlingRightStaticConstructorsOf() {
		assertEquals(CastlingRight.WhiteKingSide, CastlingRight.of('K'));
		assertEquals(CastlingRight.WhiteQueenSide, CastlingRight.of('Q'));
		assertEquals(CastlingRight.BlackKingSide, CastlingRight.of('k'));
		assertEquals(CastlingRight.BlackQueenSide, CastlingRight.of('q'));
		assertNull(CastlingRight.of('a'));

		assertEquals(CastlingRight.WhiteKingSide, CastlingRight.of(PlayerColor.WHITE, true));
		assertEquals(CastlingRight.WhiteQueenSide, CastlingRight.of(PlayerColor.WHITE, false));
		assertEquals(CastlingRight.BlackKingSide, CastlingRight.of(PlayerColor.BLACK, true));
		assertEquals(CastlingRight.BlackQueenSide, CastlingRight.of(PlayerColor.BLACK, false));
	}


	@Test
	public void testCastlingRightIsKingSide() {
		assertTrue(CastlingRight.WhiteKingSide.isKingSide());
		assertTrue(CastlingRight.BlackKingSide.isKingSide());
		assertFalse(CastlingRight.WhiteQueenSide.isKingSide());
		assertFalse(CastlingRight.BlackQueenSide.isKingSide());
	}

	@Test
	public void testCastlingRightIsQueenSide() {
		assertTrue(CastlingRight.WhiteQueenSide.isQueenSide());
		assertTrue(CastlingRight.BlackQueenSide.isQueenSide());
		assertFalse(CastlingRight.WhiteKingSide.isQueenSide());
		assertFalse(CastlingRight.BlackKingSide.isQueenSide());
	}

	@Test
	public void testHasAnyCastlingRights() {
		assertFalse( castling.hasAnyCastlingRights() );

		castling.addRight(CastlingRight.WhiteKingSide);

		assertTrue( castling.hasAnyCastlingRights() );
	}

	@Test
	public void testHasAnyCastlingRightsForColorWhenHasWhiteKingSide() {
		castling.addRight(CastlingRight.WhiteKingSide);
		assertTrue( castling.hasAnyCastlingRights(PlayerColor.WHITE) );
	}

	@Test
	public void testHasAnyCastlingRightsForColorWhenHasWhiteQueenSide() {
		castling.addRight(CastlingRight.WhiteQueenSide);
		assertTrue( castling.hasAnyCastlingRights(PlayerColor.WHITE) );
	}

	@Test
	public void testHasAnyCastlingRightsForColorWhenHasBlackKingSide() {
		castling.addRight(CastlingRight.BlackKingSide);
		assertTrue( castling.hasAnyCastlingRights(PlayerColor.BLACK) );
	}

	@Test
	public void testHasAnyCastlingRightsForColorWhenHasBlackQueenSide() {
		castling.addRight(CastlingRight.BlackQueenSide);
		assertTrue( castling.hasAnyCastlingRights(PlayerColor.BLACK) );
	}


	@Test
	public void testRightsIteratorWithAllRights() {
		addAllRights(castling);
		assertContainsRights(
				castling.rights(PlayerColor.WHITE),
				CastlingRight.WhiteKingSide, CastlingRight.WhiteQueenSide
		);
	}

	@Test
	public void testRightsIteratorWithQueenSideOnly() {
		addAllRights(castling);
		castling.removeRight(CastlingRight.WhiteKingSide);

		assertContainsRights(
				castling.rights(PlayerColor.WHITE),
				CastlingRight.WhiteQueenSide
		);
	}

	@Test
	public void testRightsIteratorWithKingSideOnly() {
		addAllRights(castling);
		castling.removeRight(CastlingRight.WhiteQueenSide);

		assertContainsRights(
				castling.rights(PlayerColor.WHITE),
				CastlingRight.WhiteKingSide
		);
	}

	@Test
	public void testResetWhenOtherHasAllRights() {
		Castling other = Castling.empty();
		addAllRights(other);
		assertHasAllRights(other);

		castling.reset(other);
		assertHasAllRights(castling);
	}

	@Test
	public void testResetWhenOtherHasMoreWhiteRights() {
		Castling other = Castling.empty();
		other.addRight(CastlingRight.WhiteKingSide);
		other.addRight(CastlingRight.WhiteQueenSide);

		castling.reset(other);
		assertHasRights(castling, CastlingRight.WhiteKingSide, CastlingRight.WhiteQueenSide);
	}

	@Test
	public void testResetWhenOtherHasBlackRightsAndThisHasWhiteRights() {
		castling.addRight(CastlingRight.WhiteKingSide);
		castling.addRight(CastlingRight.WhiteQueenSide);

		Castling other = Castling.empty();
		other.addRight(CastlingRight.BlackKingSide);
		other.addRight(CastlingRight.BlackQueenSide);

		castling.reset(other);
		assertHasRights(castling, CastlingRight.BlackKingSide, CastlingRight.BlackQueenSide);
	}


	@Test
	public void testCanCastleWhiteKingSideWhenHavingWhiteKingSideRight() {
		assertFalse( castling.hasRight(CastlingRight.WhiteKingSide) );
		castling.addRight(CastlingRight.WhiteKingSide);
		assertTrue( castling.hasRight(CastlingRight.WhiteKingSide) );
	}

	@Test
	public void testCanNotCastleWhiteKingSideWhenHavingBlackKingSideRight() {
		assertFalse( castling.hasRight(CastlingRight.WhiteKingSide) );
		castling.addRight(CastlingRight.BlackKingSide);
		assertFalse( castling.hasRight(CastlingRight.WhiteKingSide) );
	}


	@Test
	public void testCanCastleWhiteQueenSideWhenHavingWhiteQueenSideRight() {
		assertFalse( castling.hasRight(CastlingRight.WhiteQueenSide) );
		castling.addRight(CastlingRight.WhiteQueenSide);
		assertTrue( castling.hasRight(CastlingRight.WhiteQueenSide) );
	}

	@Test
	public void testCanNotCastleWhiteQueenSideWhenHavingBlackQueenSideRight() {
		assertFalse( castling.hasRight(CastlingRight.WhiteQueenSide) );
		castling.addRight(CastlingRight.BlackQueenSide);
		assertFalse( castling.hasRight(CastlingRight.WhiteQueenSide) );
	}


	@Test
	public void testHasRight() {
		assertHasNoRights(castling);

		castling.addRight(CastlingRight.BlackQueenSide);
		assertTrue(castling.hasRight(CastlingRight.BlackQueenSide));
	}

	@Test
	public void testRemoveRight() {
		castling.addRight(CastlingRight.WhiteKingSide);
		assertTrue(castling.hasRight(CastlingRight.WhiteKingSide));
		castling.removeRight(CastlingRight.WhiteKingSide);
		assertFalse(castling.hasRight(CastlingRight.WhiteKingSide));
	}

	@Test
	public void testKingRookMoved() {
		addAllRights(castling);
		castling.kingRookMoved(PlayerColor.WHITE);
		assertHasRights(castling, CastlingRight.WhiteQueenSide, CastlingRight.BlackKingSide, CastlingRight.BlackQueenSide);
		assertNotHaveRights(castling, CastlingRight.WhiteKingSide);
	}

	@Test
	public void testQueenRookMoved() {
		addAllRights(castling);
		castling.queenRookMoved(PlayerColor.WHITE);
		assertHasRights(castling, CastlingRight.WhiteKingSide, CastlingRight.BlackKingSide, CastlingRight.BlackQueenSide);
		assertNotHaveRights(castling, CastlingRight.WhiteQueenSide);
	}

	@Test
	public void testKingMoved() {
		addAllRights(castling);
		castling.kingMoved(PlayerColor.WHITE);
		assertHasRights(castling, CastlingRight.BlackKingSide, CastlingRight.BlackQueenSide);
		assertNotHaveRights(castling, CastlingRight.WhiteKingSide, CastlingRight.WhiteQueenSide);
	}

	@Test
	public void testToStringWhenCastlingEmpty() {
		String emptyExpected = castling.toString();
		assertEquals("", emptyExpected);
	}

	@Test
	public void testToStringWhenCastlingAllRights() {
		addAllRights(castling);
		String allExpected = castling.toString();
		assertEquals("KQkq", allExpected);
	}

	private void addAllRights(Castling castling) {
		for (CastlingRight right : CastlingRight.values()) {
			castling.addRight(right);
		}
	}

	private void assertHasRights(Castling castling, CastlingRight... values) {
		for (CastlingRight right : values) {
			assertTrue("Not found expected right: " + right, castling.hasRight(right));
		}
	}

	private void assertHasAllRights(Castling castling) {
		assertHasRights(castling, CastlingRight.values());
	}

	private void assertNotHaveRights(Castling castling, CastlingRight... values) {
		for (CastlingRight right : values) {
			assertFalse("Found not expected right: " + right, castling.hasRight(right));
		}
	}

	private void assertHasNoRights(Castling castling) {
		assertNotHaveRights(castling, CastlingRight.values());
	}

	private void assertContainsRights(Iterable<CastlingRight> rights, CastlingRight... expectedRights) {
		int index = 0;

		List<CastlingRight> actualRightsFound = new ArrayList<>();
		for (CastlingRight right : rights) {
			actualRightsFound.add(right);
		}

		assertEquals("Different sizes found, expected: " + Arrays.toString(expectedRights) + ", actual: " + actualRightsFound,
				expectedRights.length, actualRightsFound.size());

		for (CastlingRight right : actualRightsFound) {
			assertEquals(right, expectedRights[index++]);
		}
	}

}