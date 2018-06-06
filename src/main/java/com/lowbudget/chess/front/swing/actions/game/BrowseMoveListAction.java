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

package com.lowbudget.chess.front.swing.actions.game;

import com.lowbudget.chess.front.app.UIApplication;
import com.lowbudget.chess.front.app.UIApplication.StateListener;
import com.lowbudget.chess.front.swing.common.UIUtils;
import com.lowbudget.chess.front.swing.actions.ApplicationAction;
import com.lowbudget.chess.model.board.MoveList;

import java.awt.event.ActionEvent;

public class BrowseMoveListAction extends ApplicationAction implements StateListener {

	private final MoveList.BrowseType browseType;

	public BrowseMoveListAction(UIApplication application, String iconName, String tooltipKey, MoveList.BrowseType browseType) {
		super(application, null, UIUtils.getImage(iconName), tooltipKey);
		this.browseType = browseType;
		application.addStateListener(this);
	}

	@Override
	protected void doAction(ActionEvent e) {
		application.browseMoveList(browseType);
	}

	@Override
	public void onStateChanged() {
		setEnabled( application.isBrowseMoveListActionAllowed() );
	}

}
