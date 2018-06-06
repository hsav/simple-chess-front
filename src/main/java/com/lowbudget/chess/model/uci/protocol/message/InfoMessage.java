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
import com.lowbudget.chess.model.uci.protocol.EngineMessage;
import com.lowbudget.chess.model.uci.protocol.EngineMessageHandler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.lowbudget.chess.model.uci.protocol.message.UCIMessageUtils.*;
import static com.lowbudget.util.Strings.join;

@SuppressWarnings("unused")
public class InfoMessage extends UCIMessage implements EngineMessage {

	public static class Score {
		private final int centiPawns;
		private final int mate;
		private final boolean lowerBound;
		private final boolean upperBound;

		private Score(int centiPawns, int mate, boolean lowerBound, boolean upperBound) {
			this.centiPawns = centiPawns;
			this.mate = mate;
			this.lowerBound = lowerBound;
			this.upperBound = upperBound;
		}

		public int getCentiPawns() {
			return centiPawns;
		}

		public int getMate() {
			return mate;
		}

		public boolean isLowerBound() {
			return lowerBound;
		}

		public boolean isUpperBound() {
			return upperBound;
		}

		@Override
		public String toString() {
			// note: this object needs to return the same string that would be used if it was converted to a command
			// after skipping the initial "score" token
			return Token.CP + ' ' + getCentiPawns() +
					(mate >= 0 ? ' ' + Token.MATE + ' ' + getMate() : "") +
					(isLowerBound() ? ' ' + Token.LOWER_BOUND : "") +
					(isUpperBound() ? ' ' + Token.UPPER_BOUND : "");
		}

		public static Score.Builder builder() {
			return new Score.Builder();
		}
		@SuppressWarnings("UnusedReturnValue")
		public static class Builder {
			private int centiPawns = 0;
			private int mate = -1;
			private boolean lowerBound;
			private boolean upperBound;

			private Builder() {}

			public Score.Builder centiPawns(int value) {
				this.centiPawns = value;
				return this;
			}
			public Score.Builder mate(int moves) {
				this.mate = moves;
				return this;
			}
			public Score.Builder lowerBound() {
				this.lowerBound = true;
				return this;
			}
			public Score.Builder upperBound() {
				this.upperBound = true;
				return this;
			}
			public Score build() {
				return new Score(centiPawns, mate, lowerBound, upperBound);
			}
		}
	}

	public static class CurrentLine {
		private final int cpuNumber;
		private final List<Move> moves;

		private CurrentLine(int cpuNumber, List<Move> moves) {
			this.cpuNumber = cpuNumber;
			this.moves = Collections.unmodifiableList(new ArrayList<>(moves));
		}

		public int getCpuNumber() {
			return cpuNumber;
		}

		public List<Move> getMoves() {
			return moves;
		}

		@SuppressWarnings("SpellCheckingInspection")
		@Override
		public String toString() {
			// note: this object needs to return the same string that would be used if it was converted to a command
			// after skipping the initial "currline" token
			return (cpuNumber >= 0 ? String.valueOf(getCpuNumber()) + ' ' : "") + join(moves, " ");
		}

		public static CurrentLine.Builder builder() {
			return new CurrentLine.Builder();
		}

		@SuppressWarnings("UnusedReturnValue")
		public static class Builder {
			private Builder() {}

			private int cpuNumber = -1;
			private List<Move> moves;

			public CurrentLine.Builder cpuNumber(int value) {
				this.cpuNumber = value;
				return this;
			}

			public CurrentLine.Builder moves(List<Move> moves) {
				this.moves = moves;
				return this;
			}

			public CurrentLine build() {
				return new CurrentLine(cpuNumber, moves);
			}
		}

	}

	private final int depth;
	private final int selectiveDepth;
	private final long time;
	private final int nodes;
	private final List<Move> pv;
	private final int multiPV;
	private final Score score;
	private final Move currentMove;
	private final int currentMoveNumber;	// note: currentMoveNumber should actually start from 1
	private final int hashFull;
	private final int nodesPerSecond;
	private final int tableBaseHits;
	private final int shredderBaseHits;
	private final int cpuLoad;
	private final String string;
	private final List<Move> refutation;
	private final CurrentLine currentLine;

	private InfoMessage(int depth, int selectiveDepth, long time, int nodes, List<Move> pv, int multiPV, Score score, Move currentMove, int currentMoveNumber, int hashFull, int nodesPerSecond, int tableBaseHits, int shredderBaseHits, int cpuLoad, String string, List<Move> refutation, CurrentLine currentLine) {
		super(Type.INFO);
		this.depth = depth;
		this.selectiveDepth = selectiveDepth;
		this.time = time;
		this.nodes = nodes;
		this.pv = pv;
		this.multiPV = multiPV;
		this.score = score;
		this.currentMove = currentMove;
		this.currentMoveNumber = currentMoveNumber;
		this.hashFull = hashFull;
		this.nodesPerSecond = nodesPerSecond;
		this.tableBaseHits = tableBaseHits;
		this.shredderBaseHits = shredderBaseHits;
		this.cpuLoad = cpuLoad;
		this.string = string;
		this.refutation = refutation;
		this.currentLine = currentLine;
	}

	public int getDepth() {
		return depth;
	}

	public int getSelectiveDepth() {
		return selectiveDepth;
	}

	public long getTime() {
		return time;
	}

	public int getNodes() {
		return nodes;
	}

	public List<Move> getPv() {
		return pv;
	}

	public int getMultiPV() {
		return multiPV;
	}

	public Score getScore() {
		return score;
	}

	public Move getCurrentMove() {
		return currentMove;
	}

	public int getCurrentMoveNumber() {
		return currentMoveNumber;
	}

	public int getHashFull() {
		return hashFull;
	}

	public int getNodesPerSecond() {
		return nodesPerSecond;
	}

	public int getTableBaseHits() {
		return tableBaseHits;
	}

	public int getShredderBaseHits() {
		return shredderBaseHits;
	}

	public int getCpuLoad() {
		return cpuLoad;
	}

	public String getString() {
		return string;
	}

	public List<Move> getRefutation() {
		return refutation;
	}

	public CurrentLine getCurrentLine() {
		return currentLine;
	}

	@Override
	public void accept(EngineMessageHandler handler) {
		handler.handle(this);
	}

	@Override
	public String toCommand() {
		StringBuilder sb = new StringBuilder(Type.INFO.command());
		appendNumber(sb, Token.DEPTH, depth);
		appendNumber(sb, Token.SEL_DEPTH, selectiveDepth);
		appendNumber(sb, Token.TIME, time);
		appendNumber(sb, Token.NODES, nodes);
		appendMoveList(sb, Token.PV, pv);
		appendNumber(sb, Token.MULTI_PV, multiPV);
		appendObject(sb, Token.SCORE, score);
		appendMove(sb, Token.CURR_MOVE, currentMove);
		appendNumber(sb, Token.CURR_MOVE_NUMBER, currentMoveNumber);
		appendNumber(sb, Token.HASH_FULL, hashFull);
		appendNumber(sb, Token.NPS, nodesPerSecond);
		appendNumber(sb, Token.TB_HITS, tableBaseHits);
		appendNumber(sb, Token.SB_HITS, shredderBaseHits);
		appendNumber(sb, Token.CPU_LOAD, cpuLoad);
		appendObject(sb, Token.STRING, string);
		appendMoveList(sb, Token.REFUTATION, refutation);
		appendObject(sb, Token.CURR_LINE, currentLine);
		return sb.toString();
	}

	public static Builder builder() {
		return new Builder();
	}

	@SuppressWarnings("UnusedReturnValue")
	public static class Builder {
		private int depth = -1;
		private int selectiveDepth = -1;
		private long time = -1;
		private int nodes = -1;
		private List<Move> pv;
		private int multiPV = -1;
		private Score score;
		private Move currentMove;
		private int currentMoveNumber = -1;
		private int hashFull = -1;
		private int nodesPerSecond = -1;
		private int tableBaseHits = -1;
		private int shredderBaseHits = -1;
		private int cpuLoad = -1;
		private String string;
		private List<Move> refutation;
		private CurrentLine currentLine;

		private Builder() {}

		public Builder depth(int depth) {
			this.depth = depth;
			return this;
		}

		public Builder selectiveDepth(int selectiveDepth) {
			this.selectiveDepth = selectiveDepth;
			return this;
		}

		public Builder time(long time) {
			this.time = time;
			return this;
		}

		public Builder nodes(int nodes) {
			this.nodes = nodes;
			return this;
		}

		public Builder pv(List<Move> pv) {
			this.pv = pv;
			return this;
		}

		public Builder multiPV(int multiPV) {
			this.multiPV = multiPV;
			return this;
		}

		public Builder score(Score score) {
			this.score = score;
			return this;
		}

		public Builder currentMove(Move currentMove) {
			this.currentMove = currentMove;
			return this;
		}

		public Builder currentMoveNumber(int currentMoveNumber) {
			this.currentMoveNumber = currentMoveNumber;
			return this;
		}

		public Builder hashFull(int hashFull) {
			this.hashFull = hashFull;
			return this;
		}

		public Builder nodesPerSecond(int nodesPerSecond) {
			this.nodesPerSecond = nodesPerSecond;
			return this;
		}

		public Builder tableBaseHits(int tableBaseHits) {
			this.tableBaseHits = tableBaseHits;
			return this;
		}

		public Builder shredderBaseHits(int shredderBaseHits) {
			this.shredderBaseHits = shredderBaseHits;
			return this;
		}

		public Builder cpuLoad(int cpuLoad) {
			this.cpuLoad = cpuLoad;
			return this;
		}

		public Builder string(String string) {
			this.string = string;
			return this;
		}

		public Builder refutation(List<Move> refutation) {
			this.refutation = refutation;
			return this;
		}

		public Builder currentLine(CurrentLine currentLine) {
			this.currentLine = currentLine;
			return this;
		}

		public InfoMessage build() {
			return new InfoMessage(depth, selectiveDepth, time, nodes, pv, multiPV, score, currentMove,
					currentMoveNumber, hashFull, nodesPerSecond, tableBaseHits, shredderBaseHits, cpuLoad,
					string, refutation, currentLine);
		}
	}
}
