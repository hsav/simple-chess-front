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

import com.lowbudget.chess.model.*;
import com.lowbudget.chess.model.Castling.CastlingRight;
import com.lowbudget.chess.model.board.Board;
import com.lowbudget.chess.model.board.MovePredicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static com.lowbudget.chess.model.board.MovePredicate.*;

/**
 * Provides move generation functionality
 */
public class MoveGenerator {
	private static final Logger log = LoggerFactory.getLogger(MoveGenerator.class);
	private static final boolean DEBUG = false;//log.isDebugEnabled();

	private MoveGenerator() {}

	/**
	 * Generates all legal moved of the board for the current side to move
	 * @param board the board for which all legal moves will be generated
	 * @return a list with all the generated moves
	 */
	public static List<Move> allLegalMoves(Board board) {
		return searchAllLegalMoves(board, board.getPlayingColor(), allMoves());
	}

	/**
	 * Generates all legal moves starting from the specified square
	 */
	static List<Move> getAllLegalMovesFromSquare(Board board, PlayerColor color, Square from) {
		return searchAllLegalMoves(board, color, startingFrom(from));
	}

	/**
	 * Checks if the move specified is legal
	 */
	static boolean isLegalMove(Board board, Move move) {
		List<Move> legalMoves = searchAllLegalMoves(board, move.getPiece().color(), specificMoveOnly(move));
		return legalMoves.contains(move);
	}

	/**
	 * Returns the state for the king of the color specified by also taking into account the last move played
	 * @param board the current board
	 * @param color the color of the king
	 * @param lastMove the last move played
	 * @return a {@link KingState} containing the information if the king is in check and if there any valid moves
	 * available for the side of the specified color
	 */
	static KingState getKingState(Board board, PlayerColor color, Move lastMove) {
		Context context = Context.of(board, firstMoveOnly(), 1);
		boolean inCheck = legalMovesForKingState(context, color, lastMove);
		boolean hasNoMoves = context.results.size() == 0;
		return new KingState(inCheck, hasNoMoves);
	}

	/**
	 * Searches all legal moves that satisfy the specified predicate
	 */
	private static List<Move> searchAllLegalMoves(Board board, PlayerColor sideToMove, MovePredicate movePredicate) {
		Context context = Context.of(board, movePredicate);
		legalMovesForKingState(context, sideToMove, Move.NONE);
		return context.results;
	}

	/**
	 * <p>Main method that is responsible to generate legal moves.</p>
	 * <p>This method will take into account the king's state and will generated either check evasions moves only if
	 * king is in check or all the legal moves otherwise.</p>
	 * @param context the generation context to use (contains information about the board, the predicate to filter moves
	 *                and collects any moves found)
	 * @param sideToMove the color of the side for which legal moves will be generated
	 * @param lastMove the last move played (if any)
	 * @return {@code true} if the king of the color specified is currently in check, {@code false} otherwise
	 */
	private static boolean legalMovesForKingState(Context context, PlayerColor sideToMove, Move lastMove) {
		Square kingSquare = context.board.getKingSquare(sideToMove);

		if (DEBUG) {
			log.debug("Generating all legal moves. First checking if king is in check ...");
		}
		List<Square> kingAttackers = lastMove.isValid()
				? attacksToKingSquareByLastMove(context.board, kingSquare, lastMove)
				: attacksToSquare(context.board, kingSquare, sideToMove.opposite(), 2);

		int attackers = kingAttackers.size();
		boolean isKingInCheck = attackers > 0;
		if (DEBUG) {
			log.debug("King is in check? {}, attackers: {}", isKingInCheck, kingAttackers);
		}
		if (attackers == 0) {
			legalMovesWhenKingIsNotInCheck(context, sideToMove);
		} else if (attackers > 1) {
			legalMovesWhenKingIsInDoubleCheck(context, kingSquare);
		} else {
			legalMovesWhenKingIsInSingleCheck(context, sideToMove, kingSquare, kingAttackers.get(0));
		}
		if (DEBUG) {
			log.debug("Move generation finished.");
		}
		return isKingInCheck;
	}

	private static void legalMovesWhenKingIsNotInCheck(Context context, PlayerColor sideToMove) {
		if (DEBUG) {
			log.debug("King is not in check. Generating all legal moves...");
		}
		// no check - just generate all legal moves with the predicate specified by the caller
		for (Piece piece : Piece.getPieces(sideToMove)) {
			Iterable<Square> groupSquares = context.board.getGroupSquares(piece);
			for (Square from : groupSquares) {
				switch (piece.getType()) {
					case PAWN:
						movesForPawn(context, piece, from);
						break;
					case KING:
						movesForKing(context, false, piece, from);
						break;
					case KNIGHT:
						movesForKnight(context, piece, from);
						break;
					default:
						movesForSlider(context, piece, from);
						break;
				}
				if (context.movePredicate.isSatisfied()) {
					return;
				}
			}
			if (context.movePredicate.isSatisfied()) {
				return;
			}
		}
	}

	private static void legalMovesWhenKingIsInDoubleCheck(Context context, Square kingSquare) {
		if (DEBUG) {
			log.debug("King is in double check. Generating only king moves...");
		}
		// double check - only king moves are allowed
		Piece king = context.board.getPieceAt(kingSquare);
		movesForKing(context, true, king, kingSquare);
	}

	private static void legalMovesWhenKingIsInSingleCheck(Context context, PlayerColor sideToMove, Square kingSquare, Square attackerSquare) {
		// one attacker - check how to defend
		if (DEBUG) {
			log.debug("King is in check. Generating check evasion moves...");
		}
		// generate legal moves for the king
		Piece king = context.board.getPieceAt(kingSquare);
		movesForKing(context, true, king, kingSquare);
		if (context.movePredicate.isSatisfied()) {
			return;
		}

		// generate moves that can capture or block the attacker
		Direction attackRay = Direction.between(kingSquare, attackerSquare);
		allCheckEvasionMoves(context, attackRay, sideToMove, kingSquare, attackerSquare);
	}

	private static void allCheckEvasionMoves(Context context, Direction attackRay, PlayerColor defenderColor, Square kingSquare, Square attackerSquare) {
		for (Square raySquare : attackRay.closedPath(kingSquare, attackerSquare)) {
			checkEvasionMovesAttackingOrBlockingRaySquare(context, defenderColor, raySquare);
			if (context.movePredicate.isSatisfied()) {
				return;
			}
		}
	}

	private static void checkEvasionMovesAttackingOrBlockingRaySquare(Context context, PlayerColor defenderColor, Square raySquare) {
		for (Piece piece : Piece.getPieces(defenderColor)) {
			if (piece.isKing()) {
				continue;
			}

			Iterable<Square> groupSquares = context.board.getGroupSquares(piece);

			if (piece.isPawn()) {
				checkEvasionMovesForPawns(context, piece, raySquare);
			} else if (piece.isKnight()) {
				checkEvasionMovesForKnights(context, piece, groupSquares, raySquare);
			} else {
				checkEvasionMovesForSliders(context, piece, groupSquares, raySquare);
			}
			if (context.movePredicate.isSatisfied()) {
				return;
			}
		}
	}

	private static void checkEvasionMovesForPawns(Context context, Piece pawn, Square raySquare) {
		if (DEBUG) {
			log.debug("Generating pawn check evasion moves to ray square: {}", raySquare);
		}

		Piece kingAttacker = context.board.getPieceAt(raySquare);
		if (kingAttacker != null) {
			// we are looking for pawn captures of the king attacker
			checkEvasionMovesForPawnCaptures(context, pawn, raySquare);
			if (context.movePredicate.isSatisfied()) {
				return;
			}

			Square enPassant = context.board.getEnPassant();
			if (kingAttacker.isPawn() && enPassant.isValid()) {
				Direction down = kingAttacker.isWhite() ? Direction.DOWN : Direction.UP;
				Square previous = down.next(raySquare);
				if (enPassant.equals(previous)) {
					// the attacker can be captured en-passant
					checkEvasionMovesForPawnCaptures(context, pawn, enPassant);
				}
			}
		} else {
			// one square advance: possible if the pawn is not blocked
			Direction down = pawn.isWhite() ? Direction.DOWN : Direction.UP;
			Square oneSquareDown = down.next(raySquare);

			if (oneSquareDown.isValid()) {
				Piece pieceOneSquareDown = context.board.getPieceAt(oneSquareDown);
				boolean oneSquareDownOccupied = pieceOneSquareDown != null;

				if (oneSquareDownOccupied) {
					// note the last check for pin: if the pawn is pinned for the one-square advance it will also be pinned for the
					// two-square advance - we don't need to perform two checks
					if (pawn.equals(pieceOneSquareDown) && !context.board.isPinned(pawn, oneSquareDown, raySquare)) {
						context.addPawnPromotionMoves(pawn, oneSquareDown, raySquare);
					}
				} else {
					// two-square advance: possible only if:
					// a) one-square advance is already possible
					// b) the pawn is at its starting rank
					// c) the 2nd rank is not blocked
					Square twoSquareDown = down.next(oneSquareDown);
					if (twoSquareDown.isValid() && pawn.equals(context.board.getPieceAt(twoSquareDown)) && Piece.isPawnAtStartingSquare(pawn, twoSquareDown) && !context.board.isPinned(pawn, twoSquareDown, raySquare)) {
						context.addMove(pawn, twoSquareDown, raySquare);
					}
				}
			}
		}
	}

	private static void checkEvasionMovesForPawnCaptures(Context context, Piece pawn, Square raySquare) {
		for (Square attack : getAttacksToSquare(pawn, raySquare)) {
			if (pawn.equals(context.board.getPieceAt(attack)) && !context.board.isPinned(pawn, attack, raySquare)) {
				if (context.addPawnPromotionMoves(pawn, attack, raySquare)) {
					return;
				}
			}
		}
	}

	private static void checkEvasionMovesForKnights(Context context, Piece knight, Iterable<Square> defenderSquares, Square raySquare) {
		if (DEBUG) {
			log.debug("Generating knight check evasion moves to ray square: {}", raySquare);
		}

		// due to the way the knight moves (it can never move along the pinning ray), we only need to check for pins once
		boolean checkedPinned = false;
		boolean isPinned = false;
		for (Square from : defenderSquares) {
			Direction d = Direction.between(from, raySquare);
			if (d.isKnight()) {
				if (!checkedPinned) {
					isPinned = context.board.isPinned(knight, from, raySquare);
					checkedPinned = true;
				}
				if (!isPinned && context.addMove(knight, from, raySquare)) {
					return;
				}
			}
		}
	}

	private static void checkEvasionMovesForSliders(Context context, Piece slider, Iterable<Square> defenderSquares, Square raySquare) {
		if (DEBUG) {
			log.debug("Generating slider check evasion moves to ray square: {}", raySquare);
		}

		for (Square defenderSquare : defenderSquares) {
			Direction direction = Direction.between(defenderSquare, raySquare);
			if (slider.canMoveAlong(direction) && context.board.isRayEmpty(direction, defenderSquare, raySquare) && !context.board.isPinned(slider, defenderSquare, raySquare)) {
				if (context.addMove(slider, defenderSquare, raySquare)) {
					return;
				}
			}
		}
	}

	private static void movesForKing(Context context, boolean isCheck, Piece king, Square kingSquare) {
		PlayerColor enemyColor = king.color().opposite();

		if (DEBUG) {
			log.debug("Generating king moves starting from: {}", kingSquare);
		}

		// first handle all normal moves for the squares around the king
		boolean isKingLeftSquareSafe = false;
		boolean isKingRightSquareSafe = false;
		for (Square p : getAttacksFromSquare(king, kingSquare)) {
			if (canBeOccupiedByPiece(context.board, king, p) && isSquareSafe(context, p, enemyColor)) {
				if (kingSquare.isAtSameRank(p)) {
					if (p.isOnRight(kingSquare)) {
						isKingRightSquareSafe = true;
					} else if (p.isOnLeft(kingSquare)) {
						isKingLeftSquareSafe = true;
					}
				}
				if (context.addMove(king, kingSquare, p)) {
					return;
				}
			}
		}

		// add castling moves (if any)
		// note that if during move generation we have found that both squares next to the king are unsafe then
		// no castling is possible
		if (!isCheck && (isKingRightSquareSafe || isKingLeftSquareSafe) && context.board.getCastling().hasAnyCastlingRights(king.color())) {
			movesForKingCastling(context, king, kingSquare, isKingRightSquareSafe, isKingLeftSquareSafe);
		}
	}

	private static void movesForKingCastling(Context context, Piece king, Square kingSquare, boolean isKingRightSquareSafe, boolean isKingLeftSquareSafe) {
		PlayerColor kingColor = king.color();
		PlayerColor enemyColor = king.color().opposite();

		// add castling moves (if any)
		Castling castling = context.board.getCastling();
		for (CastlingRight castlingRight : castling.rights(kingColor)) {

			Square originalRookSquare = castlingRight.originalRookSquare();
			Piece rook = context.board.getPieceAt(originalRookSquare);
			// should never happen
			if (rook == null || !rook.isRook() || rook.hasOppositeColor(kingColor)) {
				throw new IllegalStateException("Castling right found " + castlingRight + " but rook is invalid: " + rook);
			}

			Direction direction = castlingRight.direction();

			// use the information we already have for the first square in the king's path in case we can reject it
			if (castlingRight.isKingSide()) {
				boolean isRightSquareSafe = isKingRightSquareSafe && context.board.isSquareEmpty(direction.next(kingSquare));
				if (!isRightSquareSafe) {
					continue;
				}
			} else if (castlingRight.isQueenSide()) {
				boolean isLeftSquareSafe = isKingLeftSquareSafe && context.board.isSquareEmpty(direction.next(kingSquare));
				if (!isLeftSquareSafe) {
					continue;
				}
			}

			Square castledKingSquare = castlingRight.castledKingSquare();

			// check if the path to the castling square is clear for the king
			boolean isPathSafe = context.board.isSquareEmpty(castledKingSquare) && isSquareSafe(context, castledKingSquare, enemyColor);

			// if this is queen-side castling also check that the rook path is clear (but ignore attacks)
			if (castlingRight.isQueenSide()) {
				Square to = Square.of(originalRookSquare.rank(), originalRookSquare.file() + 1);
				isPathSafe = isPathSafe && context.board.isSquareEmpty(to);
			}

			// if the path is clear the king can castle
			if (isPathSafe) {
				if (context.addMove(king, kingSquare, castledKingSquare)) {
					return;
				}
			}
		}
	}

	private static void movesForPawn(Context context, Piece piece, Square from) {
		if (DEBUG) {
			log.debug("Generating pawn moves starting from: {}", from);
		}

		// for pawns there are 5 legal moves we need to examine: one-square advance, two-square advance,
		// 2 pawn captures and en-passant capture.

		movesForPawnAdvances(context, piece, from);
		if (context.movePredicate.isSatisfied()) {
			return;
		}

		// captures: possible if there are pieces diagonally on adjusting files
		for (Square p : getAttacksFromSquare(piece, from)) {
			movesForPawnCaptures(context, piece, from, p);
			if (context.movePredicate.isSatisfied()) {
				return;
			}
		}
	}

	private static void movesForPawnAdvances(Context context, Piece piece, Square from) {
		// one square advance: possible if the pawn is not blocked
		Direction up = piece.isWhite() ? Direction.UP : Direction.DOWN;
		Square oneSquareUp = up.next(from);

		// note the last check for pin: if the pawn is pinned for the one-square advance it will also be pinned for the
		// two-square advance - we don't need to perform two checks
		if (canBeOccupiedByPawn(context.board, oneSquareUp) && !context.board.isPinned(piece, from, oneSquareUp)) {
			if (context.addPawnPromotionMoves(piece, from, oneSquareUp)) {
				return;
			}

			// two-square advance: possible only if:
			// a) one-square advance is already possible
			// b) the pawn is at its starting rank
			// c) the 2nd rank is not blocked
			Square twoSquareUp = up.next(oneSquareUp);
			if (canBeOccupiedByPawn(context.board, twoSquareUp) && Piece.isPawnAtStartingSquare(piece, from)) {
				context.addMove(piece, from, twoSquareUp);
			}
		}
	}

	private static void movesForPawnCaptures(Context context, Piece piece, Square from, Square to) {
		// check if we can capture an enemy piece either directly or via en-passant
		if (canPawnCaptureAtSquare(context.board, piece, from, to) && !context.board.isPinned(piece, from, to) ) {
			context.addPawnPromotionMoves(piece, from, to);
		}
	}

	private static void movesForSlider(Context context, Piece piece, Square from) {
		if (DEBUG) {
			log.debug("Generating slider moves for piece: {} starting from: {}", piece, from);
		}

		Iterable<Direction> directions = piece.getMoveDirections();
		for (Direction direction : directions) {
			movesForSliderAlongDirection(context, direction, piece, from);
			if (context.movePredicate.isSatisfied()) {
				return;
			}
		}
	}

	private static void movesForSliderAlongDirection(Context context, Direction direction, Piece piece, Square from) {
		// traverse along the direction until we fall out of the board or we are blocked by a piece (enemy or friend)

		// note that the pinning state of a slider changes only per direction (i.e. if a move along a direction is not
		// pinned then all the moves for that direction are not pinned and vice-versa)
		boolean checkedPin = false;
		boolean isPinned = false;

		for (Square p : direction.openPath(from)) {
			if (canBeOccupiedByPiece(context.board, piece, p)) {
				if (!checkedPin) {
					isPinned = context.board.isPinned(piece, from, p);
					checkedPin = true;
				}
				if (!isPinned && context.addMove(piece, from, p)) {
					return;
				}
			}
			if (!context.board.isSquareEmpty(p)) {
				break;
			}
		}
	}

	private static void movesForKnight(Context context, Piece piece, Square from) {
		if (DEBUG) {
			log.debug("Generating knight moves starting from: {}", from);
		}
		// due to the way the knight moves (it can never move along the pinning ray), we only need to check for pins once
		boolean checkedPinned = false;
		boolean isPinned = false;
		for (Square p : getAttacksFromSquare(piece, from)) {
			if (canBeOccupiedByPiece(context.board, piece, p) ) {
				if (!checkedPinned) {
					isPinned = context.board.isPinned(piece, from , p);
					checkedPinned = true;
				}
				if (!isPinned && context.addMove(piece, from, p)) {
					return;
				}
			}
		}
	}

	private static List<Square> attacksToKingSquareByLastMove(Board board, Square kingSquare, Move lastMove) {
		final int max = 2;

		if (DEBUG) {
			log.debug("Searching if there are at least {} attacks to the king at {} after move: {}", max, kingSquare, lastMove);
		}
		List<Square> results = new ArrayList<>();
		boolean isPromotion = lastMove.getPromotionType() != null;
		Piece movedPiece = lastMove.getPiece();
		Square from = lastMove.getFrom();
		Square to = lastMove.getTo();
		Piece attacker = isPromotion ? movedPiece.asType(lastMove.getPromotionType()) : movedPiece;

		switch (attacker.getType()) {
			case PAWN:
				if (isPawnAttackingKing(attacker, to, kingSquare)) {
					results.add(to);
				}
				break;
			case KNIGHT:
				// note: this covers both cases - if the moving piece is a knight or if it was a pawn promoted to a knight
				attackByKnight(attacker, to, kingSquare, results);
				break;
			case BISHOP:
			case ROOK:
			case QUEEN:
				// note: this covers both cases - if the moving piece is a slider or if it was a pawn promoted to a slider
				attackBySlider(board, attacker, to, kingSquare, results);
				break;
		}

		// so far we have found either 0 or 1 attack to the king which is delivered by the moving piece

		// check if any of the rest of the sliders give a discovered check. If there is such slider the ray will
		// pass from the starting position of the moving piece and the king's square
		discoveredAttacksBySliders(board, attacker, from , to , kingSquare, max, results);
		return results;
	}

	@SuppressWarnings("SameParameterValue")
	private static void discoveredAttacksBySliders(Board board, Piece attacker, Square from, Square to, Square kingSquare, int max, List<Square> results) {
		Direction discoveredRay = Direction.between(from, kingSquare);
		if (discoveredRay.isVerticalOrHorizontalOrDiagonal()) {
			for (Square sliderSquare : board.getGroupSquares(attacker)) {
				if (sliderSquare.equals(to)) { // we have already checked this slider above
					continue;
				}
				if (Direction.between(sliderSquare, kingSquare) == discoveredRay) {
					attackBySlider(board, attacker, sliderSquare, kingSquare, results);
					if (results.size() == max) {
						break;
					}
				}
			}
		}
	}

	private static List<Square> attacksToSquare(Board board, Square target, PlayerColor attackColor, int max) {
		if (DEBUG) {
			log.debug("Searching attack count for square: {}", target);
		}
		List<Square> results = new ArrayList<>();
		Iterable<Piece> pieces = Piece.getPieces(attackColor);
		for (Piece attacker : pieces) {
			switch (attacker.getType()) {
				case KING:
					attackByKing(board, attackColor, target, results);
					break;
				case PAWN:
					attacksByAllPawns(board, attacker, target, max, results);
					break;
				case KNIGHT:
					attacksByAllKnights(board, attacker, target, max, results);
					break;
				case BISHOP:
				case ROOK:
				case QUEEN:
					attacksByAllSliders(board, attacker, target, max, results);
					break;
			}
			if (results.size() == max) {
				break;
			}
		}
		return results;
	}

	private static void attacksByAllPawns(Board board, Piece attacker, Square target, int max, List<Square> results) {
		for (Square pawnSquare : getAttacksToSquare(attacker, target)) {
			if (DEBUG) {
				log.debug("Checking if any {} pawn at {} attacks {}", attacker.color(), pawnSquare, target);
			}
			if (attacker.equals(board.getPieceAt(pawnSquare))) {
				results.add(pawnSquare);
				if (results.size() == max) {
					break;
				}
			}
		}
	}

	private static void attacksByAllKnights(Board board, Piece attacker, Square target, int max, List<Square> results) {
		for (Square knightSquare : board.getGroupSquares(attacker)) {
			attackByKnight(attacker, knightSquare, target, results);
			if (results.size() == max) {
				break;
			}
		}
	}

	private static void attacksByAllSliders(Board board, Piece attacker, Square target, int max, List<Square> results) {
		Iterable<Square> sliders = board.getGroupSquares(attacker);
		for (Square sliderSquare : sliders) {
			attackBySlider(board, attacker, sliderSquare, target, results);
			if (results.size() == max) {
				break;
			}
		}
	}

	private static void attackByKing(Board board, PlayerColor attackColor, Square target, List<Square> results) {
		Square kingSquare = board.getKingSquare(attackColor);
		if (DEBUG) {
			log.debug("Checking if {} king at {} attacks {}", attackColor, kingSquare, target);
		}
		if (kingSquare.isAdjacentTo(target)) {
			results.add(kingSquare);
		}
	}

	private static void attackByKnight(Piece attacker, Square knightSquare, Square target, List<Square> results) {
		if (DEBUG) {
			log.debug("Checking if {} knight at {} attacks {}", attacker.color(), knightSquare, target);
		}
		if (Direction.between(knightSquare, target).isKnight()) {
			results.add(knightSquare);
		}
	}

	private static void attackBySlider(Board board, Piece attacker, Square sliderSquare, Square target, List<Square> results) {
		if (DEBUG) {
			log.debug("Checking if {} slider at {} attacks {}", attacker, sliderSquare, target);
		}
		// is there a valid ray from the enemy to the square?
		Direction ray = Direction.between(sliderSquare, target);
		if (attacker.canMoveAlong(ray) && board.isRayEmptyIgnoringKing(ray, sliderSquare, target, attacker.color().opposite())) {
			results.add(sliderSquare);
		}
	}

	/**
	 * <p>Returns the squares from where the specified piece can attack the square specified.</p>
	 * @param attacker the piece for which we need to find the squares from where it can attack the {@code target} square
	 * @param target the square that is being attacked
	 * @return all the squares from where the {@code piece} can attack the {@code target} square.
	 * <p>Example: To answer the question "From which squares can a black pawn attack E2?"
	 * we will call this method as {@code getAttacksToSquare(Piece.PAWN_BLACK, Square.E2)} which will return
	 * the squares: {@code D3} and {@code F3}</p>
	 * <p>Note: the result of this method and {@link #getAttacksFromSquare(Piece, Square)} are identical except the
	 * case of pawns</p>
	 */
	private static Square[] getAttacksToSquare(Piece attacker, Square target) {
		switch (attacker.getType()) {
			case PAWN:
				return PreGenerated.getSquareAttacks(attacker.enemy(), target);
			case KNIGHT:
				return PreGenerated.getSquareAttacks(attacker, target);
			case KING:
				return PreGenerated.getSquareAttacks(attacker, target);
			default:
				throw new IllegalStateException("Tried to get attack moves from cache for a non-cached piece: " + attacker);
		}
	}

	/**
	 * <p>Returns the squares that are attacked from the specified piece standing at the square specified.</p>
	 * @param attacker the piece for which we need to find the squares it can attack
	 * @param from the square that the piece is standing at
	 * @return all the squares attacked by the {@code piece} standing at the {@code target} square.
	 * <p>Example: To answer the question "Which squares are attacked by a white pawn standing at E2?"
	 * we will call this method as {@code getAttacksFromSquare(Piece.PAWN_WHITE, Square.E2)} which will return
	 * the squares: {@code D3} and {@code F3}</p>
	 * <p>Note: the result of this method and {@link #getAttacksToSquare(Piece, Square)} are identical except the
	 * case of pawns</p>
	 */
	private static Square[] getAttacksFromSquare(Piece attacker, Square from) {
		switch (attacker.getType()) {
			case PAWN:
				return PreGenerated.getSquareAttacks(attacker, from);
			case KNIGHT:
				return PreGenerated.getSquareAttacks(attacker, from);
			case KING:
				return PreGenerated.getSquareAttacks(attacker, from);
			default:
				throw new IllegalStateException("Tried to get attack moves from cache for a non-cached piece: " + attacker);
		}
	}

	/**
	 * <p>Returns a list of all the squares from where pieces of the specified color can attack the square specified.</p>
	 * @param square the square to check if it is being attacked
	 * @param attackColor the color of the pieces that attack the square
	 * @return a list of all the squares that have pieces of the specified color that attack the square
	 */
	static List<Square> getAllSquareAttacks(Board board, Square square, PlayerColor attackColor) {
		if (DEBUG) {
			log.debug("Getting all attackers for square: {}", square);
		}
		List<Square> attackMoves = new ArrayList<>();
		for (Piece attacker : Piece.getPieces(attackColor)) {
			if (attacker.isSlider()) {
				// attacks of slider pieces
				getAllSquareAttacksBySliders(board, attacker, square, attackMoves);
			} else {
				// for attacks of non-sliders (i.e. pawns, kings and knights) we use the "super-piece" approach:
				// if we want to find the black pieces of some type attacking the square, assume a white piece
				// of the same type on the square and check which pieces it attacks itself. For example in the case where
				// the super-piece is a white pawn, we generate the possible attacks for it (the two diagonal squares in front of it).
				// If any of those squares is occupied by a black pawn then that pawn is one of the attackers of the original square.
				getAllSquareAttacksByNonSliders(board, attacker, square, attackMoves);
			}
		}
		if (DEBUG) {
			log.debug("Attackers found for square: {} : {}", square, attackMoves);
		}
		return attackMoves;
	}

	/**
	 * Finds any squares that attack the specified square if the attacker is a non-slider (pawn, king or knight).
	 * @param attacker      the type and color of the attacker
	 * @param square        the square being checked for attacks
	 * @param attackSquares   a list to add any attack squares found
	 */
	private static void getAllSquareAttacksByNonSliders(Board board, Piece attacker, Square square, List<Square> attackSquares) {
		// get the attack squares for the pawn/king/knight
		// for each attack square check if it is actually possible i.e. if a piece of the same type and color occupies
		// the square the move starts from
		for (Square p : getAttacksToSquare(attacker, square)) {
			Piece piece = board.getPieceAt(p);
			if (DEBUG) {
				log.debug("Checking if non-slider {} at {} attacks {}", attacker, p, square);
			}
			if (attacker.equals(piece)) {
				// the piece has the same type and color with the enemy and is not null, so we have found an attacker
				attackSquares.add(p);
			}
		}
	}

	/**
	 * Finds any squares that attack the specified square if the attacker is a slider piece.
	 * @param attacker      the type and color of the attacker
	 * @param square        the square being checked for attacks
	 * @param attackSquares   a list to add any attack squares found
	 */
	private static void getAllSquareAttacksBySliders(Board board, Piece attacker, Square square, List<Square> attackSquares) {
		// get the squares of all the enemy pieces of the same type (i.e. all the black rooks)
		Iterable<Square> attackerGroupSquares = board.getGroupSquares(attacker);

		PlayerColor defendColor = attacker.color().opposite();

		for (Square attackerSquare : attackerGroupSquares) {
			if (DEBUG) {
				log.debug("Checking if slider {} at {} attacks {}", attacker, attackerSquare, square);
			}

			// is there a valid ray from the enemy to the square?
			// if yes, we need to check if the ray is clear between the slider and the square
			// note however that the king of the opposite color needs to be ignored, otherwise the slider cannot "see" the
			// squares beyond the king, which has the side-effect that the squares away from the slider but along the ray
			// to be marked as safe, while for our purposes (i.e. to avoid a check) they aren't
			Direction ray = Direction.between(attackerSquare, square);
			if (attacker.canMoveAlong(ray) && board.isRayEmptyIgnoringKing(ray, attackerSquare, square, defendColor)) {
				attackSquares.add(attackerSquare);
			}
		}
	}

	private static boolean isPawnAttackingKing(Piece attacker, Square pawnSquare, Square kingSquare) {
		Square[] pawnAttacks = getAttacksFromSquare(attacker, pawnSquare);
		for (Square square : pawnAttacks) {
			if (kingSquare.equals(square)) {
				return true;
			}
		}
		return false;
	}

	private static boolean canBeOccupiedByPiece(Board board, Piece piece, Square target) {
		if (!target.isValid()) {
			return false;
		}
		Piece other = board.getPieceAt(target);
		return other == null || other.hasOppositeColor(piece);
	}

	private static boolean canBeOccupiedByPawn(Board board, Square target) {
		return target.isValid() && board.isSquareEmpty(target);
	}

	private static boolean canPawnCaptureAtSquare(Board board, Piece pawn, Square from, Square to) {
		Piece other = board.getPieceAt(to);
		if (other == null) {
			// no enemy at the capture square. check if this is an en-passant capture
			other = board.getEnPassantCapturablePawn(pawn, from, to);
		}
		return other != null && other.hasOppositeColor(pawn);
	}

	/**
	 * <p>Checks if a square is safe from attacks by any piece of the color specified.</p>
	 * <p>Note that there might be no piece at the specified square that could actually be captured. This method is
	 * useful to determine if it would be safe for a king to move to the square.</p>
	 *
	 * @param context the current move generation context
	 * @param square the square to check if it is safe
	 * @param attackColor the color of pieces that might attack the square
	 * @return {@code true} if the square is not attacked by any piece of the specified color, {@code false} otherwise
	 */
	private static boolean isSquareSafe(Context context, Square square, PlayerColor attackColor) {
		if (DEBUG) {
			log.debug("Checking if square {} is attacked by color {}", square, attackColor);
		}
		List<Square> attackers = attacksToSquare(context.board, square, attackColor, 1);
		return attackers.size() == 0;
	}

	static class KingState {
		private final boolean isInCheck;
		private final boolean hasNoMoves;

		KingState(boolean isInCheck, boolean hasNoMoves) {
			this.isInCheck = isInCheck;
			this.hasNoMoves = hasNoMoves;
		}

		boolean isInCheck() {
			return isInCheck;
		}

		boolean hasNoMoves() {
			return hasNoMoves;
		}
	}

	private static class Context {
		final Board board;
		final MovePredicate movePredicate;
		final List<Move> results;

		private Context(Board board, MovePredicate movePredicate) {
			this(board, movePredicate, -1);
		}

		private Context(Board board, MovePredicate movePredicate, int expectedResults) {
			this.board = board;
			this.movePredicate = movePredicate;
			this.results = expectedResults > 0 ? new ArrayList<>(expectedResults) : new ArrayList<>();
		}

		/**
		 * Similar to {@link #addMove(Piece, Square, Square, PieceType) addMove} specific for
		 * a pawn move. This method takes into account pawn promotions thus it will add all possible promotion moves to
		 * the result if the pawn moves to the last rank
		 *
		 * @return the result of the call {@link MovePredicate#isSatisfied()} call if the move has been successfully
		 * added to the results, {@code false} otherwise. In case of pawn promotions only one pawn promotion move will be
		 * added in case the predicate specifies early exit
		 */
		boolean addPawnPromotionMoves(Piece pawn, Square from, Square to) {
			boolean result = false;
			if (Piece.isAtLastRank(pawn, to)) {
				// pawn at last rank this is a pawn promotion - add all possible promotion moves to the result
				for (Piece piece : Piece.getPieces(pawn.color())) {
					if (piece.isPromotable()) {
						if (addMove(pawn, from, to, piece.getType())) {
							return true;
						}
					}
				}
			} else {
				// normal pawn move
				result = addMove(pawn, from, to);
			}
			return result;
		}

		boolean addMove(Piece piece, Square from, Square to) {
			return addMove(piece, from, to, null);
		}

		/**
		 * This small code block is encapsulated inside a method because it its common usage
		 * It will test a move that we have found with the specified predicate and will add it to the results if the test
		 * was successful.
		 *
		 * @return the result of the call {@link MovePredicate#isSatisfied()} call if the move has been successfully
		 * added to the results, {@code false} otherwise.
		 */
		boolean addMove(Piece piece, Square from, Square to, PieceType promotionType) {
			Move move = Move.of(piece, from, to, promotionType);
			if (movePredicate.test(move)) {
				results.add(move);
				return movePredicate.isSatisfied();
			}
			return false;
		}

		static Context of(Board board, MovePredicate movePredicate) {
			return new Context(board, movePredicate);
		}

		@SuppressWarnings("SameParameterValue")
		static Context of(Board board, MovePredicate movePredicate, int expectedResults) {
			return new Context(board, movePredicate, expectedResults);
		}
	}

}
