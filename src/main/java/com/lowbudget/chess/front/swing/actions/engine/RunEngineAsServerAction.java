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
import com.lowbudget.chess.front.app.UIApplication.StateListener;
import com.lowbudget.chess.model.uci.engine.UCIEngineConfig;
import com.lowbudget.chess.front.swing.actions.ApplicationAction;
import com.lowbudget.chess.front.swing.common.UIUtils;
import com.lowbudget.chess.front.swing.components.forms.RunAsServerDialog;

import java.awt.event.ActionEvent;

public class RunEngineAsServerAction extends ApplicationAction implements StateListener {

	public RunEngineAsServerAction(UIApplication application, String titleKey) {
		super(application, titleKey, UIUtils.getImage("play.png"));
		application.addStateListener(this);
		setUseWaitingCursor(true);
	}

	@Override
	protected void doAction(ActionEvent e) {
		RunAsServerDialog dialog = new RunAsServerDialog(application);
		dialog.setVisible(true);
		if (!dialog.isCancelled()) {
			UCIEngineConfig engineConfig = dialog.getEngine();
			int serverPort = dialog.getServerPort();
			application.startServer(engineConfig, serverPort);
		}
	}

	@Override
	public void onStateChanged() {
		setEnabled( application.isRunServerActionAllowed() );
	}
}
