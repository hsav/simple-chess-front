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

public class RegisterMessage extends UCIMessage implements ClientMessage {

	public static final RegisterMessage LATER = new RegisterMessage();

	private final String name;

	private final String code;

	private final boolean isLater;

	private RegisterMessage() {
		super(Type.REGISTER);
		this.name = null;
		this.code = null;
		this.isLater = true;
	}

	RegisterMessage(String name, String code) {
		super(Type.REGISTER);
		this.name = name;
		this.code = code;
		this.isLater = false;
	}

	@Override
	public void accept(ClientMessageHandler handler) {
		handler.handle(this);
	}

	@Override
	public String toCommand() {
		return Type.REGISTER.command() + ' ' + (isLater ? Token.LATER : (Token.NAME + ' ' + name + ' ' + Token.CODE + ' ' + code));
	}
}
