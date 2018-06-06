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

import com.lowbudget.chess.model.board.Board;

import java.util.HashMap;
import java.util.Map;

class BoardHash {

	// keeps track of all the previous board positions (used to detect draws like three-fold repetition or 50 move rule)
	private final Map<String, Counter> previousPositions = new HashMap<>();

	String generateHash(Board board) {
		// we use a trivial hashing for now based on the board's fen representation
		String fen = board.toFEN();
		// since this is used to detect 3-fold repetition it is adequate to ignore the half move clock and the
		// move number of a valid FEN string to generate the hash
		int index = fen.lastIndexOf(' ');
		index = fen.lastIndexOf(' ', index - 1);
		return fen.substring(0, index);
	}

	boolean saveBoardPosition(Board board) {
		String hashKey = generateHash(board);
		boolean result = false;
		Counter count = previousPositions.computeIfAbsent(hashKey, k -> new Counter());
		int newValue = count.increase(1);
		if (newValue >= 3) {
			result = true;
		}
		return result;
	}

	void removeBoardPosition(String boardKey) {
		Counter count = previousPositions.get(boardKey);
		if (count != null) {
			int newCount = count.decrease(1);
			if (newCount <= 0) {
				previousPositions.remove(boardKey);
			}
		}
	}

	void clear() {
		previousPositions.clear();
	}
}
