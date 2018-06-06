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

package com.lowbudget.chess.front.swing;

import com.lowbudget.chess.front.app.impl.HeadlessApplication;
import com.lowbudget.chess.front.app.Player;
import com.lowbudget.common.Messages;
import com.lowbudget.chess.model.Piece;
import com.lowbudget.chess.model.PieceType;
import com.lowbudget.chess.model.PlayerColor;
import com.lowbudget.chess.model.board.GameState;
import com.lowbudget.chess.model.board.IllegalMoveException;
import com.lowbudget.chess.front.swing.actions.Actions;
import com.lowbudget.chess.front.swing.common.UIUtils;
import com.lowbudget.chess.front.swing.components.forms.RegistrationDialog;
import com.lowbudget.chess.front.swing.components.forms.WaitDialog;
import com.lowbudget.chess.front.swing.components.painter.BoardPainter;
import com.lowbudget.chess.front.swing.components.painter.ThemePainter;
import com.lowbudget.chess.front.swing.components.views.MainWindow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;

public class SwingUIApplication extends HeadlessApplication {

	@SuppressWarnings("SpellCheckingInspection")
	private static final String IMAGE_PACKAGE = "com/lowbudget/chess/front/themes/";

	private static final Logger log = LoggerFactory.getLogger(SwingUIApplication.class);

	private Frame mainWindow;

	private BoardPainter boardPainter;

	Frame getMainWindow() {
		return mainWindow;
	}

	private void setMainWindow(Frame mainWindow) {
		this.mainWindow = mainWindow;
	}

	public BoardPainter getBoardPainter() {
		if (boardPainter == null) {
			boardPainter = new ThemePainter();
			boardPainter.loadTheme(model.getTheme());
		}
		return boardPainter;
	}

	@Override
	public void initialize() {
		SwingUtilities.invokeLater( () -> {
			super.initialize();
			setUITimerFactory(SwingUITimer::new);
			setWaitDialogHolderFactory(SwingWaitDialogHolder::new);
			try {
				UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			} catch (Exception e) {
				//ignore
			}

			Actions actions = new Actions(this);

			MainWindow window = new MainWindow(this, actions, Messages.get("app.title"));
			setMainWindow(window);
			window.setVisible(true);
		});
	}

	@Override
	public void setTheme(String newTheme) {
		// make sure our painter is updated before the model. It is crucial for the board views that the painter is
		// correctly updated to calculate correct board dimensions
		getBoardPainter().loadTheme(newTheme);
		model.setTheme(newTheme);
	}

	public void showValidationFailedWarningMessage(String title, String error) {
		UIUtils.showWarningMessage(getMainWindow(), title, error);
	}

	public static String getPieceImageName(String theme, Piece piece) {
		return IMAGE_PACKAGE + theme + "/" + piece.color().shortName() + piece.getType().shortName() + ".png";
	}

	public static String getThemeImageName(String theme, String imageName) {
		return IMAGE_PACKAGE + theme + "/" + imageName;
	}

	public static String getThemeProperties(String theme) {
		return IMAGE_PACKAGE + theme + "/theme.properties";
	}

	/**
	 * Maps the piece type to a message key with the piece's name translation.
	 * @param pieceType the piece type for which we want to find the translation key to use
	 * @return a {@code String} key to be used to find the translation of the piece's name
	 */
	public static String getPieceTypeNameKey(PieceType pieceType) {
		String result;
		switch (pieceType) {
			case KNIGHT:
				result = "board.piece.knight";
				break;
			case BISHOP:
				result = "board.piece.bishop";
				break;
			case ROOK:
				result = "board.piece.rook";
				break;
			case QUEEN:
				result = "board.piece.queen";
				break;
			default:
				throw new IllegalArgumentException("No translation key found for piece type: " + pieceType);
		}
		return result;
	}

	@Override
	protected void runInUIThread(Runnable r) {
		SwingUtilities.invokeLater(r);
	}

	@Override
	protected void showEngineConnectionTesterTimedOutWarningMessage() {
		UIUtils.showWarningMessage(getMainWindow(),
				"app.dialog.manage.engines.test.error.timed.out.title",
				"app.dialog.manage.engines.test.timeout.message");
	}

	@Override
	protected void showEngineConnectionTesterFailedWarningMessage(Exception error) {
		UIUtils.showWarningMessage(getMainWindow(),
				"app.dialog.manage.engines.test.error.unknown.title",
				"app.dialog.manage.engines.test.error.message",
				(error != null ? error.getClass().getName() + ": " + error.getMessage() : "<none>"));
	}

	@Override
	protected void showEngineConnectionTesterSuccessMessage() {
		UIUtils.showInfoMessage(getMainWindow(), "app.success", "app.dialog.manage.engines.test.successful");
	}

	@Override
	protected void showAddEngineSuccessMessage() {
		UIUtils.showInfoMessage(getMainWindow(), "app.success", "app.dialog.manage.engines.add.successful");
	}

	@Override
	protected void showEnginePlayerRegistrationDialog(Player player, RegistrationDialogSuccessListener successListener, DialogCancelListener cancelListener) {
		RegistrationDialog dialog = new RegistrationDialog(this, player);
		dialog.setVisible(true);

		if (dialog.isCancelled()) {
			cancelListener.onDialogCanceled();
		} else {
			successListener.onDialogSuccess(dialog.getRegistrationName(), dialog.getRegistrationCode());
		}
	}

	@Override
	protected void showOpenErrorMessage(File openedFile, Exception exception) {
		UIUtils.showErrorMessage(getMainWindow(), "game.open.illegal.pgn.title", "game.open.illegal.pgn.msg", openedFile, exception.getMessage());
	}

	@Override
	protected void showExceptionMessage(Exception exception) {
		UIUtils.showExceptionMessage(getMainWindow(), "app.error", exception);
	}

	@Override
	protected void showEnginePlayerUnexpectedDisconnectionErrorMessage() {
		UIUtils.showErrorMessage(getMainWindow(), "app.dialog.session.closed.title", "app.dialog.session.closed.msg");
	}

	@Override
	protected void showEnginePlayerCopyProtectionWarningMessage(String playerName, PlayerColor playerColor) {
		UIUtils.showWarningMessage(getMainWindow(),
				Messages.get("app.dialog.copy.protection.title", playerColor),
				Messages.get("app.dialog.copy.protection.message", playerName));
	}

	@Override
	protected void showIllegalMoveInfoMessage(IllegalMoveException e) {
		UIUtils.showInfoMessage(getMainWindow(), "app.board.error.title", e.getMessage(), e.getArg());
	}

	@Override
	protected void showGamePauseDialog() {
		UIUtils.showInfoMessage(getMainWindow(), "app.game.paused.title", "app.game.paused");
	}

	@Override
	protected void showGameFinalMoveInfoMessage(GameState state) {
		UIUtils.showInfoMessage(getMainWindow(), "app.game.ended.title", "app.game.ended", state);
	}

	@Override
	protected void showPasteFenFailedWarningMessage(String fen) {
		UIUtils.showWarningMessage(getMainWindow(),"board.illegal.fen.title", "board.illegal.fen.msg", fen);
	}

	@Override
	protected void showDeleteEngineConfirmDialog(DialogSuccessListener successListener) {
		int result = UIUtils.showOkCancelDialog(getMainWindow(),
				"app.dialog.manage.engines.delete.confirm.title",
				"app.dialog.manage.engines.delete.confirm.message");
		if (result == JOptionPane.OK_OPTION) {
			successListener.onDialogSuccess();
		}
	}

	@Override
	protected boolean showCloseGameConfirmDialog(DialogSuccessListener successListener) {
		int result = UIUtils.showYesNoCancelDialog(getMainWindow(),
				"app.dialog.game.save.confirm.title",
				"app.dialog.game.save.confirm.msg");
		if (result == JOptionPane.YES_OPTION) {
			successListener.onDialogSuccess();
		}
		return result == JOptionPane.CANCEL_OPTION;
	}

	@Override
	protected void showGameTimeElapsedWarningMessage(String looserName) {
		UIUtils.showInfoMessage(getMainWindow(), "app.game.ended.title", "app.game.ended.time.elapsed", looserName);
	}

	@Override
	protected File showSelectOpenFileDialog() {
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setCurrentDirectory( getLastOpenFolderPath() );
		fileChooser.setFileFilter(new FileNameExtensionFilter(Messages.get("game.open.pgn.description"), "pgn", "txt"));
		int result = fileChooser.showOpenDialog(getMainWindow());
		return (result == JFileChooser.APPROVE_OPTION) ? fileChooser.getSelectedFile() : null;
	}

	@Override
	protected File showSelectSaveFileDialog() {
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setCurrentDirectory( getLastOpenFolderPath() );
		int result = fileChooser.showSaveDialog(getMainWindow());
		return (result == JFileChooser.APPROVE_OPTION) ? fileChooser.getSelectedFile() : null;
	}

	protected boolean hasTransferableText() {
		Transferable contents = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);
		return (contents != null) && contents.isDataFlavorSupported(DataFlavor.stringFlavor);
	}

	@Override
	protected void copyToClipboard(String text) {
		Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(text), null);
	}

	@Override
	protected String pasteFromClipboard() {
		Transferable contents = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);

		String text = null;
		boolean hasTransferableText = (contents != null) && contents.isDataFlavorSupported(DataFlavor.stringFlavor);
		if (hasTransferableText) {
			try {
				text = (String) contents.getTransferData(DataFlavor.stringFlavor);
			} catch (Exception ex) {
				log.error(ex.getMessage(), ex);
			}
		}
		return text;
	}

	static class SwingWaitDialogHolder implements WaitDialogHolder {
		private WaitDialog waitDialog;

		@Override
		public void openDialog(DialogCancelListener cancelListener) {
			if (waitDialog != null) {
				throw new IllegalStateException("A wait dialog already exists");
			}
			if (log.isDebugEnabled()) {
				log.debug("Opening wait dialog");
			}
			waitDialog = new WaitDialog("app.dialog.wait.title", "app.dialog.wait.message");

			// if the user closes the window we treat this as a cancel operation and call the callback specified
			waitDialog.addWindowListener(new WindowAdapter() {
				@Override
				public void windowClosing(WindowEvent e) {
					cancelListener.onDialogCanceled();
				}
			});
			waitDialog.setVisible(true);
			if (log.isDebugEnabled()) {
				log.debug("Wait dialog closed (in openDialog())");
			}
		}

		@Override
		public void close() {
			if (waitDialog != null) {
				if (log.isDebugEnabled()) {
					log.debug("Closing wait dialog");
				}
				waitDialog.setVisible(false);
				waitDialog.dispose();
				waitDialog = null;
			}
		}
	}
}
