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

import com.lowbudget.chess.model.uci.protocol.ClientMessage;
import com.lowbudget.chess.model.uci.protocol.ClientMessageHandler;

public class SetOptionMessage extends UCIMessage implements ClientMessage {

	private final String name;

	private final String value;

	SetOptionMessage(String name, String value) {
		super(Type.SET_OPTION);
		this.name = name;
		this.value = value;
	}

	public boolean isUCIOpponentMessage() {
		return Token.OPTION_UCI_OPPONENT.equals(this.name);
	}

	public String getName() {
		return name;
	}

	public String getValue() {
		return value;
	}

	@Override
	public void accept(ClientMessageHandler handler) {
		handler.handle(this);
	}

	@Override
	public String toCommand() {
		return Type.SET_OPTION.command() + ' ' + Token.NAME + ' ' + name
				+ (value != null ? ' ' + Token.VALUE + ' ' + value : "");
	}

	public static class OpponentMessage extends SetOptionMessage {

		private final Token.OpponentTitle title;
		private final int elo;
		private final boolean isComputer;
		private final String opponentName;

		OpponentMessage(Token.OpponentTitle title, int elo, boolean isComputer, String opponentName) {
			super(Token.OPTION_UCI_OPPONENT, title.toString() + ' ' +
					(elo >= 0 ? String.valueOf(elo) : Token.NONE) + ' ' +
					(isComputer ? Token.COMPUTER : Token.HUMAN) + ' ' +
					opponentName
			);
			this.title = title;
			this.elo = elo;
			this.isComputer = isComputer;
			this.opponentName = opponentName;
		}

		public Token.OpponentTitle getTitle() {
			return title;
		}

		@SuppressWarnings("unused")
		public int getElo() {
			return elo;
		}

		@SuppressWarnings("unused")
		public boolean isComputer() {
			return isComputer;
		}

		public String getOpponentName() {
			return opponentName;
		}
	}
}
