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
import com.lowbudget.common.Messages;
import com.lowbudget.chess.front.swing.ComponentNames;
import com.lowbudget.chess.front.swing.actions.Actions;
import com.lowbudget.chess.front.swing.components.painter.BoardPainter;
import com.lowbudget.chess.front.swing.components.views.board.BoardBackgroundView;
import com.lowbudget.chess.front.swing.components.views.board.BoardView;

import javax.swing.*;
import java.awt.*;

/**
 * A panel displaying al the information of a game i.e. the board, time controls, captured pieces etc
 */
class GameWindowView extends JPanel {

	/** Stores any information related to the engine's thinking lines */
	private final JTextArea engineInfoArea;

	/** References to split panes so we can set their divider locations after this component is created */
	private final JSplitPane rightBottom;
	private final JSplitPane mainPane;

	GameWindowView(UIApplication application, Actions actions, BoardPainter painter) {

		mainPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);

		mainPane.setResizeWeight(0.75); // when resized left component should take 75% of horizontal space

		engineInfoArea = new JTextArea();

		// Our layout: the main area is split to left and right (let's call them MAIN_LEFT, MAIN_RIGHT)
		// MAIN_LEFT: the BoardBackgroundView that draws the background and contains the BoardView that draws the board
		// MAIN_RIGHT: a panel that contains 2 components: RIGHT_TOP, RIGHT_BOTTOM.
		//     RIGHT_TOP: the TimeControlView that displays time left
		//     RIGHT_BOTTOM: a split pane that splits the area to RIGHT_BOTTOM_TOP, RIGHT_BOTTOM_BOTTOM
		//         RIGHT_BOTTOM_TOP: the MoveListView that displays the moves
		//         RIGHT_BOTTOM_BOTTOM: a tabbed pane that contains everything else (i.e. captured view and info view)

		// MAIN_RIGHT a panel that uses BorderLayout to display the top TimeControlView component with a fixed height
		// and the bottom split pane to occupy the rest of the area
		JPanel rightPane = new JPanel();
		rightPane.setBorder(BorderFactory.createEmptyBorder());
		rightPane.setLayout(new BorderLayout());

		// RIGHT_TOP
		TimeControlView timeControlView = new TimeControlView(application);
		rightPane.add(timeControlView, BorderLayout.NORTH);

		// RIGHT_BOTTOM_TOP
		MoveListView moveListView = new MoveListView(application);

		// RIGHT_BOTTOM_BOTTOM a tabbed pane that contains captured view and board info
		JTabbedPane tabbedPane = new JTabbedPane();
		tabbedPane.add(Messages.get("app.view.captured.title"), new CapturedView(application));
		tabbedPane.add(Messages.get("app.view.board.info.title"), new BoardInfoView(application));
		tabbedPane.add(Messages.get("app.view.engine.info"), new JScrollPane(engineInfoArea));

		// RIGHT_BOTTOM: a vertical split of the move list view at the top and the the tabbed pane at the bottom
		this.rightBottom = new JSplitPane(JSplitPane.VERTICAL_SPLIT, moveListView, tabbedPane);
		rightBottom.setBorder(BorderFactory.createEmptyBorder());
		rightBottom.setResizeWeight(0.5); // when resized split space equally
		rightPane.add(rightBottom, BorderLayout.CENTER);

		// MAIN_LEFT
		BoardBackgroundView leftPane = new BoardBackgroundView(application, painter);
		BoardView boardView = new BoardView(application, ComponentNames.BOARD_GAME_VIEW, application.getGameModel(), painter, new Dimension(500, 500));
		leftPane.add(boardView);

		mainPane.setLeftComponent(new JScrollPane(leftPane));
		mainPane.setRightComponent(rightPane);

		setLayout(new BorderLayout());

		add(createTopToolbar(actions), BorderLayout.NORTH);
		add(mainPane, BorderLayout.CENTER);

		application.addModelListener(new UIApplication.ModelAdapter() {
			@Override
			public void onEngineInfoAvailable(String info) {
				engineInfoArea.append(info + "\n");
			}

		});
	}

	void setDefaultDividerLocations() {
		// note: setting divider locations must be called after pack()
		mainPane.setDividerLocation(0.75);
		rightBottom.setDividerLocation(0.5);	// split the pane in half vertically

	}

	private static JToolBar createTopToolbar(Actions actions) {
		JToolBar toolBar = new JToolBar();
		toolBar.setRollover(true);
		toolBar.setFloatable(false);
		toolBar.setFocusable(false);
		toolBar.setOrientation(JToolBar.HORIZONTAL);

		toolBar.add( button(actions.getNewGameAction()) );
		toolBar.add( button(actions.getOpenGameAction()) );
		toolBar.add( button(actions.getSaveGameAction()) );
		toolBar.add( button(actions.getPauseGameAction()) );
		toolBar.add( button(actions.getStopGameAction()) );
		toolBar.add( button(actions.getCloseGameAction()) );

		toolBar.addSeparator();

		toolBar.add( button(actions.getCopyAction()) );
		toolBar.add( button(actions.getPasteAction()) );

		toolBar.addSeparator();

		toolBar.add( button(actions.getEnterSetupModeAction()) );
		toolBar.add( button(actions.getFlipBoardAction()) );

		return toolBar;
	}

	private static AbstractButton button(Action action) {
		JButton button = new JButton(action);
		button.setFocusable(false);
		button.setHideActionText(true);
		return button;
	}
}
