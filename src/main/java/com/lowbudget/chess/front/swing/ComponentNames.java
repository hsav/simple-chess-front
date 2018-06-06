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

import com.lowbudget.chess.model.Piece;
import com.lowbudget.chess.model.Square;

/**
 * <p>Stores component names constants to be used for our swing components.</p>
 * <p>It is essential to have names in the components because they are used by our AssertJ-Swing tests.</p>
 */
public class ComponentNames {

	public static final String OK_BUTTON = "generic-ok-button";
	public static final String CANCEL_BUTTON = "generic-cancel-button";

	public static final String BOARD_GAME_VIEW = "generic-board-view";
	public static final String BOARD_SETUP_VIEW = "generic-setup-view";

	public static String boardSquareName(String boardName, Square square) {
		return boardName + "-" + square.toString();
	}

	public static final String PROMOTION_POPUP = "generic-promotion-popup";
	private static final String PROMOTION_POPUP_ITEM = "generic-promotion-popup-item";

	public static String promotionPopupMenuItemName(Piece piece) {
		return PROMOTION_POPUP_ITEM + "-" + piece.toString();
	}

	public static final String SETUP_MOVE_BUTTON = "setup-move-piece-button";
	public static final String SETUP_INITIAL_POSITION_BUTTON = "setup-initial-position-button";
	public static final String SETUP_EMPTY_POSITION_BUTTON = "setup-empty-position-button";

	private static final String SETUP_PIECE_BUTTON = "setup-piece-button";

	public static String setupPieceButtonName(Piece piece) {
		return SETUP_PIECE_BUTTON + "-" + piece.toString();
	}

	public static class DialogNewGame {
		public static final String FIRST_PLAYER_HUMAN_RADIO = "new-game-dialog-first-player-human-radio";
		public static final String FIRST_PLAYER_ENGINE_RADIO = "new-game-dialog-first-player-engine-radio";
		public static final String FIRST_PLAYER_NAME = "new-game-dialog-first-player-name";
		public static final String FIRST_PLAYER_ENGINE_COMBO = "new-game-dialog-first-player-engine-combo";
		public static final String FIRST_PLAYER_TIME = "new-game-dialog-first-player-time";
		public static final String SECOND_PLAYER_HUMAN_RADIO = "new-game-dialog-second-player-human-radio";
		public static final String SECOND_PLAYER_ENGINE_RADIO = "new-game-dialog-second-player-engine-radio";
		public static final String SECOND_PLAYER_NAME = "new-game-dialog-second-player-name";
		public static final String SECOND_PLAYER_ENGINE_COMBO = "new-game-dialog-second-player-engine-combo";
		public static final String SECOND_PLAYER_TIME = "new-game-dialog-second-player-time";
		public static final String USE_SETUP_POSITION = "new-game-dialog-use-setup-position";
		public static final String USE_GAME_POSITION = "new-game-dialog-use-game-position";
	}

	public static class DialogRegistration {
		public static final String NAME = "registration-dialog-name";
		public static final String CODE = "registration-dialog-code";
	}

	public static class DialogManageEngines {
		public static final String ENGINES_LIST = "manage-engines-list";
		public static final String ADD_BUTTON = "manage-engines-button-add";
		public static final String EDIT_BUTTON = "manage-engines-button-edit";
		public static final String TEST_BUTTON = "manage-engines-button-test";
		public static final String DELETE_BUTTON = "manage-engines-button-delete";
		public static final String SELECT_ENGINE_FILE_BUTTON = "manage-engines-button-select-engine-file";
	}

	public static class ViewMoveList {
		public static final String PREVIOUS_MOVE = "move-list-view-previous-move";
		public static final String FIRST_MOVE = "move-list-view-first-move";
		public static final String NEXT_MOVE = "move-list-view-next-move";
		public static final String LAST_MOVE = "move-list-view-last-move";
	}


	private ComponentNames() {}
	
	
}
