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

import com.lowbudget.chess.front.app.*;
import com.lowbudget.chess.front.app.config.ReadOnlyEnginesConfig;
import com.lowbudget.chess.front.app.config.GameSetup;
import com.lowbudget.chess.front.app.config.PlayerSetup;
import com.lowbudget.chess.front.app.controller.EngineConnectionTester;
import com.lowbudget.chess.front.app.controller.game.NormalGame;
import com.lowbudget.chess.front.app.controller.game.ServerGame;
import com.lowbudget.chess.front.app.controller.game.TimeControl;
import com.lowbudget.chess.front.app.controller.player.EnginePlayer;
import com.lowbudget.chess.front.app.controller.player.HumanPlayer;
import com.lowbudget.chess.front.app.controller.player.ViewOnlyPlayer;
import com.lowbudget.chess.front.app.model.BoardModel;
import com.lowbudget.chess.front.app.model.GameModel;
import com.lowbudget.chess.front.app.model.GameModel.GameModelListener;
import com.lowbudget.common.ReadOnlyRecentHistory;
import com.lowbudget.chess.model.ChessConstants;
import com.lowbudget.chess.model.Move;
import com.lowbudget.chess.model.PlayerColor;
import com.lowbudget.chess.model.board.GameState;
import com.lowbudget.chess.model.board.IllegalMoveException;
import com.lowbudget.chess.model.board.MoveList;
import com.lowbudget.chess.model.notation.pgn.*;
import com.lowbudget.chess.model.uci.server.UCIServer;
import com.lowbudget.chess.model.uci.client.ThreadContextSessionListener;
import com.lowbudget.chess.model.uci.client.UCIClient;
import com.lowbudget.chess.model.uci.connect.Connectable;
import com.lowbudget.chess.model.uci.engine.UCIEngineConfig;
import com.lowbudget.chess.model.uci.engine.options.ButtonOption;
import com.lowbudget.chess.model.uci.engine.options.UCIOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;

/**
 * <p>A headless application that implements our application' s logic by leaving unimplemented GUI specific features</p>
 * <p>More specifically this implementation does not implement the following features:</p>
 * <ul>
 *     <li>It cannot create an actual {@link UIApplication.UITimer}. A dummy object is returned</li>
 *     <li>Any gui specific feature is silently ignored (i.e. showing a dialog or prompt a user for a message.
 *     As a result any functionality that depends on the dialog blocking (i.e. like {@link EnginePlayer} or
 *     {@link EngineConnectionTester}) <strong>will not work</strong> as is.</li>
 *     <li>It does not provide any thread running functionality in {@link #runInUIThread(Runnable)}. It
 *     just executes the runnable passed.</li>
 *     <li>It does not support actual copy-paste operations. The actual copy paste is ignored</li>
 * </ul>
 * <p>Isolating the non-gui stuff to a separate class helps in testing and it could help in other ways too e.g.
 * providing a command line version of the application if this becomes desirable</p>
 */
public class HeadlessApplication implements UIApplication {

	private static final Logger log = LoggerFactory.getLogger(HeadlessApplication.class);

	protected final UIModel model;

	private UCIClient.Factory clientFactory;
	private UCIServer.Factory serverFactory;

	private boolean hasCopiedText = false;
	private UITimerFactory timerFactory;
	private WaitDialogHolderFactory waitDialogHolderFactory;
	private ApplicationPlayerListener playerListener;

	public HeadlessApplication() {
		this.model = new UIModel();
	}

	@Override
	public void initialize() {
		this.playerListener = new ApplicationPlayerListener(this);
		GameModelListener gameModelListener = new ApplicationGameListener(this);

		GameModel gameModel = model.getGameModel();
		gameModel.addBoardModelListener(gameModelListener);
		gameModel.addGameModelListener(gameModelListener);

		model.getSetupBoardModel().addBoardModelListener(gameModelListener);

		setUCIClientFactory(UCIClient.DEFAULT_FACTORY);
		setUCIServerFactory(UCIServer.DEFAULT_FACTORY);
		setUITimerFactory( delay -> new EmptyUITimer() );
		setWaitDialogHolderFactory( EmptyWaitDialogHolder::new );
	}

	@Override
	public void exit() {
		GameModel gameModel = model.getGameModel();

		boolean cancelled = false;

		// make sure the game, the timer and all the player's resources (i.e. for engine players) are all stopped
		// otherwise the app might not close
		if (gameModel.hasBoard()) {
			closeGame();
			// if there is still a board, operation was cancelled
			if (gameModel.hasBoard()) {
				cancelled = true;
			}
		}

		if (!cancelled) {
			model.setServer(null);
			model.fireApplicationExit();
		}
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	@Override
	public void loadDefaultConfiguration() {
		model.loadConfiguration();
	}

	@Override
	public void loadConfiguration(File configFile) {
		model.loadConfiguration(configFile);
	}

	@Override
	public void setCheckUnsavedGame(boolean checkUnsavedGame) {
		model.setCheckUnsavedData(checkUnsavedGame);
	}

	@Override
	public void setSaveConfigurationOnExit(boolean saveConfigurationOnExit) {
		model.setSaveConfigurationOnExit(saveConfigurationOnExit);
	}

	@Override
	public void addStateListener(StateListener stateListener) {
		model.addStateListener(stateListener);
	}

	@Override
	public void removeStateListener(StateListener stateListener) {
		model.removeStateListener(stateListener);
	}

	@Override
	public void addModelListener(ModelListener listener) {
		model.addUIModelListener(listener);
	}

	@Override
	public void removeModelListener(ModelListener listener) {
		model.removeUIModelListener(listener);
	}

	@Override
	public GameModel getGameModel() {
		return model.getGameModel();
	}

	@Override
	public BoardModel getSetupBoardModel() {
		return model.getSetupBoardModel();
	}

	@Override
	public File getLastOpenFolderPath() {
		return model.getLastOpenFolderPath();
	}

	@Override
	public void setLastOpenFolderPath(File path) {
		model.setLastOpenFolderPath(path);
	}

	@Override
	public ReadOnlyRecentHistory<GameSetup> getRecentGameSetups() {
		return model.getRecentGameSetups();
	}

	@Override
	public GameSetup getLastGameSetup() {
		return model.getLastGameSetup();
	}

	@Override
	public ReadOnlyEnginesConfig getEnginesConfig() {
		return model.getEnginesConfig();
	}

	@Override
	public int getMaxRecentCount() {
		return model.getMaxRecentCount();
	}

	@Override
	public void setMaxRecentCount(int maxRecentCount) {
		model.setMaxRecentCount(maxRecentCount);
	}

	@Override
	public boolean isViewAttacksEnabled() {
		return model.isViewAttacksEnabled();
	}

	@Override
	public void setViewAttacksEnabled(boolean optionViewAttacksEnabled) {
		model.setViewAttacksEnabled(optionViewAttacksEnabled);
	}

	@Override
	public boolean isViewLegalMovesEnabled() {
		return model.isViewLegalMovesEnabled();
	}

	@Override
	public void setViewLegalMovesEnabled(boolean optionViewLegalMovesEnabled) {
		model.setViewLegalMovesEnabled(optionViewLegalMovesEnabled);
	}

	@Override
	public boolean isViewLastMoveEnabled() {
		return model.isViewLastMoveEnabled();
	}

	@Override
	public void setViewLastMoveEnabled(boolean optionViewLastMoveEnabled) {
		model.setViewLastMoveEnabled(optionViewLastMoveEnabled);
	}

	@Override
	public boolean isEnginesAlwaysRegisterLater() {
		return model.isEnginesAlwaysRegisterLater();
	}

	@Override
	public void setEnginesAlwaysRegisterLater(boolean value) {
		model.setEnginesAlwaysRegisterLater(value);
	}

	@Override
	public int getDefaultServerPort() {
		return model.getDefaultServerPort();
	}

	@Override
	public void setDefaultServerPort(int defaultPort) {
		model.setDefaultServerPort(defaultPort);
	}

	@Override
	public boolean isSetupMode() {
		return model.isSetupMode();
	}

	@Override
	public void flipBoard() {
		model.flipBoard();
	}

	@Override
	public boolean isBoardFlipped() {
		return model.isBoardFlipped();
	}

	@Override
	public void setTheme(String newTheme) {
		model.setTheme(newTheme);
	}

	@Override
	public String getTheme() {
		return model.getTheme();
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	@Override
	public void setUCIClientFactory(UCIClient.Factory clientFactory) {
		this.clientFactory = Objects.requireNonNull(clientFactory, "Client factory cannot be null!");
	}

	@Override
	public void setUCIServerFactory(UCIServer.Factory serverFactory) {
		this.serverFactory = Objects.requireNonNull(serverFactory, "Server factory cannot be null!");
	}

	@Override
	public void setUITimerFactory(UITimerFactory timerFactory) {
		this.timerFactory = Objects.requireNonNull(timerFactory, "UI timer factory cannot be null!");
	}

	@Override
	public void setWaitDialogHolderFactory(WaitDialogHolderFactory waitDialogHolderFactory) {
		this.waitDialogHolderFactory = Objects.requireNonNull(waitDialogHolderFactory, "WAit dialog holder factory cannot be null!");
	}

	@Override
	public boolean isNewGameActionAllowed() {
		GameModel boardModel = model.getGameModel();
		return !model.hasServer() && (!boardModel.hasBoard() || boardModel.hasStoppedGame());
	}

	@Override
	public boolean isPauseGameActionAllowed() {
		return !model.hasServer() && model.getGameModel().hasPlayingGame();
	}

	@Override
	public boolean isStopGameActionAllowed() {
		return !model.hasServer() && model.getGameModel().hasPlayingGame();
	}

	@Override
	public boolean isEnterSetupModeActionAllowed() {
		return !model.isSetupMode() && !model.hasServer() && !model.getGameModel().hasPlayingGame();
	}

	@Override
	public boolean isCloseGameActionAllowed() {
		return !model.hasServer() && model.getGameModel().hasBoard();
	}

	@Override
	public boolean isLoadGameActionAllowed() {
		GameModel boardModel = model.getGameModel();
		return !model.hasServer() && (!boardModel.hasBoard() || boardModel.hasStoppedGame());
	}

	@Override
	public boolean isSaveGameActionAllowed() {
		GameModel gameModel = model.getGameModel();
		return gameModel.hasStoppedGame() && gameModel.hasUnsavedGame();
	}

	@Override
	public boolean isBrowseMoveListActionAllowed() {
		GameModel boardModel = model.getGameModel();
		return boardModel.hasStoppedGame() && boardModel.hasMovesInMoveList();
	}

	@Override
	public boolean isExitSetupModeActionAllowed() {
		return model.isSetupMode();
	}

	@Override
	public boolean isCopyFenActionAllowed() {
		return model.getGameModel().hasBoard() || model.isSetupMode();
	}

	@Override
	public boolean isPasteFenActionAllowed() {
		return model.isSetupMode() && hasTransferableText();
	}

	@Override
	public boolean isManageEnginesActionAllowed() {
		return !model.hasServer() && !model.getGameModel().hasPlayingGame();
	}

	@Override
	public boolean isRunServerActionAllowed() {
		return !model.hasServer() && !model.getGameModel().hasPlayingGame();
	}

	@Override
	public boolean isStopServerActionAllowed() {
		return model.hasServer();
	}

	@Override
	public boolean isEngineSettingsActionAllowed(PlayerColor color) {
		return isEnginePlayer(color);
	}

	@Override
	public UCIEngineConfig getEngineConfigForPlayerOfColor(PlayerColor color) {
		return getEngineForPlayerOfColor(color).getEngineConfig();
	}

	@Override
	public void setEnginePlayerOptions(PlayerColor engineColor, List<UCIOption> newOptions) {
		Engine engine = getEngineForPlayerOfColor(engineColor);
		UCIEngineConfig engineConfig = engine.getEngineConfig();

		// copy the values from the new (possibly modified) options to the engine profile's options
		List<UCIOption> modifiedOptions = engineConfig.copyValuesFrom(newOptions);

		if (modifiedOptions.size() > 0) {
			// the engine is expected to be a running engine with its config in EnginesConfig, so mark the config as modified
			model.getEnginesConfig().setModified(true);

			// let the player know for any modifications so it can send the new values to the engine
			engine.sendModifiedOptions(modifiedOptions);
		}
	}

	@Override
	public void setEnginePlayerButtonOption(PlayerColor engineColor, ButtonOption buttonOption) {
		// let the player know for the button option clicked so it can notify the engine
		Engine engine = getEngineForPlayerOfColor(engineColor);
		engine.sendModifiedOptions(Collections.singletonList(buttonOption));
	}

	@Override
	public void addEngineConfig(Connectable.Params params, Consumer<UCIEngineConfig> successListener) {
		testEngineConnectionAndDoWithEngineConfig(params, testerEngineConfig -> {
			successListener.accept(testerEngineConfig);
			mergeEngineConfig(testerEngineConfig);
			showAddEngineSuccessMessage();
		});
	}

	@Override
	public void testEngineConfig(Connectable.Params params, DialogSuccessListener successListener) {

		testEngineConnectionAndDoWithEngineConfig(params, testerEngineConfig -> {
			// we are testing an existing engine profile or we are adding a new engine. Either way there is no
			// active engine player so we do not need to send any options back to the engine even if they are modified
			mergeEngineConfig(testerEngineConfig);
			successListener.onDialogSuccess();
			showEngineConnectionTesterSuccessMessage();
		});
	}

	@Override
	public boolean updateEngineConfigOptions(int engineConfigIndex, List<UCIOption> newOptions) {
		checkIndex(engineConfigIndex);
		UCIEngineConfig engineConfig = model.getEnginesConfig().get(engineConfigIndex);

		// copy the values from the dialog's (possibly modified) options to the engine profile's options
		List<UCIOption> modifiedOptions = engineConfig.copyValuesFrom(newOptions);

		boolean modified = modifiedOptions.size() > 0;
		if (modified) {
			model.getEnginesConfig().setModified(true);
		}
		return modified;
	}

	@Override
	public void removeEngineConfig(int engineConfigIndex, DialogSuccessListener successListener) {
		checkIndex(engineConfigIndex);

		showDeleteEngineConfirmDialog( () -> {
			model.getEnginesConfig().remove(engineConfigIndex);
			successListener.onDialogSuccess();
		});
	}

	@Override
	public void copyFenPosition() {
		String fen = model.getCurrentBoardModel().getFenPosition();
		copyToClipboard(fen);
		model.fireTextCopiedToClipboard();
	}

	@Override
	public void pasteFenPosition() {
		String fen = pasteFromClipboard();
		if (fen != null) {
			try {
				model.getSetupBoardModel().setFenPosition(fen);
			} catch (Exception ex) {
				showPasteFenFailedWarningMessage(fen);
			}
		}
	}

	@Override
	public void enterSetupMode() {
		model.setSetupMode(true);
	}

	@Override
	public void exitSetupMode() {
		model.setSetupMode(false);
	}

	@Override
	public void startNewGame(GameSetup gameSetup) {
		GameModel gameModel = model.getGameModel();
		closeAnyExistingGame();
		if (gameModel.hasBoard()) {
			return; // if the game still exists that means the used cancelled the action
		}

		// make sure we are not in setup mode
		model.setSetupMode(false);

		long whiteTotalTime = gameSetup.getWhiteSetup().getTime();
		long blackTotalTime = gameSetup.getBlackSetup().getTime();

		PlayerSetup whiteSetup = gameSetup.getWhiteSetup();
		PlayerSetup blackSetup = gameSetup.getBlackSetup();

		if (gameSetup.isRandomColor()) {
			// we should randomly assign a color to each player
			boolean firstPlayerIsWhite = ThreadLocalRandom.current().nextBoolean();

			if (!firstPlayerIsWhite) {
				whiteSetup = gameSetup.getBlackSetup().ofOppositeColor();
				blackSetup = gameSetup.getWhiteSetup().ofOppositeColor();
			}
		}
		Player whitePlayer = createHumanOrEnginePlayer(whiteSetup);
		Player blackPlayer = createHumanOrEnginePlayer(blackSetup);
		TimeControl timeControl = createTimeControl(whiteTotalTime, blackTotalTime, gameModel.getPlayingColor());

		Game game = new NormalGame(gameModel, whitePlayer, blackPlayer, timeControl);
		gameModel.setGame(game);
		gameModel.setFenPosition(gameSetup.getFenPosition());

		whitePlayer.addPlayerListener(playerListener);
		blackPlayer.addPlayerListener(playerListener);

		model.addRecentGameSetup(gameSetup);
		gameModel.startGame();
	}

	@Override
	public void pauseGame() {
		model.getGameModel().pauseGame();
	}

	@Override
	public void stopGame() {
		model.getGameModel().stopGame();
	}

	@Override
	public void closeGame() {
		boolean cancelled = false;
		if (model.isCheckUnsavedData()) {
			cancelled = checkGameAndSaveIfNeeded();
		}
		if (!cancelled) {
			GameModel gameModel = model.getGameModel();
			gameModel.getPlayerByColor(PlayerColor.WHITE).removePlayerListener(playerListener);
			gameModel.getPlayerByColor(PlayerColor.BLACK).removePlayerListener(playerListener);
			gameModel.closeGame();
		}
	}

	@Override
	public void loadGame() {
		GameModel gameModel = model.getGameModel();
		closeAnyExistingGame();
		if (gameModel.hasBoard()) {
			return; // user cancelled
		}

		File selectedFile = showSelectOpenFileDialog( );
		if (selectedFile == null) {
			return;
		}

		PgnGame pgnGame;
		try {
			pgnGame = PgnNotation.convertFromTextFile(selectedFile);
		} catch (PgnParseException e) {
			log.error("Error loading PGN", e);
			showOpenErrorMessage(selectedFile, e);
			return;
		}

		// make sure we are not in setup mode
		model.setSetupMode(false);

		Player whitePlayer = new ViewOnlyPlayer(PlayerColor.WHITE);
		Player blackPlayer = new ViewOnlyPlayer(PlayerColor.BLACK);
		TimeControl timeControl = createTimeControl(0, 0, gameModel.getPlayingColor());

		Game game = new NormalGame(gameModel, whitePlayer, blackPlayer, timeControl);
		gameModel.setGame(game);

		if (pgnGame.hasTag(PgnTag.WHITE_TAG_NAME)) {
			whitePlayer.setName(pgnGame.getTagValue(PgnTag.WHITE_TAG_NAME));
		}
		if (pgnGame.hasTag(PgnTag.BLACK_TAG_NAME)) {
			blackPlayer.setName(pgnGame.getTagValue(PgnTag.BLACK_TAG_NAME));
		}

		gameModel.loadGame(pgnGame);
	}

	@Override
	public void saveGame() {
		File selectedFile = showSelectSaveFileDialog( );
		if (selectedFile != null) {
			GameModel gameModel = model.getGameModel();
			PgnGame pgnGame = createPgnGameFromGameModel(gameModel);
			String pgn = PgnNotation.convertToString(pgnGame);
			try {
				Files.write(selectedFile.toPath(), pgn.getBytes());
			} catch (IOException e) {
				throw new RuntimeException("Could not save game to file: " + selectedFile.getAbsolutePath());
			}
			gameModel.saveGame();
		}
	}

	@Override
	public void browseMoveList(MoveList.BrowseType browseType) {
		model.getGameModel().browseMoveList(browseType);
	}

	@Override
	public void startServer(UCIEngineConfig engineConfig, int serverPort) {
		GameModel gameModel = model.getGameModel();
		closeAnyExistingGame();
		if (gameModel.hasBoard()) {
			return; // user cancelled
		}

		if (log.isDebugEnabled()) {
			log.debug("Running server at port: {} for engine {}", serverPort, engineConfig);
		}
		UCIServer server = serverFactory.create(engineConfig.getParams(), serverPort);

		gameModel.setFenPosition(ChessConstants.EMPTY_BOARD_FEN);

		TimeControl timeControl = createTimeControl(0, 0, gameModel.getPlayingColor());

		ServerGame game = new ServerGame(gameModel, new ViewOnlyPlayer(PlayerColor.WHITE), new ViewOnlyPlayer(PlayerColor.BLACK), timeControl);

		// the server game is also a session listener, wrap it with a thread context so it is notified in the
		// application's UI thread
		server.setSessionListener( new ThreadContextSessionListener(game, this::runInUIThread) );

		// this will close any previously existing game (if any - if the game exists it could only be in a stopped
		// state though otherwise this action would have been disabled)
		gameModel.setGame(game);

		model.setServer(server); // note: this notifies the listeners about state change
		server.start();
	}

	@Override
	public void stopServer() {
		closeGame();

		boolean cancelled = model.getGameModel().hasBoard();
		if (!cancelled) {
			model.setServer(null);
		}

	}

	WaitDialogHolder createWaitDialogHolder() {
		return waitDialogHolderFactory.createWaitDialogHolder();
	}

	protected void copyToClipboard(String text) {
		this.hasCopiedText = true;
	}

	protected String pasteFromClipboard() { return null; }

	protected boolean hasTransferableText() {
		return this.hasCopiedText;
	}

	protected void runInUIThread(Runnable r) {
		r.run();
	}

	protected void showDeleteEngineConfirmDialog(DialogSuccessListener successListener) {
		throw new UnsupportedOperationException();
	}

	protected void showEngineConnectionTesterTimedOutWarningMessage() {
		throw new UnsupportedOperationException();
	}
	protected void showEngineConnectionTesterFailedWarningMessage(Exception error) {
		throw new UnsupportedOperationException();
	}
	protected void showEngineConnectionTesterSuccessMessage() {
		throw new UnsupportedOperationException();
	}
	protected void showAddEngineSuccessMessage() {
		throw new UnsupportedOperationException();
	}
	protected void showEnginePlayerRegistrationDialog(Player player, RegistrationDialogSuccessListener successListener, DialogCancelListener cancelListener) {
		throw new UnsupportedOperationException();
	}
	protected void showEnginePlayerUnexpectedDisconnectionErrorMessage() {
		throw new UnsupportedOperationException();
	}
	protected void showEnginePlayerCopyProtectionWarningMessage(String playerName, PlayerColor playerColor) {
		throw new UnsupportedOperationException();
	}
	protected void showGameFinalMoveInfoMessage(GameState state) {
		throw new UnsupportedOperationException();
	}
	protected void showIllegalMoveInfoMessage(IllegalMoveException e) {
		throw new UnsupportedOperationException();
	}
	protected void showGamePauseDialog() {
		throw new UnsupportedOperationException();
	}
	protected void showPasteFenFailedWarningMessage(String fen) {
		throw new UnsupportedOperationException();
	}
	protected void showGameTimeElapsedWarningMessage(String looserName) {
		throw new UnsupportedOperationException();
	}
	protected void showExceptionMessage(Exception exception) {
		throw new UnsupportedOperationException();
	}
	protected void showOpenErrorMessage(File openedFile, Exception exception) {
		throw new UnsupportedOperationException();
	}
	protected boolean showCloseGameConfirmDialog(DialogSuccessListener successListener) {
		// note: we cannot throw an exception here, some of our tests depend on this method returning false
		return false;
	}
	protected File showSelectSaveFileDialog() {
		throw new UnsupportedOperationException();
	}
	protected File showSelectOpenFileDialog() {
		throw new UnsupportedOperationException();
	}

	/*package*/ List<UCIOption> mergeEngineConfig(UCIEngineConfig newEngineConfig) {
		return model.getEnginesConfig().merge(newEngineConfig);
	}

	/*package*/ void publishOnEngineInfoAvailable(String info) {
		model.fireInfoAvailable(info);
	}

	private static PgnGame createPgnGameFromGameModel(GameModel gameModel) {
		SimplePgnGame pgnGame = new SimplePgnGame();

		// add seven roster tags
		pgnGame.addTag(PgnTag.EVENT_TAG_NAME, "Casual game");
		pgnGame.addTag(PgnTag.SITE_TAG_NAME, "Local game");
		pgnGame.addTag(PgnTag.DATE_TAG_NAME, LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy.MM.dd")));
		pgnGame.addTag(PgnTag.ROUND_TAG_NAME, "");
		pgnGame.addTag(PgnTag.WHITE_TAG_NAME, gameModel.getPlayerByColor(PlayerColor.WHITE).getName());
		pgnGame.addTag(PgnTag.BLACK_TAG_NAME, gameModel.getPlayerByColor(PlayerColor.BLACK).getName());
		pgnGame.addTag(PgnTag.RESULT_TAG_NAME, gameModel.getGameResult().text());

		int totalMoves = gameModel.getTotalMoves();
		for (int i = 0; i < totalMoves; i++) {
			Move white = gameModel.getMove(i, 0);
			Move black = gameModel.getMove(i, 1);
			if (white.isValid()) {
				pgnGame.addMove(white);
			}
			if (black.isValid()) {
				pgnGame.addMove(black);
			}
		}
		pgnGame.setGameResult(gameModel.getGameResult());

		return pgnGame;
	}

	private void checkIndex(int index) {
		int size = model.getEnginesConfig().size();
		if (index < 0 || index >= size) {
			throw new IndexOutOfBoundsException("Engines config index: " + index + " is invalid, current size: " + size);
		}
	}

	private TimeControl createTimeControl(long whiteTime, long blackTime, PlayerColor sideToMove) {
		UITimer timer = timerFactory.createTimer(TimeControl.TIMER_INTERVAL);
		TimeControl timeControl = new TimeControl(timer);
		timeControl.setup(whiteTime, blackTime, sideToMove.isWhite());
		return timeControl;
	}

	private void closeAnyExistingGame() {
		GameModel gameModel = model.getGameModel();
		if (gameModel.hasBoard()) {
			closeGame();
		}
	}

	private boolean checkGameAndSaveIfNeeded() {
		GameModel gameModel = model.getGameModel();
		boolean cancelled = false;
		if (gameModel.hasUnsavedGame()) {
			cancelled = showCloseGameConfirmDialog(this::saveGame);
		}
		return cancelled;
	}

	private Player createHumanOrEnginePlayer(PlayerSetup playerSetup) {
		if (playerSetup.isHuman()) {
			return new HumanPlayer(playerSetup.getName(), playerSetup.getColor());
		}

		Connectable.Params params = playerSetup.getConnectParams();
		String name = playerSetup.getName();

		// make sure the client's delegate methods are execute in the UI thread
		UCIClient client = clientFactory.create(firstWord(name), params, this::runInUIThread);

		EnginePlayer player = new EnginePlayer(name, playerSetup.getColor(), client);
		player.setAlwaysRegisterLater(model.isEnginesAlwaysRegisterLater());
		return player;
	}

	private static String firstWord(String name) {
		String result = name;
		String[] parts = name.split(" ");
		if (parts.length > 0) {
			result = parts[0].toLowerCase();
		}
		return result;
	}

	private boolean isEnginePlayer(PlayerColor color) {
		GameModel boardModel = model.getGameModel();
		return boardModel.hasPlayingGame() && boardModel.getPlayerByColor(color).isEngine();
	}

	private Engine getEngineForPlayerOfColor(PlayerColor enginePlayerColor) {
		GameModel boardModel = model.getGameModel();
		if (!isEnginePlayer(enginePlayerColor)) {
			throw new IllegalStateException("Player with color: " + enginePlayerColor + " is not an engine!");
		}
		return (Engine) boardModel.getPlayerByColor(enginePlayerColor);
	}

	private void testEngineConnectionAndDoWithEngineConfig(Connectable.Params params, Consumer<UCIEngineConfig> successListener) {
		UCIClient client = clientFactory.create("test", params, this::runInUIThread);
		UIApplication.UITimer timer = timerFactory.createTimer(5 * 1000); // 5 seconds
		UIApplication.WaitDialogHolder dialogHolder = waitDialogHolderFactory.createWaitDialogHolder();
		EngineConnectionTester connectionTester = new EngineConnectionTester(client, timer, dialogHolder);

		if (log.isInfoEnabled()) {
			log.info("Testing connection to engine for params: {}", params);
		}

		// Warning: here we depend on the fact that the connection tester displays a dialog - this is a limitation
		// of this class' implementation
		connectionTester.start();

		if (log.isInfoEnabled()) {
			log.info("Testing connection to engine finished");
		}

		if (!connectionTester.isSuccessful()) {
			if (connectionTester.isTimedOut()) {
				showEngineConnectionTesterTimedOutWarningMessage();
			} else {
				Exception error = connectionTester.getError();
				showEngineConnectionTesterFailedWarningMessage(error);
			}
		} else {
			successListener.accept(connectionTester.getEngineConfig());
		}
	}

}
