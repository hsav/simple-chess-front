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

import com.lowbudget.chess.front.app.model.BoardModel;
import com.lowbudget.chess.front.app.UIApplication;
import com.lowbudget.chess.front.swing.SwingUIApplication;
import com.lowbudget.chess.front.swing.common.UIUtils;
import com.lowbudget.chess.model.Piece;
import com.lowbudget.chess.model.PieceType;
import com.lowbudget.chess.model.Square;
import com.lowbudget.util.OSUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.image.BufferedImage;
import java.util.function.Consumer;

/**
 * Manages drag and drop operation when a user moves a piece on the board
 */
class SquareViewDragHandler extends TransferHandler implements Transferable {

	private static final Logger log = LoggerFactory.getLogger(SquareViewDragHandler.class);

	private static final DataFlavor[] DATA_FLAVORS = {DataFlavor.imageFlavor};

	/** The square that is currently being dragged */
	private SquareView draggedSquareView;

	/** the current application object */
	private final UIApplication application;

	SquareViewDragHandler(UIApplication application) {
		this.application = application;
	}

	////////////////////drag handler methods

	@Override
	public int getSourceActions(JComponent c) {
		return TransferHandler.MOVE;
	}

	@Override
	public DataFlavor[] getTransferDataFlavors() {
		return DATA_FLAVORS;
	}

	@Override
	public boolean isDataFlavorSupported(DataFlavor flavor) {
		return flavor == DATA_FLAVORS[0];
	}

	@Override
	public Transferable createTransferable(JComponent c) {
		// Clear
		draggedSquareView = null;

		Transferable result = null;

		if (c instanceof SquareView) {
			draggedSquareView = (SquareView) c;
			result = this;

			Square dragSquare = draggedSquareView.getSquare();

			BoardModel boardModel = draggedSquareView.getModel();

			if (boardModel.hasBoard()) {
				Piece piece = boardModel.getPieceAtSquare(dragSquare);
				if (piece != null) {
					String imageName = SwingUIApplication.getPieceImageName(application.getTheme(), piece);
					// get the piece image scaled to the square view's width i.e. with the same size drawn on the board
					BufferedImage pieceImage = UIUtils.getImage(imageName, draggedSquareView.getWidth());

					// set it as drag image
					setDragImage(pieceImage);

					// also set the drag image offset so the image is centered under the mouse cursor

					// Not sure why this happens: in Mac OS (tested in VirtualBox with Yosemite) we need to use a
					// negative offset to achieve the desired behaviour
					int sign = OSUtils.isMacOS() ? -1 : 1;
					setDragImageOffset(new Point(sign * pieceImage.getWidth() / 2, sign * pieceImage.getHeight() / 2));
				}
			}
		}
		return result;
	}

	@Override
	public Object getTransferData(DataFlavor flavor) {
		if (isDataFlavorSupported(flavor)) {
			return draggedSquareView;
		}
		//noinspection ConstantConditions
		return null;
	}


	//////////////////// drop handler methods
	@Override
	public boolean canImport(TransferHandler.TransferSupport info) {
		boolean result = false;
		if (info.isDrop() && info.isDataFlavorSupported(DataFlavor.imageFlavor)) {
			Component c = info.getComponent();
			if (c instanceof SquareView) {
				result = true;
			}
		}
		return result;
	}

	@Override
	public boolean importData(TransferHandler.TransferSupport info) {
		if (!canImport(info)) {
			return false;
		}

		try {
			Point dropPoint = info.getDropLocation().getDropPoint();
			SquareView dropView = (SquareView) info.getComponent();
			Square dropSquare = dropView.getSquare();

			String theme = application.getTheme();
			BoardModel boardModel = dropView.getModel();

			if (!boardModel.isBoardInSetupMode() && boardModel.isPromotionMove(dropSquare)) {
				// promotion move i.e. we have a pawn legally reaching the 8-th rank - open a popup menu for the user to select the promotion type

				// when we have the promotion type the popup selection listener will call the board model to perform the final move
				Consumer<PieceType> popupSelectionListener = promotionType -> boardModel.onPieceDragFinished(dropSquare, promotionType);
				PromotionPopupMenu popupMenu = new PromotionPopupMenu(boardModel.getPlayingColor(), theme, popupSelectionListener);
				popupMenu.show(dropView, dropPoint.x, dropPoint.y);
			} else {
				// normal move - let the board model know
				boardModel.onPieceDragFinished(dropSquare, null);
			}
		} catch (Exception e) {
			// exceptions during drag n drop can be swallowed so we at least log them to help debugging
			log.error(e.getMessage(), e);
			throw e;
		}
		return true;
	}
}
