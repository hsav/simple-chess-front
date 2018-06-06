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

package com.lowbudget.chess.front.swing.actions;

import com.lowbudget.chess.front.app.Constants.Theme;
import com.lowbudget.chess.front.app.UIApplication;
import com.lowbudget.chess.model.PlayerColor;
import com.lowbudget.chess.front.swing.actions.engine.EngineSettingsAction;
import com.lowbudget.chess.front.swing.actions.engine.ManageEnginesAction;
import com.lowbudget.chess.front.swing.actions.engine.RunEngineAsServerAction;
import com.lowbudget.chess.front.swing.actions.engine.StopServerAction;
import com.lowbudget.chess.front.swing.actions.game.*;
import com.lowbudget.chess.front.swing.actions.setup.EnterSetupModeAction;
import com.lowbudget.chess.front.swing.actions.setup.ExitSetupModeAction;
import com.lowbudget.chess.front.swing.actions.setup.PasteFenAction;
import com.lowbudget.chess.front.swing.common.RecentHistoryMenu;

import javax.swing.*;

/**
 * <p>Stores all the actions used by the application and are available thought the application menus.</p>
 * <p>Most of these actions need to be used in more than one place (e.g. in the application's menu and in a toolbar)
 * so this is the place where we keep their references</p>
 */
public class Actions {

	private final UIApplication application;

	private final Action exitAction;

	private final Action newGameAction;
	private final Action openGameAction;
	private final Action saveGameAction;
	private final Action pauseGameAction;
	private final Action stopGameAction;
	private final Action closeGameAction;
	private final Action enterSetupModeAction;
	private final Action exitSetupModeAction;

	private final Action flipBoardAction;
	private final Action viewLegalMovesAction;
	private final Action viewAttacksAction;
	private final Action viewLastMoveAction;
	private final Action changeThemeWikipediaAction;
	private final Action changeThemeStoneAction;
	private final Action changeThemeWoodAction;

	private final Action copyAction;
	private final Action pasteAction;
	private final Action preferencesAction;

	private final Action manageEnginesAction;
	private final Action runAsServerAction;
	private final Action stopServerAction;
	private final Action whiteEngineSettingsAction;
	private final Action blackEngineSettingsAction;

	public Actions(UIApplication application) {
		this.application = application;

		// game menu
		newGameAction = new NewGameAction(application, "app.action.game.new");
		openGameAction = new OpenGameAction(application, "app.action.game.open");
		saveGameAction = new SaveGameAction(application, "app.action.game.save");
		pauseGameAction = new PauseGameAction(application, "app.action.game.pause");
		stopGameAction = new StopGameAction(application, "app.action.game.stop");
		closeGameAction = new CloseGameAction(application, "app.action.game.close");
		enterSetupModeAction = new EnterSetupModeAction(application, "app.action.game.setup.start");
		exitSetupModeAction = new ExitSetupModeAction(application, "app.action.game.setup.stop");
		exitAction = new ExitAction(application, "app.action.exit");

		// view menu
		flipBoardAction = new FlipBoardAction(application, "app.action.view.flip.board");
		viewLegalMovesAction = new ViewOptionAction(application, "app.action.view.legal.moves", application::isViewLegalMovesEnabled, application::setViewLegalMovesEnabled);
		viewAttacksAction = new ViewOptionAction(application, "app.action.view.attacks", application::isViewAttacksEnabled, application::setViewAttacksEnabled);
		viewLastMoveAction = new ViewOptionAction(application, "app.action.view.last.move", application::isViewLastMoveEnabled, application::setViewLastMoveEnabled);
		// themes sub-menu
		changeThemeWikipediaAction = new ChangeThemeAction(application, "app.action.view.theme.wikipedia", Theme.WIKIPEDIA);
		changeThemeStoneAction = new ChangeThemeAction(application, "app.action.view.theme.stone", Theme.STONE);
		changeThemeWoodAction = new ChangeThemeAction(application, "app.action.view.theme.wood", Theme.WOOD);

		// edit menu
		copyAction = new CopyFenAction(application, "app.action.edit.copy.board.fen");
		pasteAction = new PasteFenAction(application, "app.action.edit.paste.board.fen");
		preferencesAction = new PreferencesAction(application, "app.action.preferences");

		// engine menu
		manageEnginesAction = new ManageEnginesAction(application, "app.action.engine.manage");
		runAsServerAction = new RunEngineAsServerAction(application, "app.action.engine.run.as.server");
		stopServerAction = new StopServerAction(application, "app.action.engine.stop.server");
		whiteEngineSettingsAction = new EngineSettingsAction(application, "app.action.engine.settings.white", PlayerColor.WHITE);
		blackEngineSettingsAction = new EngineSettingsAction(application, "app.action.engine.settings.black", PlayerColor.BLACK);
	}

	public RecentHistoryMenu.RecentMenuActionFactory getRecentMenuActionFactory() {
		return () -> new NewGameAction(application);
	}

	public Action getExitAction() {
		return exitAction;
	}

	public Action getNewGameAction() {
		return newGameAction;
	}

	public Action getOpenGameAction() {
		return openGameAction;
	}

	public Action getSaveGameAction() {
		return saveGameAction;
	}

	public Action getPauseGameAction() {
		return pauseGameAction;
	}

	public Action getStopGameAction() {
		return stopGameAction;
	}

	public Action getCloseGameAction() {
		return closeGameAction;
	}

	public Action getEnterSetupModeAction() {
		return enterSetupModeAction;
	}

	public Action getExitSetupModeAction() {
		return exitSetupModeAction;
	}

	public Action getFlipBoardAction() {
		return flipBoardAction;
	}

	public Action getViewLegalMovesAction() {
		return viewLegalMovesAction;
	}

	public Action getViewAttacksAction() {
		return viewAttacksAction;
	}

	public Action getViewLastMoveAction() {
		return viewLastMoveAction;
	}

	public Action getChangeThemeWikipediaAction() {
		return changeThemeWikipediaAction;
	}

	public Action getChangeThemeStoneAction() {
		return changeThemeStoneAction;
	}

	public Action getChangeThemeWoodAction() {
		return changeThemeWoodAction;
	}

	public Action getCopyAction() {
		return copyAction;
	}

	public Action getPasteAction() {
		return pasteAction;
	}

	public Action getPreferencesAction() {
		return preferencesAction;
	}

	public Action getManageEnginesAction() {
		return manageEnginesAction;
	}

	public Action getRunAsServerAction() {
		return runAsServerAction;
	}

	public Action getStopServerAction() {
		return stopServerAction;
	}

	public Action getWhiteEngineSettingsAction() {
		return whiteEngineSettingsAction;
	}

	public Action getBlackEngineSettingsAction() {
		return blackEngineSettingsAction;
	}
}
