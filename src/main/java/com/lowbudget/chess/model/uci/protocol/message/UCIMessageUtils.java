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

package com.lowbudget.chess.model.uci.protocol.message;

import com.lowbudget.chess.model.Move;

import java.util.List;

/**
 * Common utility methods used by the various {@link UCIMessage} sub-classes
 */
class UCIMessageUtils {
	private UCIMessageUtils() {}

	static void appendNumber(StringBuilder sb, String token, long value) {
		if (value >= 0) {
			sb.append(' ').append(token).append(' ').append(value);
		}
	}

	static void appendMoveList(StringBuilder sb, String token, List<Move> moves) {
		if (moves != null && moves.size() > 0) {
			sb.append(' ').append(token).append(' ');
			int size = moves.size();
			for (int i = 0; i < size; i++) {
				Move move = moves.get(i);
				sb.append(move.toUCIString());
				if (i < size - 1) {
					sb.append(' ');
				}
			}
		}
	}

	@SuppressWarnings("SameParameterValue")
	static void appendMove(StringBuilder sb, String token, Move move) {
		if (move != null && move.isValid()) {
			sb.append(' ').append(token).append(' ').append(move.toUCIString());
		}
	}

	static void appendObject(StringBuilder sb, String token, Object object) {
		if (object != null) {
			sb.append(' ').append(token).append(' ').append(object.toString());
		}
	}

	static void appendBooleanIfTrue(StringBuilder sb, String token, boolean value) {
		if (value) {
			sb.append(' ').append(token);
		}
	}
}
