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
import com.lowbudget.chess.model.uci.protocol.EngineMessage;
import com.lowbudget.chess.model.uci.protocol.EngineMessageHandler;

public class SimpleMessage extends UCIMessage implements EngineMessage, ClientMessage {
	/*
	 * These messages (generated by GUI) have no additional arguments thus there is no need to create more than one instance.
	 */
	public static final SimpleMessage UCI = new SimpleMessage(Type.UCI);
	public static final SimpleMessage IS_READY = new SimpleMessage(Type.IS_READY);
	public static final SimpleMessage UCI_NEW_GAME = new SimpleMessage(Type.UCI_NEW_GAME);
	public static final SimpleMessage STOP = new SimpleMessage(Type.STOP);
	public static final SimpleMessage PONDER_HIT = new SimpleMessage(Type.PONDER_HIT);
	public static final SimpleMessage QUIT = new SimpleMessage(Type.QUIT);

	/*
	 * These messages (generated by engine) have no additional arguments thus there is no need to create more than one instance.
	 */
	public static final EngineMessage UCI_OK = new SimpleMessage(Type.UCI_OK);
	public static final EngineMessage READY_OK = new SimpleMessage(Type.READY_OK);

	private SimpleMessage(Type type) {
		super(type);
	}

	@Override
	public void accept(EngineMessageHandler handler) {
		handler.handle(this);
	}
	@Override
	public void accept(ClientMessageHandler handler) {
		handler.handle(this);
	}

	@Override
	public String toCommand() {
		return getType().command();
	}

	static class UnknownMessage extends SimpleMessage {

		private final String message;

		UnknownMessage(String message) {
			super(Type.UNKNOWN);
			this.message = message;
		}

		@Override
		public String toString() {
			return Type.UNKNOWN.toString() + ':' + message;
		}
	}
}
