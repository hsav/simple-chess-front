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
import com.lowbudget.chess.model.board.array.ArrayBoard;
import com.lowbudget.chess.model.board.array.MoveGenerator;

import java.util.*;

import static org.junit.Assert.assertEquals;

class Perft {
	private Perft() {}

	static void assertPerft(Board board, int depth, long nodes) {
		PerftTestMonitor monitor = new PerftTestMonitor(board, depth);
		PerftResult result = perft(depth, board, monitor);
		assertEquals(nodes, result.getNodes());
	}

	static void assertPerft(Board board, int depth, PerftResult expectedResult) {
		PerftTestMonitor monitor = new PerftTestMonitor.CheckMonitor(board, depth);
		PerftResult result = perft(depth, board, monitor);
		assertEquals(expectedResult, result);
	}

	@SuppressWarnings({"SameParameterValue", "unused"})
	static void assertPerft(Board board, int depth, int debugDepth, long nodes) {
		// for debugging perft (i.e. to see divide results)
		PerftTestMonitor monitor = new PerftTestMonitor.DebugMonitor(board, depth, debugDepth);
		PerftResult result = perft(depth, board, monitor);
		assertEquals(nodes, result.getNodes());
	}

	@SuppressWarnings("WeakerAccess")
	static PerftResult perft(int depth, Board board, PerftTestMonitor monitor) {
		if (depth == 0) {
			return new PerftResult(depth, 1);
		}

		PerftResult perftResult = new PerftResult(depth, 0);

		List<Move> moves = null;
		try {
			moves = MoveGenerator.allLegalMoves(board);
			monitor.onAvailableMoves(depth, moves);

			for (Move m : moves) {

				monitor.onBeforeMove(depth, m);

				board.makeMove(m);

				Move last = board.getCurrentMove();
				perftResult.add(last);

//				if (!last.isCheckmate()) {
					PerftResult result = perft(depth - 1, board, monitor);
					perftResult.add(result);
					board.undoMove(last);
					monitor.onAfterMove(depth, last, result);
//				} else {
//					board.undoMove(last);
//				}
			}
		} catch (Exception e) {
			// re-throws the exception as a runtime one and will be re-caught at the higher depth
			monitor.onException(depth, e, moves);
		}
		return perftResult;
	}

	public static void main(String[] args) {
		long start = System.nanoTime();

		Board board = new ArrayBoard();
		board.fromFEN(FENConstants.CHESS_PROGRAMMING_POSITION_2_KIWIPETE);

		PerftTestMonitor monitor = new PerftTestMonitor(board, 5);
		perft(5, board, monitor);

		long end = System.nanoTime();
		System.out.println("Test run in " + ((end - start) / 1000000) + " ms");
	}

	static class PerftTestMonitor {

		private static final Comparator<Integer> DESCENDING = (o1, o2) -> -1 * o1.compareTo(o2);

		private final int startingDepth;
		private Exception firstException;
		private final Board board;
		private int errorDepth;

		final Map<Integer, String> messages = new TreeMap<>(DESCENDING);
		protected final Map<Integer, Move> moves = new HashMap<>();

		PerftTestMonitor(Board board, int startingDepth) {
			this.startingDepth = startingDepth;
			this.board = board;
		}

		void onAvailableMoves(int depth, List<Move> allPossibleMoves) {}

		void onBeforeMove(int depth, Move move) {
			moves.put(depth, move);
		}

		void onAfterMove(int depth, Move move, PerftResult perftResult) {}

		void onException(int depth, Exception e, List<Move> availableMoves) {
			if (firstException == null) {
				this.firstException = e;
				this.errorDepth = depth;
			}
			if (availableMoves != null) {
				messages.put(depth, availableMoves.toString());
			}

			if (depth == startingDepth) {
				print();
			}
			if (e instanceof RuntimeException) {
				throw (RuntimeException) e;
			} else {
				throw new RuntimeException(e);
			}
		}

		private void print() {
			for (Map.Entry<Integer, String> entry : messages.entrySet()) {
				int depth = entry.getKey();
				System.err.println("Depth=" + depth + ", move " + moves.get(depth) + ", all moves " + entry.getValue());
			}
			String boardString = "FEN: " + board.toFEN() + '\n' + board.getMoveList() + '\n';

			System.err.println("\n" + boardString + "Error occurred at depth: " + errorDepth + ", exception: " + firstException);
		}

		@SuppressWarnings("unused")
		static class DebugMonitor extends PerftTestMonitor {

			private final int debugDepth;

			DebugMonitor(Board board, int startingDepth, int debugDepth) {
				super(board, startingDepth);
				this.debugDepth = debugDepth;
			}

			void onAvailableMoves(int depth, List<Move> allPossibleMoves) {
				messages.putIfAbsent(depth, allPossibleMoves.toString());
			}

			@Override
			void onAfterMove(int depth, Move move, PerftResult perftResult) {
				if (depth == debugDepth) {
					System.out.println(move.toUCIString() + "\t" + perftResult.getNodes());
				}
			}

		}

		static class CheckMonitor extends PerftTestMonitor {

			CheckMonitor(Board board, int startingDepth) {
				super(board, startingDepth);
			}

			@Override
			void onAvailableMoves(int depth, List<Move> allPossibleMoves) {
//				Comparator<Move> c = Comparator.comparing(m -> m.getTo().fileName());
//				allPossibleMoves.sort(c.reversed());
//				allPossibleMoves.removeIf( move -> {
//					switch (depth) {
//						case 5:
//							return move.getTo() != Square.F3 || !move.getPiece().isPawn();
//						case 4: return move.getTo() != Square.E6 || !move.getPiece().isPawn();
//						case 3: return move.getTo() != Square.G4 || !move.getPiece().isPawn();
//						case 2: return move.getTo() != Square.H4 || !move.getPiece().isQueen();
//						default:
//							return false;
//					}
//				});
			}

			@Override
			void onAfterMove(int depth, Move move, PerftResult perftResult) {
				if (move.isCheckmate()) {
					int d = depth;
					while (--d > 0) {
						moves.remove(d);
					}

					List<Move> played = new ArrayList<>(moves.values());
					Collections.reverse(played);
					System.out.println(move.toUCIString() + "\t" + played);

				}
			}
		}
	}

	static class PerftResult {
		private final long depth;
		private long nodes;
		private long captures;
		private long ep;
	//    private long castles;
	//    private long promotions;
		private long checks;
		private long checkMates;

		PerftResult(int depth, long nodes) {
			this(depth, nodes, 0, 0, 0, 0);
		}

		PerftResult(int depth, long nodes, long captures, long ep, long checks, long checkMates) {
			this.nodes = nodes;
			this.depth = depth;
			this.captures = captures;
			this.ep = ep;
			this.checks = checks;
			this.checkMates = checkMates;
		}

		long getNodes() {
			return nodes;
		}

		@SuppressWarnings("unused")
		long getChecks() {
			return checks;
		}

		void add(PerftResult result) {
			//if (result.nodes > 0) {
			this.nodes += result.nodes;
			this.captures += result.captures;
			this.ep += result.ep;
			this.checks += result.checks;
			this.checkMates += result.checkMates;
			//}
		}

		void add(Move move) {
			if (move.isCheck()) {
				this.checks++;
			}
			if (move.isCheckmate()) {
				this.checkMates++;
			}
			if (move.getCaptured() != null) {
				this.captures++;
			}
			if (move.isEnPassantCapture()) {
				this.ep++;
			}
		}

		@Override
		public boolean equals(Object o) {

			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			PerftResult that = (PerftResult) o;
			return depth == that.depth &&
					nodes == that.nodes
					&&
					captures == that.captures &&
					ep == that.ep &&
					checks == that.checks &&
					checkMates == that.checkMates
					;
		}

		@Override
		public int hashCode() {
			return Objects.hash(depth, nodes);//, captures, ep, checks, checkMates);
		}

		@Override
		public String toString() {
			return "PerftResult{" +
					"depth=" + depth +
					", nodes=" + nodes +
					", captures=" + captures +
					", ep=" + ep +
					", checks=" + checks +
					", checkMates=" + checkMates +
					'}';
		}
	}
}
