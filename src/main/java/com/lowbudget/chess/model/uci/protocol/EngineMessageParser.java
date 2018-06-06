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

import com.lowbudget.chess.model.Move;
import com.lowbudget.chess.model.uci.protocol.message.*;
import com.lowbudget.chess.model.uci.protocol.message.UCIMessage.*;
import com.lowbudget.chess.model.uci.protocol.message.InfoMessage.CurrentLine;
import com.lowbudget.chess.model.uci.protocol.message.InfoMessage.Score;
import java.util.*;

import static com.lowbudget.chess.model.uci.protocol.MessageParser.*;
import static com.lowbudget.chess.model.uci.protocol.message.UCIMessage.newUnknownMessage;

/**
 * Parses messages sent by the engine to the GUI
 */
public class EngineMessageParser {

	public static EngineMessage parseMessage(String message) {

		TokenInput input = new TokenInput(message);

		EngineMessage result;
		try {
			String command = input.oneOf(UCIMessage.Type.ENGINE_COMMAND_TOKENS);
			UCIMessage.Type messageType = UCIMessage.getEngineMessageType(command);
			switch (messageType) {
				case ID: 				result = id(input); break;
				case UCI_OK: 			result = SimpleMessage.UCI_OK; break;
				case READY_OK:			result = SimpleMessage.READY_OK; break;
				case BEST_MOVE:			result = bestMove(input); break;
				case COPY_PROTECTION:	result = copyProtection(input); break;
				case REGISTRATION:		result = registration(input); break;
				case INFO:				result = info(input); break;
				case OPTION:			result = option(input); break;
				default:
					result = newUnknownMessage(message);
					break;
			}
		} catch (ParserException e) {
			//log.error("Parsing error, ignoring message", e);
			result = UCIMessage.newUnknownMessage(message);
		}
		return result;
	}

	private static EngineMessage id(TokenInput input) {
		// require either the name or author token
		String property = input.oneOf(Token.NAME, Token.AUTHOR);
		// then read its value until the rest of the input
		String value = input.asString();

		switch (property) {
			case Token.NAME:
				return UCIMessage.newIdNameMessage(value);
			case Token.AUTHOR:
				return UCIMessage.newIdAuthorMessage(value);
			default:
				throw new ParserException("Unknown id property: " + property);
		}
	}

	private static EngineMessage bestMove(TokenInput input) {
		Move move = parseRequiredMove(input);
		Move ponderMove = input.exists(Token.PONDER) ? parseRequiredMove(input) : Move.NONE;
		return UCIMessage.newBestMoveMessage(move, ponderMove);
	}

	private static EngineMessage registration(TokenInput input) {
		EngineMessage result;
		String status = input.oneOf(Token.CHECKING, Token.OK, Token.ERROR);
		switch (status) {
			case Token.CHECKING:	result = RegistrationMessage.CHECKING; break;
			case Token.OK: 			result = RegistrationMessage.OK; break;
			case Token.ERROR:  		result = RegistrationMessage.ERROR; break;
			default:
				throw new ParserException("Unknown registration status: " + status);
		}
		return result;
	}

	private static EngineMessage copyProtection(TokenInput input) {
		EngineMessage result;
		String status = input.oneOf(Token.CHECKING, Token.OK, Token.ERROR);
		switch (status) {
			case Token.CHECKING:	result = CopyProtectionMessage.CHECKING; break;
			case Token.OK: 			result = CopyProtectionMessage.OK; break;
			case Token.ERROR:  		result = CopyProtectionMessage.ERROR; break;
			default:
				throw new ParserException("Unknown copy protection status: " + status);
		}
		return result;
	}

	private static EngineMessage info(TokenInput input) {
		Set<String> infoTokens = Token.INFO_TOKENS;
		InfoMessage.Builder builder = InfoMessage.builder();
		String token;
		while ((token = input.optionalValue()) != null) {
			if (infoTokens.contains(token)) {
				switch (token) {
					case Token.DEPTH: 				builder.depth(input.intValue(-1)); break;
					case Token.SEL_DEPTH: 			builder.selectiveDepth(input.intValue(-1)); break;
					case Token.TIME: 				builder.time(input.intValue(-1)); break;
					case Token.NODES: 				builder.nodes(input.intValue(-1)); break;
					case Token.PV: 					builder.pv( parseMoveList(input) ); break;
					case Token.MULTI_PV: 			builder.multiPV(input.intValue(-1)); break;
					case Token.SCORE: 				builder.score(score(input)); break;
					case Token.CURR_MOVE: 			builder.currentMove( parseRequiredMove(input) ); break;
					case Token.CURR_MOVE_NUMBER: 	builder.currentMoveNumber(input.intValue(-1)); break;
					case Token.HASH_FULL: 			builder.hashFull(input.intValue(-1)); break;
					case Token.NPS: 				builder.nodesPerSecond(input.intValue(-1)); break;
					case Token.TB_HITS: 			builder.tableBaseHits(input.intValue(-1)); break;
					case Token.SB_HITS: 			builder.shredderBaseHits(input.intValue(-1)); break;
					case Token.CPU_LOAD: 			builder.cpuLoad(input.intValue(-1)); break;
					case Token.REFUTATION: 			builder.refutation( parseMoveList(input) ); break;
					case Token.CURR_LINE: 			builder.currentLine(currentLine(input)); break;
					case Token.STRING:
						builder.string(input.asString());
						// in this case return immediately since we have read the rest of the line
						return builder.build();
					default:
						throw new IllegalStateException("Wrong info token: " + token);    // should never happen
				}
			}
		}
		return builder.build();
	}

	private static Score score(TokenInput input) {
		String scoreToken = "";
		Score.Builder builder = Score.builder();
		while (scoreToken != null) {
			scoreToken = input.anyOf(Token.CP, Token.MATE, Token.LOWER_BOUND, Token.UPPER_BOUND);
			if (scoreToken != null) {
				switch (scoreToken) {
					case Token.CP: 			builder.centiPawns(input.intValue(-1)); break;
					case Token.MATE: 		builder.mate(input.intValue(-1)); break;
					case Token.LOWER_BOUND: builder.lowerBound(); break;
					case Token.UPPER_BOUND: builder.upperBound(); break;
					default:
						throw new IllegalStateException("Invalid score token: " + scoreToken); // should never happen
				}
			}
		}
		return builder.build();
	}

	private static CurrentLine currentLine(TokenInput input) {
		CurrentLine.Builder builder = CurrentLine.builder();
		int cpuNumber = input.intValue(-1);
		builder.cpuNumber(cpuNumber);
		builder.moves( parseMoveList(input) );
		return builder.build();
	}

	private static EngineMessage option(TokenInput input) {
		// require the token 'name' and then read its value until the token 'type' is encountered
		String name = input.require(Token.NAME).valueUntil(Token.TYPE);

		// the 'type' token follows and then its value which can be one of the option types
		String type = input.require(Token.TYPE).oneOf(Token.CHECK, Token.COMBO, Token.SPIN, Token.STRING, Token.BUTTON);

		EngineMessage result;
		String defaultValue, minValue, maxValue;
		switch (type) {
			case Token.BUTTON:
				result = UCIMessage.newButtonOption(name);
				break;
			case Token.STRING:
				defaultValue = input.require(Token.DEFAULT).asString();
				result = UCIMessage.newStringOption(name, defaultValue);
				break;
			case Token.CHECK:
				defaultValue = input.require(Token.DEFAULT).optionalValue();
				result = UCIMessage.newCheckOption(name, Boolean.parseBoolean(defaultValue));
				break;
			case Token.COMBO:
				defaultValue = input.require(Token.DEFAULT).value();
				List<String> comboOptions = input.listOf(Token.VAR);
				result = UCIMessage.newComboOption(name, defaultValue, comboOptions);
				break;
			case Token.SPIN:
				List<String> values = input.allOf(Token.DEFAULT, Token.MIN, Token.MAX);
				defaultValue = values.get(0);
				minValue = values.get(1);
				maxValue = values.get(2);
				result = UCIMessage.newSpinOption(name, toInt(defaultValue), toInt(minValue), toInt(maxValue, Integer.MAX_VALUE));
				break;
			default:
				throw new IllegalStateException("Invalid option type: " + type);    // should never happen
		}
		return result;
	}
}