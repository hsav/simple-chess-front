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

package com.lowbudget.chess.front.swing.actions.engine;

import com.lowbudget.chess.front.app.UIApplication;
import com.lowbudget.chess.front.app.UIApplication.StateListener;
import com.lowbudget.chess.model.uci.engine.UCIEngineConfig;
import com.lowbudget.chess.front.swing.common.UIAction;
import com.lowbudget.chess.front.swing.components.forms.EngineSettingsDialog;
import com.lowbudget.chess.front.swing.components.forms.EngineSettingsDialog.ButtonActionFactory;
import com.lowbudget.chess.model.PlayerColor;

import javax.swing.*;
import java.awt.event.ActionEvent;

/**
 * <p>Displays the available options of a chess engine.</p>
 * <p>This action can be called by mor ethan one places either from the manage engines dialog to test an already
 * registered engine config or from the menu for the black and/or white players in case one or both of them are an
 * engine. We distinguish the case we are currently running in from the color {@link #enginePlayerColor} member.</p>
 */
public class EngineSettingsAction extends AbstractEngineAction implements StateListener {

	/**
	 * <p>The color of the engine player for which we have opened the settings dialog for in case a game is currently in
	 * progress and one or both players are an engine.</p>
	 * <p>In case the action is called from the manage engines dialog (where no game is in progress) this field has
	 * the value of {@code null}</p>
	 */
	private final PlayerColor enginePlayerColor;

	EngineSettingsAction(UIApplication application, String titleKey) {
		this(application, titleKey, null);
	}

	public EngineSettingsAction(UIApplication application, String titleKey, PlayerColor enginePlayerColor) {
		super(application, titleKey);
		this.enginePlayerColor = enginePlayerColor;

		if (enginePlayerColor != null) {
			application.addStateListener(this);
		}
	}

	@Override
	protected void doAction(ActionEvent e) {

		if (enginePlayerColor == null) {
			// we are editing a stored profile
			UCIEngineConfig engineConfig = getSelectedEngineConfig();

			// factory to create actions for button options (if any)
			ButtonActionFactory buttonActionFactory = createButtonActionFactory(application, false, null);

			EngineSettingsDialog dialog = new EngineSettingsDialog(engineConfig, buttonActionFactory);
			dialog.setVisible(true);
			if (!dialog.isCancelled()) {
				int selectedIndex = getSelectedIndex();
				boolean modified = application.updateEngineConfigOptions(selectedIndex, dialog.getOptions());
				if (modified) {
					getListModel().itemUpdated(selectedIndex);
				}
			}
		} else {
			// there is an engine player running, use its engine's config for editing.
			UCIEngineConfig engineConfig = application.getEngineConfigForPlayerOfColor(enginePlayerColor);

			// create the appropriate factory to create actions for button options (if any)
			ButtonActionFactory buttonActionFactory = createButtonActionFactory(application, true, enginePlayerColor);

			EngineSettingsDialog dialog = new EngineSettingsDialog(engineConfig, buttonActionFactory);
			dialog.setVisible(true);
			if (!dialog.isCancelled()) {
				// let the engine player know for any modifications so it can send the new values to the engine
				application.setEnginePlayerOptions(enginePlayerColor, dialog.getOptions());
			}
		}
	}

	@Override
	public void onStateChanged() {
		setEnabled(application.isEngineSettingsActionAllowed(enginePlayerColor) );
	}

	private static ButtonActionFactory createButtonActionFactory(UIApplication application, boolean isEngineRunning, PlayerColor engineColor) {
		return buttonOption -> {
			Action buttonAction = new UIAction("app.dialog.chess.engine.settings.action.execute") {
				@Override
				protected void doAction(ActionEvent e) {
					if (isEngineRunning) {
						application.setEnginePlayerButtonOption(engineColor, buttonOption);
					}
				}
			};
			buttonAction.setEnabled(isEngineRunning);
			return buttonAction;
		};
	}
}
