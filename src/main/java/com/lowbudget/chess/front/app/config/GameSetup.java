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

package com.lowbudget.chess.front.app.config;

import com.lowbudget.chess.model.ChessConstants;

import javax.xml.bind.annotation.*;
import java.util.Objects;

/**
 * Stores the configuration for a new game
 */
@XmlRootElement(name="game-setup")
@XmlAccessorType(XmlAccessType.FIELD)
public class GameSetup {

	@XmlElement(name="white")
	private final PlayerSetup whiteSetup;

	@XmlElement(name="black")
	private final PlayerSetup blackSetup;

	@XmlAttribute(name="random-color")
	private final boolean randomColor;

	@XmlTransient
	private final String fenPosition;

	@SuppressWarnings("unused")
	// for JAXB
	private GameSetup() {
		this.whiteSetup = null;
		this.blackSetup = null;
		this.fenPosition = ChessConstants.INITIAL_FEN;
		this.randomColor = false;
	}

	public GameSetup(PlayerSetup whiteSetup, PlayerSetup blackSetup) {
		this(whiteSetup, blackSetup, ChessConstants.INITIAL_FEN, false);
	}

	public GameSetup(PlayerSetup whiteSetup, PlayerSetup blackSetup, String fenPosition, boolean randomColor) {
		this.whiteSetup = Objects.requireNonNull(whiteSetup);
		this.blackSetup = Objects.requireNonNull(blackSetup);
		this.fenPosition = Objects.requireNonNull(fenPosition);
		this.randomColor = randomColor;
	}

	public PlayerSetup getWhiteSetup() {
		return whiteSetup;
	}

	public PlayerSetup getBlackSetup() {
		return blackSetup;
	}

	public String getFenPosition() {
		return fenPosition;
	}

	public boolean isRandomColor() {
		return randomColor;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		GameSetup gameSetup = (GameSetup) o;
		return randomColor == gameSetup.randomColor &&
				Objects.equals(whiteSetup, gameSetup.whiteSetup) &&
				Objects.equals(blackSetup, gameSetup.blackSetup);
	}

	@Override
	public int hashCode() {
		return Objects.hash(whiteSetup, blackSetup, randomColor);
	}

}
