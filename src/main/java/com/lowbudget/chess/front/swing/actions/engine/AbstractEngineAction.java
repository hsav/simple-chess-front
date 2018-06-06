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
import com.lowbudget.chess.model.uci.engine.UCIEngineConfig;
import com.lowbudget.chess.front.swing.actions.ApplicationAction;
import com.lowbudget.chess.front.swing.components.forms.DialogHelper.EngineComboBoxModel;
import com.lowbudget.chess.front.swing.components.forms.ManageEnginesDialog;

/**
 * <p>Base class that should be extended by all the actions used in the manage engines dialog (add, test, delete etc)</p>
 * <p>This class provides common functionality used by those actions (mainly interacting with the existing
 * {@link EngineComboBoxModel} that is available while the manage dialog remains open).</p>
 */
abstract class AbstractEngineAction extends ApplicationAction {

	AbstractEngineAction(UIApplication application, String titleKey) {
		super(application, titleKey);

	}

	UCIEngineConfig getSelectedEngineConfig() {
		EngineComboBoxModel listModel = getListModel();
		UCIEngineConfig engineConfig = (UCIEngineConfig) listModel.getSelectedItem();
		if (engineConfig == null) {
			throw new IllegalStateException("No engine config is selected");
		}
		return engineConfig;
	}

	int getSelectedIndex() {
		EngineComboBoxModel listModel = getListModel();
		int index = listModel.getSelectedIndex();
		if (index < 0) {
			throw new IllegalStateException("No selected index specified: " + index);
		}
		return index;
	}

	EngineComboBoxModel getListModel() {
		EngineComboBoxModel listModel = (EngineComboBoxModel) getValue(ManageEnginesDialog.LIST_MODEL_KEY);
		if (listModel == null) {
			throw new IllegalStateException("No list model found. Cannot resolve selected engine item");
		}
		return listModel;
	}

}
