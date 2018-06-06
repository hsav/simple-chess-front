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

import com.lowbudget.chess.model.ChessConstants.File;
import com.lowbudget.chess.model.ChessConstants.Rank;

import static com.lowbudget.chess.model.ChessConstants.MAX_FILES;
import static com.lowbudget.chess.model.ChessConstants.MAX_RANKS;

public enum Square {

	A1(Rank._1, File.A),
	B1(Rank._1, File.B),
	C1(Rank._1, File.C),
	D1(Rank._1, File.D),
	E1(Rank._1, File.E),
	F1(Rank._1, File.F),
	G1(Rank._1, File.G),
	H1(Rank._1, File.H),
	A2(Rank._2, File.A),
	B2(Rank._2, File.B),
	C2(Rank._2, File.C),
	D2(Rank._2, File.D),
	E2(Rank._2, File.E),
	F2(Rank._2, File.F),
	G2(Rank._2, File.G),
	H2(Rank._2, File.H),
	A3(Rank._3, File.A),
	B3(Rank._3, File.B),
	C3(Rank._3, File.C),
	D3(Rank._3, File.D),
	E3(Rank._3, File.E),
	F3(Rank._3, File.F),
	G3(Rank._3, File.G),
	H3(Rank._3, File.H),
	A4(Rank._4, File.A),
	B4(Rank._4, File.B),
	C4(Rank._4, File.C),
	D4(Rank._4, File.D),
	E4(Rank._4, File.E),
	F4(Rank._4, File.F),
	G4(Rank._4, File.G),
	H4(Rank._4, File.H),
	A5(Rank._5, File.A),
	B5(Rank._5, File.B),
	C5(Rank._5, File.C),
	D5(Rank._5, File.D),
	E5(Rank._5, File.E),
	F5(Rank._5, File.F),
	G5(Rank._5, File.G),
	H5(Rank._5, File.H),
	A6(Rank._6, File.A),
	B6(Rank._6, File.B),
	C6(Rank._6, File.C),
	D6(Rank._6, File.D),
	E6(Rank._6, File.E),
	F6(Rank._6, File.F),
	G6(Rank._6, File.G),
	H6(Rank._6, File.H),
	A7(Rank._7, File.A),
	B7(Rank._7, File.B),
	C7(Rank._7, File.C),
	D7(Rank._7, File.D),
	E7(Rank._7, File.E),
	F7(Rank._7, File.F),
	G7(Rank._7, File.G),
	H7(Rank._7, File.H),
	A8(Rank._8, File.A),
	B8(Rank._8, File.B),
	C8(Rank._8, File.C),
	D8(Rank._8, File.D),
	E8(Rank._8, File.E),
	F8(Rank._8, File.F),
	G8(Rank._8, File.G),
	H8(Rank._8, File.H),
	NONE(-1, -1)
	;

	/**
	 * <p>Cache to map a square's index as returned by {@link Square#index(int, int)} to a square</p>
	 * <p>Each square stores its own index thus the mapping from square to an index is trivial. This array
	 * stores the inverse transformation</p>
	 */
	private static final Square[] CACHE = Square.values();

	/**
	 * <p>Maps a rank and a file representing a square to that square's index.</p>
	 * <p>The index can be used in an array that represents the board to find a specific square. The order in which
	 * the squares are stored in the array is assumed to be bottom-up and left-right i.e. the white's point of view.</p>
	 * @param rank the rank of the square
	 * @param file the file of the square
	 * @return the index that corresponds to the square
	 */
	public static int index(int rank, int file) {
		return rank * MAX_FILES + file;
	}

	private final int rank;
	private final int file;
	private final int index;

	Square(int rank, int file) {
		this.rank = rank;
		this.file = file;
		this.index = Square.index(rank, file);
	}

	public static String rankName(int rank) {
		return String.valueOf(rank + 1);
	}

	public static String fileName(int file) {
		return String.valueOf(Character.toChars('a' + file));
	}

	public int fileDistance(Square other) {
		return file - other.file;
	}

	public boolean isValid() {
		return this != NONE;
	}

	public int rankDistance(Square other) {
		return rank - other.rank;
	}

	public int index() {
		return this.index;
	}

	public int rank() {
		return rank;
	}

	public int file() {
		return file;
	}

	@SuppressWarnings("unused")
	public String rankName() {
		return rankName(rank);
	}

	public String fileName() {
		return fileName(file);
	}

	private static boolean isValid(int rank, int file) {
		return isRankValid(rank) && isFileValid(file);
	}

	public static boolean isRankValid(int rank) {
		return rank >= 0 && rank < MAX_RANKS;
	}
	public static boolean isFileValid(int file) {
		return file >= 0 && file < MAX_FILES;
	}

	public static Square of(int index) {
		return CACHE[index];
	}

	public static Square of(int rank, int file) {
		if (isValid(rank, file)) {
			return CACHE[index(rank, file)];
		}
		return NONE;
	}

	public boolean isAdjacentTo(Square other) {
		return Math.abs(rankDistance(other)) <= 1 && Math.abs(fileDistance(other)) <= 1;
	}

	@Override
	public String toString() {
		return (this == NONE) ? "<none>" : fileName(file) + rankName(rank);
	}

	public Square flip() {
		return Square.of(Rank.inverted(rank), File.inverted(file));
	}

	public boolean isAtSameRank(Square other) {
		return rank == other.rank;
	}

	public boolean isOnLeft(Square other) {
		return file < other.file;
	}

	public boolean isOnRight(Square other) {
		return file > other.file;
	}

}