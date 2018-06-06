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

package com.lowbudget.chess.front.swing;


import com.lowbudget.chess.model.ChessConstants;
import com.lowbudget.chess.model.Piece;
import com.lowbudget.chess.model.Square;
import com.lowbudget.chess.model.Move;
import com.lowbudget.chess.model.uci.server.UCIServer;
import com.lowbudget.chess.model.uci.client.UCIClient;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;

import static com.lowbudget.chess.front.swing.SwingHelper.runServerAction;
import static org.assertj.swing.edt.GuiActionRunner.execute;

public class SwingServerGameTest extends BaseSwingTest {

	private UCIServer server;
	private GameMoveListener gameMoveListener;

	@Before
	public void setUp() {
		super.setUp();

		execute( () -> {
			// setup the default factories to actually create the server and the client
			// for the server cheat a little and get a reference to the instance returned
			application.setUCIServerFactory( (params, port) -> {
				UCIServer uciServer = UCIServer.DEFAULT_FACTORY.create(params, port);
				this.server = uciServer;
				return uciServer;
			});
			application.setUCIClientFactory(UCIClient.DEFAULT_FACTORY);

			// add a game move listener to be able to know when a move is made
			gameMoveListener = new GameMoveListener();
			application.getGameModel().addBoardModelListener(gameMoveListener);
		});
	}

	@After
	@Override
	public void tearDown() {
		super.tearDown();
		this.server = null;
	}

	@Test
	public void testServerMovesAreDisplayedLocally() {
		click(runServerAction);
		clickOk(window.dialog());

		connectClientAndPlayMove(Move.of(Square.E2, Square.E4));

		// server should observe the move and play it on the board
		verifyEmptySquares(Square.E2);
		verifyWhitePawnSquares(Square.E4);
	}

	@Test
	public void testServerRestartsGameAutomaticallyOnNewClient() {
		click(runServerAction);
		clickOk(window.dialog());

		UCIClient client = connectClientAndPlayMove(Move.of(Square.E2, Square.E4));

		// server should observe the move and play it on the board
		verifyEmptySquares(Square.E2, Square.D4);
		verifyWhitePawnSquares(Square.E4, Square.D2);

		// now disconnect the client to attempt a second connection
		execute(client::disconnect);

		// reset our move listener
		gameMoveListener.reset();

		// wait for the server to close the previous session. If we are too quick, the new client will be rejected
		waitUntilServerCanAcceptNewConnection();

		// create a new client and now play a different move
		connectClientAndPlayMove(Move.of(Square.D2, Square.D4));

		// server should observe the new move (the old move should not exist on the board)
		verifyEmptySquares(Square.E4, Square.D2);
		verifyWhitePawnSquares(Square.E2, Square.D4);
	}

	private UCIClient connectClientAndPlayMove(Move move) {
		// create a new client and handle the UCI protocol initialisation until the engine is ready
		UCIClient client = connectClientAndWaitUntilReady(application.getDefaultServerPort());

		// send the initial position and our move
		execute(() -> client.sendFenPosition(ChessConstants.INITIAL_FEN, Collections.singletonList(move)));

		// wait until the EDT is notified for the move made. Note that the fact the client sends commands in EDT
		// is not sufficient for our test to succeed because between sending the position and verifying the squares
		// other threads are involved. The actions that occur are:
		// - client sends the position and the move to be made (EDT)
		// - engine is notified (arbitrary thread)
		// - engine notifies session listener and thus our UI for the move (in EDT)
		// - we verify the squares (in EDT)
		// The last two event sin EDT however do not have a guaranteed order so if we do not wait, it is possible
		// the events to be scheduled in EDT in the following order:
		// client sends position -> squares' verification -> engine notifies GUI about the move
		// which will cause our test to fail
		waitUntilMoveMade(gameMoveListener);
		return client;
	}

	private void verifyEmptySquares(Square... squares) {
		for (Square square : squares) {
			square(square).requireEmpty();
		}
	}

	private void verifyWhitePawnSquares(Square... squares) {
		for (Square square : squares) {
			square(square).requirePiece(Piece.PAWN_WHITE);
		}
	}

	private void waitUntilServerCanAcceptNewConnection() {
		waitUntil(conditionOf("Waiting until server is ready to accept new client", server::canAcceptNewConnection));
	}

}
