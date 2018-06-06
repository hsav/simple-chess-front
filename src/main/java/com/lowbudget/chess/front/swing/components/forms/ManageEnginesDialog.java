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
import com.lowbudget.chess.model.uci.engine.UCIEngineConfig;
import com.lowbudget.chess.front.swing.ComponentNames.DialogManageEngines;
import com.lowbudget.chess.front.swing.components.forms.DialogHelper.EngineComboBoxModel;
import com.lowbudget.chess.front.swing.components.forms.DialogHelper.EngineListCellRenderer;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS;

/**
 * <p>Displays a modal dialog with a list of all the chess engines already registered and allows for the management
 * of their respective configuration (i.e. the user can add a new engine, edit the options of an already registered
 * engine, delete it etc.</p>
 */
public class ManageEnginesDialog extends ApplicationFormDialog {

	/**
	 * Defines a factory of the available actions that we can perform with an engine profile. Each action is assigned
	 * to its corresponding button displayed in this dialog
	 */
	public interface ButtonActionFactory {
		/**
		 * Creates an action for the add button
		 * @return the add action
		 */
		Action createAddAction();

		/**
		 * Creates an action for the settings button
		 * @return the settings action
		 */
		Action createSettingsAction();

		/**
		 * Creates an action for the test button
		 * @return the test action
		 */
		Action createTestAction();

		/**
		 * Creates an action for the delete button
		 * @return the delete action
		 */
		Action createDeleteAction();
	}

	public static final String LIST_MODEL_KEY = "manage.engines.list.model.key";
	private static final String KEY_MAIN_DIALOG_TITLE = "app.action.engine.manage";
	private static final String KEY_CLOSE_BUTTON = "app.button.close";

	private final JButton addButton;
	private final JButton testButton;
	private final JButton editButton;
	private final JButton deleteButton;
	private JList<UCIEngineConfig> list;

	private final EngineComboBoxModel listModel;

	public ManageEnginesDialog(UIApplication application, ButtonActionFactory buttonActionFactory) {
		super(application, Messages.get(KEY_MAIN_DIALOG_TITLE));

		setDisplayOkButton(false);
		setCancelButtonLabel(Messages.get(KEY_CLOSE_BUTTON));

		listModel = new EngineComboBoxModel(application);

		// create the actions for the buttons
		Action addAction = buttonActionFactory.createAddAction();
		Action testAction = buttonActionFactory.createTestAction();
		Action settingsAction = buttonActionFactory.createSettingsAction();
		Action deleteAction = buttonActionFactory.createDeleteAction();

		// populate actions with the list model
		populateListModelProperty(listModel, addAction, testAction, settingsAction, deleteAction);

		addButton = new JButton(addAction);
		testButton = new JButton(testAction);
		editButton = new JButton(settingsAction);
		deleteButton = new JButton(deleteAction);

		// required for our swing tests
		addButton.setName(DialogManageEngines.ADD_BUTTON);
		editButton.setName(DialogManageEngines.EDIT_BUTTON);
		testButton.setName(DialogManageEngines.TEST_BUTTON);
		deleteButton.setName(DialogManageEngines.DELETE_BUTTON);
	}

	@Override
	protected JPanel createTopPanel() {
		JPanel topPanel = new JPanel(new BorderLayout());

		JScrollPane listScroll = new JScrollPane();

		list = new JList<>(listModel);

		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		list.setCellRenderer(new EngineListCellRenderer());
		listScroll.setViewportView(list);
		listScroll.setVerticalScrollBarPolicy(VERTICAL_SCROLLBAR_ALWAYS);

		list.addListSelectionListener(this::onSelectionChanged);
		list.setName(DialogManageEngines.ENGINES_LIST);

		topPanel.add(listScroll);

		onSelectionChanged(null);

		return topPanel;
	}

	@Override
	protected List<JButton> createExtraButtons() {
		List<JButton> result = new ArrayList<>();

		result.add(addButton);
		result.add(testButton);
		result.add(editButton);
		result.add(deleteButton);
		return result;
	}

	private void onSelectionChanged(@SuppressWarnings("unused") ListSelectionEvent event) {
		boolean hasSelection = list.getSelectedIndex() >= 0;
		editButton.setEnabled(hasSelection);
		deleteButton.setEnabled(hasSelection);
		testButton.setEnabled(hasSelection);
		listModel.setSelectedIndex( list.getSelectedIndex() );
	}

	private static void populateListModelProperty(Object propertyValue, Action... actions) {
		for (Action a : actions) {
			a.putValue(LIST_MODEL_KEY, propertyValue);
		}
	}
}
