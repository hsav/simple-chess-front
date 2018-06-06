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

import com.lowbudget.chess.front.swing.SwingUIApplication;
import com.lowbudget.chess.front.swing.common.UIAction;
import com.lowbudget.chess.front.swing.common.UIUtils;
import com.lowbudget.chess.model.PieceType;

import java.awt.event.ActionEvent;
import java.util.function.Consumer;

/**
 * <p>This action is a special action that is not used from the application's menu. Instead it is meant to be used
 * from a popup menu where the user selects the type of the promotion piece, a pawn should promote to, when it
 * reaches the 8th rank.</p>
 * <p>Each item in the popup menu is assigned an instance of this action for each different piece type from the
 * ones available for promotion (e.g. an instance for knight promotion, another instance for rook promotion etc)</p>
 */
public class PromotePawnAction extends UIAction {

	/**
	 * The promotion type the pawn will promote to. Evey menu item should use a different instance of this class, one
	 * for each available promotion type.
	 */
	private final PieceType promotionType;

	/**
	 * A listener to be notified upon selection about the selected promotion type
	 */
	private final Consumer<PieceType> selectionListener;

	/**
	 * Create a new instance of this class
	 * @param promotionType the promotion type the pawn should promote to in case the user clicks the assigned menu item
	 * @param image the image of the promotion piece
	 * @param selectionListener a {@link Consumer} to be notified if the {@link PieceType} represented by this action
	 *                          is selected
	 */
	public PromotePawnAction(PieceType promotionType, String image, Consumer<PieceType> selectionListener) {
		// find the key corresponding to the piece name and
		// get the image corresponding to the piece using a scaled version (48px seems ok)
		super(SwingUIApplication.getPieceTypeNameKey(promotionType), UIUtils.getImage(image, 48));
		this.selectionListener = selectionListener;
		this.promotionType = promotionType;
	}

	@Override
	protected void doAction(ActionEvent e) {
		selectionListener.accept(promotionType);
	}

}
