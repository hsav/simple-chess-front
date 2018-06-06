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

package com.lowbudget.chess.model.board.array;

import com.lowbudget.chess.model.*;

import java.util.*;

/**
 * <p>Stores pre-generated data created once on the first class access</p>
 * <p>The data that are currently stored are the squares that are attacked by each non-slider {@link Piece} (i.e.
 * pawns, knight and king) from every square of the board.</p>
 * <p>Note that for pawns the color also matters as white pawns and black pawns move along the opposite directions of
 * each other</p>
 */
class PreGenerated {

	/** Attack squares for king and knight */
	private static final EnumMap<PieceType, EnumMap<Square, Square[]>> ATTACK_SQUARE_CACHE = createAttackSquareCache();

	/** Attack squares for white and black pawns */
	private static final EnumMap<PlayerColor, EnumMap<Square, Square[]>> PAWN_ATTACK_SQUARE_CACHE = createPawnAttackSquareCache();

	private PreGenerated() {}

	static Square[] getSquareAttacks(Piece piece, Square target) {
		if (piece.isPawn()) {
			return PAWN_ATTACK_SQUARE_CACHE.get(piece.color()).get(target);
		}
		return ATTACK_SQUARE_CACHE.get(piece.getType()).get(target);
	}

	private static List<Square> resolvePawnValidCaptureSquares(Square from, PlayerColor color) {
		List<Square> captureSquares = new ArrayList<>(2);
		Direction upRight = color.isWhite() ? Direction.UP_RIGHT : Direction.DOWN_RIGHT;
		Direction upLeft = color.isWhite() ? Direction.UP_LEFT : Direction.DOWN_LEFT;
		Square captureUpRight = upRight.next(from);
		Square captureUpLeft = upLeft.next(from);
		if (captureUpLeft.isValid()) {
			captureSquares.add(captureUpLeft);
		}
		if (captureUpRight.isValid()) {
			captureSquares.add(captureUpRight);
		}
		return captureSquares;
	}

	private static void attackMovesForKing(PieceType pieceType, Square square, Collection<Square> results) {
		Iterable<Direction> directions = pieceType.getMoveDirections(PlayerColor.WHITE); // color does not matter for king
		for (Direction direction : directions) {
			Square enemySquare = direction.next(square);
			if (enemySquare.isValid()) {
				// the square falls inside the board, so this is a square attacked by the king
				results.add(enemySquare);
			}
		}
	}

	private static void attackMovesForPawn(Piece piece, Square square, Collection<Square> results) {
		// pawn attack squares are the diagonal squares where a pawn could capture
		List<Square> captures = resolvePawnValidCaptureSquares(square, piece.color());
		results.addAll(captures);
	}

	private static void attackMovesForKnight(PieceType pieceType, Square target, Collection<Square> results) {
		Iterable<Direction> directions = pieceType.getMoveDirections(PlayerColor.WHITE); // color does not matter for knights
		for (Direction direction : directions) {
			Square enemySquare = direction.next(target);
			if (enemySquare.isValid()) {
				results.add(enemySquare);
			}
		}
	}

	private static EnumMap<PieceType, EnumMap<Square, Square[]>> createAttackSquareCache() {

		EnumMap<PieceType, EnumMap<Square, Square[]>> cache = new EnumMap<>(PieceType.class);

		PieceType[] pieceTypes = { PieceType.KING, PieceType.KNIGHT };
		long start = System.nanoTime();
		for (PieceType pieceType : pieceTypes) {
			EnumMap<Square, Square[]> pieceTypeCache = new EnumMap<>(Square.class);
			for (Square p : Square.values()) {
				Collection<Square> squares = newSquareCollection();
				if (pieceType == PieceType.KNIGHT) {
					attackMovesForKnight(pieceType, p, squares);
				} else {
					attackMovesForKing(pieceType, p, squares);
				}
				pieceTypeCache.put(p, squares.toArray(new Square[0]));
			}
			cache.put(pieceType, pieceTypeCache);
		}
		long end = System.nanoTime();
		System.out.println("Move generator cache created in " + ((end - start) / 1_000_000L) + " millis");
		return cache;
	}

	private static EnumMap<PlayerColor, EnumMap<Square, Square[]>> createPawnAttackSquareCache() {

		EnumMap<PlayerColor, EnumMap<Square, Square[]>> cache = new EnumMap<>(PlayerColor.class);

		Piece[] pieces = { Piece.PAWN_WHITE, Piece.PAWN_BLACK };
		long start = System.nanoTime();
		for (Piece piece : pieces) {
			EnumMap<Square, Square[]> pieceTypeCache = new EnumMap<>(Square.class);
			for (Square p : Square.values()) {
				Collection<Square> squares = newSquareCollection();
				attackMovesForPawn(piece, p, squares);
				pieceTypeCache.put(p, squares.toArray(new Square[0]));
			}
			cache.put(piece.color(), pieceTypeCache);
		}
		long end = System.nanoTime();
		System.out.println("Move generator pawn cache created in " + ((end - start) / 1_000_000L) + " millis");
		return cache;
	}

	private static Collection<Square> newSquareCollection() {
		//return EnumSet.noneOf(Square.class);//new ArrayList<>();
		return new ArrayList<>();
	}
}
