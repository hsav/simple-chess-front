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

package com.lowbudget.chess.model.board.array;

import static com.lowbudget.chess.model.Castling.CastlingRight.isKingRookAtStartSquare;
import static com.lowbudget.chess.model.Castling.CastlingRight.isQueenRookAtStartSquare;
import static com.lowbudget.chess.model.Piece.isKingAtStartingSquare;

import java.util.*;
import java.util.stream.Stream;

import com.lowbudget.chess.model.*;
import com.lowbudget.chess.model.Castling.CastlingRight;
import com.lowbudget.chess.model.Castling.CastlingType;
import com.lowbudget.chess.model.Move.Disambiguation;
import com.lowbudget.chess.model.Move.MoveType;
import com.lowbudget.chess.model.board.*;
import com.lowbudget.chess.model.board.array.BoardStates.BoardState;
import com.lowbudget.chess.model.board.array.MoveGenerator.KingState;
import com.lowbudget.chess.model.notation.fen.FenBoard;
import com.lowbudget.chess.model.notation.fen.FenNotation;
import com.lowbudget.chess.model.notation.pgn.PgnGame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>A chess board representation that uses array like structures to store piece information.</p>
 * <p>This is the slowest of all the possible representations however its advantages are that it is simpler to
 * understand and it should be sufficient for a chess front application.</p>
 * <p>The only downside is that even though this board is meant to support a chess front, it needs to be able to
 * support move generation (i.e. to check if a move is legal). The perft tests that can verify the correctness of
 * such an implementation however run slower comparing to other solutions (i.e. bit boards)</p>
 *
 * @author Administrator
 */
public class ArrayBoard implements Board, FenBoard {

	private static final Logger log = LoggerFactory.getLogger(ArrayBoard.class);
	private static final boolean DEBUG = false;//log.isDebugEnabled();

	/** Stores a list of previous board states so we can browse/restore them at will */
	private final BoardStates boardStates = new BoardStates();

	/** Stores and manages the pieces on the board */
	private final PiecesArray pieces;

	/** Keeps track of pieces captured so far */
	private final List<Piece> capturedPieces = new ArrayList<>();

	/**
	 * Keeps track of hashed versions of previous board positions so we can detect draws like three-fold repetition
	 * or 50 move rule
	 */
	private final BoardHash boardHash = new BoardHash();

	/** Current en-passant square (if any) */
	private Square enPassant = Square.NONE;

	/** Current side to move */
	private PlayerColor playingColor = PlayerColor.WHITE;

	/** Current castling rights */
	private final Castling castling = Castling.empty();

	/**
	 * The "half-move" counter. This counter is incremented after each player's move, starting from zero.
	 * The fifty-move rule applies when both players have made 50 moves (i.e. this counter reaches 100).
	 * The counter however is reset after a capture or a pawn move
	 */
	private int halfMoveClock = 0;

	/**
	 * Current move number. It starts from 1 not 0 and is incremented every time it is white's turn
	 * e.g. after 1. e4 e5 this increments to 2
	 */
	private int moveNumber = 1;

	/** Keeps track of the moves played so far */
	private final MoveList moveList = new MoveList();

	/** Indicates if this board is in setup mode or not - default is {@code false} */
	private boolean setupMode;

	public ArrayBoard() {
		this.pieces = new PiecesArray();
	}

	@Override
	public Iterable<Square> getAllOccupiedSquares() {
		return pieces.getAllOccupiedSquares();
	}

	@Override
	public String toFEN() {
		return FenNotation.convertToString(this);
	}

	@Override
	public void fromFEN(String fen) {
		FenBoard fenBoard = FenNotation.convertFromString(fen);
		set(fenBoard);

		// check if castling is possible
		removeInvalidCastlingRightsThatAreNotPossible();

		boardHash.saveBoardPosition(this);
		boardStates.setCurrentState(new BoardState(enPassant, castling, playingColor, halfMoveClock, moveNumber), new PiecesArray(this.pieces), Collections.emptyList());
	}

	@Override
	public void fromPGN(PgnGame pgnGame) {
		fromFEN(pgnGame.getFenPosition());

		List<Move> moveList = pgnGame.getMoveList();
		for (Move pgnMove : moveList) {
			// loaded moves need to be fixed with respect to the starting square because they usually don't have
			// the starting rank, starting file or both
			Move fixedMove = fixPgnMove(pgnMove);
			makePlayerMove(fixedMove.getFrom(), fixedMove.getTo(), fixedMove.getPromotionType());
		}
	}

	@Override
	public void setSetupMode(boolean value) {
		this.setupMode = value;
	}

	@Override
	public boolean isInSetupMode() {
		return setupMode;
	}

	@Override
	public boolean isRayEmpty(Direction direction, Square from, Square to) {
		return pieces.isRayEmpty(direction, from, to);
	}

	@Override
	public boolean isRayEmptyIgnoringKing(Direction direction, Square from, Square to, PlayerColor colorOfKingToIgnore) {
		return pieces.isRayEmptyIgnoringKing(direction, from, to, colorOfKingToIgnore);
	}

	@Override
	public void movePieceInSetupMode(Square from, Square to) {
		if (!this.setupMode) {
			throw new IllegalStateException("Cannot setup piece, board is not in setup mode");
		}

		Piece piece = getPieceAt(from);
		if (piece == null) {
			throw new IllegalStateException("No piece exists at square: " + from);
		}

		checkSetupMoveIsValid(to, piece);

		removePiece(from);
		if (getPieceAt(to) != null) {
			removePiece(to);
		}
		setPieceAt(piece, to);
	}

	@Override
	public void setPieceInSetupMode(Piece piece, Square at) {
		Objects.requireNonNull(at);
		if (!this.setupMode) {
			throw new IllegalStateException("Cannot setup piece, board is not in setup mode");
		}
		if (getPieceAt(at) != null) {
			checkKingsAreNotDeleted(at);
			removePiece(at);
		}
		if (piece != null) {
			checkSetupMoveIsValid(at, piece);
			setPieceAt(piece, at);
		}
	}

	@Override
	public void undoPlayerMove() {
		String boardKey = boardHash.generateHash(this);
		boardHash.removeBoardPosition(boardKey);

		Move move = moveList.getCurrentMove();
		undoMove(move);
		if (move.getCaptured() != null) {
			capturedPieces.remove(move.getCaptured());
		}
	}

	@Override
	public GameState makePlayerMove(Square from, Square to, PieceType promotionType) {
		Piece piece = getPieceAt(from);
		if (piece == null) {
			throw new IllegalMoveException("board.illegal.move.no.piece.msg", from);
		}
		if (piece.hasOppositeColor(playingColor)) {
			throw new IllegalMoveException("board.illegal.turn.msg", playingColor);
		}
		if (!isLegalMove(piece, from, to, promotionType)) {
			throw new IllegalMoveException("board.illegal.move.msg", from.toString() + "-" + to.toString());
		}

		// for king and pawns we do not need any further disambiguation information
		Disambiguation disambiguation = (piece.isKing() || piece.isPawn()) ? Disambiguation.NONE : resolveMoveDisambiguation(piece, from, to);

		saveFullBoardState();

		GameState result = doMove(piece, from, to, promotionType, disambiguation);

		Move last = moveList.getCurrentMove();
		if (last.getCaptured() != null) {
			capturedPieces.add(last.getCaptured());
		}

		boolean threeFoldRepetition = this.boardHash.saveBoardPosition(this);
		if (threeFoldRepetition) {
			result = GameState.DRAW_THREEFOLD_REPETITION;
		} else if (halfMoveClock == 100) {
			result = GameState.DRAW_FIFTY_MOVES;
		} else if (result == GameState.PLAYING && hasInsufficientMaterial()) {
			result = GameState.DRAW_INSUFFICIENT_MATERIAL;
		}

		boardStates.setCurrentState(new BoardState(enPassant, castling, playingColor, halfMoveClock, moveNumber), new PiecesArray(this.pieces), new ArrayList<>(this.capturedPieces));
		return result;
	}

	@Override
	public void makeMove(Move move) {
		saveBasicBoardState();
		doMove(move.getPiece(), move.getFrom(), move.getTo(), move.getPromotionType(), move.getDisambiguation());
	}

	@Override
	public List<Move> findLegalMoves(Piece piece, Square from) {
		return MoveGenerator.getAllLegalMovesFromSquare(this, piece.color(), from);
	}

	@Override
	public void browseMoveList(MoveList.BrowseType browseType) {
		moveList.browse(browseType);
		int moveIndex = moveList.getCurrentMoveIndex();

		// the state index always equals: move index + 1. Note however that index + 1 is not a valid index for the list
		// where the states are stored. In that case the current board state will be used
		restoreFullBoardState(moveIndex + 1);
	}

	@Override
	public void undoMove(Move move) {
		Piece capturedPiece = move.getCaptured();
		Piece piece = move.getPiece();

		removePiece(move.getTo());
		setPieceAt(piece, move.getFrom());

		// if castling restore rooks
		if (move.isCastling()) {
			CastlingRight castlingRight = CastlingRight.of(piece.color(), move.isKingSideCastling());
			Square castledRookSquare = castlingRight.castledRookSquare();
			Piece rook = getPieceAt(castledRookSquare);
			if (rook == null) {
				throw new IllegalStateException("Expected to find the rook after castling at: " + castledRookSquare);
			}
			removePiece(castledRookSquare);
			setPieceAt(rook, castlingRight.originalRookSquare());
		}

		if (capturedPiece != null) {
			if (move.isEnPassantCapture()) {
				Square cp = move.getTo(); // this is the en-passant square not the piece's actual square
				Square p = Square.of(move.getFrom().rank(), cp.file()); // the pawn that is captured en-passant is actually at the same rank with the enemy that captures it
				setPieceAt(capturedPiece, p);
			} else {
				setPieceAt(capturedPiece, move.getTo());
			}
		}

		BoardState previousBoardState = boardStates.removeLastBoardState();
		restoreBasicBoardState(previousBoardState);
		this.moveList.removeLast();
	}

	@Override
	public Move getCurrentMove() {
		return moveList.getCurrentMove();
	}

	@Override
	public Piece getEnPassantCapturablePawn(Piece piece, Square from, Square to) {
		Piece other = null;
		if (Piece.isPawnAtFifthRank(piece, from) && to.equals(enPassant)) {
			other = getPieceAt(from.rank(), enPassant.file());
		}
		return other;
	}

	@Override
	public boolean isSquareEmpty(Square p) {
		return getPieceAt(p) == null;
	}

	@Override
	public List<Square> getAllSquareAttacks(Square square, PlayerColor color) {
		return MoveGenerator.getAllSquareAttacks(this, square, color);
	}

	@Override
	public Piece getPieceAt(Square square) {
		return pieces.get(square);
	}

	@Override
	public Piece getPieceAt(int rank, int file) {
		return pieces.get(rank, file);
	}

	@Override
	public Castling getCastling() {
		return castling;
	}

	@Override
	public Square getEnPassant() {
		return enPassant;
	}

	@Override
	public PlayerColor getPlayingColor() {
		return playingColor;
	}

	@Override
	public int getHalfMoveClock() {
		return halfMoveClock;
	}

	@Override
	public int getMoveNumber() {
		return moveNumber;
	}

	@Override
	public List<Piece> getCapturedPieces() {
		return capturedPieces;
	}

	@Override
	public TabularMoveList getMoveList() {
		return moveList;
	}

	@Override
	public boolean isPinned(Piece piece, Square from, Square to) {
		Square kingSquare = getKingSquare(piece.color());

		Direction pinningRay = Direction.between(kingSquare, from);
		if (!pinningRay.isVerticalOrHorizontalOrDiagonal()) {
			return false;
		}

		Direction rayPieceMove = Direction.between(from, to);
		if (rayPieceMove.isInvalid()) {
			// this is illegal, we are given an invalid move - should never happen
			throw new IllegalMoveException("The piece tries to perform an illegal move: " + piece + ", " + from + "-" + to);
		}

		if (rayPieceMove == pinningRay || rayPieceMove.isReverse(pinningRay)) {
			// even if the piece is pinned (we don't know yet if there is a slider pinning it),
			// it tries to move along the pinning ray, this is legal
			return false;
		}

		boolean specialEnPassantCase =
				Piece.isPawnAtFifthRank(piece, from) &&					// is pawn at fifth rank
				Piece.isAtFifthRank(piece.color(), kingSquare) &&		// is king at fifth rank
				getEnPassantCapturablePawn(piece, from, to) != null		// we can capture en-passant
				;

		if (DEBUG) {
			log.debug("Checking for pin: {} {} {}", piece, from, to);
		}

		// iterate over all enemy sliders, first get the corresponding slider piece types with the enemy color
		for (Piece slider : Piece.getSliderPieces(piece.color().opposite())) {
			// for each slider type we get the set of all current enemy sliders (i.e. all black rooks)
			Iterable<Square> sliderSquares = pieces.getPieceGroup(slider);
			for (Square sliderSquare : sliderSquares) {
				// for each slider now check if there is a ray from the king to the slider
				Direction rayKingToSlider = Direction.between(kingSquare, sliderSquare);
				if (rayKingToSlider != pinningRay || !slider.canMoveAlong(rayKingToSlider)) {
					continue;
				}

				// at this point the king, the moving piece and the slider all lie along the same direction

				// handle special case of horizontally pinned pawn for en-passant capture
				if (specialEnPassantCase && isEnPassantAtFifthRankPinned(pinningRay, kingSquare, sliderSquare, from)) {
					return true;
				}

				// all that is left, is to verify that the rays (from king to piece and from piece to slider)
				// are not blocked i.e. no other piece lies in-between the king-piece-slider ray
				if (isRayEmpty(pinningRay, kingSquare, from) && isRayEmpty(pinningRay, from, sliderSquare)) {
					return true;
				}
			}
		}
		return false;
	}

	private boolean isEnPassantAtFifthRankPinned(Direction pinningRay, Square kingSquare, Square sliderSquare, Square from) {
		// special en-passant case: the king, the pawn to move (that will capture en-passant), the pawn that can be
		// captured en-passant and the slider all lie horizontally at the fifth rank.
		// There are two cases regarding the attacker's position: (k: king, ep: the pawn that can be captured en-passant,
		// p: the pawn performing the capture, s: slider
		// 1) en-passant on the left:   k ---- ep p ----- s
		// 2) en-passant on the right:  k ---- p ep ----- s

		Square enPassantCaptureSquare = Square.of(from.rank(), enPassant.file());
		Square firstSquare;
		Square secondSquare;
		if (Math.abs(kingSquare.fileDistance(enPassant)) < Math.abs(kingSquare.fileDistance(from))) {
			// the capturable pawn is between king and the pawn
			firstSquare = enPassantCaptureSquare;
			secondSquare = from;
		} else {
			// the capturable pawn is between pawn and the slider
			firstSquare = from;
			secondSquare = enPassantCaptureSquare;
		}
		return isRayEmpty(pinningRay, kingSquare, firstSquare) && isRayEmpty(pinningRay, secondSquare, sliderSquare);
	}

	@Override
	public boolean isLegalMove(Piece piece, Square from, Square to, PieceType promotionType) {
		Move move = Move.of(piece, from, to, promotionType);
		return MoveGenerator.isLegalMove(this, move);
	}

	@Override
	public Square getKingSquare(PlayerColor color) {
		return color.isWhite() ? pieces.getWhiteKingSquare() : pieces.getBlackKingSquare();
	}

	@Override
	public Iterable<Square> getGroupSquares(Piece piece) {
		return pieces.getPieceGroup(piece);
	}

	private GameState doMove(Piece piece, Square from, Square to, PieceType promotionType, Disambiguation disambiguation) {
		if (DEBUG) {
			log.debug("Playing move for piece: {} {} {} (promotionType: {})", piece, from, to, promotionType);
		}

		boolean twoSquareAdvance = false;
		EnumSet<MoveType> moveType = EnumSet.noneOf(MoveType.class);

		Piece capturedPiece = getPieceAt(to);
		Square captureSquare = to;
		boolean isCapture = capturedPiece != null;
		if (isCapture) {
			moveType.add(MoveType.CAPTURE);
		} else {
			capturedPiece = getEnPassantCapturablePawn(piece, from, to);
			if (capturedPiece != null) {
				isCapture = true;
				captureSquare = Square.of(from.rank(), enPassant.file());
				moveType.add(MoveType.EN_PASSANT_CAPTURE);
			}
		}

		if (isCapture) {
			// capture piece
			capturePiece(piece, from, to, capturedPiece, captureSquare);
			updateCastlingRights(capturedPiece, captureSquare);
			updateCastlingRights(piece, from);
			if (piece.isPawn()) {
				promotePawnIfAtLastRank(piece, to, piece.asType(promotionType));
			}
		} else {
			switch (piece.getType()) {
				case PAWN:
					movePieceNormally(piece, from, to);
					twoSquareAdvance = Piece.isPawnTwoSquareDistance(from, to);
					promotePawnIfAtLastRank(piece, to, piece.asType(promotionType));
					break;
				case KING:
					if (CastlingType.isKingSideDistance(from, to)) {
						castle(piece, from, to, CastlingRight.of(piece.color(), true));
						moveType.add(MoveType.KINGSIDE_CASTLING);
					} else if (CastlingType.isQueenSideDistance(from, to)) {
						castle(piece, from, to, CastlingRight.of(piece.color(), false));
						moveType.add(MoveType.QUEENSIDE_CASTLING);
					} else {
						// normal king move
						movePieceNormally(piece, from, to);
					}
					updateCastlingRights(piece, from);
					break;
				case ROOK:
					movePieceNormally(piece, from, to);
					updateCastlingRights(piece, from);
					break;
				default:
					movePieceNormally(piece, from, to);
					break;
			}
		}

		enPassant = twoSquareAdvance ? Piece.getNewEnPassantSquare(piece, from, to) : Square.NONE;

		// we have a capture or a pawn moved - reset 50-move rule
		if (isCapture || piece.isPawn()) {
			halfMoveClock = 0;
		} else {
			halfMoveClock++;
		}

		// the other side now plays
		playingColor = playingColor.opposite();

		if (playingColor.isWhite()) {
			moveNumber++;
		}

		GameState result = checkGameState(playingColor, Move.of(piece, from, to, promotionType));

		if (result == GameState.CHECK) {
			moveType.add(MoveType.CHECK);
		} else if (result == GameState.CHECKMATE) {
			moveType.add(MoveType.CHECKMATE);
		}

		moveList.addMove( new Move(piece, from, to, capturedPiece, promotionType, moveType, disambiguation) );

		if (DEBUG) {
			log.debug("Move ended, Game state: {}", result);
		}
		return result;
	}

	private void setPieceAt(Piece piece, Square square) {
		checkSquareEmpty(square);

		pieces.set(piece, square);
	}

	private Disambiguation resolveMoveDisambiguation(Piece piece, Square from, Square to) {
		Disambiguation result = Disambiguation.NONE;
		Iterable<Square> samePieceTypeSquares = pieces.getPieceGroup(piece);
		boolean existsPieceWithSameRank = false;
		boolean existsPieceWithSameFile = false;
		boolean otherPiecesCanMoveToSameSquare = false;

		// for each piece of the same type check if it is possible to move to the same target square
		for (Square s : samePieceTypeSquares) {
			if (s.equals(from)) {
				continue;
			}

			boolean isLegalMove = isLegalMove(piece, s, to, null);
			otherPiecesCanMoveToSameSquare |= isLegalMove;
			if (isLegalMove) {
				existsPieceWithSameFile |= s.file() == from.file();
				existsPieceWithSameRank |= s.rank() == from.rank();

				if (existsPieceWithSameFile && existsPieceWithSameRank) {
					break;
				}
			}
		}
		if (otherPiecesCanMoveToSameSquare) {
			if (!existsPieceWithSameFile) {
				result = Disambiguation.FILE;
			} else if (!existsPieceWithSameRank) {
				result = Disambiguation.RANK;
			} else {
				result = Disambiguation.BOTH;
			}
		}
		return result;
	}

	private void removeInvalidCastlingRightsThatAreNotPossible() {
		for (CastlingRight castlingRight: CastlingRight.values()) {
			if (castling.hasRight(castlingRight)) {
				Square kingSquare = getKingSquare(castlingRight.color());
				Piece king = getPieceAt(kingSquare);
				Piece rook = getPieceAt(castlingRight.originalRookSquare());
				Piece expectedRook = castlingRight.color().isWhite() ? Piece.ROOK_WHITE : Piece.ROOK_BLACK;
				if (!isKingAtStartingSquare(king, kingSquare) || !expectedRook.equals(rook)) {
					castling.removeRight(castlingRight);
				}
			}
		}
	}

	private void checkSetupMoveIsValid(Square square, Piece pieceToSet) {
		// check that we do not place a pawn at first or eighth rank
		if (pieceToSet.isPawn() && (pieceToSet.isAtLastRank(square) || pieceToSet.isAtFirstRank(square))) {
			throw new IllegalMoveException("app.board.error.cannot.set.pawn");
		}

		// check that we do not overwrite a king
		checkKingsAreNotDeleted(square);
	}

	private void checkKingsAreNotDeleted(Square square) {
		// check that we do not overwrite a king
		Piece existingPiece = getPieceAt(square);
		if (existingPiece != null && existingPiece.isKing()) {
			throw new IllegalMoveException("app.board.error.cannot.delete.king");
		}
	}

	/**
	 * Returns the "complete" move that is represented by the incomplete move specified.
	 * The specified move is assumed to have an invalid "from" square (i.e. it is missing the rank, the file or both)
	 * which is the case for moves loaded from a PGN file.
	 * @param pgnMove an incomplete move where the starting square might have missing information
	 * @return a move that corresponds to the specified {@code pgnMove} with its starting square completed correctly
	 */
	private Move fixPgnMove(Move pgnMove) {
		if (pgnMove.isValid()) {
			return pgnMove;
		}
		Square from = pgnMove.getFrom();
		int rank = Square.isRankValid(from.rank()) ? from.rank() : -1;
		int file = Square.isFileValid(from.file()) ? from.file() : -1;

		Piece searchPiece = pgnMove.getPiece();
		Iterable<Square> pieceSquares = pieces.getPieceGroup(searchPiece);
		for (Square s : pieceSquares) {
			if (isLegalMove(searchPiece, s, pgnMove.getTo(), pgnMove.getPromotionType())) {
				if (rank != -1 && rank != s.rank()) {
					continue;
				}
				if (file != -1 && file != s.file()) {
					continue;
				}
				return Move.of(searchPiece, s, pgnMove.getTo(), pgnMove.getPromotionType());
			}
		}
		// if we reach here the pgn move is illegal for the current board state
		return Move.NONE;
	}

	private void saveBasicBoardState() {
		BoardState previousBoardState = new BoardState(enPassant, castling, playingColor, halfMoveClock, moveNumber);
		boardStates.add(previousBoardState);
	}

	private void saveFullBoardState() {
		BoardState previousBoardState = new BoardState(enPassant, castling, playingColor, halfMoveClock, moveNumber);

		previousBoardState.pieces = new PiecesArray(this.pieces);
		previousBoardState.capturedPieces = new ArrayList<>(this.capturedPieces);

		boardStates.add(previousBoardState);
	}

	private void restoreBasicBoardState(BoardState state) {
		enPassant = state.enPassant;
		castling.reset(state.castling);
		playingColor = state.color;
		halfMoveClock = state.halfMoveClock;
		moveNumber = state.moveNumber;
	}

	private void restoreFullBoardState(int stateIndex) {
		BoardState state = boardStates.getState(stateIndex);
		this.capturedPieces.clear();
		this.capturedPieces.addAll(state.capturedPieces);
		this.pieces.copyFrom(state.pieces);

		restoreBasicBoardState(state);
	}

	private GameState checkGameState(PlayerColor color, Move lastMove) {
		if (DEBUG) {
			log.debug("Checking game state after move...");
		}
		KingState kingState = MoveGenerator.getKingState(this, color, lastMove);
		GameState result = kingState.isInCheck() ? GameState.CHECK : GameState.PLAYING;

		// if we have a check, examine if we have a checkmate
		if (result == GameState.CHECK) {
			if (kingState.hasNoMoves()) {
				result = GameState.CHECKMATE;
			}
		} else if (kingState.hasNoMoves()) {
			// if we are not in check and cannot play any move, its a stalemate
			result = GameState.STALEMATE;
		}
		if (DEBUG) {
			log.debug("Game state checking completed. Result: {}", result);
		}
		return result;
	}

	/**
	 * Used by {@link #hasInsufficientMaterial()} method <u>exclusively</u>. Our perft tests show that resetting
	 * all the counters and reusing them, is a little bit faster then creating the map each time
	 */
	private final Map<PieceType, Counter> counters = Stream.of(PieceType.values()).collect(
			() -> new EnumMap<>(PieceType.class),
			(map, type) -> map.put(type, new Counter()),
			EnumMap::putAll
	);

	private boolean hasInsufficientMaterial() {
		// detect if there is insufficient material

		// reset all counters
		counters.values().forEach(Counter::reset);

		int blacks = 0;
		int whites = 0;

		// iterate all the pieces and count how many exist for each piece
		for (Piece piece : Piece.values()) {
			int count = pieces.getPieceGroupSize(piece);
			counters.get(piece.getType()).increase(count);
			if (piece.isBlack()) {
				blacks++;
			} else if (piece.isWhite()) {
				whites++;
			}
		}
		// if there are any pawns, rooks or queens then we have sufficient material
		if (counters.get(PieceType.PAWN).count() > 0 || counters.get(PieceType.ROOK).count() > 0 || counters.get(PieceType.QUEEN).count() > 0) {
			return false;
		}

		// if we reach here, we can only have kings, knights and bishops

		//noinspection SimplifiableIfStatement
		if ((whites == 2 && blacks == 1) || (whites == 1 && blacks == 2)) {
			// if there are 2 white pieces and 1 black or vice versa these should cover
			// the cases of KB-K, KN-K for both colors
			return true;
		}
		// Note: we do not take into account some unusual cases like one side having two or more bishops of the same
		// color (in this case a mate is impossible).

		// only kings are left
		return whites == 1 && blacks == 1;
	}

	/**
	 * Sets the status of this board from the specified fen board
	 * @param fenBoard the {@link FenBoard} according to which this board is set
	 */
	private void set(FenBoard fenBoard) {
		this.pieces.clear();
		this.capturedPieces.clear();
		this.boardHash.clear();
		this.moveList.clear();
		this.boardStates.clear();

		this.castling.reset(fenBoard.getCastling());
		this.playingColor = fenBoard.getPlayingColor();
		this.enPassant = fenBoard.getEnPassant();
		this.halfMoveClock = fenBoard.getHalfMoveClock();
		this.moveNumber = fenBoard.getMoveNumber();

		for (Square square : fenBoard.getAllOccupiedSquares()) {
			Piece piece = fenBoard.getPieceAt(square);
			pieces.set(piece, square);
		}
	}

	private void removePiece(Square p) {
		checkSquareNotEmpty(p);

		pieces.remove(p);
	}

	private void updateCastlingRights(Piece piece, Square p) {
		if (isKingAtStartingSquare(piece, p)) {
			castling.kingMoved(piece.color());
		} else if (piece.isRook()) {
			PlayerColor color = piece.color();
			if (isKingRookAtStartSquare(color, p)) {
				castling.kingRookMoved(color);
			} else if (isQueenRookAtStartSquare(color, p)) {
				castling.queenRookMoved(color);
			}
		}
	}

	private void movePieceNormally(Piece piece, Square from, Square to) {
		removePiece(from);
		setPieceAt(piece, to);
	}

	private void promotePawnIfAtLastRank(Piece piece, Square to, Piece promotion) {
		if (Piece.isAtLastRank(piece, to)) {
			if (promotion == null) {
				throw new IllegalMoveException("board.illegal.move.error.msg", "promotion piece cannot be null when the pawn is at the last rank");
			}
			removePiece(to);
			setPieceAt(promotion, to);
		}
	}

	private void castle(Piece king, Square kingSquare, Square castledKingSquare, CastlingRight castlingRight) {
		Square originalRookSquare = castlingRight.originalRookSquare();
		Square castledRookSquare = castlingRight.castledRookSquare();
		Piece rook = getPieceAt(originalRookSquare);

		removePiece(kingSquare);
		removePiece(originalRookSquare);
		setPieceAt(king, castledKingSquare);
		setPieceAt(rook, castledRookSquare);
	}

	private void capturePiece(Piece piece, Square from, Square to, Piece capturedPiece, Square captureSquare) {
		if (DEBUG) {
			log.debug("Capture piece: {} at square {}", capturedPiece, to);
		}
		removePiece(captureSquare);
		removePiece(from);
		setPieceAt(piece, to);
	}

	private void checkSquareEmpty(Square p) {
		Piece existed = getPieceAt(p);
		if (existed != null) {
			throw new IllegalStateException("Square: " + p + " is not empty!");
		}
	}

	private void checkSquareNotEmpty(Square p) {
		Piece existed = getPieceAt(p);
		if (existed == null) {
			throw new IllegalStateException("Square: " + p + " is empty!");
		}
	}
}
