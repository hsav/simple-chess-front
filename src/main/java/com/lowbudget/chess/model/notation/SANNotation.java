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

package com.lowbudget.chess.model.notation;

import com.lowbudget.chess.model.Move;
import com.lowbudget.chess.model.Piece;
import com.lowbudget.chess.model.PieceType;
import com.lowbudget.chess.model.Square;

import static com.lowbudget.chess.model.Square.fileName;
import static com.lowbudget.chess.model.Square.rankName;

/**
 * Contains Standard Algebraic notation related methods
 */
public class SANNotation {

	private SANNotation() {}

	/**
	 * Converts a move to a string using the Standard Algebraic Notation
	 * @param move the move to convert to string
	 * @return the converted String
	 */
	public static String convertToSAN(Move move) {
		Piece piece = move.getPiece();
		if (piece == null) {
			return convertToUCIString(move);
		}

		StringBuilder sb = new StringBuilder();

		Piece captured = move.getCaptured();
		Square from = move.getFrom();
		Square to = move.getTo();
		PieceType promotionType = move.getPromotionType();
		Move.Disambiguation disambiguation = move.getDisambiguation();

		if (piece.isPawn()) {
			if (captured != null) {
				sb.append(from.fileName())
						.append('x');
			}
			// pawns only need file disambiguation in case of a capture which is already handled
			sb.append(to);
			if (promotionType != null) {
				sb.append('=').append(promotionType.shortName());
			}
		} else if (move.isCastling()) {
			if (move.isKingSideCastling()) {
				sb.append("O-O");
			} else {
				sb.append("O-O-O");
			}
		} else {
			sb.append(piece.getType().shortName());
			if (disambiguation == Move.Disambiguation.FILE) {
				sb.append(fileName(from.file()));
			} else if (disambiguation == Move.Disambiguation.RANK) {
				sb.append(rankName(from.rank()));
			} else if (disambiguation == Move.Disambiguation.BOTH) {
				sb.append(fileName(from.file())).append(rankName(from.rank()));
			}

			if (captured != null) {
				sb.append('x');
			}
			sb.append(to);
		}
		if (move.isCheckmate()) {
			sb.append('#');
		} else if (move.isCheck()) {
			sb.append('+');
		}
		return sb.toString();
	}

	/**
	 * Converts a move to a string using the Long Algebraic Notation used in UCI protocol
	 * @param move the move to be converted to string
	 * @return the converted string
	 */
	public static String convertToUCIString(Move move) {
		StringBuilder sb = new StringBuilder();
		sb.append(move.getFrom());
		sb.append(move.getTo());
		PieceType promotionType = move.getPromotionType();
		if (promotionType != null) {
			sb.append( Character.toLowerCase(promotionType.shortName()) );
		}
		return sb.toString();
	}
}
