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

import com.lowbudget.chess.model.uci.protocol.MessageParser.ParserException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class TokenInput {
	private final List<String> tokens;
	private int tokenIndex = -1;

	TokenInput(String input) {
		// copied from Scanner class. We want to collapse any white spaces to a single space
		this.tokens = Arrays.asList(input.split("\\p{javaWhitespace}+"));
	}

	private boolean hasNext() {
		return tokenIndex < tokens.size() - 1;
	}

	void backward() {
		tokenIndex--;
	}

	private String next() {
		tokenIndex++;
		return tokens.get(tokenIndex);
	}

	@SuppressWarnings("SameParameterValue")
	boolean peek(String token) {
		boolean found = false;
		int index = tokenIndex;
		while (!found && index < tokens.size() - 1) {
			String value = tokens.get(++index);
			found = token.equals(value);
		}
		return found;
	}

	boolean exists(String token) {
		boolean found = false;
		int index = tokenIndex;
		while (!found && index < tokens.size() - 1) {
			String value = tokens.get(++index);
			found = token.equals(value);
		}
		if (found) {
			tokenIndex = index;
		}
		return found;
	}

	String anyOf(String... tokens) {
		for (String token : tokens) {
			if (exists(token)) {
				return token;
			}
		}
		return null;
	}

	String optionalValue() {
		return hasNext() ? next() : null;
	}

	String value() {
		String result = optionalValue();
		if (result == null) {
			throw new ParserException("End of input");
		}
		return result;
	}

	String asString() {
		StringBuilder sb = new StringBuilder();
		while (hasNext()) {
			sb.append(next()).append(' ');
		}
		return sb.toString().trim();
	}

	TokenInput require(String token) {
		if (!exists(token)) {
			throw new ParserException("Could not find token: " + token);
		}
		return this;
	}

	String oneOf(String... tokens) {
		String token = anyOf(tokens);
		if (token == null) {
			throw new ParserException("None of the tokens found: " + Arrays.toString(tokens));
		}
		return token;
	}

	List<String> allOf(String... tokens) {
		List<String> result = new ArrayList<>();
		for (String token : tokens) {
			int index = tokenIndex;
			result.add( require(token).value() );
			tokenIndex = index;
		}
		return result;
	}

	@SuppressWarnings("SameParameterValue")
	List<String> listOf(String token) {
		List<String> result = new ArrayList<>();
		while (exists(token)) {
			result.add(value());
		}
		return result;
	}

	String valueUntil(String endToken) {
		StringBuilder sb = new StringBuilder();
		String token;
		while ((token = value()) != null) {
			if (!endToken.equals(token)) {
				sb.append(token).append(' ');
			} else {
				backward();
				break;
			}
		}
		return sb.toString().trim();
	}

	@SuppressWarnings("SameParameterValue")
	int intValue(int invalidValue) {
		String value = value();
		int result = MessageParser.toInt(value, invalidValue);
		if (result == invalidValue) { // failed to parse an int from the value
			backward();
		}
		return result;
	}

}
