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

import com.lowbudget.chess.front.swing.common.FormDialog;
import com.lowbudget.common.Messages;
import com.lowbudget.chess.front.swing.common.UIAction;
import com.lowbudget.chess.model.uci.engine.*;
import com.lowbudget.chess.model.uci.engine.options.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

import static com.lowbudget.chess.front.swing.components.forms.DialogHelper.createBorderlessCheckBox;

/**
 * Displays a settings dialog for the engine's available options.
 */
public class EngineSettingsDialog extends FormDialog {

	/* Message keys used by this class */
	private static final String KEY_DIALOG_TITLE = "app.dialog.chess.engine.settings";
	private static final String KEY_ENGINE_NAME = "app.dialog.chess.engine.settings.name";
	private static final String KEY_ENGINE_AUTHOR = "app.dialog.chess.engine.settings.author";
	private static final String KEY_BUTTON_RESET = "app.dialog.chess.engine.settings.action.reset";
	private static final String KEY_BUTTON_OK = "app.button.ok";
	private static final String KEY_BUTTON_CANCEL = "app.button.cancel";

	@FunctionalInterface
	public interface ButtonActionFactory {
		Action createAction(ButtonOption option);
	}

	private final List<Row> rows = new ArrayList<>();

	private final List<UCIOption> options;

	public EngineSettingsDialog(UCIEngineConfig engineConfig, ButtonActionFactory buttonActionFactory) {
		super(Messages.get(KEY_DIALOG_TITLE));

		// create a copy of the options so the user can modify them without persisting the changes to the engine
		this.options = engineConfig.getOptions().stream()
				.map(UCIOption::copy)
				.collect(Collectors.toList());

		// create a list with rows containing the components that will be used by the form's first and second columns.
		// Beside the options, we add two extra rows (i.e. a pair of labels) for the engine's name and author

		// name row
		rows.add( Row.withLabel(Messages.get(KEY_ENGINE_NAME)).andLabel(engineConfig.getName()) );

		// author row
		rows.add( Row.withLabel(Messages.get(KEY_ENGINE_AUTHOR)).andLabel(engineConfig.getAuthor()) );

		// use an option component factory that knows how to create the appropriate component for each option
		OptionComponentFactory factory = new OptionComponentFactory(buttonActionFactory);

		// add one row for each option with a label and its corresponding component
		for (UCIOption option : options) {
			rows.add(Row.withLabel(option.getName()).andComponent(factory.createComponent(option)));
		}

		// set the rows factory
		setRowsFactory( () -> rows);

		// set the labels of the buttons
		setOkButtonLabel(Messages.get(KEY_BUTTON_OK));
		setCancelButtonLabel(Messages.get(KEY_BUTTON_CANCEL));
	}

	/**
	 * Creates an extra reset button in addition to the default ok and cancel buttons.
	 * @return an array with the extra reset button.
	 */
	@Override
	protected List<JButton> createExtraButtons() {
		// The extra reset button, resets all fields to the default values of their corresponding options (wherever
		// applicable, e.g. a button option has neither a value, nor a default value).
		List<JButton> result = new ArrayList<>();
		result.add( new JButton(
				new UIAction(KEY_BUTTON_RESET) {
					@Override
					protected void doAction(ActionEvent e) {
						// reset all options to their default values
						options.forEach(UCIOption::reset);

						// synchronize all components to the values of their corresponding options
						synchronizeComponentsWithOptions();
					}
				})
		);
		return result;
	}

	@Override
	protected void onValidationSuccess() {
		// set all the options' values to the values of their corresponding components
		synchronizeOptionsWithComponents();
	}

	public List<UCIOption> getOptions() {
		return this.options;
	}

	/**
	 * Synchronizes all components to the values of their corresponding options
	 */
	private void synchronizeComponentsWithOptions() {
		// iterate all the options and set the components from the options' values
		iterateOptions(new OptionToComponentSetter());
	}

	/**
	 * Synchronizes all options to the values of their corresponding components
	 */
	private void synchronizeOptionsWithComponents() {
		// iterate all the options and set their values from the components
		iterateOptions(new ComponentToOptionSetter());
	}

	private void iterateOptions(ValueSetter valueSetter) {
		// iterate all the components (skip the first two, since those are the labels for the engine's name and author)
		int size = options.size();
		for (int i = 0; i < size; i++) {
			Component component = rows.get(i + 2).getComponent();
			UCIOption option = options.get(i);
			// set the value (either option to component or component to option depending on the setter)
			valueSetter.set(option, component);
		}
	}

	/**
	 * Visitor that knows how to create an appropriate {@link JComponent} for each {@link UCIOption} sub-type
	 * e.g. a {@link JButton} for an option of type {@link UCIOption.Type#BUTTON},
	 * a {@link JTextField} for an option of type {@link UCIOption.Type#STRING} etc.
	 */
	private static class OptionComponentFactory implements OptionVisitor {

		private final ButtonActionFactory buttonActionFactory;

		/**
		 * This member is used only temporarily during the method call of {@link #createComponent(UCIOption)}
		 */
		private Component optionComponent;

		OptionComponentFactory(ButtonActionFactory buttonActionFactory) {
			this.buttonActionFactory = buttonActionFactory;
		}

		Component createComponent(UCIOption option) {
			this.optionComponent = null;
			option.accept(this);

			// note: we utilize the component name to be able to match each component with its corresponding option
			// when synchronizing the values in synchronizeComponentsWithOptions()
			optionComponent.setName(option.getName());
			return this.optionComponent;
		}

		@Override
		public void visit(ButtonOption option) {
			// assign an action to the button which, when clicked, will send a message to the engine with the option's name.
			// The engine interprets such button options as commands that modify its state i.e. a button option could
			// be a "Clear Hash" command.
			optionComponent = new JButton( buttonActionFactory.createAction(option) );
		}

		@Override
		public void visit(StringOption option) {
			optionComponent = new JTextField(option.getValue());
		}

		@Override
		public void visit(CheckOption option) {
			JCheckBox checkBox = createBorderlessCheckBox();
			checkBox.setSelected(option.getValue());
			optionComponent = checkBox;
		}

		@Override
		public void visit(SpinOption option) {
			SpinnerModel model = new SpinnerNumberModel(option.getValue(), option.getMin(), option.getMax(), 1);
			optionComponent = new JSpinner(model);
		}

		@Override
		public void visit(ComboOption option) {
			JComboBox<String> comboBox = new JComboBox<>(option.getAvailableValues().toArray(new String[0]));
			comboBox.setSelectedItem(option.getValue());
			optionComponent = comboBox;
		}
	}

	/**
	 * <p>Base class for a Visitor that knows how to set values between a {@link UCIOption} and its
	 * corresponding component (i.e. either set the component's value from the option or the option's value from the
	 * component.</p>
	 */
	private static abstract class ValueSetter implements OptionVisitor {
		/**
		 * This member is used only temporarily during the method call of {@link #set(UCIOption, Component)}
		 */
		Component component;

		/**
		 * Exchanges values between the option and the component
		 * @param option the option to use
		 * @param component the component to use
		 */
		void set(UCIOption option, Component component) {
			this.component = component;
			option.accept(this);
		}

		@Override
		public void visit(ButtonOption option) {
			// button options (and their components) do not have values so nothing to do
		}

		@Override
		public void visit(StringOption option) {
			JTextField textField = (JTextField) component;
			set(option, textField);
		}

		@Override
		public void visit(CheckOption option) {
			JCheckBox checkBox = (JCheckBox) component;
			set(option, checkBox);
		}

		@Override
		public void visit(SpinOption option) {
			JSpinner spinner = (JSpinner) component;
			set(option, spinner);
		}

		@Override
		public void visit(ComboOption option) {
			JComboBox<?> comboBox = (JComboBox<?>) component;
			set(option, comboBox);
		}

		abstract void set(StringOption option, JTextField textField);
		abstract void set(CheckOption option, JCheckBox checkBox);
		abstract void set(SpinOption option, JSpinner spinner);
		abstract void set(ComboOption option, JComboBox<?> comboBox);
	}

	/**
	 * <p>Gets the value of each option and uses it to set its corresponding component's value.</p>
	 */
	private static class OptionToComponentSetter extends ValueSetter {
		@Override
		void set(StringOption option, JTextField textField) {
			textField.setText(option.getValue());
		}

		@Override
		void set(CheckOption option, JCheckBox checkBox) {
			checkBox.setSelected(option.getValue());
		}

		@Override
		void set(SpinOption option, JSpinner spinner) {
			spinner.getModel().setValue(option.getValue());
		}

		@Override
		void set(ComboOption option, JComboBox<?> comboBox) {
			comboBox.setSelectedItem(option.getValue());
		}
	}

	/**
	 * <p>Sets the value of each option from the value of its corresponding component.</p>
	 */
	private static class ComponentToOptionSetter extends ValueSetter {
		@Override
		void set(StringOption option, JTextField textField) {
			option.setValue(textField.getText());
		}

		@Override
		void set(CheckOption option, JCheckBox checkBox) {
			option.setValue(checkBox.isSelected());
		}

		@Override
		void set(SpinOption option, JSpinner spinner) {
			SpinnerNumberModel model = (SpinnerNumberModel) spinner.getModel();
			option.setValue(model.getNumber().intValue());
		}

		@Override
		void set(ComboOption option, JComboBox<?> comboBox) {
			option.setValue(String.valueOf(comboBox.getSelectedItem()));
		}
	}
}
