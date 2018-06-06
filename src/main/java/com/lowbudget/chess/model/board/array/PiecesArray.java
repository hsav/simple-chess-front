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
 * Stores the current pieces' locations on the board.
 * The pieces are stored in an array which maps squares to pieces following the indexing scheme implied by {@link Square#index(int, int)}
 */
class PiecesArray {

	private final Piece[] pieces;
	private Square whiteKingSquare = Square.NONE;
	private Square blackKingSquare = Square.NONE;

	// stores the squares of pieces of the same type and color (i.e. all white pawns, all black rooks etc)
	private final Map<Piece, Set<Square>> pieceGroups = new EnumMap<>(Piece.class);

	PiecesArray() {
		this.pieces = new Piece[ChessConstants.TOTAL_SQUARES];
		for (Piece piece : Piece.values()) {
			pieceGroups.put(piece, EnumSet.noneOf(Square.class));
		}
	}

	@SuppressWarnings("CopyConstructorMissesField")
	PiecesArray(PiecesArray other) {
		this();
		copyFrom(other);
	}

	void clear() {
		Arrays.fill(pieces, null);
		clearSets();
	}

	Piece get(Square square) {
		return pieces[square.index()];
	}

	Piece get(int rank, int file) {
		int index = Square.index(rank, file);
		return pieces[index];
	}

	void set(Piece piece, Square square) {
		pieces[square.index()] = piece;

		Set<Square> pieces = resolveGroup(piece);
		pieces.add(square);

		if (piece.isKing()) {
			if (piece.isWhite()) {
				whiteKingSquare = square;
			} else {
				blackKingSquare = square;
			}
		}
	}

	void remove(Square square) {
		int index = square.index();
		Piece piece = pieces[index];
		pieces[index] = null;

		Set<Square> pieceGroup = resolveGroup(piece);
		pieceGroup.remove(square);
	}

	Iterable<Square> getPieceGroup(Piece piece) {
		Set<Square> pieceGroup = resolveGroup(piece);
		if (pieceGroup == null) {
			throw new IllegalStateException("No group found for piece: " + piece); // should never happen
		}
		return pieceGroup;
	}

	int getPieceGroupSize(Piece piece) {
		return resolveGroup(piece).size();
	}

	Square getWhiteKingSquare() {
		return whiteKingSquare;
	}

	Square getBlackKingSquare() {
		return blackKingSquare;
	}

	void copyFrom(PiecesArray other) {
		System.arraycopy(other.pieces, 0, pieces, 0, pieces.length);

		clearSets();

		copySets(other.pieceGroups);

		whiteKingSquare = other.whiteKingSquare;
		blackKingSquare = other.blackKingSquare;
	}

	boolean isRayEmpty(Direction direction, Square from, Square to) {
		for (Square p : direction.openPath(from, to)) {
			if (pieces[p.index()] != null) {
				return false;
			}
		}
		return true;
	}

	boolean isRayEmptyIgnoringKing(Direction direction, Square from, Square to, PlayerColor colorOfKingToIgnore) {
		Square kingIgnoredSquare = colorOfKingToIgnore.isWhite() ? whiteKingSquare : blackKingSquare;
		for (Square p : direction.openPath(from, to)) {
			if (p.equals(kingIgnoredSquare)) {
				continue;
			}
			if (pieces[p.index()] != null) {
				return false;
			}
		}
		return true;
	}

	Iterable<Square> getAllOccupiedSquares() {
		Set<Square> result = EnumSet.noneOf(Square.class);
		for (Piece piece : Piece.values()) {
			Set<Square> groupSquares = resolveGroup(piece);
			result.addAll(groupSquares);
		}
		return result;
	}

	private Set<Square> resolveGroup(Piece piece) {
		return pieceGroups.get(piece);
	}

	private void clearSets() {
		for (Set<Square> squares : pieceGroups.values()) {
			squares.clear();
		}
	}

	private void copySets(Map<Piece, Set<Square>> other) {
		for (Map.Entry<Piece, Set<Square>> pieceGroup : pieceGroups.entrySet()) {
			pieceGroup.getValue().addAll( other.get(pieceGroup.getKey()) );
		}
	}
}
