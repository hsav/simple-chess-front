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

package com.lowbudget.chess.model.uci.protocol;

import com.lowbudget.chess.model.uci.protocol.message.*;

/**
 * <p>Interface that defines the "Visitor" part of the Visitor design pattern for handling {@link UCIMessage}
 * sub-classes that involve messages sent by the UCI engine.</p>
 */
public interface EngineMessageHandler {

	void handle(IdNameMessage message);

	void handle(IdAuthorMessage message);

	void handle(CopyProtectionMessage message);

	void handle(SimpleMessage message);

	void handle(RegistrationMessage message);

	void handle(BestMoveMessage message);

	void handle(InfoMessage message);

	void handle(OptionMessage message);

	class Adapter implements EngineMessageHandler {

		@Override
		public void handle(IdNameMessage message) {}

		@Override
		public void handle(IdAuthorMessage message) {}

		@Override
		public void handle(CopyProtectionMessage message) {}

		@Override
		public void handle(SimpleMessage message) {}

		@Override
		public void handle(RegistrationMessage message) {}

		@Override
		public void handle(BestMoveMessage message) {}

		@Override
		public void handle(InfoMessage message) {}

		@Override
		public void handle(OptionMessage message) {}
	}
}
