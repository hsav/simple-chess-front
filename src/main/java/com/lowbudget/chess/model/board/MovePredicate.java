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

package com.lowbudget.chess.model.board;

import com.lowbudget.chess.model.Move;
import com.lowbudget.chess.model.Square;

import java.util.function.Predicate;

/**
 * An interface useful to filter moves. The additional functionality offered comparing to {@link Predicate}
 * is that we can stop the process of generating moves early depending on the purpose of the move generation (i.e.
 * do we search if a square is attacked? Or perhaps do we want to know if a move is legal?). This allows us to
 * implement functionality like "search until the first move is found", "search until a specific move is found" etc.
 */
public interface MovePredicate extends Predicate<Move> {

	boolean isSatisfied();

	static MovePredicate firstMoveOnly() {
		return new FirstMoveOnlyPredicate();
	}

	static MovePredicate allMoves() {
		return AllMovesPredicate.INSTANCE;
	}

	static MovePredicate startingFrom(Square from) {
		return new StartingSquarePredicate(from);
	}

	static MovePredicate specificMoveOnly(Move move) {
		return new SpecificMovePredicate(move);
	}

	/**
	 * Predicate that allows us to stop after the first move has been found
	 */
	class FirstMoveOnlyPredicate implements MovePredicate {

		private boolean foundFirst = false;

		private FirstMoveOnlyPredicate() {}

		@Override
		public boolean isSatisfied() {
			return foundFirst;
		}

		@Override
		public boolean test(Move move) {
			foundFirst = true;
			return true;
		}
	}

	/**
	 * Predicate that accepts all moves and never ends early. This is used if we need to find all moves
	 */
	class AllMovesPredicate implements MovePredicate {
		//private final Predicate<Move> predicate;
		private static final MovePredicate INSTANCE = new AllMovesPredicate();

		private AllMovesPredicate() {}

		@Override
		public boolean isSatisfied() {
			return false;
		}

		@Override
		public boolean test(Move move) {
			return true;
		}
	}

	/**
	 * Predicate that allows us to stop after a specific move has been found
	 */
	class SpecificMovePredicate implements MovePredicate {
		private final Move move;
		private boolean found = false;

		private SpecificMovePredicate(Move move) {
			this.move = move;
		}

		@Override
		public boolean isSatisfied() {
			return found;
		}

		@Override
		public boolean test(Move move) {
			boolean result = this.move.equals(move);
			if (result) {
				this.found = true;
			}
			return result;
		}
	}

	/**
	 * Predicate that allows to accept only moves starting from a specific square
	 */
	class StartingSquarePredicate implements MovePredicate {
		private final Square from;

		private StartingSquarePredicate(Square from) {
			this.from = from;
		}

		@Override
		public boolean test(Move move) {
			return from.equals(move.getFrom()) /*&& super.test(move)*/;
		}

		@Override
		public boolean isSatisfied() {
			return false;
		}
	}
}
