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

import static com.lowbudget.chess.model.ChessConstants.*;

/**
 * <p>Stores castling information for both sides.</p>
 */
public class Castling {

	public enum CastlingType {
		KingSide(File.E, KING_CASTLE_DISTANCE, KING_ROOK_DISTANCE, KING_ROOK_DISTANCE_AFTER_CASTLE),
		QueenSide(File.E, -KING_CASTLE_DISTANCE, QUEEN_ROOK_DISTANCE, QUEEN_ROOK_DISTANCE_AFTER_CASTLE);

		private final int rookFile;
		private final int castledKingFile;
		private final int castledRookFile;

		CastlingType(int kingFile, int kingDistance, int rookDistance, int rookDistanceAfterCastle) {
			this.rookFile = kingFile + rookDistance;
			this.castledKingFile = kingFile + kingDistance;
			this.castledRookFile = castledKingFile + rookDistanceAfterCastle;
		}

		public static boolean isKingSideDistance(Square from, Square to) {
			return to.fileDistance(from) == KING_CASTLE_DISTANCE;
		}

		public static boolean isQueenSideDistance(Square from, Square to) {
			return to.fileDistance(from) == -KING_CASTLE_DISTANCE;
		}
	}

	public enum CastlingRight {
		WhiteKingSide('K', PlayerColor.WHITE, CastlingType.KingSide),
		WhiteQueenSide('Q', PlayerColor.WHITE, CastlingType.QueenSide),
		BlackKingSide('k', PlayerColor.BLACK, CastlingType.KingSide),
		BlackQueenSide('q', PlayerColor.BLACK, CastlingType.QueenSide);

		private final char name;
		private final CastlingType castlingType;
		private final PlayerColor color;

		private final Square rookSquare;
		private final Square castledKingSquare;
		private final Square castledRookSquare;

		CastlingRight(char c, PlayerColor color, CastlingType castlingType) {
			this.name = c;
			this.castlingType = castlingType;
			this.color = color;
			int castleRank = color.isWhite() ? Rank._1 : Rank._8;
			this.rookSquare = Square.of(castleRank, castlingType.rookFile);
			this.castledKingSquare = Square.of(castleRank, castlingType.castledKingFile);
			this.castledRookSquare = Square.of(castleRank, castlingType.castledRookFile);
		}

		public static CastlingRight of(char c) {
			CastlingRight result;
			switch (c) {
				case 'K':
					result = WhiteKingSide;
					break;
				case 'Q':
					result = WhiteQueenSide;
					break;
				case 'k':
					result = BlackKingSide;
					break;
				case 'q':
					result = BlackQueenSide;
					break;
				default:
					result = null;
					break;
			}
			return result;
		}

		public char shortName() {
			return this.name;
		}

		public boolean isKingSide() {
			return castlingType == CastlingType.KingSide;
		}
		public boolean isQueenSide() {
			return castlingType == CastlingType.QueenSide;
		}
		public Direction direction() {
			return isKingSide() ? Direction.RIGHT : Direction.LEFT;
		}
		public PlayerColor color() {
			return this.color;
		}

		public Square castledKingSquare() {
			return this.castledKingSquare;
		}
		public Square originalRookSquare() {
			return this.rookSquare;
		}
		public Square castledRookSquare() {
			return this.castledRookSquare;
		}

		public static boolean isKingRookAtStartSquare(PlayerColor color, Square p) {
			return (color.isWhite() && WhiteKingSide.rookSquare.equals(p)) || (color.isBlack() && BlackKingSide.rookSquare.equals(p));
		}
		public static boolean isQueenRookAtStartSquare(PlayerColor color, Square p) {
			return (color.isWhite() && WhiteQueenSide.rookSquare.equals(p)) || (color.isBlack() && BlackQueenSide.rookSquare.equals(p));
		}

		public static CastlingRight kingSideOf(PlayerColor color) {
			return color.isWhite() ? WhiteKingSide : BlackKingSide;
		}
		public static CastlingRight queenSideOf(PlayerColor color) {
			return color.isWhite() ? WhiteQueenSide : BlackQueenSide;
		}

		public static CastlingRight of(PlayerColor color, boolean isKingSide) {
			return isKingSide ? kingSideOf(color) : queenSideOf(color);
		}
	}

	private final EnumMap<PlayerColor,  EnumSet<CastlingRight>> availableRights = new EnumMap<>(PlayerColor.class);

	private Castling() {
		availableRights.put(PlayerColor.WHITE, EnumSet.noneOf(CastlingRight.class));
		availableRights.put(PlayerColor.BLACK, EnumSet.noneOf(CastlingRight.class));
	}

	private Castling(Castling other) {
		availableRights.put(PlayerColor.WHITE, EnumSet.copyOf(other.availableRights.get(PlayerColor.WHITE)));
		availableRights.put(PlayerColor.BLACK, EnumSet.copyOf(other.availableRights.get(PlayerColor.BLACK)));
	}

	public static Castling empty() {
		return new Castling();
	}

	public static Castling allOf(Castling other) {
		return new Castling(other);
	}

	public boolean hasAnyCastlingRights() {
		return hasAnyCastlingRights(PlayerColor.WHITE) || hasAnyCastlingRights(PlayerColor.BLACK);
	}

	public boolean hasAnyCastlingRights(PlayerColor color) {
		return availableRights.get(color).size() > 0;
	}

	public Iterable<CastlingRight> rights(PlayerColor color) {
		return availableRights.get(color);
	}

	public void reset(Castling other) {
		EnumSet<CastlingRight> white = availableRights.get(PlayerColor.WHITE);
		EnumSet<CastlingRight> black = availableRights.get(PlayerColor.BLACK);
		white.clear();
		black.clear();
		white.addAll( other.availableRights.get(PlayerColor.WHITE) );
		black.addAll( other.availableRights.get(PlayerColor.BLACK) );
	}

	public boolean hasRight(CastlingRight castlingRight) {
		return availableRights.get(castlingRight.color).contains(castlingRight);
	}

	public void addRight(CastlingRight castlingRight) {
		availableRights.get(castlingRight.color).add(castlingRight);
	}

	public void removeRight(CastlingRight castlingRight) {
		availableRights.get(castlingRight.color).remove(castlingRight);
	}

	public void kingRookMoved(PlayerColor color) {
		removeRight(CastlingRight.kingSideOf(color));
	}

	public void queenRookMoved(PlayerColor color) {
		removeRight(CastlingRight.queenSideOf(color));
	}

	public void kingMoved(PlayerColor color) {
		availableRights.get(color).clear();
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (CastlingRight castlingRight : CastlingRight.values()) {
			if (hasRight(castlingRight)) {
				sb.append(castlingRight.shortName());
			}
		}
		return sb.toString();
	}
}
