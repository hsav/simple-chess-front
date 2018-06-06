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

public final class ChessConstants {
	private ChessConstants() {}

	// The initial position in FEN (Forsyth-Edwards Notation, see https://en.wikipedia.org/wiki/Forsyth%E2%80%93Edwards_Notation)
	@SuppressWarnings("SpellCheckingInspection")
	public static final String INITIAL_FEN = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";
	public static final String KINGS_ONLY_FEN = "4k3/8/8/8/8/8/8/4K3 w - - 0 1";
	public static final String EMPTY_BOARD_FEN = "8/8/8/8/8/8/8/8 w - - 0 1";
	public static final int MAX_RANKS = 8;
	public static final int MAX_FILES = 8;
	public static final int TOTAL_SQUARES = MAX_RANKS * MAX_FILES;

	static final int PAWN_TWO_SQUARE_ADVANCE = 2;
	static final int PAWN_CAPTURE_RANK_DISTANCE_WHITE = +1;
	static final int PAWN_CAPTURE_RANK_DISTANCE_BLACK = -1;
	static final int PAWN_RANK_WHITE = Rank._2;
	static final int PAWN_RANK_BLACK = Rank._7;

	static final int KING_CASTLE_DISTANCE = 2;
	// these values are relative to the king square
	static final int KING_ROOK_DISTANCE = File.H - File.E;
	static final int KING_ROOK_DISTANCE_AFTER_CASTLE = -1;
	static final int QUEEN_ROOK_DISTANCE = File.A - File.E;
	static final int QUEEN_ROOK_DISTANCE_AFTER_CASTLE = +1;

	@SuppressWarnings("WeakerAccess")
	public static final class File {
		public static final int A = 0;
		public static final int B = 1;
		public static final int C = 2;
		public static final int D = 3;
		public static final int E = 4;
		public static final int F = 5;
		public static final int G = 6;
		public static final int H = 7;

		public static int of(char c) {
			return c - 'a';
		}

		public static int inverted(int file) {
			return MAX_FILES - file - 1;
		}
	}

	@SuppressWarnings("WeakerAccess")
	public static final class Rank {
		public static final int _1 = 0;
		public static final int _2 = 1;
		public static final int _3 = 2;
		public static final int _4 = 3;
		public static final int _5 = 4;
		public static final int _6 = 5;
		public static final int _7 = 6;
		public static final int _8 = 7;

		public static int of(char c) {
			return c - '0' - 1;
		}

		public static int inverted(int rank) {
			return MAX_RANKS - rank - 1;
		}
	}
}
