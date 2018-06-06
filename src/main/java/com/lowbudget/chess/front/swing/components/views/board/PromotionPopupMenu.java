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

package com.lowbudget.chess.front.swing.components.views.board;

import com.lowbudget.chess.front.swing.ComponentNames;
import com.lowbudget.chess.front.swing.SwingUIApplication;
import com.lowbudget.chess.front.swing.actions.game.PromotePawnAction;
import com.lowbudget.chess.model.Piece;
import com.lowbudget.chess.model.PieceType;
import com.lowbudget.chess.model.PlayerColor;

import javax.swing.*;
import java.util.function.Consumer;

import static com.lowbudget.chess.front.swing.ComponentNames.promotionPopupMenuItemName;

/**
 * A {@link JPopupMenu} that allows the user to select the {@link PieceType} a pawn should be promoted to, when it
 * reaches the 8th rank.
 */
class PromotionPopupMenu  extends JPopupMenu {

	/**
	 * <p>Creates a popup menu that has as many menu items as the available {@link PieceType}s a pawn can promote to.</p>
	 * <p>This menu can open as a result of a user's drag operation when she moves a pawn on the 8-th rank.</p>
	 * @param color the color of the promotion pieces that should be displayed in the menu
	 * @param theme the current theme of the application
	 * @param popupSelectionListener a listener to be notified about the piece type selected
	 */
	PromotionPopupMenu(PlayerColor color, String theme, Consumer<PieceType> popupSelectionListener) {
		Piece pawn = Piece.of(color, PieceType.PAWN);

		setName(ComponentNames.PROMOTION_POPUP);

		for (PieceType pieceType : PieceType.values()) {
			if (pieceType.isPromotable()) {
				Piece piece = pawn.asType(pieceType);
				String pieceImage = SwingUIApplication.getPieceImageName(theme, piece);
				JMenuItem item = new JMenuItem(new PromotePawnAction(pieceType, pieceImage, popupSelectionListener));
				add(item);
				item.setName( promotionPopupMenuItemName(piece) );
			}
		}
	}
}
