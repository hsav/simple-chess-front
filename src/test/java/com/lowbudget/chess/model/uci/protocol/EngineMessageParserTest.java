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

import com.lowbudget.chess.model.Square;
import com.lowbudget.chess.model.Move;
import com.lowbudget.chess.model.uci.protocol.message.*;
import com.lowbudget.chess.model.uci.protocol.message.InfoMessage.CurrentLine;
import com.lowbudget.chess.model.uci.protocol.message.InfoMessage.Score;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.function.Consumer;

import static org.junit.Assert.*;

public class EngineMessageParserTest {

	@Test
	public void testIdMessageWithName() {
		String input = "id name some-name";
		assertMessage(input, UCIMessage.Type.ID, IdNameMessage.class, (message) -> assertEquals("some-name", message.getName()));
	}

	@Test
	public void testIdMessageWithoutNameOrAuthor() {
		String input = "id jojo some-name";
		assertUnknownMessage(input);
	}

	@Test
	public void testArbitraryWhiteSpacesAreIgnored() {
		String input = "id  \t   \t name\t\t\t  the   author name";

		assertMessage(input, UCIMessage.Type.ID, IdNameMessage.class, (message) -> assertEquals("the author name", message.getName()));
	}

	@Test
	public void testUnrecognizedTokensAreIgnoredOnInputStart() {
		String input = "jojo id name some-name";

		assertMessage(input, UCIMessage.Type.ID, IdNameMessage.class, (message) -> assertEquals("some-name", message.getName()));
	}

	/**
	 * According to the result of the UCI specification this test case has undefined behaviour, however instead of
	 * throwing an exception we allow it as long as it does not create any disambiguation
	 */
	@Test
	public void testUnrecognizedTokensAreIgnoredOnInputMiddle() {
		String input = "id jojo author some-author";

		assertMessage(input, UCIMessage.Type.ID, IdAuthorMessage.class, (message) -> assertEquals("some-author", message.getAuthor()));
	}

	@Test
	public void testOptionCheck() {
		String input = "option name Debug type check default false";

		assertMessage(input, UCIMessage.Type.OPTION, OptionMessage.CheckOptionMessage.class, (message) -> {
			assertEquals("Debug", message.getName());
			assertFalse(message.getDefaultValue());
		});
	}

	@Test
	public void testOptionCheckWithoutDefaultValue() {
		String input = "option name Debug type check default";
		assertMessage(input, UCIMessage.Type.OPTION, OptionMessage.CheckOptionMessage.class, (message) -> {
			assertEquals("Debug", message.getName());
			assertFalse(message.getDefaultValue());
		});
	}

	@Test
	public void testOptionSpin() {
		String input = "option name Spin type spin default 2 min 1 max 3";

		assertMessage(input, UCIMessage.Type.OPTION, OptionMessage.SpinOptionMessage.class, (message) -> {
			assertEquals("Spin", message.getName());
			assertEquals(2, message.getDefaultValue());
			assertEquals(1, message.getMin());
			assertEquals(3, message.getMax());
		});
	}

	@Test
	public void testOptionSpinMixed() {
		String input = "option name Spin type spin min 1 max 3 default 2";

		assertMessage(input, UCIMessage.Type.OPTION, OptionMessage.SpinOptionMessage.class, (message) -> {
			assertEquals("Spin", message.getName());
			assertEquals(2, message.getDefaultValue());
			assertEquals(1, message.getMin());
			assertEquals(3, message.getMax());
		});
	}

	@Test
	public void testOptionButton() {
		String input = "option name Hide lines type button";

		assertMessage(input, UCIMessage.Type.OPTION, OptionMessage.ButtonOptionMessage.class, (message) -> {
			assertEquals("Hide lines", message.getName());
			assertEquals(UCIMessage.Token.BUTTON, message.getOptionType());
		});
	}

	@Test
	public void testOptionStringWithSimpleDefaultValue() {
		String input = "option name About type string default some-value";

		assertMessage(input, UCIMessage.Type.OPTION, OptionMessage.StringOptionMessage.class, (message) -> {
			assertEquals("About", message.getName());
			assertEquals("some-value", message.getDefaultValue());
		});
	}

	@Test
	public void testOptionStringWithEmptyTagDefaultValue() {
		String input = "option name About type string default <empty>";

		assertMessage(input, UCIMessage.Type.OPTION, OptionMessage.StringOptionMessage.class, (message) -> {
			assertEquals("About", message.getName());
			assertEquals("", message.getDefaultValue());
		});
	}

	@Test
	public void testOptionStringWithMultipleWordsDefaultValue() {
		String input = "option name About type string default first second third";

		assertMessage(input, UCIMessage.Type.OPTION, OptionMessage.StringOptionMessage.class, (message) -> {
			assertEquals("About", message.getName());
			assertEquals("first second third", message.getDefaultValue());
		});
	}


	@Test
	public void testOptionStringWithEmptyValue() {
		String input = "option name About type string default";

		assertMessage(input, UCIMessage.Type.OPTION, OptionMessage.StringOptionMessage.class, (message) -> {
			assertEquals("About", message.getName());
			assertEquals("", message.getDefaultValue());
		});
	}


	@Test
	public void testOptionCombo() {
		String input = "option name Level type combo default Medium var Easy var Medium var Hard";

		assertMessage(input, UCIMessage.Type.OPTION, OptionMessage.ComboOptionMessage.class, (message) -> {
			assertEquals("Level", message.getName());
			assertEquals("Medium", message.getDefaultValue());
			assertEquals(Arrays.asList("Easy", "Medium", "Hard"), message.getComboOptions());
		});

	}

	@Test
	public void testOptionComboWithNonConsecutiveComboValues() {
		String input = "option name Level type combo default Medium var Easy other var Medium var Hard";

		assertMessage(input, UCIMessage.Type.OPTION, OptionMessage.ComboOptionMessage.class, (message) -> {
			assertEquals("Level", message.getName());
			assertEquals("Medium", message.getDefaultValue());

			// note that the unknown token 'other' is ignored
			assertEquals(Arrays.asList("Easy", "Medium", "Hard"), message.getComboOptions());
		});
	}


	/**
	 * Tests parsing of a combo option where 'var' values are not specified. The UCI protocol is not clear if this
	 * is allowed or not (it probably isn't), however its cheap for us to support it.
	 */
	@Test
	public void testOptionComboWithoutComboValues() {
		String input = "option name Level type combo default Medium";

		assertMessage(input, UCIMessage.Type.OPTION, OptionMessage.ComboOptionMessage.class, (message) -> {
			assertEquals("Level", message.getName());
			assertEquals("Medium", message.getDefaultValue());
			assertEquals(Collections.singletonList("Medium"), message.getComboOptions());
		});

	}

	@Test
	public void testCopyProtectionMessage() {
		//noinspection SpellCheckingInspection
		String[] inputs = {"copyprotection checking", "copyprotection ok", "copyprotection error"};
		String[] expectedStatuses = {UCIMessage.Token.CHECKING, UCIMessage.Token.OK, UCIMessage.Token.ERROR};

		for (int i = 0; i < inputs.length; i++) {
			String input = inputs[i];
			String expectedStatus = expectedStatuses[i];
			assertMessage(input, UCIMessage.Type.COPY_PROTECTION, CopyProtectionMessage.class,
					(message) -> assertEquals(expectedStatus, message.getStatus()));
		}
	}

	@Test
	public void testRegistrationMessage() {
		String[] inputs = {"registration checking", "registration ok", "registration error"};
		String[] expectedStatuses = {UCIMessage.Token.CHECKING, UCIMessage.Token.OK, UCIMessage.Token.ERROR};

		for (int i = 0; i < inputs.length; i++) {
			String input = inputs[i];
			String expectedStatus = expectedStatuses[i];
			assertMessage(input, UCIMessage.Type.REGISTRATION, RegistrationMessage.class,
					(message) -> assertEquals(expectedStatus, message.getStatus()));
		}
	}

	@Test
	public void testBestMoveMessage() {
		//noinspection SpellCheckingInspection
		String input = "bestmove e2e4";

		assertMessage(input, UCIMessage.Type.BEST_MOVE, BestMoveMessage.class, (message) -> {
			assertEquals(Move.of(Square.E2, Square.E4), message.getMove());
			assertFalse(message.getPonderMove().isValid());
		});
	}

	@Test
	public void testBestMoveMessageWhenInvalidMove() {
		//noinspection SpellCheckingInspection
		String input = "bestmove nomove";
		assertUnknownMessage(input);
	}

	@Test
	public void testBestMoveMessageWithPonderMove() {
		//noinspection SpellCheckingInspection
		String input = "bestmove e2e4 ponder e7e5";

		assertMessage(input, UCIMessage.Type.BEST_MOVE, BestMoveMessage.class, (message) -> {
			assertEquals(Move.of(Square.E2, Square.E4), message.getMove());
			assertEquals(Move.of(Square.E7, Square.E5), message.getPonderMove());
		});
	}

	@Test
	public void testInfoMessageString() {
		String input = "info string a long string value";
		assertMessage(input, UCIMessage.Type.INFO, InfoMessage.class,
				(message) -> assertEquals("a long string value", message.getString()));
	}

	@Test
	public void testInfoMessageWithDepth() {
		//noinspection SpellCheckingInspection
		String input = "info depth 1 seldepth 2";

		assertMessage(input, UCIMessage.Type.INFO, InfoMessage.class, (message) -> {
			assertEquals(1, message.getDepth());
			assertEquals(2, message.getSelectiveDepth());
		});
	}

	@Test
	public void testInfoMessageWithScore() {
		//noinspection SpellCheckingInspection
		String input = "info score cp 10 lowerbound";

		assertMessage(input, UCIMessage.Type.INFO, InfoMessage.class, (message) -> {
			Score score = message.getScore();
			assertNotNull(score);
			assertEquals(10, score.getCentiPawns());
			assertEquals(-1, score.getMate());
			assertTrue(score.isLowerBound());
			assertFalse(score.isUpperBound());
		});
	}

	@Test
	public void testInfoMessageWithPV() {
		String input = "info pv e2e4 e7e5";

		assertMessage(input, UCIMessage.Type.INFO, InfoMessage.class, (message) -> {
			assertNotNull(message.getPv());
			assertEquals(Arrays.asList(Move.of(Square.E2, Square.E4), Move.of(Square.E7, Square.E5)), message.getPv());
		});
	}

	@Test
	public void testInfoMessageWithCurrentLineWithoutCpuNumber() {
		//noinspection SpellCheckingInspection
		String input = "info currline e2e4 e7e5";

		assertMessage(input, UCIMessage.Type.INFO, InfoMessage.class, (message) -> {
			CurrentLine currentLine = message.getCurrentLine();
			assertNotNull(currentLine);
			assertEquals(-1, currentLine.getCpuNumber());
			assertEquals(Arrays.asList(Move.of(Square.E2, Square.E4), Move.of(Square.E7, Square.E5)), currentLine.getMoves());
		});
	}

	@Test
	public void testInfoMessageWithCurrentLineWithCpuNumber() {
		//noinspection SpellCheckingInspection
		String input = "info currline 2 e2e4 e7e5";

		assertMessage(input, UCIMessage.Type.INFO, InfoMessage.class, (message) -> {
			CurrentLine currentLine = message.getCurrentLine();
			assertNotNull(currentLine);
			assertEquals(2, currentLine.getCpuNumber());
			assertEquals(Arrays.asList(Move.of(Square.E2, Square.E4), Move.of(Square.E7, Square.E5)), currentLine.getMoves());
		});
	}

	private void assertUnknownMessage(String input) {
		assertMessage(input, UCIMessage.Type.UNKNOWN, null, null);
	}


	/**
	 * <p>Wraps common code used in all tests.</p>
	 * <p>This method will perform the following actions:
	 * <ul>
	 *     <li>Parse the input and return a {@link UCIMessage}</li>
	 *     <li>Assert that the message type agrees with the expected type</li>
	 *     <li>Cast the resulted message in the specified {@link UCIMessage} sub-class</li>
	 *     <li>Will execute any extra assertion code specified by the {@link Consumer} functional interface (usually
	 *     in a form of a lambda)</li>
	 * </ul>
	 * </p>
	 *
	 * @param input the input to parse
	 * @param expectedType the expected message type
	 * @param expectedClass the expected subclass of {@link UCIMessage} where the parsed message will be cast to.
	 * @param assertions extra assertions we need to perform on the casted object in the form of a lambda
	 * @param <T> a subtype of {@link UCIMessage} that represents the actual class of the parsed message
	 */
	private <T extends UCIMessage> void assertMessage(String input, UCIMessage.Type expectedType, Class<T> expectedClass, Consumer<T> assertions) {
		EngineMessage engineMessage = EngineMessageParser.parseMessage(input);

		assertTrue(UCIMessage.class.isAssignableFrom(engineMessage.getClass()));

		UCIMessage message = (UCIMessage) engineMessage;

		assertEquals(expectedType, message.getType());

		if (message.getType().isKnown()) {
			T casted = expectedClass.cast(message);
			assertions.accept(casted);
		}
	}
}
