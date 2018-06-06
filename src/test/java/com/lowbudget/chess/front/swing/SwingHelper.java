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

import com.lowbudget.chess.front.app.model.BoardModel;
import com.lowbudget.chess.front.app.Helper.Locator;
import com.lowbudget.common.Messages;
import com.lowbudget.chess.model.Piece;
import com.lowbudget.chess.model.PlayerColor;
import com.lowbudget.chess.model.Square;
import com.lowbudget.chess.front.swing.components.views.board.SquareView;
import org.assertj.swing.awt.AWT;
import org.assertj.swing.core.Robot;
import org.assertj.swing.driver.JComponentDriver;
import org.assertj.swing.fixture.AbstractComponentFixture;
import org.assertj.swing.fixture.ComponentFixtureExtension;

import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.io.File;
import java.util.Collections;

import static com.lowbudget.chess.front.app.Helper.Locator.Factory.button;
import static com.lowbudget.chess.front.app.Helper.Locator.Factory.menu;
import static org.assertj.swing.edt.GuiActionRunner.execute;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;

class SwingHelper {

	static final String APP_TITLE = Messages.get("app.title");

	static final String BUTTON_OK = Messages.get("app.button.ok");
	static final String BUTTON_CANCEL = Messages.get("app.button.cancel");
	static final String BUTTON_YES = Messages.get("app.button.yes");
	static final String BUTTON_NO = Messages.get("app.button.no");
	static final String BUTTON_LATER = Messages.get("app.dialog.registration.button.later");

	static final String DELETE_ENGINE_DIALOG_TITLE = Messages.get("app.dialog.manage.engines.delete.confirm.title");
	static final String ENGINE_SETTINGS_DIALOG_TITLE = Messages.get("app.dialog.chess.engine.settings");
	static final String ADD_ENGINE_DIALOG_TITLE = Messages.get("app.dialog.engine.add.title");
	static final String TEST_ENGINE_TIME_OUT_DIALOG_TITLE = Messages.get("app.dialog.manage.engines.test.error.timed.out.title");
	static final String TEST_ENGINE_UNKNOWN_ERROR_DIALOG_TITLE = Messages.get("app.dialog.manage.engines.test.error.unknown.title");
	static final String MANAGE_ENGINES_DIALOG_TITLE = Messages.get("app.action.engine.manage");

	static final String SUCCESS_DIALOG_TITLE = Messages.get("app.success");
	static final String SAVE_DIALOG_TITLE = Messages.get("app.dialog.game.save.confirm.title");
	static final String COPY_PROTECTION_WHITE_DIALOG_TITLE = Messages.get("app.dialog.copy.protection.title", PlayerColor.WHITE);
	static final String REGISTRATION_WHITE_DIALOG_TITLE = Messages.get("app.dialog.registration.title", PlayerColor.WHITE);
	static final String REGISTRATION_BLACK_DIALOG_TITLE = Messages.get("app.dialog.registration.title", PlayerColor.BLACK);


	static final Locator newGameAction = menu("app.menu.game", "app.action.game.new");
	static final Locator loadGameAction = menu("app.menu.game", "app.action.game.open");
	static final Locator saveGameAction = menu("app.menu.game", "app.action.game.save");
	static final Locator enterSetupModeAction = menu("app.menu.game", "app.action.game.setup.start");
	static final Locator exitSetupModeAction = menu("app.menu.game", "app.action.game.setup.stop");
	static final Locator pauseGameAction = menu("app.menu.game", "app.action.game.pause");
	static final Locator stopGameAction = menu("app.menu.game", "app.action.game.stop");
	static final Locator closeGameAction = menu("app.menu.game", "app.action.game.close");
	static final Locator copyFenAction = menu("app.menu.edit", "app.action.edit.copy.board.fen");
	static final Locator pasteFenAction = menu("app.menu.edit", "app.action.edit.paste.board.fen");
	static final Locator manageEnginesAction = menu("app.menu.engine", "app.action.engine.manage");
	static final Locator runServerAction = menu("app.menu.engine", "app.action.engine.run.as.server");
	static final Locator stopServerAction = menu("app.menu.engine", "app.action.engine.stop.server");
	static final Locator whiteSettingsAction = menu("app.menu.engine", "app.action.engine.settings.white");
	static final Locator blackSettingsAction = menu("app.menu.engine", "app.action.engine.settings.black");

	// browse move list action does not reside in a menu. There are actually 4 such actions, we check just one of them
	static final Locator browseMoveListAction = button(ComponentNames.ViewMoveList.FIRST_MOVE);

	static final Locator exitAction = menu("app.menu.game", "app.action.exit");

	static void clearClipboardTextByCopyingSomethingElse() {
		File f = new File(".");
		Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new Transferable() {
			@Override
			public DataFlavor[] getTransferDataFlavors() {
				return new DataFlavor[] {DataFlavor.javaFileListFlavor};
			}

			@Override
			public boolean isDataFlavorSupported(DataFlavor flavor) {
				return DataFlavor.javaFileListFlavor.equals(flavor);
			}

			@Override
			public Object getTransferData(DataFlavor flavor) {
				return Collections.singletonList(f);
			}
		}, null);
	}

	private static class SquareViewDriver extends JComponentDriver {

		SquareViewDriver(Robot robot) {
			super(robot);
		}

		void drag(SquareView squareView) {
			this.robot.waitForIdle();
			super.drag(squareView, AWT.visibleCenterOf(squareView));
		}
	}

	public static class SquareViewFixture extends AbstractComponentFixture<SquareViewFixture, SquareView, SquareViewDriver> {

		SquareViewFixture(Robot robot, String squareViewName) {
			super(SquareViewFixture.class, robot, squareViewName, SquareView.class);
		}

		SquareViewFixture dragToSquare(SquareViewFixture dropTarget) {
			this.driver().drag(target());
			this.driver().drop(dropTarget.target());
			return this;
		}

		SquareViewFixture dragToSelf() {
			this.driver().drag(target());
			this.driver().drop(target());
			return this;
		}

		@SuppressWarnings("UnusedReturnValue")
		SquareViewFixture requireSelected() {
			Square square = target().getSquare();
			assertEquals(square, execute( () -> target().getModel().getSelectedSquare()) );
			return this;
		}

		SquareViewFixture requireUnSelected() {
			Square square = target().getSquare();
			assertNotEquals(square, execute( () -> target().getModel().getSelectedSquare()) );
			return this;
		}

		SquareViewFixture requirePiece(Piece piece) {
			Square square = target().getSquare();
			assertEquals(piece, execute( () -> target().getModel().getPieceAtSquare(square)) );
			return this;
		}
		SquareViewFixture requireEmpty() {
			Square square = target().getSquare();
			assertNull(execute( () -> target().getModel().getPieceAtSquare(square)) );
			return this;
		}

		static void assertBoardFen(BoardModel boardModel, String fen) {
			assertEquals(fen, execute(boardModel::getFenPosition));
		}

		@Override
		protected SquareViewDriver createDriver(Robot robot) {
			return new SquareViewDriver(robot);
		}
	}

	public static class SquareViewExtension extends ComponentFixtureExtension<SquareView, SquareViewFixture> {

		static SquareViewExtension squareWithName(String name) {
			return new SquareViewExtension(name);
		}

		private final String name;

		private SquareViewExtension(String name) {
			this.name = name;
		}

		public SquareViewFixture createFixture(Robot robot, Container root) {
			return new SquareViewFixture(robot, name);
		}
	}
}
