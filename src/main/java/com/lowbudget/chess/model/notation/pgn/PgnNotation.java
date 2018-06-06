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

import com.lowbudget.chess.model.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static com.lowbudget.chess.model.notation.pgn.PgnNotation.GameResult.GAME_RESULT_TOKENS;
import static java.util.stream.Collectors.toMap;

/**
 * Provides methods to convert a game to/from PGN notation compatible string
 */
public class PgnNotation {

	public enum GameResult {
		WHITE_WINS("1-0"),
		BLACK_WINS("0-1"),
		DRAW("1/2-1/2"),
		UNDEFINED("*");

		private final String text;
		GameResult(String text) {
			this.text = text;
		}
		public String text() {
			return this.text;
		}

		public static final String[] GAME_RESULT_TOKENS = Stream.of(values()).map(GameResult::text).toArray(String[]::new);

		public static final Map<String, GameResult> GAME_RESULT_MAP_BY_TEXT = Stream.of(values()).collect(toMap(result -> result.text, Function.identity()));

		public static GameResult of(String value) {
			return GAME_RESULT_MAP_BY_TEXT.get(value);
		}
	}

	private PgnNotation() {}

	public static String convertToString(PgnGame game) {
		Objects.requireNonNull(game, "Pgn game cannot be null");

		StringBuilder sb = new StringBuilder();

		for (PgnTag tag : game.tags()) {
			sb.append('[').append(tag.getName()).append(' ')
					.append("\"")
					.append(tag.getValue())
					.append("\"")
					.append(']').append('\n');
		}

		List<Move> moveList = game.getMoveList();
		convertMoveTextToString(moveList, sb, game.getGameResult());

		return sb.toString();
	}

	/**
	 * Converts a list of moves to a string that is compatible with the move text section of a PGN game.
	 * This method does not write the full move text section of a PGN game i.e. it ignores the game result
	 * @param moveList a list of moves to be converted
	 * @param sb the {@link StringBuilder} where the generated text is appended to
	 */
	public static void convertMoveTextToString(List<Move> moveList, StringBuilder sb) {
		if (moveList.size() > 0) {

			int moveIndex = 1;
			for (int i = 0; i < moveList.size(); i++) {
				Move move = moveList.get(i);

				boolean isWhite = move.getPiece().color().isWhite();

				if (moveIndex > 1) {
					sb.append(' ');
				}

				if (isWhite || i == 0) {
					sb.append(moveIndex++);
					sb.append('.');
					if (!isWhite) {
						sb.append('.').append('.');
					}
				}

				sb.append(move.toMoveString());
			}
		}
	}

	/**
	 * Converts the move text of a pgn game to string (this is the part of the moves only without the tags)
	 * @param moveList a list of moves to be converted
	 * @param sb the {@link StringBuilder} where the generated text is appended to
	 * @param gameResult the game result to append at the end of the move list
	 */
	private static void convertMoveTextToString(List<Move> moveList, StringBuilder sb, GameResult gameResult) {
		convertMoveTextToString(moveList, sb);
		if (gameResult != null) {
			sb.append(' ').append(gameResult.text());
		}

		// after the move list we should always leave a blank line
		if (moveList.size() > 0) {
			sb.append('\n');
		}
	}

	/**
	 * Parses a PGN string to a PGN game
	 * @param pgn the pgn string to convert
	 * @return a {@link PgnGame} representing the text
	 */
	public static PgnGame convertFromString(String pgn) {
		Objects.requireNonNull(pgn, "Pgn string cannot be null");
		if (pgn.isEmpty()) {
			return new SimplePgnGame();
		}
		return convertFromLines(pgn);
	}

	/**
	 * Parses a text file to a PGN game
	 * @param pgnFile the pgn file that contains the string to convert
	 * @return a {@link PgnGame} representing the text
	 */
	public static PgnGame convertFromTextFile(File pgnFile) {
		Objects.requireNonNull(pgnFile, "Pgn file cannot be null");
		PgnGame game;
		try {
			game = convertFromLines( new String(Files.readAllBytes(pgnFile.toPath())));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return game;
	}

	private static PgnGame convertFromLines(String text) {

		SimplePgnGame game = new SimplePgnGame();
		PgnInput input = new PgnInput(text);

		parseTags(input, game);
		parseMoveText(input, game);

		return game;
	}

	private static void parseTags(PgnInput input, SimplePgnGame game) {
		input.skipWhiteSpace();
		while (input.has('[')) {
			String tagName = input.require('[').skipWhiteSpace().token(PgnTag::isTagAllowedCharacter);
			String tagValue = input.skipWhiteSpace().allTokensBetween('\"');
			game.addTag(tagName, tagValue);
			input.skipWhiteSpace().require(']');
			input.skipWhiteSpace();
		}
	}

	private static void parseMoveText(PgnInput input, SimplePgnGame game) {
		input.skipWhiteSpace();
		Optional<String> gameResultText = Optional.empty();
		while (!input.isEndOfInput()) {
			gameResultText = input.skipWhiteSpace().anyTokenOf(GAME_RESULT_TOKENS);
			if (gameResultText.isPresent()) {
				break;
			}

			Optional<Integer> moveNumber = input.anyNumber();
			String dots = input.skipWhiteSpace().zeroOrMoreOf('.');
			boolean isWhite = moveNumber.isPresent() && dots.length() <= 1;

			input.skipWhiteSpace();
			Move move = parseMove(input.token(), isWhite);

			// parses comments and recursive move lists - we don't use those for now
			input.skipWhiteSpace().anyTokensBetween('{', '}');
			input.skipWhiteSpace().anyTokensBetween('(', ')');

			game.addMove(move);
		}
		if (gameResultText.isPresent()) {
			GameResult gameResult = GameResult.of(gameResultText.get());
			if (gameResult == null) {
				throw new PgnParseException("Could not recognize game result: " + gameResultText.get());
			}
			game.setGameResult(gameResult);
		}
	}

	private static final Pattern pattern = Pattern.compile("([NBRQK])?([a-h])?([1-8])?(x)?([a-h][1-8])(=[NBRQK])?([+#])?");

	private static Move parseMove(String token, boolean isWhite) {
		// for castling we just use direct equality check
		if (token.equals("O-O")) {
			return isWhite ? Move.WHITE_KING_SIDE_CASTLING : Move.BLACK_KING_SIDE_CASTLING;
		}
		if (token.equals("O-O-O")) {
			return isWhite ? Move.WHITE_QUEEN_SIDE_CASTLING : Move.BLACK_QUEEN_SIDE_CASTLING;
		}

		// for the rest of the moves we use the regular expression
		Matcher matcher = pattern.matcher(token);
		if (!matcher.matches()) {
			throw new PgnParseException("Could not match regular expression for input: " + token);
		}

		// if a group is not found it is null
		String pieceCode = matcher.group(1);
		String startFile = matcher.group(2);
		String startRank = matcher.group(3);
		//String capture = matcher.group(4);
		String targetSquare = matcher.group(5);
		String promotion = matcher.group(6);
		//String checkOrCheckMate = matcher.group(7);

		Piece piece = resolvePiece(pieceCode, isWhite);
		PieceType promotionType = resolvePromotionType(promotion, isWhite);
		int fromFile = startFile != null ? ChessConstants.File.of(startFile.charAt(0)) : -1;
		int fromRank = startRank != null ? ChessConstants.Rank.of(startRank.charAt(0)) : -1;
		Square to = parseSquare(targetSquare);
		return Move.of(piece, Square.of(fromRank, fromFile), to, promotionType);
	}

	private static Piece resolvePiece(String pieceCode, boolean isWhite) {
		if (pieceCode == null) {
			// no piece code - this is a pawn
			return isWhite ? Piece.PAWN_WHITE : Piece.PAWN_BLACK;
		}
		char pieceTypeChar = pieceCode.charAt(pieceCode.length() - 1);
		// piece code character always matches white (i.e. a capital letter is used)
		Piece piece = Piece.of(pieceTypeChar);
		if (!isWhite) {
			piece = Piece.of(piece.color().opposite(), piece.getType());
		}
		return piece;
	}

	private static PieceType resolvePromotionType(String token, boolean isWhite) {
		return token == null ? null : resolvePiece(token, isWhite).getType();
	}

	private static Square parseSquare(String token) {
		char fileChar = token.charAt(token.length() - 2);
		char rankChar = token.charAt(token.length() - 1);
		Square square = Square.of( ChessConstants.Rank.of(rankChar), ChessConstants.File.of(fileChar) );
		if (!square.isValid()) {
			throw new PgnParseException("Could not identify target square in move: " + token);
		}
		return square;
	}

}
