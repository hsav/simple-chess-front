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

package com.lowbudget.chess.model.uci.client;

import com.lowbudget.chess.model.Move;
import com.lowbudget.chess.model.uci.connect.Connectable.Params;
import com.lowbudget.chess.model.uci.engine.options.*;
import com.lowbudget.chess.model.uci.protocol.*;
import com.lowbudget.chess.model.uci.protocol.message.*;
import com.lowbudget.chess.model.uci.protocol.message.UCIMessage.*;
import com.lowbudget.chess.model.uci.session.Session;
import com.lowbudget.chess.model.uci.session.SessionListener;
import com.lowbudget.chess.model.uci.session.Sessions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;

/**
 * <p>A UCI client that can connect to a UCI engine using a provided {@link Session}.</p>
 * <p>This class have convenient methods to send all the messages that can be sent from the GUI side of the communication
 * and implements the {@link SessionListener} interface so it can receive all the messages coming from the
 * engine.</p>
 * <p>Any incoming messages are transformed to objects and then delegated to the corresponding methods of a
 * {@link UCIClientDelegate} to provide a higher abstraction of the client side of the communication.</p>
 * <p>Additionally this class takes care of threading issues described in {@link SessionListener}
 * by using a provided {@link ThreadContext} so any session listener methods in this class (and thus any delegate methods)
 * run in the desired thread provided by that context.</p>
 * <p>To obtain an instance of the client use the {@link UCIClient#DEFAULT_FACTORY}.</p>
 * <pre>
 *     File engineExecutable = new File("...");
 *     // the thread context that defines the thread where client code should be executed
 *     // i.e. for swing use SwingUtilities::invokeLater
 *     ThreadContext threadContext = SwingUtilities::invokeLater;
 *     UCIClient client = UCIClient.DEFAULT_FACTORY.create("my-client", Connectable.Params.of(engineExecutable), threadContext );
 *     MyDelegate delegate = new MyDelegate(client);
 *     ...
 *     // somewhere in MyDelegate class and before connecting the client
 *     class MyDelegate implements UCIClientDelegate {
 *         public void startDelegate() {
 *             client.setDelegate(this);    // set the delegate to the client
 *     	       client.connect();            // connect
 *             client.sendUci();            // initiate the uci protocol
 *         }
 *
 *         // a little later the engine will most probably answer with the id name message so the
 *         // client will call the delegate method below
 *
 *         {@code @Override}
 *         public void onIdName(String name) {
 *             // do something with the engine's name here
 *             // this is called in the thread specified by the thread context
 *         }
 *     }
 * </pre>
 *
 * @see Session
 * @see SessionListener
 * @see UCIClientDelegate
 */
@SuppressWarnings("WeakerAccess")
public class UCIClient implements SessionListener {
	private static final Logger log = LoggerFactory.getLogger(UCIClient.class);

	@FunctionalInterface
	public interface Factory {
		/**
		 * Creates a new client from the parameters specified
		 * @param sessionName the name the client's session should use
		 * @param params a {@link Params} object with the engine connection details
		 * @param threadContext the thread context under which all {@link SessionListener} methods will be executed
		 * @return a new {@link UCIClient} instance
		 */
		UCIClient create(String sessionName, Params params, ThreadContext threadContext);
	}

	/**
	 * Default factory to obtain a new instance of a UCI client
	 */
	public static final Factory DEFAULT_FACTORY = (name, params, threadContext) -> {
		Objects.requireNonNull(name);
		Objects.requireNonNull(params);
		Objects.requireNonNull(threadContext);
		UCIClient client;
		Session session = Sessions.newSession(name, params);
		client = new UCIClient(session, params);
		// wrap the listener with the thread context specified so all listeners invocations will be executed in the
		// desired thread
		session.setSessionListener( new ThreadContextSessionListener(client, threadContext) );
		return client;
	};

	/**
	 * Factory that creates a session-less UCI client, useful for testing purposes
	 */
	public static final Factory TEST_FACTORY = (name, params, threadContext) -> new UCIClient(new Session() {
		@Override
		public void setSessionListener(SessionListener listener) {}

		@Override
		public void start() {}

		@Override
		public void stop() {}

		@Override
		public void sendMessage(String message) {}
	}, Params.NONE);

	/**
	 * The underlying session that handles the connection to the chess engine
	 */
	private final Session session;

	/**
	 * The params used for creating the session. This class does not use this field it is only stored here for
	 * caller's convenience
	 */
	private final Params params;

	/**
	 * An {@link EngineMessageHandler} class using the visitor pattern to handle engine's messages
	 */
	private final EngineMessageHandler messageHandler;

	/**
	 * <p>The delegate where the client will propagate any incoming messages from the engine.</p>
	 * <p>This needs to be set by calling {@link #setDelegate(UCIClientDelegate)} before {@link #connect()} is called.</p>
	 */
	private UCIClientDelegate delegate;

	/**
	 * Creates a new client.
	 * This method is private to avoid creating the session directly, instead use the {@link #DEFAULT_FACTORY#create(String, Params, ThreadContext)}
	 * static factory
	 * @param session the session to use
	 * @param params the {@link Params} used to create the specified session
	 */
	private UCIClient(Session session, Params params) {
		this.session = session;
		messageHandler = new DefaultEngineMessageHandler();
		this.params = params;
	}

	/**
	 * @return the {@link Params} objects used to create the session with which this client has been created
	 */
	public Params getParams() {
		return params;
	}

	/**
	 * Sets the delegate that will handle the incoming messages.
	 * The delegate needs to be set before the client is connected
	 * @param delegate the delegate to use
	 * @throws NullPointerException if the {@code delegate} specified is {@code null}
	 */
	public void setDelegate(UCIClientDelegate delegate) {
		this.delegate = Objects.requireNonNull(delegate, "Delegate cannot be null");
	}

	/**
	 * Starts the underlying session to connect to the engine
	 * @throws NullPointerException if no delegate has been set
	 */
	public void connect() {
		Objects.requireNonNull(delegate, "No delegate has been set");
		session.start();
	}

	/**
	 * <p>Stops the underlying session.</p>
	 * <p>After this call any messages sent to the session will just be ignored</p>
	 */
	public void disconnect() {
		sendMessage(SimpleMessage.QUIT);
		session.stop();
	}

	/**
	 * Sends a series of "set option" commands, one for each of the options in the specified list
	 * @param options the list of options to send to the engine
	 */
	public void sendSetOptions(List<UCIOption> options) {
		for (UCIOption option : options) {
			sendSetOption(option);
		}
	}

	/**
	 * Sends a single "set option" command for the option specified
	 * @param option the option to send to the engine
	 */
	@SuppressWarnings("WeakerAccess")
	public void sendSetOption(UCIOption option) {
		String value = option.getValueAsString();
		sendMessage(UCIMessage.newSetOptionMessage(option.getName(), value));
	}

	/**
	 * Sends a single "set option" command to the engine for a general name-value option i.e. one that is not a {@link UCIOption}
	 * @param name the option name
	 * @param value the option value
	 */
	@SuppressWarnings("unused")
	public void sendSetOption(String name, String value) {
		sendMessage(UCIMessage.newSetOptionMessage(name, value));
	}

	/**
	 * Sends a special "set option" command for the opponent's name
	 * @param title the title of the player
	 * @param elo the elo rating of the player, it can be {@code -1} if elo is not known
	 * @param isComputer if {@code true} the opponent is a computer, if {@code false} the opponent is a human
	 * @param name the name of the opponent
	 */
	public void sendOpponentName(Token.OpponentTitle title, int elo, boolean isComputer, String name) {
		sendMessage(UCIMessage.newSetOptionUCIOpponentMessage(title, elo, isComputer, name));
	}

	/**
	 * <p>Sends register information to the engine using the "register" command.</p>
	 * <p>This is usually a response to a "registration error" message coming from the engine that states that the
	 * engine requires registration. In that case the GUI can respond with the registration information (possibly
	 * after reading these values from the user via a dialog prompt)</p>
	 * <p>Alternatively the client can respond with a {@link #sendRegisterLater()} call if registration will be
	 * handled at a later time</p>
	 * @param name the name to use for registration
	 * @param code the code to use for registration
	 */
	public void sendRegisterInformation(String name, String code) {
		sendMessage( UCIMessage.newRegisterMessage(name, code) );
	}

	/**
	 * <p>Sends the "register later" command communicating to the engine that registration will be done at a later time.</p>
	 * <p>This is usually a response to a "registration error" message coming from the engine that states that the
	 * engine requires registration.</p>
	 */
	public void sendRegisterLater() {
		sendMessage(RegisterMessage.LATER);
	}

	/**
	 * <p>Sends the "uci" command that initiates the UCI protocol.</p>
	 * <p>The engine is expected to respond with the "id" message and then a series of "option" messages to let us
	 * know about the options it supports. The engine is expected to finish the series of these messages with a
	 * "uciok" message</p>
	 */
	@SuppressWarnings("SpellCheckingInspection")
	public void sendUci() {
		sendMessage(SimpleMessage.UCI);
	}

	/**
	 * Sends the "isready" command to the engine to query its status
	 * The engine is expected to answer with the "readyok" message
	 */
	@SuppressWarnings("SpellCheckingInspection")
	public void sendIsReady() {
		sendMessage(SimpleMessage.IS_READY);
	}

	/**
	 * Sends the "stop" command to let the engine know that it should stop searching (which is initiated
	 * by a "go" message)
	 */
	@SuppressWarnings("unused")
	public void sendStop() {
		sendMessage(SimpleMessage.STOP);
	}

	/**
	 * Sends the "ponderhit" command to the engine to let it know that the user played the expected move
	 * The engine sends the expected ponder move with the "bestmove" message.
	 */
	@SuppressWarnings({"unused", "SpellCheckingInspection"})
	public void sendPonderHit() {
		sendMessage(SimpleMessage.PONDER_HIT);
	}

	/**
	 * Sends the "debug" command to the engine
	 * @param on {@code true} if debug should be turned on, {@code false} otherwise
	 */
	@SuppressWarnings("unused")
	public void sendDebugMode(boolean on) {
		sendMessage(on ? DebugMessage.ON : DebugMessage.OFF);
	}

	/**
	 * Sends the start position to the engine.
	 * This message needs to be sent if the engine plays the white color when it is time to start the game
	 */
	@SuppressWarnings("unused")
	public void sendStartPosition() {
		sendMessage( UCIMessage.newStartPositionMessage() );
	}

	/**
	 * Sends the current board position along with a list of moves that should be played from that position
	 * This usually has a single move (the one the engine's opponent played) but the protocol allows for more
	 * @param fen the board position in FEN notation
	 * @param moves the list of moves that should be played from that position
	 */
	public void sendFenPosition(String fen, List<Move> moves) {
		sendMessage( UCIMessage.newFenPositionMessage(fen, moves) );
	}

	/**
	 * Sends the "go" command.
	 * @param builder a {@link GoMessage.Builder} class that was used to build the go message. This message has a lot
	 *                of parameters and not all of them are required.
	 */
	public void sendGo(GoMessage.Builder builder) {
		sendMessage(builder.build());
	}

	/**
	 * Sends the "ucinewgame" command so the engine knows that a new game is being started
	 */
	@SuppressWarnings("SpellCheckingInspection")
	public void sendNewUciGame() {
		sendMessage(SimpleMessage.UCI_NEW_GAME);
	}

	/**
	 * <p>Implementation of {@link SessionListener#onClientMessage(String)}.</p>
	 * <p>This is an empty implementation since all the messages arriving here are messages sent by this client.</p>
	 * @param message the message sent by the client of the chess engine.
	 */
	@Override
	public void onClientMessage(String message) {
		// do nothing
	}

	/**
	 * <p>Implementation of {@link SessionListener#onEngineMessage(String)}.</p>
	 * <p>The text message coming from the engine will be converted to a {@link UCIMessage} which is then handled by
	 * the {@link DefaultEngineMessageHandler} class by delegating to the appropriate delegate's method. </p>
	 * @param command the message sent by the chess engine.
	 */
	@Override
	public void onEngineMessage(String command) {
		EngineMessage message = EngineMessageParser.parseMessage(command);
		message.accept(messageHandler);
	}

	/**
	 * <p>Implementation of {@link SessionListener#onError(Exception)}.</p>
	 * @param exception the error occurred in the session.
	 */
	@Override
	public void onError(Exception exception) {
		delegate.onError(exception);
	}

	@Override
	public void onDisconnect(boolean stopRequested) {
		delegate.onDisconnect(stopRequested);
	}

	/**
	 * Sends a message using the session after it has converted the specified {@link UCIMessage} to {@code String}
	 * @param message the message to send
	 */
	private void sendMessage(UCIMessage message) {
		session.sendMessage(message.toCommand());
	}

	/**
	 * Handler for the messages received by the engine.
	 * It uses the visitor pattern to handle each type of message in a separate method and it propagates to the
	 * appropriate method of the {@link #delegate} specified
	 */
	private class DefaultEngineMessageHandler implements EngineMessageHandler {

		@Override
		public void handle(IdNameMessage message) {
			print(message);
			delegate.onIdName(message.getName());
		}

		@Override
		public void handle(IdAuthorMessage message) {
			print(message);
			delegate.onIdAuthor(message.getAuthor());
		}

		@Override
		public void handle(CopyProtectionMessage message) {
			print(message);
			if (message.isError()) {
				delegate.onCopyProtectionError();
			}
		}

		@Override
		public void handle(SimpleMessage message) {
			print(message);
			switch (message.getType()) {
				case READY_OK:
					delegate.onReadyOk();
					break;
				case UCI_OK:
					delegate.onUciOk();
					break;
			}
		}

		@Override
		public void handle(RegistrationMessage message) {
			print(message);
			if (message.isError()) {
				delegate.onRegistrationError();
			} else if (message.isOk()) {
				delegate.onRegistrationOk();
			}
		}

		@Override
		public void handle(BestMoveMessage message) {
			print(message);
			delegate.onBestMove(message.getMove(), message.getPonderMove());
		}

		@Override
		public void handle(InfoMessage message) {
			print(message);
			delegate.onInfoAvailable(message);
		}

		@Override
		public void handle(OptionMessage message) {
			print(message);

			String optionName = message.getName();
			UCIOption option;
			switch (message.getOptionType()) {
				case Token.BUTTON:
					option = new ButtonOption(optionName);
					break;
				case Token.CHECK:
					OptionMessage.CheckOptionMessage checkMessage = (OptionMessage.CheckOptionMessage) message;
					option = new CheckOption(optionName, checkMessage.getDefaultValue());
					break;
				case Token.STRING:
					OptionMessage.StringOptionMessage stringMessage = (OptionMessage.StringOptionMessage) message;
					option = new StringOption(optionName, stringMessage.getDefaultValue());
					break;
				case Token.COMBO:
					OptionMessage.ComboOptionMessage comboMessage = (OptionMessage.ComboOptionMessage) message;
					option = new ComboOption(optionName, comboMessage.getDefaultValue(), comboMessage.getComboOptions());
					break;
				case Token.SPIN:
					OptionMessage.SpinOptionMessage spinMessage = (OptionMessage.SpinOptionMessage) message;
					option = new SpinOption(optionName, spinMessage.getDefaultValue(), spinMessage.getMin(), spinMessage.getMax());
					break;
				default:
					throw new IllegalArgumentException("Received illegal option type: " + message.getOptionType());
			}

			delegate.onOption( option );
		}

		private void print(UCIMessage message) {
			if (log.isDebugEnabled()) {
				log.debug("{}", message);
			}
		}
	}
}
