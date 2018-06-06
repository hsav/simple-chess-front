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

package com.lowbudget.chess.model.notation.pgn;

import java.util.Optional;

/**
 * Helper class for parsing a PGN compatible text input
 */
@SuppressWarnings("SameParameterValue")
class PgnInput {

	@FunctionalInterface
	public interface CharacterPredicate {
		boolean test(char c);
	}

	private final char[] input;
	private int index;

	PgnInput(String input) {
		this.input = input.toCharArray();
		index = 0;
	}

	/**
	 * Checks if the next input character is the character specified
	 * @param c the character to test
	 * @return {@code true} if the next character is the character specified or {@code false} otherwise
	 */
	boolean has(char c) {
		return !isEndOfInput() && current() == c;
	}

	/**
	 * Advances the input by one character and checks that the character read is the one specified
	 * @param c the character we expect to be returned next
	 * @return this object
	 * @throws PgnParseException if the next character is different then the one specified
	 */
	PgnInput require(char c) {
		if (current() != c) {
			throw new PgnParseException("Character '" + c + "' was required but '" + current() + "' was found instead");
		}
		advance();
		return this;
	}

	/**
	 * Reads the next required token from the input until a non-whitespace is encountered or end of input is reached
	 * @return the next token found
	 * @throws PgnParseException if no character is read
	 */
	String token() {
		return token(c -> !Character.isWhitespace(c));
	}

	/**
	 * Reads the next required token from the input until the supplied predicate returns {@code false} or end of input is reached
	 * @return the next token found
	 * @throws PgnParseException if no character is read
	 */
	String token(CharacterPredicate predicate) {
		String token = value(predicate);
		if (token.isEmpty()) {
			throw new PgnParseException("Could not find required token. End of input");
		}
		return token;
	}

	/**
	 * Return all the tokens between the characters specified, as a single string
	 * @param start the starting character
	 * @param end the ending character
	 * @return an {@code Optional<String>} that contains the value read between the {@code start} and {@code end} characters.
	 * If no such value is available or if the next character is not the starting character, the optional will be empty
	 */
	Optional<String> anyTokensBetween(char start, char end) {
		String result = null;
		if (has(start)) {
			advance();
			result = value(input -> input != end || isEscaped());
			advance(); // skip the end character
		}
		return Optional.ofNullable(result);
	}

	/**
	 * Return all the tokens between the same starting and ending character, as a single string
	 * @param quoteChar the starting and ending characters
	 * @return a {@code String} that contains the value read
	 * @throws PgnParseException if the next character is not the starting character
	 */
	String allTokensBetween(char quoteChar) {
		return allTokensBetween(quoteChar, quoteChar);
	}

	/**
	 * Parses consecutive characters as a number until it encounters a character that is not a digit or end of input is reached
	 * @return an {@code Optional<Integer>} that contains an integer if at least one digit character was available or
	 * an empty optional otherwise
	 */
	Optional<Integer> anyNumber() {
		int position = index;
		String value = value(Character::isDigit);
		if (value.isEmpty()) {
			index = position;
		}
		return value.isEmpty() ? Optional.empty() : Optional.of(Integer.parseInt(value));
	}

	/**
	 * Skips all whitespace until it encounters a non-whitespace character or end of input. The characters that are
	 * considered white spaces are decided by using the {@link Character#isWhitespace(char)} method
	 * @return this object
	 */
	PgnInput skipWhiteSpace() {
		while ( !isEndOfInput() && Character.isWhitespace(current()) ) {
			advance();
		}
		return this;
	}

	/**
	 * Reads one or more characters of the input that equal to the character specified until it finds a different
	 * character or end of input
	 * @param c the character to read one or more times
	 * @return a string where the specified character is concatenated as many times as it exists in the input or the
	 * empty string if no such character is available. For example assuming this function is called with the
	 * {@code '.'} character as an argument:
	 * <ul>
	 *     <li>Input: {@code "...abc"} will return {@code "..."}</li>
	 *     <li>Input: {@code "   abc"} will return {@code ""}</li>
	 *     <li>Input: {@code ".  abc"} will return {@code "."}</li>
	 * </ul>
	 */
	String zeroOrMoreOf(char c) {
		return value( input -> input == c );
	}

	/**
	 * Searches for any of the tokens specified in the input
	 * @param tokens the array of tokens to search
	 * @return an {@code Optional<String>} that contains the first matched token if one is found or is empty otherwise
	 */
	Optional<String> anyTokenOf(String... tokens) {
		if (isEndOfInput()) {
			return Optional.empty();
		}
		String foundToken = null;
		for (String token : tokens) {
			int position = index;
			boolean found = true;
			for (int i = 0; i < token.length(); i++) {
				if (current() != token.charAt(i)) {
					index = position;
					found = false;
					break;
				} else {
					advance();
				}
			}
			if (found) {
				foundToken = token;
				break;
			}
		}
		return Optional.ofNullable(foundToken);
	}

	private String allTokensBetween(char start, char end) {
		Optional<String> value = anyTokensBetween(start, end);
		return value.orElseThrow(() -> new PgnParseException("Required value between '" + start + "' - '" + end + "' not found"));
	}

	private boolean isEscaped() {
		int escapes = 0;
		int i = index - 1;
		while (i >= 0 && input[i] == '\\') {
			i--;
			escapes++;
		}
		return (escapes % 2) != 0; // there should be an odd number of '\' characters for the current character to be considered escaped
	}

	private String value(CharacterPredicate predicate) {
		StringBuilder sb = new StringBuilder();
		while (!isEndOfInput() && predicate.test(current())) {
			sb.append(current());
			advance();
		}
		return sb.toString();
	}

	private char current() {
		if (index >= input.length) {
			throw new PgnParseException("End of input");
		}
		return input[index];
	}

	boolean isEndOfInput() {
		return index >= input.length;
	}

	private void advance() {
		index++;
	}
}
