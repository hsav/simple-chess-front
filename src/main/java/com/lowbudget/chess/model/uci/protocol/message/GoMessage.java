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
import com.lowbudget.chess.model.uci.protocol.ClientMessage;
import com.lowbudget.chess.model.uci.protocol.ClientMessageHandler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static com.lowbudget.chess.model.uci.protocol.message.UCIMessageUtils.appendBooleanIfTrue;
import static com.lowbudget.chess.model.uci.protocol.message.UCIMessageUtils.appendMoveList;
import static com.lowbudget.chess.model.uci.protocol.message.UCIMessageUtils.appendNumber;

@SuppressWarnings("unused")
public class GoMessage extends UCIMessage implements ClientMessage {
	private final List<Move> searchMoves;
	private final boolean ponder;
	private final long whiteTime;
	private final long blackTime;
	private final long whiteIncrement;
	private final long blackIncrement;
	private final int movesToGo;
	private final int depth;
	private final int nodes;
	private final int mate;
	private final int moveTime;
	private final boolean infinite;

	private GoMessage(List<Move> searchMoves, boolean ponder, long whiteTime, long blackTime, long whiteIncrement,
					  long blackIncrement, int movesToGo, int depth, int nodes, int mate, int moveTime,
					  boolean infinite) {
		super(Type.GO);

		this.searchMoves = Collections.unmodifiableList(searchMoves);
		this.ponder = ponder;
		this.whiteTime = whiteTime;
		this.blackTime = blackTime;
		this.whiteIncrement = whiteIncrement;
		this.blackIncrement = blackIncrement;
		this.movesToGo = movesToGo;
		this.depth = depth;
		this.nodes = nodes;
		this.mate = mate;
		this.moveTime = moveTime;
		this.infinite = infinite;
	}

	public List<Move> getSearchMoves() {
		return searchMoves;
	}

	public boolean isPonder() {
		return ponder;
	}

	public long getWhiteTime() {
		return whiteTime;
	}

	public long getBlackTime() {
		return blackTime;
	}

	public long getWhiteIncrement() {
		return whiteIncrement;
	}

	public long getBlackIncrement() {
		return blackIncrement;
	}

	public int getMovesToGo() {
		return movesToGo;
	}

	public int getDepth() {
		return depth;
	}

	public int getNodes() {
		return nodes;
	}

	public int getMate() {
		return mate;
	}

	public int getMoveTime() {
		return moveTime;
	}

	public boolean isInfinite() {
		return infinite;
	}

	public static Builder builder() {
		return new Builder();
	}

	@Override
	public void accept(ClientMessageHandler handler) {
		handler.handle(this);
	}

	@Override
	public String toCommand() {
		StringBuilder sb = new StringBuilder(Type.GO.command());
		appendMoveList(sb, Token.SEARCH_MOVES, searchMoves);
		appendBooleanIfTrue(sb, Token.PONDER, ponder);
		appendNumber(sb, Token.WHITE_TIME, whiteTime);
		appendNumber(sb, Token.BLACK_TIME, blackTime);
		appendNumber(sb, Token.WHITE_INC, whiteIncrement);
		appendNumber(sb, Token.BLACK_INC, blackIncrement);
		appendNumber(sb, Token.MOVES_TO_GO, movesToGo);
		appendNumber(sb, Token.DEPTH, depth);
		appendNumber(sb, Token.NODES, nodes);
		appendNumber(sb, Token.MATE, mate);
		appendNumber(sb, Token.MOVE_TIME, moveTime);
		appendBooleanIfTrue(sb, Token.INFINITE, infinite);
		return sb.toString();
	}

	@SuppressWarnings("UnusedReturnValue")
	public static class Builder {
		private final List<Move> searchMoves = new ArrayList<>();
		private boolean ponder;
		private long whiteTime = -1;
		private long blackTime = -1;
		private long whiteIncrement = -1;
		private long blackIncrement = -1;
		private int movesToGo = -1;
		private int depth = -1;
		private int nodes = -1;
		private int mate = -1;
		private int moveTime = -1;
		private boolean infinite;

		private Builder() {}

		public Builder searchMoves(List<Move> searchMoves) {
			this.searchMoves.addAll( Objects.requireNonNull(searchMoves));
			return this;
		}
		public Builder ponder(boolean value) {
			this.ponder = value;
			return this;
		}
		public Builder whiteTime(long millis) {
			this.whiteTime = millis;
			return this;
		}
		public Builder blackTime(long millis) {
			this.blackTime = millis;
			return this;
		}
		public Builder whiteIncrement(long incrementPerMoveMillis) {
			this.whiteIncrement = incrementPerMoveMillis;
			return this;
		}
		public Builder blackIncrement(long incrementPerMoveMillis) {
			this.blackIncrement = incrementPerMoveMillis;
			return this;
		}
		public Builder movesToGo(int movesToGo) {
			this.movesToGo = movesToGo;
			return this;
		}
		public Builder depth(int depth) {
			this.depth = depth;
			return this;
		}
		public Builder nodes(int nodes) {
			this.nodes = nodes;
			return this;
		}
		public Builder mate(int mate) {
			this.mate = mate;
			return this;
		}
		public Builder moveTime(int searchMillis) {
			this.moveTime = searchMillis;
			return this;
		}
		public Builder infinite(boolean value) {
			this.infinite = value;
			return this;
		}
		public GoMessage build() {
			return new GoMessage(searchMoves, ponder, whiteTime, blackTime, whiteIncrement, blackIncrement,
			  movesToGo, depth, nodes, mate, moveTime, infinite);
		}
	}
}
