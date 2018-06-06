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

import com.lowbudget.chess.front.app.UIApplication;
import com.lowbudget.chess.model.uci.engine.UCIEngineConfig;
import com.lowbudget.chess.front.swing.ComponentNames;
import com.lowbudget.common.Messages;
import com.lowbudget.chess.front.swing.common.FormDialog;
import com.lowbudget.chess.front.swing.components.forms.DialogHelper.EngineComboBoxModel;
import com.lowbudget.chess.front.swing.components.forms.DialogHelper.EngineListCellRenderer;

import javax.swing.*;
import java.util.Arrays;
import java.util.Objects;

/**
 * Displays a dialog for the user to select the engine that should be exposed as a server and the port the server
 * should listen to.
 */
public class RunAsServerDialog extends ApplicationFormDialog {

	/** Message keys used by this class */
	private static final String KEY_MAIN_DIALOG_TITLE = "app.dialog.engine.run.server.title";
	private static final String KEY_OK_BUTTON = "app.button.ok";
	private static final String KEY_CANCEL_BUTTON = "app.button.cancel";
	private static final String KEY_ENGINE_LABEL = "app.dialog.engine.run.server.label.engine";
	private static final String KEY_SERVER_PORT_LABEL = "app.dialog.engine.run.server.label.port";
	private static final String KEY_VALIDATION_ERROR_DIALOG_TITLE = "app.dialog.engine.run.server.validation.error.title";
	private static final String KEY_VALIDATION_ERROR_ENGINE_REQUIRED = "app.dialog.engine.run.server.validation.error.engine";

	private final JComboBox<UCIEngineConfig> engineConfigsCombo;
	private final JSpinner portField;

	public RunAsServerDialog(UIApplication application) {
		super(application, Messages.get(KEY_MAIN_DIALOG_TITLE));

		engineConfigsCombo = new JComboBox<>(new EngineComboBoxModel(application));
		engineConfigsCombo.setEditable(false);
		engineConfigsCombo.setRenderer( new EngineListCellRenderer() );

		portField = DialogHelper.createPortSpinner(application.getDefaultServerPort());

		setRowsFactory( () -> Arrays.asList(
				FormDialog.Row.withLabel(Messages.get(KEY_ENGINE_LABEL)).andComponent(engineConfigsCombo),
				FormDialog.Row.withLabel(Messages.get(KEY_SERVER_PORT_LABEL)).andComponent(portField)
		));

		setOkButtonLabel(Messages.get(KEY_OK_BUTTON));
		setCancelButtonLabel(Messages.get(KEY_CANCEL_BUTTON));

		// component names are required by our swing tests
		setOkButtonComponentName(ComponentNames.OK_BUTTON);
		setCancelButtonComponentName(ComponentNames.CANCEL_BUTTON);
	}

	@Override
	protected FormDialog.ValidationResult validateForm() {
		UCIEngineConfig engineConfig = (UCIEngineConfig) engineConfigsCombo.getSelectedItem();
		if (engineConfig == null) {
			return new FormDialog.ValidationResult(false, Messages.get(KEY_VALIDATION_ERROR_ENGINE_REQUIRED));
		}
		return FormDialog.ValidationResult.VALIDATION_OK;
	}

	@Override
	protected void onValidationError(FormDialog.ValidationResult result) {
		application.showValidationFailedWarningMessage(KEY_VALIDATION_ERROR_DIALOG_TITLE, result.getError());
	}

	public UCIEngineConfig getEngine() {
		UCIEngineConfig engineConfig = (UCIEngineConfig) engineConfigsCombo.getSelectedItem();
		Objects.requireNonNull(engineConfig); // sanity check
		return engineConfig;
	}

	public int getServerPort() {
		return ((Number) portField.getValue()).intValue();
	}
}
