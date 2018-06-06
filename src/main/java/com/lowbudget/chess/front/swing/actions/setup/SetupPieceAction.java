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

package com.lowbudget.chess.front.swing.actions.setup;

import com.lowbudget.chess.front.app.model.BoardModel;
import com.lowbudget.chess.front.app.UIApplication;
import com.lowbudget.chess.front.swing.SwingUIApplication;
import com.lowbudget.chess.front.swing.common.UIUtils;
import com.lowbudget.chess.front.swing.actions.ApplicationAction;
import com.lowbudget.chess.model.Piece;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

/**
 * <p>Changes the current "selected piece" while in the setup position mode.</p>
 * <p>The current selected piece is selected by the user and denotes the piece that the user wishes to be set to any
 * squares clicked from now and on. For example if the user has selected the black knight from the toolbar any click
 * at a square would set a black knight to that square.</p>
 */
public class SetupPieceAction extends ApplicationAction {

	private static final Logger log = LoggerFactory.getLogger(SetupPieceAction.class);

	/**
	 * <p>Stores the current selected piece.</p>
	 * <p>A {@code null} value indicates that the user has selected the "move" cursor i.e. does not wish to set a
	 * piece at a square but rather move existing pieces around</p>
	 */
	private final Piece piece;

	private final BoardModel boardModel;

	public SetupPieceAction(UIApplication application, Piece piece, BoardModel boardModel) {
		super(application, null, loadImage(application, piece));
		this.piece = piece;
		this.boardModel = boardModel;

		application.addModelListener(new UIApplication.ModelAdapter() {
			@Override
			public void onThemeChanged(String newTheme) {
				putValue(Action.SMALL_ICON, new ImageIcon(loadImage(application, piece)));
			}
		});
	}

	public SetupPieceAction(UIApplication application, Image icon, BoardModel boardModel) {
		super(application, null, icon);
		this.piece = null;
		this.boardModel = boardModel;
	}

	private static Image loadImage(UIApplication application, Piece piece) {
		String imageName = SwingUIApplication.getPieceImageName(application.getTheme(), piece);
		return UIUtils.getImage(imageName, 32);
	}

	@Override
	protected void doAction(ActionEvent e) {
		log.debug("Setup action for piece {}", this.piece);
		boardModel.setSelectedPiece(this.piece);
	}
}
