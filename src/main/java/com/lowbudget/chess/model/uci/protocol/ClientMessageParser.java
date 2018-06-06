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
import com.lowbudget.chess.model.uci.protocol.MessageParser.ParserException;
import com.lowbudget.chess.model.uci.protocol.message.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static com.lowbudget.chess.model.uci.protocol.MessageParser.parseMoveList;
import static com.lowbudget.chess.model.uci.protocol.message.UCIMessage.*;

/**
 * Parses messages sent by the GUI to the engine
 */
public class ClientMessageParser {

	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(ClientMessageParser.class);

	public static ClientMessage parseMessage(String message) {

		TokenInput input = new TokenInput(message);

		ClientMessage result;
		try {
			String command = input.oneOf(Type.CLIENT_COMMAND_TOKENS);
			Type messageType = UCIMessage.getClientMessageType(command);
			switch (messageType) {
				case UCI: 			result = SimpleMessage.UCI; break;
				case IS_READY:		result = SimpleMessage.IS_READY; break;
				case UCI_NEW_GAME:	result = SimpleMessage.UCI_NEW_GAME; break;
				case STOP:			result = SimpleMessage.STOP; break;
				case PONDER_HIT:	result = SimpleMessage.PONDER_HIT; break;
				case QUIT:			result = SimpleMessage.QUIT; break;
				case DEBUG:			result = debug(input); break;
				case REGISTER:		result = register(input); break;
				case SET_OPTION:	result = setOption(input); break;
				case GO:			result = go(input); break;
				case POSITION:		result = position(input); break;
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

	private static ClientMessage debug(TokenInput input) {
		// the debug command is followed by one of the tokens "on" or "off"
		String property = input.oneOf(Token.ON, Token.OFF);
		return Token.ON.equals(property) ? DebugMessage.ON : DebugMessage.OFF;
	}

	private static ClientMessage setOption(TokenInput input) {
		String name = input.require(Token.NAME).value();
		if (Token.OPTION_UCI_OPPONENT.equals(name)) {
			return uciOpponent(input);
		}
		String value = input.exists(Token.VALUE) ? input.asString() : null;
		return UCIMessage.newSetOptionMessage(name, value);
	}

	private static ClientMessage uciOpponent(TokenInput input) {
		String title = input.oneOf(Token.OpponentTitle.toTokens());
		int elo = input.intValue(-1);
		String type = input.oneOf(Token.COMPUTER, Token.HUMAN);
		String opponentName = input.asString();
		return UCIMessage.newSetOptionUCIOpponentMessage(Token.OpponentTitle.fromString(title), elo, Token.COMPUTER.equals(type), opponentName);
	}

	private static ClientMessage register(TokenInput input) {
		String property = input.oneOf(Token.LATER, Token.NAME, Token.CODE);
		if (Token.LATER.equals(property)) {
			return RegisterMessage.LATER;
		}

		String name = null;
		String code = null;
		String token = "";

		while (token != null) {
			token = input.anyOf(Token.NAME, Token.CODE);
			String value = input.value();
			if (Token.NAME.equals(token)) {
				name = value;
			} else if (Token.CODE.equals(token)) {
				code = value;
			}
		}
		if (name == null || code == null) {
			throw new ParserException("Registration name or code is null!");
		}
		return UCIMessage.newRegisterMessage(name, code);
	}

	private static ClientMessage go(TokenInput input) {
		Set<String> infoTokens = Token.GO_TOKENS;
		GoMessage.Builder builder = GoMessage.builder();
		String token;
		while ((token = input.optionalValue()) != null) {
			if (infoTokens.contains(token)) {
				switch (token) {
					case Token.SEARCH_MOVES:	builder.searchMoves(parseMoveList(input)); break;
					case Token.PONDER: 			builder.ponder(true); break;
					case Token.WHITE_TIME:		builder.whiteTime(input.intValue(-1)); break;
					case Token.BLACK_TIME: 		builder.blackTime(input.intValue(-1)); break;
					case Token.WHITE_INC: 		builder.whiteIncrement(input.intValue(-1)); break;
					case Token.BLACK_INC: 		builder.blackIncrement(input.intValue(-1)); break;
					case Token.MOVES_TO_GO: 	builder.movesToGo(input.intValue(-1)); break;
					case Token.DEPTH: 			builder.depth(input.intValue(-1)); break;
					case Token.NODES: 			builder.nodes(input.intValue(-1)); break;
					case Token.MATE: 			builder.mate(input.intValue(-1)); break;
					case Token.MOVE_TIME: 		builder.moveTime(input.intValue(-1)); break;
					case Token.INFINITE: 		builder.infinite(true); break;
					default:
						throw new IllegalStateException("Wrong info token: " + token);    // should never happen
				}
			}
		}
		return builder.build();
	}

	private static ClientMessage position(TokenInput input) {
		String property = input.oneOf(Token.FEN, Token.START_POS);
		String fen = null;
		if (Token.FEN.equals(property)) {
			fen = input.peek(Token.MOVES) ? input.valueUntil(Token.MOVES) : input.asString();
		}
		List<Move> moves = input.exists(Token.MOVES) ? parseMoveList(input) : new ArrayList<>();
		return fen == null ? newStartPositionMessage(moves) : newFenPositionMessage(fen, moves);
	}
}