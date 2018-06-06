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

package com.lowbudget.chess.front;

import com.lowbudget.chess.front.app.*;

/**
 * <p>The main application entry that starts up the Swing gui.</p>
 * <p>The application is a chess front where you can register and play against a chess engine that supports
 * the Universal Chess Interface protocol (aka UCI).</p>
 * <p>Supported features:</p>
 * <ul>
 *     <li>Play against a chess engine</li>
 *     <li>Have two engines play against each other</li>
 *     <li>Play against a remote engine i.e. connect to a UCI server</li>
 *     <li>Expose a local chess engine executable as a server</li>
 *     <li>Provide the ability to setup a board supporting copy/paste of positions in Forsyth-Edwards Notation (FEN)</li>
 *     <li>Provide the ability to stop/pause/close a game</li>
 *     <li>Provide the ability to save/load games in Portable Game Notation (PGN)</li>
 * </ul>
 * <p>Future improvements:</p>
 * <ul>
 *     <li>Support pondering (engines can think on the move they expect the opponent to make while it is opponent's turn)</li>
 *     <li>Provide the ability for the user to analyse a position using an engine (i.e. to start/stop the engine on demand)</li>
 *     <li>Implement an alternative time control view where the player names and time controls are displayed on each side
 *     of the board (e.g. like in chess.com games)</li>
 *     <li>Parse recursive variations and comments in PGN files and possibly have a custom view that allows navigation
 *     in such variations</li>
 * </ul>
 */
public class SCFMain {

	public static void main(String[] args) {
		UIApplication application = UIApplication.startup();
		application.setSaveConfigurationOnExit(true);
		application.loadDefaultConfiguration();
	}

}
