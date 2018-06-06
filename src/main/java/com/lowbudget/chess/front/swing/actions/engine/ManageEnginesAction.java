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
import com.lowbudget.common.Messages;
import com.lowbudget.chess.front.swing.actions.ApplicationAction;
import com.lowbudget.chess.front.swing.components.forms.ManageEnginesDialog;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class ManageEnginesAction extends ApplicationAction implements UIApplication.StateListener {
	public ManageEnginesAction(UIApplication application, String titleKey) {
		super(application, titleKey);
		application.addStateListener(this);
	}

	@Override
	protected void doAction(ActionEvent e) {
		// create a new factory that will in turn create the actions needed by the dialog to be assigned to its buttons
		ManageEnginesDialog.ButtonActionFactory actionFactory = new ManageEnginesDialogButtonActionFactory(application);

		ManageEnginesDialog dialog = new ManageEnginesDialog(application, actionFactory);
		dialog.setVisible(true);
	}

	@Override
	public void onStateChanged() {
		setEnabled( application.isManageEnginesActionAllowed() );
	}

	private static class ManageEnginesDialogButtonActionFactory implements ManageEnginesDialog.ButtonActionFactory {

		private final UIApplication application;

		private ManageEnginesDialogButtonActionFactory(UIApplication application) {
			this.application = application;
		}

		@Override
		public Action createAddAction() {
			return new AddEngineAction(application, Messages.get("app.action.engine.add"));
		}

		@Override
		public Action createSettingsAction() {
			return new EngineSettingsAction(application, Messages.get("app.action.engine.settings"));
		}

		@Override
		public Action createTestAction() {
			return new TestEngineAction(application, "app.action.engine.test");
		}

		@Override
		public Action createDeleteAction() {
			return new DeleteEngineAction(application, Messages.get("app.action.engine.delete"));
		}
	}
}
