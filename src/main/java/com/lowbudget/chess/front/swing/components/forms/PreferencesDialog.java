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

package com.lowbudget.chess.front.swing.components.forms;

import com.lowbudget.chess.front.app.Constants;
import com.lowbudget.chess.front.app.UIApplication;
import com.lowbudget.common.Messages;
import com.lowbudget.chess.front.swing.common.FormDialog;

import javax.swing.*;
import java.util.Arrays;

import static com.lowbudget.chess.front.swing.components.forms.DialogHelper.createBorderlessCheckBox;

/**
 * Displays a modal dialog that allows to edit the application's preferences
 */
public class PreferencesDialog extends ApplicationFormDialog {

	/* Message keys used by this class */
	private static final String KEY_MAIN_DIALOG_TITLE = "app.dialog.preferences.title";
	private static final String KEY_OK_BUTTON = "app.button.ok";
	private static final String KEY_CANCEL_BUTTON = "app.button.cancel";
	private static final String KEY_MAX_RECENT_ITEMS_LABEL = "app.dialog.preferences.max.recent.items.label";
	private static final String KEY_VIEW_LEGAL_MOVES_LABEL = "app.action.view.legal.moves";
	private static final String KEY_VIEW_ATTACKS_LABEL = "app.action.view.attacks";
	private static final String KEY_VIEW_LAST_MOVE_LABEL = "app.action.view.last.move";
	private static final String KEY_THEME_LABEL = "app.dialog.preferences.theme.label";
	private static final String KEY_ALWAYS_REGISTER_LATER_LABEL = "app.dialog.preferences.register.later.label";
	private static final String KEY_DEFAULT_SERVER_PORT = "app.dialog.preferences.default.server.port.label";

	// the options that are currently allowed to be changed
	private final JSpinner maxRecentItems;
	private final JCheckBox viewLegalMoves;
	private final JCheckBox viewAttacks;
	private final JCheckBox viewLastMove;
	private final JComboBox<String> themes;
	private final JCheckBox alwaysRegisterLater;
	private final JSpinner defaultServerPort;

	public PreferencesDialog(UIApplication application) {
		super(application, Messages.get(KEY_MAIN_DIALOG_TITLE));

		SpinnerModel spinnerModel = new SpinnerNumberModel(0, 0, 20, 1);
		maxRecentItems = new JSpinner(spinnerModel);

		viewLegalMoves = createBorderlessCheckBox();
		viewAttacks = createBorderlessCheckBox();
		viewLastMove = createBorderlessCheckBox();

		alwaysRegisterLater = createBorderlessCheckBox();

		defaultServerPort = DialogHelper.createPortSpinner(application.getDefaultServerPort());

		themes = new JComboBox<>(Constants.Theme.allThemes());

		setRowsFactory(() -> Arrays.asList(
				FormDialog.Row.withLabel(Messages.get(KEY_MAX_RECENT_ITEMS_LABEL)).andComponent(maxRecentItems),
				FormDialog.Row.withLabel(Messages.get(KEY_VIEW_LEGAL_MOVES_LABEL)).andComponent(viewLegalMoves),
				FormDialog.Row.withLabel(Messages.get(KEY_VIEW_ATTACKS_LABEL)).andComponent(viewAttacks),
				FormDialog.Row.withLabel(Messages.get(KEY_VIEW_LAST_MOVE_LABEL)).andComponent(viewLastMove),
				FormDialog.Row.withLabel(Messages.get(KEY_THEME_LABEL)).andComponent(themes),
				FormDialog.Row.withLabel(Messages.get(KEY_ALWAYS_REGISTER_LATER_LABEL)).andComponent(alwaysRegisterLater),
				FormDialog.Row.withLabel(Messages.get(KEY_DEFAULT_SERVER_PORT)).andComponent(defaultServerPort)
		));

		setOkButtonLabel(Messages.get(KEY_OK_BUTTON));
		setCancelButtonLabel(Messages.get(KEY_CANCEL_BUTTON));
	}

	@Override
	protected  void populateForm() {
		maxRecentItems.setValue(application.getMaxRecentCount());
		viewLegalMoves.setSelected(application.isViewLegalMovesEnabled());
		viewAttacks.setSelected(application.isViewAttacksEnabled());
		viewLastMove.setSelected(application.isViewLastMoveEnabled());

		themes.setSelectedItem(application.getTheme());

		alwaysRegisterLater.setSelected(application.isEnginesAlwaysRegisterLater());
		defaultServerPort.setValue(application.getDefaultServerPort());
	}

	@Override
	protected void onValidationSuccess() {
		// directly set the new values to the application
		application.setMaxRecentCount( ((Number) maxRecentItems.getValue()).intValue() );
		application.setViewLegalMovesEnabled( viewLegalMoves.isSelected() );
		application.setViewAttacksEnabled( viewAttacks.isSelected() );
		application.setViewLastMoveEnabled( viewLastMove.isSelected() );
		application.setTheme((String) themes.getSelectedItem());
		application.setEnginesAlwaysRegisterLater(alwaysRegisterLater.isSelected());
		application.setDefaultServerPort( ((Number)defaultServerPort.getValue()).intValue() );
	}

}
