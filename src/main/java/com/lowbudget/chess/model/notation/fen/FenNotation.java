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

package com.lowbudget.chess.model.notation.fen;

import com.lowbudget.chess.model.Castling.CastlingRight;
import com.lowbudget.chess.model.ChessConstants;
import com.lowbudget.chess.model.Piece;
import com.lowbudget.chess.model.PlayerColor;
import com.lowbudget.chess.model.Square;
import com.lowbudget.chess.model.Castling;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

import static com.lowbudget.chess.model.ChessConstants.MAX_FILES;
import static com.lowbudget.chess.model.ChessConstants.MAX_RANKS;

/**
 * Converts a {@link FenBoard} to/from FEN string
 */
public class FenNotation {
	private static final Logger log = LoggerFactory.getLogger(FenNotation.class);
	private static final boolean DEBUG = false; //log.isDebugEnabled();

	private static final char[] DIGITS = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9'};

	private FenNotation() {}

	public static String convertToString(FenBoard fenBoard) {
		StringBuilder sb = new StringBuilder();

		for (int rank = MAX_RANKS - 1; rank >= 0; rank--) {
			int empty = 0;
			for (int file = 0; file < MAX_FILES; file++) {
				Piece p = fenBoard.getPieceAt(rank, file);
				if (p == null) {
					empty++;
				} else {
					if (empty > 0) {
						sb.append(DIGITS[empty]);
						empty = 0;
					}
					sb.append(p.shortName());
				}
			}
			if (empty > 0) {
				sb.append(DIGITS[empty]);
			}
			if (rank > 0) {
				sb.append('/');
			}
		}
		sb.append(' ').append(fenBoard.getPlayingColor() == PlayerColor.WHITE ? 'w' : 'b').append(' ');

		Castling castling = fenBoard.getCastling();
		if (!castling.hasAnyCastlingRights()) {
			sb.append('-');
		} else {
			sb.append(castling.toString());
		}
		sb.append(' ');
		Square enPassant = fenBoard.getEnPassant();
		if (enPassant.isValid()) {
			sb.append(enPassant);
		} else {
			sb.append('-');
		}
		sb.append(' ');
		sb.append(fenBoard.getHalfMoveClock()).append(' ').append(fenBoard.getMoveNumber());
		return sb.toString();
	}

	public static FenBoard convertFromString(String fen) {
		Objects.requireNonNull(fen, "Fen string cannot be null!");
		if (fen.isEmpty()) {
			throw new IllegalArgumentException("FEN string is empty!");
		}

		SimpleFenBoard board = new SimpleFenBoard();

		String[] tokens = fen.split(" ");

		// first try to parse all the other information before setting up the pieces
		board.setPlayingColor( tokens.length > 1 && "b".equals(tokens[1]) ? PlayerColor.BLACK : PlayerColor.WHITE );

		// if some castling info exists enable only the ones specified
		if (tokens.length > 2 && !"-".equals(tokens[2])) {
			Castling castling = board.getCastling();
			for (char c : tokens[2].toCharArray()) {
				CastlingRight castlingRight = CastlingRight.of(c);
				if (castlingRight != null) {
					castling.addRight(castlingRight);
				}
			}
		}
		if (tokens.length > 3 && !"-".equals(tokens[3])) {
			String s = tokens[3];
			if (s.length() == 2) {
				int file = ChessConstants.File.of(s.charAt(0));
				int rank = ChessConstants.Rank.of(s.charAt(1));
				board.setEnPassant(Square.of(rank, file));
			}
		}

		if (tokens.length > 4) {
			board.setHalfMoveClock(Integer.parseInt(tokens[4]));
		}
		if (tokens.length > 5) {
			board.setMoveNumber(Integer.parseInt(tokens[5]));
		}

		String[] rows = tokens[0].split("/");

		int rank = MAX_RANKS - 1;
		int file = 0;
		for (String row : rows) {
			for (char c : row.toCharArray()) {
				if (Character.isDigit(c)) {
					file += Integer.parseInt(String.valueOf(c));
					continue;
				}
				Piece piece = Piece.of(c);
				if (piece == null) {
					throw new IllegalArgumentException("Invalid piece type: " + String.valueOf(c));
				}
				board.setPieceAt(rank, file, piece);
				file++;
			}
			rank--;
			file = 0;
		}

		if (DEBUG) {
			log.debug("Successful setup from FEN: {}", fen);
		}
		return board;
	}

	/**
	 * Utility to convert a valid FEN position to a nice ascii print of a chess board.
	 * @param fen the FEN position to convert to ascii
	 * @param tab the left padding before each row of the board
	 * @return a string that contains a printable representation of the board as ascii
	 */
	@SuppressWarnings("unused")
	public static String toPrettyAscii(String fen, String tab) {
		StringBuilder sb = new StringBuilder();

		boolean finished = false;
		int row = 8;
		char empty = ' ';
		final int files = 8;

		sb.append(tab);
		appendBorder(sb, files);
		sb.append(tab);
		appendDigitChar(sb, row).append(' ').append('|');

		for (char c : fen.toCharArray()) {
			if (finished) {
				break;
			}
			switch (c) {
				case '/':
					sb.append('\n');
					row--;
					sb.append(tab);
					appendBorder(sb, files);

					sb.append(tab);
					appendDigitChar(sb, row).append(' ').append('|');
					break;
				case ' ':
					finished = true;
					break;
				default:
					if (Character.isDigit(c)) {
						int emptySquares = c - '0';
						for (int i = 0; i < emptySquares; i++) {
							sb.append(' ').append(empty).append(' ');
							sb.append('|');
						}

					} else {
						sb.append(' ').append(c).append(' ');
						sb.append('|');
					}
					break;
			}
		}

		sb.append('\n');
		sb.append(tab);
		appendBorder(sb, files);
		sb.append(tab);
		sb.append(' ').append(' ').append(' ');
		for (int i = 0; i < files; i++) {
			sb.append(' ');
			appendLetterChar(sb, i).append(' ').append(' ');
		}
		return sb.toString();
	}

	private static StringBuilder appendDigitChar(StringBuilder sb, int digit) {
		char digitAsChar = (char) ('0' + digit);
		sb.append(digitAsChar);
		return sb;
	}

	private static StringBuilder appendLetterChar(StringBuilder sb, int letter) {
		char digitAsChar = (char) ('A' + letter);
		sb.append(digitAsChar);
		return sb;
	}

	@SuppressWarnings("SameParameterValue")
	private static void appendBorder(StringBuilder sb, int files) {
		sb.append(' ').append(' ');
		//sb.append(' ').append(' ');
		for (int i = 0; i < 4 * files - 1; i++) {
			if (i % 4 == 0) {
				sb.append('+');
			} else {
				sb.append('-');
			}
		}
		sb.append('-').append('+').append('\n');
	}
}
