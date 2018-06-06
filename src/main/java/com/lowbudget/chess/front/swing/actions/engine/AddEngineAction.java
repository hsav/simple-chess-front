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
import com.lowbudget.chess.front.swing.components.forms.AddEngineDialog;
import com.lowbudget.chess.front.swing.components.forms.DialogHelper.EngineListModel;

import java.awt.event.ActionEvent;

public class AddEngineAction extends AbstractEngineAction {

	AddEngineAction(UIApplication application, String titleKey) {
		super(application, titleKey);
	}

	@Override
	protected void doAction(ActionEvent e) {

		AddEngineDialog dialog = new AddEngineDialog(application);
		dialog.setVisible(true);

		if (dialog.isCancelled()) {
			return;
		}

		application.addEngineConfig(dialog.getParams(), newEngineConfig -> {
			EngineListModel listModel = getListModel();

			// if the user adds an engine that already exists, this will result in a merge
			int index = listModel.indexOf(newEngineConfig);
			boolean alreadyExists = index > -1;
			if (alreadyExists) {
				// update
				listModel.itemUpdated(index);
			} else {
				// addition
				index = listModel.getSize();
				listModel.itemAdded(index);
			}
		});
	}
}
