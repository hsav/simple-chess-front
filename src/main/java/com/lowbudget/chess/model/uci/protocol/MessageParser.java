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

package com.lowbudget.chess.model.uci.protocol;

import com.lowbudget.chess.model.ChessConstants;
import com.lowbudget.chess.model.Move;
import com.lowbudget.chess.model.PieceType;
import com.lowbudget.chess.model.Square;

import java.util.ArrayList;
import java.util.List;

/**
 * Contains common code used by both {@link ClientMessageParser} and {@link EngineMessageParser}
 */
class MessageParser {

	private MessageParser() {}

	/**
	 * Parses an optional move list from next input
	 * @param input the input available
	 * @return a list of moves parsed from input or an empty {@link List} if no move could be parsed
	 */
	static List<Move> parseMoveList(TokenInput input) {
		List<Move> moves = new ArrayList<>();
		Move move;
		while ((move = parseOptionalMove(input)).isValid()) {
			moves.add(move);
		}
		return moves;
	}

	/**
	 * Parses a move in long algebraic notation from input that is expected to exist
	 * @param input the input available
	 * @return the move parsed
	 * @throws ParserException if no valid move could be parsed
	 */
	static Move parseRequiredMove(TokenInput input) {
		String value = input.value();
		Move result = parseExistingMove(value);
		if (!result.isValid()) {
			throw new ParserException("Invalid move from value: " + value);
		}
		return result;
	}

	/**
	 * Parses an optional move from the input in long algebraic notation
	 * @param input the input available
	 * @return the move parsed or {@link Move#NONE} if no move could be parsed
	 */
	private static Move parseOptionalMove(TokenInput input) {
		String value = input.optionalValue();
		Move result = Move.NONE;
		if (value != null) {
			result = parseExistingMove(value);
			if (!result.isValid()) {
				input.backward();
			}
		}
		return result;
	}

	/**
	 * Parses a string value in long algebraic notation as a move (i.e. e7e8q).
	 * The string is expected to have 4 characters with the starting and ending squares (for a normal move) or
	 * 5 characters (in case of a pawn promotion the promotion type is the 5th character)
	 * @param value the string representing the move
	 * @return the move parsed. If the characters do not represent valid squares it is possible an invalid move to be
	 * returned
	 * @throws ParserException if an invalid promotion type is encountered
	 */
	private static Move parseExistingMove(String value) {
		// check that we have a valid move
		Square from = Square.of(ChessConstants.Rank.of(value.charAt(1)), ChessConstants.File.of(value.charAt(0)));
		Square to = Square.of(ChessConstants.Rank.of(value.charAt(3)), ChessConstants.File.of(value.charAt(2)));
		PieceType promotionType = null;
		if (value.length() > 4) {
			char promotionChar = value.charAt(4);
			promotionType = PieceType.of(promotionChar).orElseThrow(() -> new ParserException("Character: " + promotionChar + " does not correspond to a valid piece type"));
		}
		return Move.of(from, to, promotionType);
	}

	static int toInt(String value) {
		return toInt(value, 0);
	}

	static int toInt(String value, int defaultValue) {
		int result;
		try {
			result = Integer.parseInt(value);
		} catch (NumberFormatException e) {
			result = defaultValue;
		}
		return result;
	}

	static class ParserException extends RuntimeException {
		ParserException(String msg) {
			super(msg);
		}
	}
}
