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

package com.lowbudget.chess.front.app.controller.game;

import com.lowbudget.chess.front.app.*;
import com.lowbudget.chess.front.app.model.GameModel;
import com.lowbudget.chess.model.ChessConstants;
import com.lowbudget.chess.model.PlayerColor;
import com.lowbudget.chess.model.Move;
import com.lowbudget.chess.model.uci.protocol.*;
import com.lowbudget.chess.model.uci.protocol.message.*;
import com.lowbudget.chess.model.uci.session.SessionListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * <p>Watches an ongoing game between the server (which is an exposed chess engine) and the connected client.</p>
 * <p>This implementation implements the {@link SessionListener} interface and listens for both the engine and client
 * messages to display the ongoing moves on the board.</p>
 * <p>Note however that this class does not take care of any threading issues described in {@link SessionListener} but
 * it assumes that any such issues are handled externally (the easiest way to do that is to wrap an instance of this
 * class using a {@link com.lowbudget.chess.model.uci.client.ThreadContextSessionListener ThreadContextSessionListener} by providing an appropriate
 * {@link com.lowbudget.chess.model.uci.client.ThreadContext ThreadContext}</p>
 */
public class ServerGame extends NormalGame implements SessionListener {

	private static final Logger log = LoggerFactory.getLogger(ServerGame.class);

	private final DefaultClientMessageHandler clientHandler = new DefaultClientMessageHandler();
	private final DefaultEngineMessageHandler engineHandler = new DefaultEngineMessageHandler();

	/** The player assigned to the client connected to the server */
	private Player clientPlayer;

	/** The player assigned the engine that resides on the server side of the connection */
	private Player enginePlayer;

	/**
	 * The name of the engine which is not known in advance - it is resolved when we receive the "id name" message during
	 * UCI protocol initialization
	 */
	private String engineName;

	/**
	 * The name of the client player which is not known in advance - it is resolved when we receive the UCI_Opponent set option
	 * by the engine (note that our application always sends this message to its opponent, however not all engines use
	 * that i.e. Stock Fish does not handle that message)
	 */
	private String clientName = Constants.UNKNOWN_NAME;

	/** Flag that denotes if we have identified the players yet during this game */
	private boolean identifiedPlayers;

	public ServerGame(GameModel boardModel, Player whitePlayer, Player blackPlayer, TimeControl timeControl) {
		super(boardModel, whitePlayer, blackPlayer, timeControl);

		// assume client is white and engine is black (will swap later if this assumption is wrong)
		clientPlayer = whitePlayer;
		enginePlayer = blackPlayer;

		setAllowReconnect(true);
	}

	@Override
	public void onEngineMessage(String message) {
		EngineMessage engineMessage = EngineMessageParser.parseMessage(message);
		if (log.isDebugEnabled()) {
			log.debug("Engine message: {}", engineMessage);
		}
		engineMessage.accept(engineHandler);
	}

	@Override
	public void onClientMessage(String message) {
		ClientMessage clientMessage = ClientMessageParser.parseMessage(message);
		if (log.isDebugEnabled()) {
			log.debug("Client message: {}", clientMessage);
		}
		clientMessage.accept(clientHandler);
	}

	@Override
	public void onError(Exception exception) {
		gameModel.publishOnGameError(exception);
	}

	@Override
	public void onDisconnect(boolean stopRequested) {
		if (!stopRequested) {
			gameModel.stopGame();
		}
	}

	/**
	 * Identifies the correct client and engine player based on the known color of the engine player.
	 * @param engineColor the color of the player that represents the engine
	 */
	private void identifyPlayers(PlayerColor engineColor) {
		boolean isEnginePlayerCorrect = enginePlayer.getColor().equals(engineColor);
		if ( !isEnginePlayerCorrect) {
			// swap players
			Player actualEnginePlayer = clientPlayer;
			clientPlayer = enginePlayer;
			enginePlayer = actualEnginePlayer;
		}

		// since we have now resolved the players, set their names
		enginePlayer.setName(engineName);
		clientPlayer.setName(clientName);

		// let the model know that the names of the players have changed
		gameModel.publishOnGameInformationChanged(whitePlayer.getName(), blackPlayer.getName());
	}

	private void onClientGoMessage(long whiteTime, long blackTime) {

		startGameIfStopped("onClientGoMessage()");

		if (!identifiedPlayers) {
			// since the client has sent the go message it is the engine's turn to play the current color of the board
			identifyPlayers(gameModel.getPlayingColor());
			identifiedPlayers = true;
		}

		timeControl.sync(whiteTime, blackTime);
		if (!timeControl.isStarted()) {
			timeControl.start();
		}
	}

	private class DefaultEngineMessageHandler extends EngineMessageHandler.Adapter {

		@Override
		public void handle(BestMoveMessage message) {
			makeMove(message.getMove());
		}

		@Override
		public void handle(IdNameMessage message) {
			engineName = message.getName();
		}
	}

	private void startGameIfStopped(String invoker) {
		if (isStopped()) {
			if (log.isDebugEnabled()) {
				log.debug("Invoked {}, Game is stopped, starting it", invoker);
			}
			gameModel.startGame();
		}
	}

	private class DefaultClientMessageHandler extends ClientMessageHandler.Adapter {

		@Override
		public void handle(SimpleMessage message) {
			// every time we receive a new UCI message we assume a new game is being started
			// if the client does not send such a message we are out of luck (note however that in such case
			// the client would not respect the UCI protocol)
			if (message.getType() == UCIMessage.Type.UCI) {
				identifiedPlayers = false;
				engineName = Constants.UNKNOWN_NAME;
				clientName = Constants.UNKNOWN_NAME;
			}
		}

		@Override
		public void handle(SetOptionMessage message) {
			if (message.isUCIOpponentMessage()) {
				SetOptionMessage.OpponentMessage uciOpponentMessage = (SetOptionMessage.OpponentMessage) message;
				clientName = uciOpponentMessage.getOpponentName();
			}
		}
		@Override
		public void handle(PositionMessage message) {

			startGameIfStopped("handle(PositionMessage)");
			if (!identifiedPlayers) {
				if (log.isDebugEnabled()) {
					log.debug("Position message when players are not yet identified. Setting board from FEN");
				}
				gameModel.setFenPosition( message.isStartPos() ? ChessConstants.INITIAL_FEN : message.getFen());
			}

			List<Move> moves = message.getMoves();
			// currently we assume a single move per message. The protocol states that sometimes the last move sent,
			// can be the ponder move but other than that it is not clear why would we need to send more than
			// one "regular" moves here during a normal game
			if (moves.size() > 0) {
				makeMove(moves.get(0));
			}
		}
		@Override
		public void handle(GoMessage message) {
			onClientGoMessage(message.getWhiteTime(), message.getBlackTime());
		}
	}


}
