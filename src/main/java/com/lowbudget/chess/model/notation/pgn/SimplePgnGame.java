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

import com.lowbudget.chess.model.ChessConstants;
import com.lowbudget.chess.model.Move;

import java.util.*;

/**
 * A standard implementation of a {@link PgnGame}
 */
public class SimplePgnGame implements PgnGame {

	private final List<PgnTag> tags = new ArrayList<>();

	private final Map<String, PgnTag> tagsMap = new HashMap<>();

	private final List<Move> moveList = new ArrayList<>();

	private PgnNotation.GameResult gameResult;

	@Override
	public boolean hasTag(String tagName) {
		return tagsMap.containsKey(tagName);
	}

	@Override
	public String getTagValue(String tagName) {
		PgnTag tag = tagsMap.get(tagName);
		return tag != null ? tag.getValue() : null;
	}

	@Override
	public List<Move> getMoveList() {
		return moveList;
	}

	public void addMove(Move move) {
		moveList.add(move);
	}

	public void addTag(String tagName, String tagValue) {
		PgnTag tag = new PgnTag(tagName, tagValue);
		tags.add(tag);
		tagsMap.put(tagName, tag);
	}

	@Override
	public Iterable<PgnTag> tags() {
		return tags;
	}

	@Override
	public int tagCount() { return tags.size(); }

	@Override
	public PgnNotation.GameResult getGameResult() {
		return gameResult;
	}

	public void setGameResult(PgnNotation.GameResult gameResult) {
		this.gameResult = gameResult;
	}

	@Override
	public String getFenPosition() {
		return hasTag(PgnTag.FEN_TAG_NAME) ? getTagValue(PgnTag.FEN_TAG_NAME) : ChessConstants.INITIAL_FEN;
	}
}
