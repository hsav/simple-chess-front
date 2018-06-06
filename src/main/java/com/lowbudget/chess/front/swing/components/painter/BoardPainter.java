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

package com.lowbudget.chess.front.swing.components.painter;

import java.awt.Graphics;
import java.awt.Rectangle;

import com.lowbudget.chess.model.Piece;
import com.lowbudget.chess.model.PlayerColor;

/**
 * Describes a painter object that is responsible for all the custom drawing when displaying a board
 */
public interface BoardPainter {

	/**
	 * Calculates the width of the border that our view should have based on the current board's scale
	 * @param scale the current board's scale
	 * @param area the bounds of the component displaying the board
	 * @return the appropriate border width
	 */
	int calculateBorderWidth(float scale, Rectangle area);

	/**
	 * Draws the background behind the board
	 * @param g the current {@code Graphics} context
	 * @param area the bounds of the component displaying the background
	 */
	void drawBackground(Graphics g, Rectangle area);

	/**
	 * Draws a square of the board
	 * @param g the current {@code Graphics} context
	 * @param color the color of the square
	 * @param area the bounds of the component displaying the square
	 * @param squareIndex the index of the square (useful for debugging purposes)
	 */
	void drawSquare(Graphics g, PlayerColor color, Rectangle area, int squareIndex);

	/**
	 * Draws the selection of a square selected by the user
	 * @param g the current {@code Graphics} context
	 * @param area the bounds of the component displaying the square
	 */
	void drawSquareSelection(Graphics g, Rectangle area);

	/**
	 * Draws a highlight around a square that indicates that the square is one of the two squares involved in the last
	 * move played on the board
	 * @param g the current {@code Graphics} context
	 * @param area the bounds of the component displaying the square
	 */
	void highlightSquare(Graphics g, Rectangle area);

	/**
	 * Draws a highlight around a square that indicates that the square is a possible landing square for a legal move
	 * of the piece standing at the square selected by the user.
	 * @param g the current {@code Graphics} context
	 * @param area the bounds of the component displaying the square
	 */
	void highlightLegalMove(Graphics g, Rectangle area);

	/**
	 * Draws a highlight between two squares that indicate that a piece standing at the square can attack the selected
	 * square (i.e. this can be an arrow between the two squares)
	 * @param g the current {@code Graphics} context
	 * @param selectedSquare the square that is currently selected by the user
	 * @param attackSquare the square where a piece attacks the selected square
	 */
	void highlightAttack(Graphics g, Rectangle selectedSquare, Rectangle attackSquare);

	/**
	 * Draws a piece
	 * @param g the current {@code Graphics} context
	 * @param area the bounds of the square component where the piece is standing at
	 * @param piece the piece to draw
	 */
	void drawPiece(Graphics g, Rectangle area, Piece piece);

	/**
	 * Draws the border around the board
	 * @param g the current {@code Graphics} context
	 * @param area the bounds of the component containing the border area
	 */
	void drawBorder(Graphics g, Rectangle area);

	/**
	 * Draws the subscripts of the board for the ranks and files (these are usually drawn on the border area i.e.
	 * just outside the board)
	 * @param g the current {@code Graphics} context
	 * @param area the bounds of the component containing the border area
	 * @param squareRadius the width of each square
	 * @param borderWidth the width of the border as calculated by {@link #calculateBorderWidth(float, Rectangle)}
	 * @param flipped the current flip status of the board, {@code true} if the board is flipped, {@code false} otherwise
	 */
	void drawSubscripts(Graphics g, Rectangle area, int squareRadius, int borderWidth, boolean flipped);

	/**
	 * Loads a new theme as long as any necessary resources to be able to draw using the specified theme
	 * @param themeName the name of the theme to be loaded
	 */
	void loadTheme(String themeName);

}
