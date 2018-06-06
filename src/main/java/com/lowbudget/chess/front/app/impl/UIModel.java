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

package com.lowbudget.chess.front.app.impl;

import com.lowbudget.chess.front.app.model.*;
import com.lowbudget.chess.front.app.Constants;
import com.lowbudget.chess.front.app.UIApplication.StateListener;
import com.lowbudget.chess.front.app.UIApplication.ModelListener;
import com.lowbudget.chess.front.app.config.Configuration;
import com.lowbudget.chess.front.app.config.EnginesConfig;
import com.lowbudget.chess.front.app.config.GameSetup;
import com.lowbudget.common.ListenerList;
import com.lowbudget.common.RecentHistory;
import com.lowbudget.chess.model.uci.server.UCIServer;

import java.io.File;
import java.util.Objects;

/**
 * <p>The model that supports our UI.</p>
 * <p>Any state needed by the various application components needs to be stored here.</p>
 */
public class UIModel {

	/**
	 * Stores listeners that wish to be notified that the application's state has changed.
	 */
	private final ListenerList<StateListener> stateListeners = new ListenerList<>();

	/**
	 * The application's configuration
	 */
	private final Configuration configuration;

	/**
	 * Indicates if the configuration should be saved automatically when the application exits
	 */
	private boolean saveConfigurationOnExit;

	/**
	 * Indicates if the application should check if the game is saved when it is closed or on application exit.
	 * In that case the user is prompted if the data should be saved
	 */
	private boolean checkUnsavedData = true;

	/**
	 * The listener list of all components that need to be notified when the UI model changes.
	 */
	private final ListenerList<ModelListener> listenerList = new ListenerList<>();

	/**
	 * Indicates if we are currently in setup mode
	 */
	private boolean setupMode;

	/**
	 * Indicates if the board is flipped
	 */
	private boolean flipped;

	/**
	 * The current running server (if any)
	 */
	private UCIServer server;

	/**
	 * The game model used for chess games
	 */
	private final GameModel gameModel;

	/**
	 * The board model used for the setup board position screen
	 */
	private final BoardModel setupBoardModel;

	UIModel() {
		this.configuration = new Configuration();
		gameModel = new DefaultGameModel();
		setupBoardModel = new SetupBoardModel();

		// listen to game related events and fire the state changed events so various actions are enabled/disabled
		// based on game state
		this.gameModel.addGameModelListener( new SingleActionGameModelListener(this::fireStateChanged) );
	}

	void loadConfiguration() {
		this.configuration.load();
	}

	void loadConfiguration(File configFile) {
		this.configuration.load(configFile);
	}

	boolean isCheckUnsavedData() {
		return checkUnsavedData;
	}

	void setSaveConfigurationOnExit(boolean saveConfigurationOnExit) {
		this.saveConfigurationOnExit = saveConfigurationOnExit;
	}

	void setCheckUnsavedData(boolean checkUnsavedData) {
		this.checkUnsavedData = checkUnsavedData;
	}

	void addStateListener(StateListener stateListener) {
		stateListeners.add(stateListener);
		// when a listener registers itself, immediately notify it to adjust its status
		stateListener.onStateChanged();
	}

	void removeStateListener(StateListener stateListener) {
		stateListeners.remove(stateListener);
	}

	void addUIModelListener(ModelListener listener) {
		this.listenerList.add(listener);
	}

	@SuppressWarnings("unused")
	void removeUIModelListener(ModelListener listener) {
		this.listenerList.remove(listener);
	}

	GameModel getGameModel() {
		return gameModel;
	}

	BoardModel getSetupBoardModel() {
		return setupBoardModel;
	}

	BoardModel getCurrentBoardModel() {
		return setupMode ? setupBoardModel : gameModel;
	}

	boolean hasServer() {
		return this.server != null;
	}

	void setServer(UCIServer server) {
		if (this.server != server) {
			if (this.server != null) {
				this.server.stop();
				this.server.setServerListener(null);
			}
			this.server = server;

			if (this.server != null) {
				this.server.setServerListener( new ServerListener() );
			}
			fireStateChanged();
		}
	}

	File getLastOpenFolderPath() {
		return configuration.getLastOpenFolderPath();
	}

	void setLastOpenFolderPath(File path) {
		configuration.setLastOpenFolderPath(path);
	}

	RecentHistory<GameSetup> getRecentGameSetups() {
		return configuration.getRecentGames();
	}

	void addRecentGameSetup(GameSetup gameSetup) {
		configuration.addRecentGameSetup(gameSetup);
	}

	GameSetup getLastGameSetup() {
		return configuration.getLastGameSetup();
	}

	EnginesConfig getEnginesConfig() {
		return this.configuration.getEnginesConfig();
	}

	int getMaxRecentCount() {
		return configuration.getMaxRecentCount();
	}

	void setMaxRecentCount(int maxRecentCount) {
		configuration.setMaxRecentCount(maxRecentCount);
	}

	boolean isViewAttacksEnabled() {
		return configuration.isViewAttacks();
	}

	void setViewAttacksEnabled(boolean value) {
		if (configuration.isViewAttacks() != value) {
			configuration.setViewAttacks(value);
			listenerList.notifyAllListeners(ModelListener::onOptionChanged);
			fireStateChanged();
		}
	}

	boolean isViewLegalMovesEnabled() {
		return configuration.isViewLegalMoves();
	}

	void setViewLegalMovesEnabled(boolean value) {
		if (configuration.isViewLegalMoves() != value) {
			configuration.setViewLegalMoves(value);
			listenerList.notifyAllListeners(ModelListener::onOptionChanged);
			fireStateChanged();
		}
	}

	boolean isViewLastMoveEnabled() {
		return configuration.isViewLastMove();
	}

	void setViewLastMoveEnabled(boolean value) {
		if (configuration.isViewLastMove() != value) {
			configuration.setViewLastMove(value);
			listenerList.notifyAllListeners(ModelListener::onOptionChanged);
			fireStateChanged();
		}
	}

	boolean isEnginesAlwaysRegisterLater() {
		return configuration.isEnginesAlwaysRegisterLater();
	}

	void setEnginesAlwaysRegisterLater(boolean value) {
		if (configuration.isEnginesAlwaysRegisterLater() != value) {
			configuration.setEnginesAlwaysRegisterLater(value);

			// let the game model know too
			gameModel.setEnginesShouldAlwaysRegisterLater(value);

			listenerList.notifyAllListeners(ModelListener::onOptionChanged);
			fireStateChanged();
		}
	}

	int getDefaultServerPort() {
		return configuration.getDefaultServerPort();
	}

	void setDefaultServerPort(int defaultPort) {
		configuration.setDefaultServerPort(defaultPort);
	}

	boolean isSetupMode() {
		return setupMode;
	}

	void setSetupMode(boolean newValue) {
		boolean oldValue = this.setupMode;
		if (oldValue != newValue) {
			if (newValue) {
				this.setupMode = true;
				listenerList.notifyAllListeners(ModelListener::onSetupModeEntered);
			} else {
				this.setupMode = false;
				listenerList.notifyAllListeners(ModelListener::onSetupModeExited);
			}
			fireStateChanged();
		}
	}


	void flipBoard() {
		this.flipped = !this.flipped;
		listenerList.notifyAllListeners(listener -> listener.onBoardFlipped(flipped));
	}

	boolean isBoardFlipped() {
		return this.flipped;
	}

	public void setTheme(final String newTheme) {
		Objects.requireNonNull(newTheme, "Theme cannot be set to null");
		configuration.setTheme(newTheme);
		listenerList.notifyAllListeners(listener -> listener.onThemeChanged(newTheme));
		fireStateChanged();
	}

	public String getTheme() {
		String theme = configuration.getTheme();
		if (theme == null || theme.isEmpty()) {
			theme = Constants.Theme.WOOD;
			setTheme(theme);
		}
		return theme;
	}

	void fireApplicationExit() {
		if (saveConfigurationOnExit) {
			configuration.save();
		}

		listenerList.notifyAllListeners(ModelListener::onApplicationExit);
		listenerList.clear();
		stateListeners.clear();
	}

	void fireTextCopiedToClipboard() {
		fireStateChanged();
	}

	void fireInfoAvailable(String info) {
		listenerList.notifyAllListeners( listener -> listener.onEngineInfoAvailable(info) );
	}

	private void fireStateChanged() {
		stateListeners.notifyAllListeners(StateListener::onStateChanged);
	}

	private class ServerListener implements UCIServer.ServerListener {

		@Override
		public void onServerStarted(int port) {
			listenerList.notifyAllListeners(listener -> listener.onServerStarted(port));
		}

		@Override
		public void onServerStopped() {
			listenerList.notifyAllListeners(ModelListener::onServerStopped);
		}

		@Override
		public void onClientConnected(int port) {
			listenerList.notifyAllListeners(listener -> listener.onClientConnected(port));
		}

		@Override
		public void onClientDisconnected(int port) {
			listenerList.notifyAllListeners(listener -> listener.onClientDisconnected(port));
		}
	}
}
