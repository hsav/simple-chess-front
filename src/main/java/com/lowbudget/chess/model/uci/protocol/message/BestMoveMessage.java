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

import java.util.Objects;

public class BestMoveMessage extends UCIMessage implements EngineMessage {

	private final Move move;
	private final Move ponderMove;

	BestMoveMessage(Move move, Move ponderMove) {
		super(Type.BEST_MOVE);
		this.move = Objects.requireNonNull(move);
		this.ponderMove = Objects.requireNonNull(ponderMove);
	}

	public Move getMove() {
		return move;
	}

	public Move getPonderMove() {
		return ponderMove;
	}

	@Override
	public void accept(EngineMessageHandler handler) {
		handler.handle(this);
	}

	@Override
	public String toCommand() {
		return Type.BEST_MOVE.command() + ' ' + move.toUCIString() +
				(ponderMove != null && ponderMove.isValid() ? ' ' + Token.PONDER + ' ' + ponderMove.toUCIString() : "");
	}
}
