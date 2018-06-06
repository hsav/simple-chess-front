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
import com.lowbudget.chess.front.swing.components.forms.DialogHelper.EngineListModel;
import com.lowbudget.chess.model.uci.engine.UCIEngineConfig;

import java.awt.event.ActionEvent;

public class TestEngineAction extends AbstractEngineAction {

	TestEngineAction(UIApplication application, String titleKey) {
		super(application, titleKey);
	}

	@Override
	protected void doAction(ActionEvent e) {
		UCIEngineConfig engineConfig = getSelectedEngineConfig();

		application.testEngineConfig(engineConfig.getParams(), () -> {
			// the engine might have been modified (i.e. a change in author's name) so let the model know
			int index = getSelectedIndex();
			EngineListModel listModel = getListModel();
			// we know the engine exists so if any options were modified this will be an update of the engine profile
			listModel.itemUpdated(index);
		});
	}
}
