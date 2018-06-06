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

import com.lowbudget.chess.model.ChessConstants;
import com.lowbudget.chess.model.Move;
import com.lowbudget.chess.model.uci.protocol.ClientMessage;
import com.lowbudget.chess.model.uci.protocol.ClientMessageHandler;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class PositionMessage extends UCIMessage implements ClientMessage {

	private final List<Move> moves;

	private final boolean startPos;

	private final String fen;

	PositionMessage(String fen, List<Move> moves) {
		super(Type.POSITION);
		this.moves = Collections.unmodifiableList(moves);
		this.startPos = false;
		this.fen = Objects.requireNonNull(fen, "FEN string cannot be null");
	}

	PositionMessage(List<Move> moves) {
		super(Type.POSITION);
		this.moves = Collections.unmodifiableList(moves);
		this.fen = ChessConstants.INITIAL_FEN;
		this.startPos = true;
	}

	public String getFen() {
		return fen;
	}

	public List<Move> getMoves() {
		return moves;
	}

	public boolean isStartPos() {
		return startPos;
	}

	@Override
	public void accept(ClientMessageHandler handler) {
		handler.handle(this);
	}

	@Override
	public String toCommand() {
		StringBuilder sb = new StringBuilder(Type.POSITION.command());
		if (startPos) {
			sb.append(' ').append(Token.START_POS);
		} else {
			sb.append(' ').append(Token.FEN).append(' ').append(fen);
		}
		UCIMessageUtils.appendMoveList(sb, Token.MOVES, moves);
		return sb.toString();
	}
}
