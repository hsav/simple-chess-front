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

package com.lowbudget.chess.model.board;

import com.lowbudget.chess.model.*;
import com.lowbudget.chess.model.board.MoveList.BrowseType;
import com.lowbudget.chess.model.notation.pgn.PgnGame;

import java.util.List;

/**
 * <p>An abstraction of a chess board.</p>
 * <p>A board implementation aims to support a GUI application with all the information it needs to display the chess
 * board's status i.e. chess moves, attacks, pins etc.</p>
 * <p>There are various representations of a chess board (i.e. array, 0x88, bit-boards etc), however as far as our
 * application is concerned a board needs to support the methods declared in this interface.</p>
 * <p>The coordinate system used for ranks and files assumes a bottom-top (ranks increase as we go up), left-right
 * (files increase as we go to the right) coordinate system i.e. bottom rank is the {@code 0} rank and the top rank is
 * {@link ChessConstants#MAX_RANKS - 1}, while the leftmost file is the {@code 0} file and the rightmost
 * file is the {@link ChessConstants#MAX_FILES - 1}. This is the same view that white has, when
 * viewing the board.</p>
 * <p>The board should also be able to support some basic features like validating legality of moves,
 * highlighting available moves, browse a move list etc.</p>
 *
 * @author Administrator
 */

public interface Board {

	/**
	 * Determines a square's color.
	 *
	 * @param p the square for which we need to find its color
	 * @return the color of the specified square
	 */
	static PlayerColor getSquareColor(Square p) {
		// The color of a square is the opposite of the "zero" square (the square at bottom-right which is always white)
		// if its rank and its file have the same parity, i.e. they are both even or odd
		PlayerColor zero = PlayerColor.WHITE;
		return (p.rank() & 1) == (p.file() & 1) ? zero.opposite() : zero;
	}

	/**
	 * Converts the board to a string using the FEN notation
	 * @return the FEN representation of the board
	 */
	String toFEN();

	/**
	 * Sets this board from a FEN string
	 * @param fen the FEN notation representing the desired board position.
	 */
	void fromFEN(String fen);

	/**
	 * Imports all the moves of a PGN game
	 * @param pgnGame the PGN details to import
	 */
	void fromPGN(PgnGame pgnGame);

	/**
	 * <p>Performs a move on the board.</p>
	 * <p>The move is assumed to be performed by a player that interacts with a GUI, so it is not necessary a valid move.</p>
	 * @param from      the square of the moving piece
	 * @param to        the square to move the piece to
	 * @param promotionType the type of the promotion piece in case this is a pawn and the {@code to} square
	 *                      belongs to the last rank (8th for white, 1st for black)
	 * @return the {@link GameState} resulted from the move
	 * @throws IllegalMoveException if there is no piece at the {@code from} square, if the color of the piece at
	 * that square does not agree with the color of the player whose turn it is to move or if the move is not legal
	 * according to the moves allowed for that piece
	 */
	GameState makePlayerMove(Square from, Square to, PieceType promotionType);

	/**
	 * <p>Undo the player's last move on the board.</p>
	 */
	void undoPlayerMove();

	/**
	 * <p>Performs the specified move on the board</p>
	 * <p>The move is assumed to be legal.</p>
	 * <p>This method is only meant to be used during testing with perft</p>
	 * @param move the move to perform
	 */
	void makeMove(Move move);

	/**
	 * <p>Undo of the last move, bringing the board to its previous state.</p>
	 * <p>This method is intended to be used by the perft tests</p>
	 * @param move the move to undo
	 */
	void undoMove(Move move);

	/**
	 * Returns the last move played
	 * @return the last move played
	 */
	Move getCurrentMove();

	/**
	 * Sets the board in the appropriate state to display a previously played move stored in the board's move list.
	 * This allows the user to navigate to previously played positions without losing the current board's state.
	 * @param browseType the {@link BrowseType} specifying in which way the move list should be browsed
	 */
	void browseMoveList(BrowseType browseType);

	/**
	 * <p>Finds the legal moves for the specified piece from its current square.</p>
	 * <p>The resulted list consists of only legal moves.</p>
	 * @param piece the piece to find the moves for
	 * @param from  the current square of the piece
	 * @return a list of legal moves for the piece specified
	 */
	List<Move> findLegalMoves(Piece piece, Square from);

	/**
	 * Returns the pawn that is actually captured in an en-passant move (if any)
	 * @param piece the pawn that tries to capture en-passant
	 * @param from  the square of the pawn. The pawn should be at the fifth rank (fourth for black)
	 * @param to    the square the pawn tries to capture en-passant diagonally. This should be at the sixth rank (3rd for black)
	 * @return the enemy pawn that is being captured en-passant. This pawn is actually at the same rank with the
	 * specified piece.
	 */
	Piece getEnPassantCapturablePawn(Piece piece, Square from, Square to);

	/**
	 * Checks if a square is empty
	 * @param p the square to check
	 * @return {@code true} if the board does not contain any piece at the specified square, {@code false} otherwise
	 */
	boolean isSquareEmpty(Square p);

	/**
	 * <p>Returns a list of all the pieces of the specified color that can attack the square specified.</p>
	 * <p>Note that it does not matter if the square is occupied or not.</p>
	 * @param square the square to check if it is being attacked
	 * @param color  the color of the pieces that attack the square
	 * @return a list of all the moves for the player with the specified color that can attack the square
	 */
	List<Square> getAllSquareAttacks(Square square, PlayerColor color);

	/**
	 * Returns the piece that occupies the square specified
	 * @param square the square to get the piece for
	 * @return the piece that occupies the specified square or {@code null} if the square is empty
	 */
	Piece getPieceAt(Square square);

	/**
	 * Returns the piece that occupies the square specified by the rank and file
	 * @param rank the rank of the square we need to examine
	 * @param file the file of the square we need to examine
	 * @return the piece that occupies the specified square or {@code null} if the square is empty
	 */
	Piece getPieceAt(int rank, int file);

	/**
	 * @return the current status for the castling rights for both players
	 */
	Castling getCastling();

	/**
	 * @return the current en-passant square (if any) or {@link Square#NONE} otherwise. This is the square behind a pawn that
	 * just made a two square advance (i.e. as if the pawn had moved only one square) and it is available only for
	 * the move just right after a two square pawn advance (i.e. if the opponent does not capture en-passant during this
	 * move then the move is no more available)
	 */
	Square getEnPassant();

	/**
	 * @return the color of the side whose turn it is to play
	 */
	PlayerColor getPlayingColor();

	/**
	 * @return the current counter of the half-move clock for the 50-move rule. This counter counts the moves from the
	 * last time a pawn has been captured or a pawn has advanced. When this counter reaches {@code 100} the player
	 * having the move can claim a draw.
	 */
	int getHalfMoveClock();

	/**
	 * @return the current move number of a full move. It starts at {@code 1} and is incremented after each time black
	 * has played.
	 */
	int getMoveNumber();

	/**
	 * @return a list of all pieces captured so far in the game
	 */
	List<Piece> getCapturedPieces();

	/**
	 * @return the {@link MoveList} that keeps track of all moves played so far
	 */
	TabularMoveList getMoveList();

	/**
	 * Checks if a specified piece's move is pinned to the king
	 * @param piece the piece to move
	 * @param from the piece's current square
	 * @param to the piece's target square
	 * @return {@code true} if the move is absolutely pinned to the king of the same color, {@code false} otherwise
	 */
	boolean isPinned(Piece piece, Square from, Square to);

	/**
	 * <p>Checks if a move is legal.</p>
	 * @param piece the piece to move
	 * @param from the square to move from
	 * @param to the square to move to
	 * @param promotionType the {@link PieceType} the moving piece should be promoted to (applies only in case of a
	 *                      pawn that reaches the last rank)
	 * @return {@code true} if the move is legal, {@code false} otherwise
	 */
	boolean isLegalMove(Piece piece, Square from, Square to, PieceType promotionType);

	/**
	 * Sets the setup mode of the board.
	 * @param value when the value is {@code true} the board is set in setup mode. In this mode the board leaves the
	 *              player to move pieces freely since this is not a normal game. When this value is {@code false}
	 *              the board will check the legality of moves and will enforce the chess game rules
	 */
	void setSetupMode(boolean value);

	/**
	 * Returns the setup mode of the board.
	 * @return {@code true} if the board is in setup mode, {@code false} if the board is in normal mode.
	 */
	@SuppressWarnings("BooleanMethodIsAlwaysInverted")
	boolean isInSetupMode();

	/**
	 * <p>Checks if a ray (horizontal, vertical or diagonal) is not occupied.</p>
	 * <p>This method will traverse the ray to see if there exists a piece strictly between the starting and
	 * ending squares.</p>
	 * <p>Note that any piece in the starting or ending squares will be ignored.</p>
	 *
	 * @param direction the direction to check
	 * @param from      the square to start from
	 * @param to        the square to finish
	 * @return Assuming a numbering scheme for the squares that will be traversed: {@code start},
	 * {@code start + 1}, {@code ...}, {@code end} (where <em>{@code start}</em> corresponds to <em>{@code from}</em>
	 * and <em>{@code end}</em> corresponds to <em>{@code to}</em>), this method will return {@code true} if all of the
	 * squares {@code (start ... end)} (both exclusive) are empty or {@code false} otherwise.
	 */
	boolean isRayEmpty(Direction direction, Square from, Square to);

	/**
	 * <p>Identical with the {@link #isRayEmpty(Direction, Square, Square)} with the additional feature to ignore the
	 * king of the specified color.</p>
	 * <p>The reason the king is ignored, is that when a slider gives check to the enemy king we need the
	 * slider to be able to "see" beyond the king so the squares along the ray that lie behind the king to be treated
	 * as attacked squares. Otherwise the squares can be considered safe for the king to avoid the check while they
	 * are not.</p>
	 * @param direction the direction to check
	 * @param from the square to start from
	 * @param to the square to finish
	 * @param colorOfKingToIgnore the {@link PlayerColor} of the king to ignore.
	 * @return {@code true} if all the squares strictly between {@code from} and {@code end} are empty (with possibly
	 * the exception of the ignored king), {@code false} otherwise.
	 */
	boolean isRayEmptyIgnoringKing(Direction direction, Square from, Square to, PlayerColor colorOfKingToIgnore);

	/**
	 * Moves a piece while the board is in setup mode. This operation implements a user's drag operation without the
	 * extra restrictions a normal move has.
	 * @param from the square to move the piece from
	 * @param to the square to move the piece to
	 */
	void movePieceInSetupMode(Square from, Square to);

	/**
	 * Sets a piece at the specified square while the board is in setup mode.
	 * @param piece the piece to set at the specified square - it can be {@code null} in order to clear the square
	 * @param at the square to set the piece at
	 */
	void setPieceInSetupMode(Piece piece, Square at);

	/**
	 * Returns the square of the king of the specified color
	 * @param color the color of the king whose square is requested
	 * @return the square the king with the specified color is located at
	 */
	Square getKingSquare(PlayerColor color);

	/**
	 * Returns a traversal of all the squares containing pieces of the same type and color of the piece specified
	 * (i.e. all white rooks or all black pawns)
	 * @param piece the piece to search
	 * @return a traversal of all the squares that contain the same piece as the piece specified
	 */
	Iterable<Square> getGroupSquares(Piece piece);
}
