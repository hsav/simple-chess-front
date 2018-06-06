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

import com.lowbudget.chess.model.uci.protocol.EngineMessage;
import com.lowbudget.chess.model.uci.protocol.EngineMessageHandler;

public class RegistrationMessage extends UCIMessage implements EngineMessage {

	public static final RegistrationMessage CHECKING = new RegistrationMessage(Token.CHECKING);
	public static final RegistrationMessage OK = new RegistrationMessage(Token.OK);
	public static final RegistrationMessage ERROR = new RegistrationMessage(Token.ERROR);

	private final String status;

	private RegistrationMessage(String status) {
		super(Type.REGISTRATION);
		this.status = status;
	}

	public String getStatus() {
		return status;
	}

	public boolean isError() {
		return Token.ERROR.equals(status);
	}

	public boolean isOk() {
		return Token.OK.equals(status);
	}

	@Override
	public void accept(EngineMessageHandler handler) {
		handler.handle(this);
	}

	@Override
	public String toCommand() {
		return Type.REGISTRATION.command() + ' ' + status;
	}

}
