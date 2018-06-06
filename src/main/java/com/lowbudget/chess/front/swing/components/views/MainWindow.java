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

package com.lowbudget.chess.front.swing.components.views;

import com.lowbudget.chess.front.app.UIApplication;
import com.lowbudget.chess.front.app.UIApplication.ModelAdapter;
import com.lowbudget.common.Messages;
import com.lowbudget.chess.front.swing.SwingUIApplication;
import com.lowbudget.chess.front.swing.actions.Actions;
import com.lowbudget.chess.front.swing.common.RecentHistoryMenu;
import com.lowbudget.chess.front.swing.common.UIUtils;
import com.lowbudget.chess.front.swing.components.painter.BoardPainter;

import javax.swing.*;
import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * <p>The main window of the application.</p>
 * <p>This basically creates the application's menu bar and two panels that represent our main screens: the game
 * screen and the setup position screen. The two of them are swapped using a {@link CardLayout}.</p>
 */
public class MainWindow extends JFrame {

	private static final String MAIN_VIEW_NAME = "main_view";
	private static final String SETUP_VIEW_NAME = "setup_view";

	private final String originalTitle;

	public MainWindow(UIApplication application, Actions actions, String title) {
		super(title);

		this.originalTitle = title;

		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		setPreferredSize(new Dimension(800, 650));
		setMinimumSize(new Dimension(400, 400));

		// our application image
		setIconImage(UIUtils.getImage("chess-icon.png"));

		// set the painter that draws our custom views using a theme
		BoardPainter painter = getThemeBoardPainter(application);

		// create two main views: one for the main game and one for setting up the board
		GameWindowView gameView = new GameWindowView(application, actions, painter);
		SetupWindowView setupView = new SetupWindowView(application, painter);

		// use a card layout so we can switch between the two main views
		CardLayout cardLayout = new CardLayout();

		JPanel rootPanel = new JPanel(cardLayout);
		rootPanel.add(gameView, MAIN_VIEW_NAME);
		rootPanel.add(setupView, SETUP_VIEW_NAME);
		setContentPane(rootPanel);

		// Note, Swing weirdness: it seems that when we build the menu with the main view instead of the root panel the
		// main view is repainted more frequently (possibly because its inputMap and actionMap are used). However this
		// causes side-effects e.g. when a new game starts the board view is repainted even though if it does not
		// explicitly handle the game started event. If the root panel is used here then the board view does need
		// to respond to the game started event otherwise a blank board is displayed (which is the correct behaviour)
		JMenuBar menuBar = createMenuBar(application, actions, rootPanel);
		setJMenuBar(menuBar);

		pack();

		// note: setting divider locations must be called after pack()
		gameView.setDefaultDividerLocations();

		// center in screen
		setLocationRelativeTo(null);

		// Important note: Order matters: this listener is added last so it is executed first
		application.addModelListener(new ModelAdapter() {

			@Override
			public void onApplicationExit() {
				dispose();
			}

			@Override
			public void onSetupModeEntered() {
				cardLayout.show(rootPanel, SETUP_VIEW_NAME);
			}

			@Override
			public void onSetupModeExited() {
				cardLayout.show(rootPanel, MAIN_VIEW_NAME);
			}

			@Override
			public void onServerStarted(int port) {
				adjustTitle(port, false);
			}

			@Override
			public void onServerStopped() {
				setTitle(originalTitle);
			}

			@Override
			public void onClientConnected(int port) {
				adjustTitle(port, true);
			}

			@Override
			public void onClientDisconnected(int port) {
				adjustTitle(port, false);
			}
		});

		// add a window listener to have a chance to save any unsaved data (i.e. changes in the configuration - if any)
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				application.exit();
			}
		});
	}

	private void adjustTitle(int port, boolean connected) {
		// convert port to string to avoid formatting like "5.000"
		setTitle(originalTitle
				+ " - "
				+ Messages.get("app.server.extra.title", String.valueOf(port))
				+ " "
				+ (connected ? Messages.get("app.server.extra.title.client.connected") : Messages.get("app.server.extra.title.client.disconnected"))
		);
	}

	private static BoardPainter getThemeBoardPainter(UIApplication application) {
		return ((SwingUIApplication) application).getBoardPainter();
	}

	private static JMenuBar createMenuBar(UIApplication application, Actions actions, JComponent rootComponent) {
		JMenuBar menuBar = new JMenuBar();

		JMenu gameMenu = gameMenu(application, actions);
		JMenu viewMenu = viewMenu(actions);
		JMenu editMenu = editMenu(actions);
		JMenu engineMenu = engineMenu(actions);

		installKeyBindingsForCopyPaste(rootComponent, actions.getCopyAction(), actions.getPasteAction());

		menuBar.add(gameMenu);
		menuBar.add(viewMenu);
		menuBar.add(editMenu);
		menuBar.add(engineMenu);
		return menuBar;
	}

	private static JMenu gameMenu(UIApplication application, Actions actions) {
		JMenu gameMenu = new JMenu(Messages.get("app.menu.game"));
		gameMenu.add(new JMenuItem(actions.getNewGameAction()));
		gameMenu.add(new JMenuItem(actions.getOpenGameAction()));
		gameMenu.add(new JMenuItem(actions.getSaveGameAction()));
		gameMenu.add(new JMenuItem(actions.getPauseGameAction()));
		gameMenu.add(new JMenuItem(actions.getStopGameAction()));
		gameMenu.add(new JMenuItem(actions.getCloseGameAction()));
		gameMenu.add(new JMenuItem(actions.getEnterSetupModeAction()));
		gameMenu.add(new JMenuItem(actions.getExitSetupModeAction()));
		gameMenu.add(new RecentHistoryMenu<>(Messages.get("app.menu.recent.games"), application.getRecentGameSetups(), actions.getRecentMenuActionFactory()));
		gameMenu.add(new JMenuItem(actions.getExitAction()));
		return gameMenu;
	}

	private static JMenu viewMenu(Actions actions) {
		JMenu viewMenu = new JMenu(Messages.get("app.menu.view"));
		viewMenu.add(new JMenuItem(actions.getFlipBoardAction()));
		viewMenu.add(new JRadioButtonMenuItem(actions.getViewLegalMovesAction()));
		viewMenu.add(new JRadioButtonMenuItem(actions.getViewAttacksAction()));
		viewMenu.add(new JRadioButtonMenuItem(actions.getViewLastMoveAction()));

		// themes sub-menu
		viewMenu.add( themesMenu(actions) );
		return viewMenu;
	}

	private static JMenu themesMenu(Actions actions) {
		// themes sub-menu
		JMenu themesMenu = new JMenu(Messages.get("app.action.view.menu.themes"));

		// group the theme related radios so the current theme is displayed as "selected" in the menu
		ButtonGroup group = new ButtonGroup();
		themesMenu.add( createThemeRadioItem(actions.getChangeThemeWikipediaAction(), group) );
		themesMenu.add( createThemeRadioItem(actions.getChangeThemeStoneAction(), group) );
		themesMenu.add( createThemeRadioItem(actions.getChangeThemeWoodAction(), group) );
		return themesMenu;
	}

	private static JMenu editMenu(Actions actions) {
		JMenu editMenu = new JMenu(Messages.get("app.menu.edit"));
		editMenu.add(new JMenuItem(actions.getCopyAction()));
		editMenu.add(new JMenuItem(actions.getPasteAction()));
		editMenu.add(new JMenuItem(actions.getPreferencesAction()));
		return editMenu;
	}

	private static JMenu engineMenu(Actions actions) {
		JMenu engineMenu = new JMenu(Messages.get("app.menu.engine"));
		engineMenu.add(new JMenuItem(actions.getManageEnginesAction()));
		engineMenu.add(new JMenuItem(actions.getRunAsServerAction()));
		engineMenu.add(new JMenuItem(actions.getStopServerAction()));
		engineMenu.add(new JMenuItem(actions.getWhiteEngineSettingsAction()));
		engineMenu.add(new JMenuItem(actions.getBlackEngineSettingsAction()));
		return engineMenu;
	}

	private static void installKeyBindingsForCopyPaste(JComponent mainArea, Action copyAction, Action pasteAction) {
		mainArea.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_MASK), "copy");
		mainArea.getActionMap().put("copy", copyAction);
		mainArea.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_V, InputEvent.CTRL_MASK), "paste");
		mainArea.getActionMap().put("paste", pasteAction);
	}

	private static JRadioButtonMenuItem createThemeRadioItem(Action action, ButtonGroup buttonGroup) {
		JRadioButtonMenuItem themeItem = new JRadioButtonMenuItem(action);
		buttonGroup.add(themeItem);
		return themeItem;
	}

}
