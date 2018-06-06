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
import com.lowbudget.common.Messages;
import com.lowbudget.chess.front.swing.ComponentNames;
import com.lowbudget.chess.front.swing.common.FormDialog;
import com.lowbudget.chess.front.swing.common.JFileField;
import com.lowbudget.chess.front.swing.common.UIUtils;
import com.lowbudget.chess.model.uci.connect.Connectable;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.Arrays;
import java.util.Collections;

/**
 * <p>Displays a modal dialog that allows the user to add the configuration of a new engine.</p>
 * <p>There are two types of engines supported: local ones (the chess engine is an executable file) and remote ones
 * (the chess engine is a remote uci server)</p>
 * <p>The dialog shows a combo box that allows the user to switch between the different types of parameters that
 * need to be declared</p>
 */
public class AddEngineDialog extends ApplicationFormDialog {

	private static final String ENGINE_TYPE_LOCAL = Messages.get("app.dialog.engine.add.type.local");
	private static final String ENGINE_TYPE_REMOTE = Messages.get("app.dialog.engine.add.type.remote");

	private static final String KEY_MAIN_DIALOG_TITLE = "app.dialog.engine.add.title";
	private static final String KEY_FILE_DIALOG_TITLE = "app.dialog.engine.add.local.file.dialog.title";
	private static final String KEY_OK_BUTTON = "app.button.ok";
	private static final String KEY_CANCEL_BUTTON = "app.button.cancel";

	private static final String KEY_LOCAL_ENGINE_PANEL_TITLE = "app.dialog.engine.add.local.panel.title";
	private static final String KEY_LOCAL_PANEL_EXECUTABLE_LABEL = "app.dialog.engine.add.local.file.dialog.label";
	private static final String KEY_REMOTE_ENGINE_PANEL_TITLE = "app.dialog.engine.add.remote.panel.title";
	private static final String KEY_REMOTE_PANEL_HOST_LABEL = "app.dialog.engine.add.remote.label.host";
	private static final String KEY_REMOTE_PANEL_PORT_LABEL = "app.dialog.engine.add.remote.label.port";

	private static final String KEY_VALIDATION_ERROR_DIALOG_TITLE = "app.dialog.engine.add.validation.error.title";
	private static final String KEY_VALIDATION_ERROR_FILE = "app.dialog.engine.add.validation.error.file";
	private static final String KEY_VALIDATION_ERROR_HOST = "app.dialog.engine.add.validation.error.host";

	// selector of the type of engine the user will add
	private final JComboBox<String> engineTypeCombo;

	// executable file for local engines
	private final JFileField fileField;

	// host and port for remote engines
	private final JTextField hostField;
	private final JSpinner portField;

	// the result of the dialog after successful validation
	private Connectable.Params params;

	public AddEngineDialog(UIApplication application) {
		super(application, Messages.get(KEY_MAIN_DIALOG_TITLE));

		fileField = new JFileField();
		fileField.setFileChooserDialogTitle(Messages.get(KEY_FILE_DIALOG_TITLE));
		fileField.setFileChooserCurrentDirectory( application.getLastOpenFolderPath() );
		fileField.setSelectButtonIcon( new ImageIcon(UIUtils.getImage("open.png")));
		fileField.setClearButtonIcon( new ImageIcon(UIUtils.getImage("delete.png")));
		fileField.setColumns(30);

		fileField.setSelectButtonName(ComponentNames.DialogManageEngines.SELECT_ENGINE_FILE_BUTTON);

		String[] engineTypes = { ENGINE_TYPE_LOCAL, ENGINE_TYPE_REMOTE };
		engineTypeCombo = new JComboBox<>(engineTypes);
		engineTypeCombo.setEditable(false);

		hostField = new JTextField();
		hostField.setHorizontalAlignment(JTextField.RIGHT);
		hostField.setText("localhost");

		portField = DialogHelper.createPortSpinner(application.getDefaultServerPort());

		setOkButtonLabel(Messages.get(KEY_OK_BUTTON));
		setCancelButtonLabel(Messages.get(KEY_CANCEL_BUTTON));

		setOkButtonComponentName(ComponentNames.OK_BUTTON);
		setCancelButtonComponentName(ComponentNames.CANCEL_BUTTON);
	}

	@Override
	protected JPanel createTopPanel() {
		CardLayout cardLayout = new CardLayout();

		JPanel cards = new JPanel(cardLayout);
		cards.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

		JPanel mainPanel = new JPanel(new BorderLayout());
		mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

		JPanel comboPanel = new JPanel(); // flow layout

		// assign an item listener that swaps the current card
		engineTypeCombo.addItemListener( event -> cardLayout.show(cards, (String) event.getItem()) );
		comboPanel.add(engineTypeCombo);

		// card 1: the local engine panel contains a label and the file selection field
		JPanel card1 = new JPanel(new GridBagLayout());
		// by using both weight x and fill horizontal we center the panel vertically while it takes all the width horizontally
		GridBagConstraints gc = new GridBagConstraints();
		gc.weightx = 1.0;
		gc.fill = GridBagConstraints.HORIZONTAL;

		JPanel innerPanel = FormDialog.createFieldsPanel(Collections.singletonList(
				FormDialog.Row.withLabel(Messages.get(KEY_LOCAL_PANEL_EXECUTABLE_LABEL)).andComponent(fileField)
		));
		card1.add(innerPanel, gc);
		card1.setBorder(BorderFactory.createTitledBorder( Messages.get(KEY_LOCAL_ENGINE_PANEL_TITLE) ));

		// card 2: the remote engine panel contains two rows with labels and text fields for host and port
		JPanel card2 = FormDialog.createFieldsPanel(Arrays.asList(
				FormDialog.Row.withLabel(Messages.get(KEY_REMOTE_PANEL_HOST_LABEL)).andComponent(hostField),
				FormDialog.Row.withLabel(Messages.get(KEY_REMOTE_PANEL_PORT_LABEL)).andComponent(portField)
		));
		card2.setBorder(BorderFactory.createTitledBorder( Messages.get(KEY_REMOTE_ENGINE_PANEL_TITLE) ));

		cards.add(card1, ENGINE_TYPE_LOCAL);
		cards.add(card2, ENGINE_TYPE_REMOTE);

		mainPanel.add(comboPanel, BorderLayout.PAGE_START);
		mainPanel.add(cards, BorderLayout.CENTER);

		return mainPanel;
	}

	@Override
	protected FormDialog.ValidationResult validateForm() {
		if (ENGINE_TYPE_LOCAL.equals(engineTypeCombo.getSelectedItem())) {
			// selected file is required
			if (fileField.getFile() == null) {
				return new FormDialog.ValidationResult(false, Messages.get(KEY_VALIDATION_ERROR_FILE));
			}
		} else {
			// host is required
			if (hostField.getText().isEmpty()) {
				return new FormDialog.ValidationResult(false, Messages.get(KEY_VALIDATION_ERROR_HOST));
			}
			// spinner should handle port validation
		}
		return FormDialog.ValidationResult.VALIDATION_OK;
	}

	@Override
	protected void onValidationError(FormDialog.ValidationResult result) {
		application.showValidationFailedWarningMessage(KEY_VALIDATION_ERROR_DIALOG_TITLE, result.getError());
	}

	@Override
	protected void onValidationSuccess() {
		if (ENGINE_TYPE_LOCAL.equals(engineTypeCombo.getSelectedItem())) {
			File selectedFile = fileField.getFile();
			this.params = Connectable.Params.of(selectedFile);
			application.setLastOpenFolderPath(selectedFile.getParentFile());
		} else {
			this.params = Connectable.Params.of(hostField.getText(), ((Number) portField.getValue()).intValue());
		}
	}

	public Connectable.Params getParams() {
		return params;
	}

}
