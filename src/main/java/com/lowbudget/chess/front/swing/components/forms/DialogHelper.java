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
import com.lowbudget.chess.front.app.config.ReadOnlyEnginesConfig;
import com.lowbudget.chess.model.uci.engine.UCIEngineConfig;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

/**
 * Utility class with helper classes and methods for the various application's dialogs.
 */
public final class DialogHelper {

	// minimum and maximum allowed ports
	private static final int MINIMUM_PORT = 0;
	private static final int MAXIMUM_PORT = 0xFFFF; // 65535

	private DialogHelper() {}

	/**
	 * Creates and configures a {@link JRadioButton} in a single call
	 * @param name the component's internal name - required for our swing tests
	 * @param title the title of the radio
	 * @param selected the selected status that the radio should be initialized with
	 * @param actionListener the action listener to assign to the radio
	 * @return the configured {@link JRadioButton}. Note that the returned radio has its border set to an empty
	 * border for alignment purposes
	 */
	static JRadioButton createBorderlessRadio(String name, String title, boolean selected, ActionListener actionListener) {
		JRadioButton radio = new JRadioButton(title);
		radio.setSelected(selected);
		radio.setBorder(BorderFactory.createEmptyBorder());
		radio.addActionListener(actionListener);
		radio.setName(name);
		return radio;
	}

	/**
	 * Creates a new check box without border
	 * Setting an empty border has the effect that the checkbox aligns with the rest of the components in the two
	 * column layout we usually use for our dialogs (otherwise it leaves a small gap on the left)
	 * @return the newly created {@link JCheckBox} component
	 */
	static JCheckBox createBorderlessCheckBox() {
		JCheckBox checkBox = new JCheckBox();
		checkBox.setBorder(BorderFactory.createEmptyBorder());
		return checkBox;
	}

	/**
	 * <p>Creates a spinner appropriate for editing a value that represents a port.</p>
	 * <p>The spinner uses a {@link SpinnerNumberModel} and and editor format that does not display grouping
	 * separators (i.e. to avoid displaying {@code "5000"} as {@code "5.000"}) </p>
	 * @param value the initial value of the spinner's model
	 * @return the newly created spinner
	 */
	static JSpinner createPortSpinner(int value) {
		SpinnerModel model = new SpinnerNumberModel(value, MINIMUM_PORT, MAXIMUM_PORT, 1);
		JSpinner spinner = new JSpinner(model);
		spinner.setEditor(new JSpinner.NumberEditor(spinner,"#")); // do not display grouping separators
		return spinner;
	}

	/**
	 * A custom {@link ListCellRenderer} renderer that displays a list of {@link UCIEngineConfig} objects.
	 * This renderer just displays the engine's name and can be used with a {@link JList} and a {@link JComboBox}
	 */
	public static class EngineListCellRenderer extends DefaultListCellRenderer {
		@Override
		public Component getListCellRendererComponent(
				JList<?> list,
				Object value,
				int index,
				boolean isSelected,
				boolean cellHasFocus)
		{
			super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
			if (value instanceof UCIEngineConfig) {
				UCIEngineConfig engine = (UCIEngineConfig) value;
				setText(engine.getName() + " (" + engine.getAuthor() + ") - " + engine.getParams().toString() );
			}
			return this;
		}
	}

	/**
	 * A custom {@link ListModel} appropriate to be used with a {@link JList} that displays a list of
	 * {@link UCIEngineConfig} objects.
	 */
	public static class EngineListModel extends AbstractListModel<UCIEngineConfig> {

		/**
		 * Read-only version of the application's engines configuration
		 */
		private final ReadOnlyEnginesConfig enginesConfig;

		EngineListModel(UIApplication model) {
			this.enginesConfig = model.getEnginesConfig();
		}

		@Override
		public int getSize() {
			return enginesConfig.size();
		}

		@Override
		public UCIEngineConfig getElementAt(int index) {
			return enginesConfig.get(index);
		}

		public int indexOf(Object item) {
			int size = getSize();
			for (int i = 0; i < size; i++) {
				UCIEngineConfig engine = getElementAt(i);
				if (engine.equals(item)) {
					return i;
				}
			}
			return -1;
		}

		/**
		 * Should be called when a new item was added to the underlying {@link #enginesConfig} so this list model can
		 * notify all of its listeners
		 * @param index the index of the new item added
		 */
		public void itemAdded(int index) {
			super.fireIntervalAdded(this, index, index);
		}

		/**
		 * Should be called when an item was removed from the underlying {@link #enginesConfig} so this list model can
		 * notify all of its listeners
		 * @param index the index of the item that was removed
		 */
		public void itemRemoved(int index) {
			super.fireIntervalRemoved(this, index, index);
		}

		/**
		 * Should be called when an item of the the underlying {@link #enginesConfig} is updated, so this list model can
		 * notify all of its listeners
		 * @param index the index of the updated item
		 */
		public void itemUpdated(int index) {
			super.fireContentsChanged(this, index, index);
		}
	}

	/**
	 * <p>A custom {@link ComboBoxModel} appropriate to be used with a {@link JComboBox} that displays a list
	 * of {@link UCIEngineConfig} objects.</p>
	 * <p>This is a simple extension of the {@link EngineListModel} that can additionally remember a singe
	 * selection.</p>
	 */
	public static class EngineComboBoxModel extends EngineListModel implements ComboBoxModel<UCIEngineConfig> {

		private int selectedIndex = -1;

		EngineComboBoxModel(UIApplication model) {
			super(model);
			if (getSize() > 0) {
				// automatically select the first item in the list
				selectedIndex = 0;
			}
		}

		public int getSelectedIndex() {
			return selectedIndex;
		}

		void setSelectedIndex(int selectedIndex) {
			this.selectedIndex = selectedIndex;
		}

		@Override
		public void setSelectedItem(Object anItem) {
			selectedIndex = indexOf(anItem);
		}

		@Override
		public Object getSelectedItem() {
			if (selectedIndex < 0) {
				return null;
			}
			return getElementAt(selectedIndex);
		}

		@Override
		public void itemAdded(int index) {
			super.itemAdded(index);
			// if there were no items before the addition, this would be the first entry, so select it automatically
			if (selectedIndex < 0 && getSize() > 0) {
				selectedIndex = 0;
			}
		}

		@Override
		public void itemRemoved(int index) {
			super.itemRemoved(index);
			// if the removed item is before the selected one, the selected item moved one position up
			// if the removed item was the selected item select the previous one or none if the item was the first
			// one in the list
			if (index <= selectedIndex) {
				selectedIndex--;
			}
		}
	}
}
