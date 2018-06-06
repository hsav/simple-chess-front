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

package com.lowbudget.chess.front.swing.actions.game;

import com.lowbudget.chess.front.app.config.GameSetup;
import com.lowbudget.chess.front.app.UIApplication;
import com.lowbudget.chess.front.app.UIApplication.StateListener;
import com.lowbudget.chess.front.app.config.PlayerSetup;
import com.lowbudget.common.Messages;
import com.lowbudget.chess.front.swing.common.RecentHistoryMenu;
import com.lowbudget.chess.front.swing.actions.ApplicationAction;
import com.lowbudget.chess.front.swing.common.UIUtils;
import com.lowbudget.chess.front.swing.components.forms.NewGameDialog;

import java.awt.event.ActionEvent;
import java.util.Objects;

import static com.lowbudget.chess.front.swing.common.TimeStringFormatter.formatTime;

public class NewGameAction extends ApplicationAction implements RecentHistoryMenu.RecentMenuAction, StateListener {

	public NewGameAction(UIApplication application) {
		super(application, null);
		application.addStateListener(this);
	}

	public NewGameAction(UIApplication application, String titleKey) {
		super(application, titleKey, UIUtils.getImage("new-game.png"), "app.action.game.new.tooltip");
		application.addStateListener(this);
	}

	@Override
	protected void doAction(ActionEvent e) {
		GameSetup gameSetup = getGameSetup();

		// if we have a valid game setup (either in the form of a recent game or a new setup created by the
		// new menu dialog) start a new game
		// note that it is natural to have a null value here (i.e. if the user cancelled the new game dialog)
		if (gameSetup != null) {
			application.startNewGame(gameSetup);
		}
	}

	private GameSetup getGameSetup() {
		// first check if we are called from the menu for a recent game setup
		GameSetup gameSetup = (GameSetup) getValue(RecentHistoryMenu.RECENT_ITEM_PROPERTY_NAME);

		// if not, open the dialog for the user to create a new game setup
		if (gameSetup == null) {
			NewGameDialog dialog = new NewGameDialog(application);
			dialog.setVisible(true);
			if (!dialog.isCancelled()) {
				gameSetup = dialog.getGameSetup();
			}
		}
		return gameSetup;
	}

	@Override
	public void onActionRemovedFromMenu() {
		// if the action is removed from the menu, it is not going to be used again, so do not listen for state
		// changes anymore
		application.removeStateListener(this);
	}

	@Override
	public String getMenuItemDescription(Object item) {
		GameSetup gameSetup = (GameSetup) item;
		return toFriendlyDescription(gameSetup, Messages.get("app.game.setup.random.color"));
	}

	@Override
	public void onStateChanged() {
		setEnabled( application.isNewGameActionAllowed() );
	}

	/**
	 * Converts the game setup specified to a string using a human-friendly form like: John - StockFish 1 min
	 * Such a description is appropriate to describe the game i.e. in a menu item
	 * @param gameSetup the game setup to convert to a human-friendly description string
	 * @param randomColorTranslation the translation that should be used for the random color
	 * @return the friendly description of the game setup specified
	 */
	private static String toFriendlyDescription(GameSetup gameSetup, String randomColorTranslation) {
		Objects.requireNonNull(gameSetup);
		PlayerSetup white = Objects.requireNonNull(gameSetup.getWhiteSetup());
		PlayerSetup black = Objects.requireNonNull(gameSetup.getBlackSetup());

		StringBuilder sb = new StringBuilder();
		appendPlayerSetup(sb, white);
		sb.append(' ').append('-').append(' ');
		appendPlayerSetup(sb, black);

		if (white.getTime() == black.getTime()) {
			sb.append(' ').append('(').append(formatTime(white.getTime(), true));

			if (gameSetup.isRandomColor()) {
				sb.append(' ').append('-').append(' ').append(randomColorTranslation);
			}
			sb.append(')');
		}

		return sb.toString();
	}

	private static void appendPlayerSetup(StringBuilder sb, PlayerSetup player) {
		sb.append(player.getName());
		if (!player.isHuman()) {
			sb.append(' ').append(player.getConnectParams());
		}
	}
}
