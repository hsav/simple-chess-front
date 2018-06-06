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

import com.lowbudget.chess.front.app.Player;
import com.lowbudget.chess.front.app.UIApplication;
import com.lowbudget.common.Messages;
import com.lowbudget.chess.front.swing.ComponentNames;
import com.lowbudget.chess.front.swing.ComponentNames.DialogRegistration;
import com.lowbudget.chess.front.swing.common.FormDialog;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>Displays a registration dialog.</p>
 * <p>This dialog is displayed if the engine states that it needs registration in which case we ask the user for a
 * name and a registration code to send them back to the engine for verification.</p>
 * <p>However registration can be postponed so the cancel button on this form is labelled as "Later".</p>
 */
public class RegistrationDialog extends ApplicationFormDialog {

	/* Message keys used by this class */
	private static final String KEY_DIALOG_TITLE = "app.dialog.registration.title";
	private static final String KEY_LABEL_PLAYER = "app.dialog.registration.top.row.label.player";
	private static final String KEY_LABEL_PLAYER_NAME = "app.dialog.registration.top.row.label.player.name";
	private static final String KEY_LABEL_NAME = "app.dialog.registration.label.name";
	private static final String KEY_LABEL_CODE = "app.dialog.registration.label.code";
	private static final String KEY_VALIDATION_ERROR_TITLE = "app.dialog.registration.validation.error";
	private static final String KEY_VALIDATION_ERROR_MESSAGE = "app.dialog.registration.validation.message";
	private static final String KEY_BUTTON_OK = "app.button.ok";
	private static final String KEY_BUTTON_LATER = "app.dialog.registration.button.later";

	/**
	 * List with {@link FormDialog.Row}s that contain labels and components for the two-column layout
	 */
	private final List<FormDialog.Row> rows = new ArrayList<>();

	/** Index in the {@link #rows} list of the name field **/
	private static final int NAME_FIELD_INDEX = 1;

	/** Index in the {@link #rows} list of the code field **/
	private static final int CODE_FIELD_INDEX = 2;

	// the results of this dialog after successful validation
	private String registrationName;
	private String registrationCode;

	/**
	 * Creates a new registration dialog with the specified title and player's name and color
	 * @param application the current swing application
	 * @param enginePlayer the {@link Player} that opens this dialog
	 */
	public RegistrationDialog(UIApplication application, Player enginePlayer) {
		super(application, Messages.get(KEY_DIALOG_TITLE, enginePlayer.getColor()));

		// information row with the player's name and color (this is useful to distinguish the dialogs between
		// two players with the same engine during our tests)
		rows.add( FormDialog.Row
				.withLabel(Messages.get(KEY_LABEL_PLAYER))
				.andLabel(Messages.get(KEY_LABEL_PLAYER_NAME, enginePlayer.getName(), enginePlayer.getColor()))
		);

		// row with a name label and text field
		rows.add( FormDialog.Row.withLabel(Messages.get(KEY_LABEL_NAME)).andNamedTextField(DialogRegistration.NAME) );

		// row with a code label and text field
		rows.add( FormDialog.Row.withLabel(Messages.get(KEY_LABEL_CODE)).andNamedTextField(DialogRegistration.CODE) );

		// set the rows factory
		setRowsFactory( () -> rows);

		// set the button labels, note that the cancel button is labeled "Later"
		setOkButtonLabel(Messages.get(KEY_BUTTON_OK));
		setCancelButtonLabel(Messages.get(KEY_BUTTON_LATER));

		// component names are required by our swing tests
		setOkButtonComponentName(ComponentNames.OK_BUTTON);
		setCancelButtonComponentName(ComponentNames.CANCEL_BUTTON);
	}

	public String getRegistrationName() {
		return registrationName;
	}

	public String getRegistrationCode() {
		return registrationCode;
	}

	/**
	 * Validates the form. We require both fields not to be empty
	 * @return the appropriate {@link FormDialog.ValidationResult}. Note that the
	 * {@link FormDialog.ValidationResult#error} field contains the (translated) name of the field that
	 * failed validation.
	 */
	@Override
	protected FormDialog.ValidationResult validateForm() {
		JTextField nameField = (JTextField) rows.get(NAME_FIELD_INDEX).getComponent();
		JTextField codeField = (JTextField) rows.get(CODE_FIELD_INDEX).getComponent();

		if (nameField.getText().isEmpty()) {
			return new FormDialog.ValidationResult(false, Messages.get(KEY_LABEL_NAME));
		}
		if (codeField.getText().isEmpty()) {
			return new FormDialog.ValidationResult(false, Messages.get(KEY_LABEL_CODE));
		}
		return FormDialog.ValidationResult.VALIDATION_OK;
	}

	/**
	 * Displays a warning message to the user that the field is invalid.
	 * @param validationResult the {@link FormDialog.ValidationResult} returned from {@link #validateForm()}.
	 *                         The {@link FormDialog.ValidationResult#error} field contains the translated name of the field
	 *                         that failed validation (that would be either the name or the code field in our case).
	 */
	@Override
	protected void onValidationError(FormDialog.ValidationResult validationResult) {
		application.showValidationFailedWarningMessage(KEY_VALIDATION_ERROR_TITLE,
				Messages.get(KEY_VALIDATION_ERROR_MESSAGE, validationResult.getError()));
	}

	/**
	 * <p>Copies the values entered by the user to {@link #registrationName} and {@link #registrationCode} fields.</p>
	 */
	@Override
	protected void onValidationSuccess() {
		JTextField nameField = (JTextField) rows.get(NAME_FIELD_INDEX).getComponent();
		JTextField codeField = (JTextField) rows.get(CODE_FIELD_INDEX).getComponent();

		registrationName = nameField.getText();
		registrationCode = codeField.getText();
	}
}
