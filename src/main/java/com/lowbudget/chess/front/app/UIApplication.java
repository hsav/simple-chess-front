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

package com.lowbudget.chess.front.app;

import com.lowbudget.chess.front.app.config.ReadOnlyEnginesConfig;
import com.lowbudget.chess.front.app.config.GameSetup;
import com.lowbudget.chess.front.app.model.BoardModel;
import com.lowbudget.chess.front.app.model.GameModel;
import com.lowbudget.common.ReadOnlyRecentHistory;
import com.lowbudget.chess.model.PlayerColor;
import com.lowbudget.chess.model.board.MoveList;
import com.lowbudget.chess.model.uci.server.UCIServer;
import com.lowbudget.chess.model.uci.client.UCIClient;
import com.lowbudget.chess.model.uci.connect.Connectable;
import com.lowbudget.chess.model.uci.engine.UCIEngineConfig;
import com.lowbudget.chess.model.uci.engine.options.ButtonOption;
import com.lowbudget.chess.model.uci.engine.options.UCIOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.List;
import java.util.function.Consumer;

/**
 * <p>Describes the functionality available in the chess front application.</p>
 * <p>The available methods consist of two sets of methods: the "model" methods meaning the ones that change application
 * state and the "controller" methods which are the ones executing an application's function like starting a new game
 * (note that most probably such methods will implicitly change the application's state too).</p>
 * <p>Additionally this class also contains classes and interfaces that are closely related with the application
 * itself.</p>
 */
public interface UIApplication {

	/**
	 * Must be called just right after the application has been created and before any other method is called on the
	 * application to give the application a chance to initialize its state
	 */
	void initialize();

	/**
	 * Must be called as the last call on the application object to give the application a chance to save any unsaved
	 * state and cleanup resources (if any)
	 */
	void exit();

	/**
	 * <p>Creates, initializes and returns a new application object.</p>
	 * @return the newly created application.
	 */
	static UIApplication startup() {
		return Initializer.startup();
	}

	///// Model ////////////////////////////////////////////////////////////////////////////////////////////////////////

	/**
	 * Loads the application's configuration from the default configuration file
	 */
	void loadDefaultConfiguration();

	/**
	 * Loads the application's configuration from the file specified
	 * @param configFile the file to load the configuration from
	 */
	void loadConfiguration(File configFile);

	/**
	 * <p>Transient option that controls the application's behaviour with respect to un-saved games.</p>
	 * <p>By default the application will check if a game is not saved when it is closed and prompt the user to save
	 * it.</p>
	 * @param checkUnsavedGame {@code true} if the application should check and save any unsaved game data when the
	 * game is about to be closed, {@code false} otherwise
	 */
	void setCheckUnsavedGame(boolean checkUnsavedGame);

	/**
	 * <p>Transient option that controls the application's behaviour with respect to saving configuration changes
	 * before exiting.</p>
	 * <p>By default the changes are not saved, the application's caller should opt-in for this functionality</p>
	 * @param saveConfigurationOnExit {@code true} if the application should persist any configuration changes before
	 *                                exiting, {@code false} otherwise
	 */
	void setSaveConfigurationOnExit(boolean saveConfigurationOnExit);

	/**
	 * Adds a new {@link StateListener} to the application.
	 * @param stateListener the listener to add
	 */
	void addStateListener(StateListener stateListener);

	/**
	 * Removes a state listener
	 * @param stateListener the listener to remove
	 */
	void removeStateListener(StateListener stateListener);

	/**
	 * Adds a new {@link ModelListener} to the application.
	 * @param listener the listener to add
	 */
	void addModelListener(ModelListener listener);

	/**
	 * Removes a model listener
	 * @param listener the listener to remove
	 */
	@SuppressWarnings("unused")
	void removeModelListener(ModelListener listener);

	/**
	 * @return the game model of the application
	 */
	GameModel getGameModel();

	/**
	 * @return the board model of the application (the one that corresponds to setting a board position)
	 */
	BoardModel getSetupBoardModel();

	/**
	 * @return the path of the last folder opened using an open file dialog
	 */
	File getLastOpenFolderPath();

	/**
	 * Sets the path of the last folder opened using an open file dialog
	 * @param path the last opened folder to remember from now and on
	 */
	void setLastOpenFolderPath(File path);

	/**
	 * @return the most recent game setups. A game setup is defined every time the user configures the parameters of
	 * a new game
	 */
	ReadOnlyRecentHistory<GameSetup> getRecentGameSetups();

	/**
	 * @return the last game setup used when configuring a new game
	 */
	GameSetup getLastGameSetup();

	/**
	 * @return the configurations of all the registered UCI engines
	 */
	ReadOnlyEnginesConfig getEnginesConfig();

	/**
	 * @return the maximum number of recent game setups the application should remember
	 */
	int getMaxRecentCount();

	/**
	 * Sets the maximum number of game setups the application should remember
	 * @param maxRecentCount the maximum count that should be remembered
	 */
	void setMaxRecentCount(int maxRecentCount);

	/**
	 * @return {@code true} if the game board should visually display the attacks to the selected squared, {@code false} otherwise
	 */
	boolean isViewAttacksEnabled();

	/**
	 * Sets the application option that controls if attacks to the selected square should be visually displayed
	 * @param optionViewAttacksEnabled {@code true} if the attacks should be displayed, {@code false} otherwise
	 */
	void setViewAttacksEnabled(boolean optionViewAttacksEnabled);

	/**
	 * @return {@code true} if the game board should visually display the legal moves of the piece standing at the
	 * selected squared, {@code false} otherwise
	 */
	boolean isViewLegalMovesEnabled();

	/**
	 * Sets the application option that controls if legal moves of the piece at the selected square should be visually displayed
	 * @param optionViewLegalMovesEnabled {@code true} if the legal moves should be displayed, {@code false} otherwise
	 */
	void setViewLegalMovesEnabled(boolean optionViewLegalMovesEnabled);

	/**
	 * @return {@code true} if the game board should visually display the last move on the board, {@code false} otherwise
	 */
	boolean isViewLastMoveEnabled();

	/**
	 * Sets the application option that controls if the last move should be visually displayed
	 * @param optionViewLastMoveEnabled {@code true} if the last move should be displayed, {@code false} otherwise
	 */
	void setViewLastMoveEnabled(boolean optionViewLastMoveEnabled);

	/**
	 * @return {@code true} if the engines that required registration should automatically postpone registration for
	 * later, {@code false} otherwise (applies only to engines that require registration)
	 */
	boolean isEnginesAlwaysRegisterLater();

	/**
	 * Sets the application option that controls if engines should automatically postpone registration for later
	 * (applies only to engines that require registration)
	 * @param value {@code true} if registration should be postponed, {@code false} otherwise
	 */
	void setEnginesAlwaysRegisterLater(boolean value);

	/**
	 * @return the default port that should be used when starting an engine server
	 */
	int getDefaultServerPort();

	/**
	 * Sets the default port that should be used when starting an engine server.
	 * @param defaultPort the value that should be used as the default port
	 */
	void setDefaultServerPort(int defaultPort);

	/**
	 * @return {@code true} if the application is currently showing the setup board screen, {@code false} otherwise
	 */
	boolean isSetupMode();

	/**
	 * Flips the board. By default the board is displayed from white's point of view, flipping it will cause it to be
	 * displayed by the black's point of view.
	 */
	void flipBoard();

	/**
	 * @return {@code true} if the board is flipped (displayed from black's point of view), {@code false} otherwise
	 */
	boolean isBoardFlipped();

	/**
	 * Sets the application's theme (theme affects the way the board is drawn)
	 * @param newTheme the name of the new theme to set
	 */
	void setTheme(String newTheme);

	/**
	 * @return the name of the application's current theme
	 */
	String getTheme();

	///// Controller ///////////////////////////////////////////////////////////////////////////////////////////////////

	/**
	 * @return {@code true} if the new game action is available, {@code false} otherwise
	 */
	boolean isNewGameActionAllowed();

	/**
	 * @return {@code true} if the pause game action is available, {@code false} otherwise
	 */
	boolean isPauseGameActionAllowed();

	/**
	 * @return {@code true} if the stop game action is available, {@code false} otherwise
	 */
	boolean isStopGameActionAllowed();

	/**
	 * @return {@code true} if the close game action is available, {@code false} otherwise
	 */
	boolean isCloseGameActionAllowed();

	/**
	 * @return {@code true} if the load game action is available, {@code false} otherwise
	 */
	boolean isLoadGameActionAllowed();

	/**
	 * @return {@code true} if the save game action is available, {@code false} otherwise
	 */
	boolean isSaveGameActionAllowed();

	/**
	 * @return {@code true} any of the browse move list game actions is available, {@code false} otherwise
	 */
	boolean isBrowseMoveListActionAllowed();

	/**
	 * @return {@code true} if we can switch to the setup board screen, {@code false} otherwise
	 */
	boolean isEnterSetupModeActionAllowed();

	/**
	 * @return {@code true} if we can exit the setup board screen, {@code false} otherwise
	 */
	boolean isExitSetupModeActionAllowed();

	/**
	 * @return {@code true} if the copy fen action is available, {@code false} otherwise
	 */
	boolean isCopyFenActionAllowed();

	/**
	 * @return {@code true} if the paste fen action is available, {@code false} otherwise
	 */
	boolean isPasteFenActionAllowed();

	/**
	 * @return {@code true} if the manage engines action is available, {@code false} otherwise
	 */
	boolean isManageEnginesActionAllowed();

	/**
	 * @return {@code true} if the run engine as server action is available, {@code false} otherwise
	 */
	boolean isRunServerActionAllowed();

	/**
	 * @return {@code true} if the stop server action is available, {@code false} otherwise
	 */
	boolean isStopServerActionAllowed();

	/**
	 * Checks if the engine settings action is available for the player of the color specified. This action is
	 * available if there is a game going on and at least one of the two players is an engine
	 * @param color the color of the player for which to check if the engine settings action ia available
	 * @return {@code true} if the engine settings action is available for the {@code color} specified, {@code false}
	 * otherwise
	 */
	boolean isEngineSettingsActionAllowed(PlayerColor color);

	/**
	 * Returns the engine config for the player of the color specified
	 * @param color the color of the player for which the engine config should be returned. Such a config is available
	 *              only if the the player of the specified color in the current game is an engine
	 * @return the engine config for the player of the {@code color} specified
	 */
	UCIEngineConfig getEngineConfigForPlayerOfColor(PlayerColor color);

	/**
	 * Modifies the engine options for the player of the color specified.
	 * @param engineColor the color of the engine player
	 * @param modifiedOptions the options to modify
	 */
	void setEnginePlayerOptions(PlayerColor engineColor, List<UCIOption> modifiedOptions);

	/**
	 * "Sets" an engine {@link ButtonOption} option. This is equivalent of running an engine command that alters the
	 * engine's state i.e. like "Clear hash"
	 * @param engineColor the color of the engine player for which the button options should be set
	 * @param buttonOption the button option to set
	 */
	void setEnginePlayerButtonOption(PlayerColor engineColor, ButtonOption buttonOption);

	/**
	 * Updates the config of a registered engine while no game is going on (and thus the engine is not currently in use)
	 * @param engineProfileIndex the index of the engine to update
	 * @param newOptions the new options to set to the engine
	 * @return {@code true} if at least one option was actually changed to a different value, {@code false} otherwise
	 */
	boolean updateEngineConfigOptions(int engineProfileIndex, List<UCIOption> newOptions);

	/**
	 * Removes an engine from the registered engines
	 * @param engineConfigIndex the index of the engine to remove
	 * @param successListener a listener to be executed after the engine is removed
	 */
	void removeEngineConfig(int engineConfigIndex, DialogSuccessListener successListener);

	/**
	 * Adds a new engine to the registered engines
	 * @param params the connection params of the engine
	 * @param successListener a listener to be executed after the engine is added
	 */
	void addEngineConfig(Connectable.Params params, Consumer<UCIEngineConfig> successListener);

	/**
	 * Tests the connection of an already registered engine
	 * @param params the connection params of the engine
	 * @param successListener a listener to be executed if testing the connection is successful
	 */
	void testEngineConfig(Connectable.Params params, DialogSuccessListener successListener);

	/**
	 * Copies the fen position of the current board (either the game board or the setup board whichever is is currently
	 * being displayed) to the clipboard
	 */
	void copyFenPosition();

	/**
	 * Pastes a fen position from the clipboard to the current setup board (note that we can copy from both boards
	 * but we can only paste to the setup board)
	 */
	void pasteFenPosition();

	/**
	 * Enters the setup board mode
	 */
	void enterSetupMode();

	/**
	 * Exits the setup board mode
	 */
	void exitSetupMode();

	/**
	 * Starts a new game
	 * @param gameSetup the setup to use to start the new game
	 */
	void startNewGame(GameSetup gameSetup);

	/**
	 * Pauses the current game
	 */
	void pauseGame();

	/**
	 * Stops the current game. After the game is stopped time controls have ben stopped and no move can be made but the
	 * user can browse through the game's move list to previous positions
	 */
	void stopGame();

	/**
	 * Closes the current game
	 */
	void closeGame();

	/**
	 * Loads a game from a file. The user will be prompted to select a PGN file to load
	 */
	void loadGame();

	/**
	 * Saves the current game to a file using the PGN format
	 */
	void saveGame();

	/**
	 * Browses the current game's move list
	 * @param browseType the type of browsing that should be executed (i.e. previous move, next move etc)
	 */
	void browseMoveList(MoveList.BrowseType browseType);

	/**
	 * Starts an engine as a server
	 * @param engineConfig the config of the engine that should be used to interact with clients
	 * @param port the listening port of the server
	 */
	void startServer(UCIEngineConfig engineConfig, int port);

	/**
	 * Stops the current server
	 */
	void stopServer();

	/**
	 * Sets the factory that should be used to create new UCI clients
	 * @param clientFactory the factory to be used to create new clients
	 */
	void setUCIClientFactory(UCIClient.Factory clientFactory);

	/**
	 * Sets the factory that should be used to create new UCI servers
	 * @param serverFactory the factory to be used to create new clients
	 */
	void setUCIServerFactory(UCIServer.Factory serverFactory);

	/**
	 * Sets the factory that should be used to create new {@link UITimer} objects
	 * @param timerFactory the factory to be used
	 */
	void setUITimerFactory(UITimerFactory timerFactory);

	/**
	 * Sets the factory that should be used to create new {@link WaitDialogHolder} objects
	 * @param waitDialogHolderFactory the factory to be used
	 */
	void setWaitDialogHolderFactory(WaitDialogHolderFactory waitDialogHolderFactory);

	///// Interfaces and classes ///////////////////////////////////////////////////////////////////////////////////////

	/**
	 * Factory that creates an instance of the application
	 */
	@FunctionalInterface
	interface UIApplicationFactory {
		UIApplication createApplication();
	}

	/**
	 * A holder object that holds the reference to a wait dialog. A wait dialog is an informative dialog that is
	 * displayed while a task is being run and does not provide any way to interact with the user other than closing
	 * the dialog (which cancels the underlying task)
	 */
	interface WaitDialogHolder {
		void openDialog(DialogCancelListener cancelListener);
		void close();
	}

	/**
	 * Factory that creates instances of {@link WaitDialogHolder} objects
	 */
	@FunctionalInterface
	interface WaitDialogHolderFactory {
		WaitDialogHolder createWaitDialogHolder();
	}

	/**
	 * Listener that receives the registration details entered in a registration dialog
	 */
	@FunctionalInterface
	interface RegistrationDialogSuccessListener {
		void onDialogSuccess(String registrationName, String registrationCode);
	}

	/**
	 * Listener that is notified on dialog success. (The term "success" here means that the user clicked the ok button
	 * and has not cancelled the dialog)
	 */
	@FunctionalInterface
	interface DialogSuccessListener {
		void onDialogSuccess();
	}

	/**
	 * Listener that is notified when a dialog is cancelled
	 */
	@FunctionalInterface
	interface DialogCancelListener {
		void onDialogCanceled();
	}

	/**
	 * Listens for events of a {@link UITimer} (i.e. every time the timer fires)
	 */
	@FunctionalInterface
	interface UITimerListener {
		void onTimer();
	}

	/**
	 * Factory to create instances of {@link UITimer} objects
	 */
	@FunctionalInterface
	interface UITimerFactory {
		UITimer createTimer(int millisInterval);
	}

	/**
	 * A timer object that can be scheduled and notify its listener(s) at regular intervals
	 */
	interface UITimer {
		void start();
		void stop();
		boolean isRunning();
		void addUITimerListener(UITimerListener listener);
	}

	/**
	 * Listener for application's state changes. Such listeners are interested if an event has occurred that affects
	 * the enabled status of the application's actions in general but not in the specific type of change
	 */
	@FunctionalInterface
	interface StateListener {
		void onStateChanged();
	}

	/**
	 * The listener used by the application's model. Classes that desire to be notified when the model changes, need to
	 * implement this interface and register themselves with the model via {@link #addModelListener(ModelListener)}.
	 */
	interface ModelListener {
		void onApplicationExit();

		void onThemeChanged(String newTheme);

		void onOptionChanged();

		void onBoardFlipped(boolean isFlipped);

		void onEngineInfoAvailable(String info);

		void onSetupModeEntered();

		void onSetupModeExited();

		void onServerStarted(int port);

		void onServerStopped();

		void onClientConnected(int port);

		void onClientDisconnected(int port);
	}

	/**
	 * Helper object that provides an empty {@link ModelListener} implementation, so we are able to override
	 * only the methods we are interested in.
	 */
	class ModelAdapter implements ModelListener {

		@Override public void onApplicationExit() {}

		@Override public void onThemeChanged(String newTheme) {}

		@Override public void onOptionChanged() {}

		@Override public void onBoardFlipped(boolean isFlipped) {}

		@Override public void onEngineInfoAvailable(String info) {}

		@Override public void onSetupModeEntered() {}

		@Override public void onSetupModeExited() {}

		@Override public void onServerStarted(int port) {}

		@Override public void onServerStopped() {}

		@Override public void onClientConnected(int port) {}

		@Override public void onClientDisconnected(int port) {}
	}

	/**
	 * Utility class that instantiates a factory and creates a new application.
	 * Currently the name of the factory class is hard-coded, but it is easy to change this behaviour to read the
	 * class name from a system or property or a configuration file
	 */
	class Initializer {
		@SuppressWarnings("SpellCheckingInspection")
		static final String DEFAULT_APPLICATION_FACTORY = "com.lowbudget.chess.front.swing.SwingUIApplicationFactory";

		private static final Logger log = LoggerFactory.getLogger(UIApplication.class);

		static final String applicationFactoryClassName = DEFAULT_APPLICATION_FACTORY;

		private static UIApplicationFactory createFactory() {
			UIApplicationFactory factory;
			try {
				Class<?> appClass = Class.forName(applicationFactoryClassName);
				if (!UIApplicationFactory.class.isAssignableFrom(appClass)) {
					throw new IllegalArgumentException("Factory class is not a a descendant of " + UIApplicationFactory.class.getName());
				}
				factory = (UIApplicationFactory) appClass.newInstance();
			} catch (ClassNotFoundException | IllegalAccessException | InstantiationException e) {
				throw new RuntimeException(e);
			}
			return factory;
		}

		private static UIApplication startup() {
			Initializer.log.info("Starting application...");
			UIApplicationFactory factory = Initializer.createFactory();
			UIApplication app = factory.createApplication();
			app.initialize();
			Initializer.log.info("Application started");
			return app;
		}
	}
}
