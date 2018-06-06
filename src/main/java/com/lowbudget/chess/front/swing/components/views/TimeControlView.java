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

import com.lowbudget.chess.front.app.Game;
import com.lowbudget.chess.front.app.UIApplication;
import com.lowbudget.chess.front.app.model.GameModel.GameModelAdapter;
import com.lowbudget.chess.front.swing.SwingUIApplication;
import com.lowbudget.chess.front.swing.common.UIUtils;
import com.lowbudget.chess.model.Piece;

import javax.swing.*;
import java.awt.*;

import static com.lowbudget.chess.front.swing.common.TimeStringFormatter.formatTime;

/**
 * <p>Displays the players' time controls.</p>
 * <p>This view uses a tabular layout of two rows where the information of each player is displayed. The information
 * consists of an icon that indicates the player's color, the time left in seconds and the player's name.</p>
 * <p>The view has the ability to flip the rows when the board is flipped and adopt a "enabled"/"disabled" look
 * depending on the game status (i.e. the control disabled its labels when the time is not running).</p>
 */
class TimeControlView extends JPanel {

	/** The top row of the view **/
	private final TimeControlRow topRow;

	/** The bottom row of the view **/
	private final TimeControlRow bottomRow;

	@SuppressWarnings("WeakerAccess")
	public TimeControlView(UIApplication application) {
		// use a grid layout of two rows
		setLayout(new GridLayout(2, 1));

		// by default black is on the top and white is on the bottom
		topRow = new TimeControlRow(application, false);
		bottomRow = new TimeControlRow(application, true);
		add(topRow);
		add(bottomRow);

		// synchronize the rows with the flip status of the board
		refreshRows(application.isBoardFlipped());

		// listen for application changes
		application.addModelListener( new DefaultModelListener() );

		// listen for game status changes
		application.getGameModel().addGameModelListener( new DefaultGameModelListener() );
	}

	/**
	 * Returns the row that represents the information of the white player. This is the bottom row by default but
	 * it becomes the top row if the board is flipped
	 */
	private TimeControlRow getWhiteRow() {
		return topRow.isWhite ? topRow : bottomRow;
	}

	/**
	 *	Returns the row that represents the information of the black player
	 */
	private TimeControlRow getBlackRow() {
		return topRow.isWhite ? bottomRow : topRow;
	}

	/**
	 * Synchronizes the rows with the flip status specified
	 * @param flipped the flipped status of the board - if {@code true} white is on the top, otherwise white is on
	 *                the bottom
	 */
	private void refreshRows(boolean flipped) {
		// take a reference to the texts before changing anything
		String whiteTime = getWhiteRow().getTime();
		String blackTime = getBlackRow().getTime();
		String whitePlayer = getWhiteRow().getPlayerName();
		String blackPlayer = getBlackRow().getPlayerName();

		// now set the values of the labels for both rows
		if (flipped) {
			topRow.setWhite(true)
					.setTime(whiteTime)
					.setPlayerName(whitePlayer)
					.refreshIcon();
			bottomRow.setWhite(false)
					.setTime(blackTime)
					.setPlayerName(blackPlayer)
					.refreshIcon();
		} else {
			topRow.setWhite(false)
					.setTime(blackTime)
					.setPlayerName(blackPlayer)
					.refreshIcon();
			bottomRow.setWhite(true)
					.setTime(whiteTime)
					.setPlayerName(whitePlayer)
					.refreshIcon();
		}
	}

	/**
	 * Default model listener so we can keep up with model's changes
	 */
	private class DefaultModelListener extends UIApplication.ModelAdapter {
		@Override
		public void onThemeChanged(String newTheme) {
			// theme changed reload the white and black icons
			topRow.loadThemeIcons(newTheme);
			bottomRow.loadThemeIcons(newTheme);
		}

		@Override
		public void onBoardFlipped(boolean isFlipped) {
			// board is flipped, synchronize the rows
			refreshRows(isFlipped);
		}
	}

	private class DefaultGameModelListener extends GameModelAdapter {

		@Override
		public void onGameStarted(Game game) {
			TimeControlRow whiteRow = getWhiteRow();
			TimeControlRow blackRow = getBlackRow();

			whiteRow.setEnabledLabels(true)
					.setPlayerName(game.getWhiteName())
					.setTime(game.getWhiteTime());

			blackRow.setEnabledLabels(true)
					.setPlayerName(game.getBlackName())
					.setTime(game.getBlackTime());
		}

		@Override
		public void onGameStopped(Game game) {
			topRow.setEnabledLabels(false);
			bottomRow.setEnabledLabels(false);
		}

		@Override
		public void onGamePaused(Game game) {
			topRow.setEnabledLabels(false);
			bottomRow.setEnabledLabels(false);
		}

		@Override
		public void onGameResumed(Game game) {
			topRow.setEnabledLabels(true);
			bottomRow.setEnabledLabels(true);
		}

		@Override
		public void onGameClosed(Game game) {
			topRow.clear();
			bottomRow.clear();
		}

		@Override
		public void onGameLoaded(Game game) {
			getWhiteRow().setPlayerName(game.getWhiteName());
			getBlackRow().setPlayerName(game.getBlackName());
		}

		@Override
		public void onTimeControlChanged(long whiteRemainingTime, long blackRemainingTime) {
			// time control changed, update the timings
			getWhiteRow().setTime(whiteRemainingTime);
			getBlackRow().setTime(blackRemainingTime);
		}

		@Override
		public void onGameInformationChanged(String whiteName, String blackName) {
			// player names have changed, re-set them
			getWhiteRow().setPlayerName(whiteName);
			getBlackRow().setPlayerName(blackName);
		}
	}

	/**
	 * <p>Loads the icon that corresponds to a piece's type by taking into account the specified theme</p>
	 * <p>The icon is scaled to a hard-coded value of {@code 48} pixels if it is larger/smaller</p>
	 * @param theme the theme for which to load the icon
	 * @param piece the piece for the type of which we should load the icon
	 * @return an {@link ImageIcon} that corresponds to the {@code piece}'s type for the {@code theme} specified
	 */
	private static ImageIcon getIcon(String theme, Piece piece) {
		String imageName = SwingUIApplication.getPieceImageName(theme, piece);
		return new ImageIcon( UIUtils.getImage(imageName, 48) );
	}

	/**
	 * Sets the alignment of the specified labels to {@link SwingConstants#CENTER}
	 * @param label the label to set the alignment for
	 */
	private static void centerLabel(JLabel label) {
		label.setHorizontalAlignment(SwingConstants.CENTER);
		label.setVerticalAlignment(SwingConstants.CENTER);
	}

	/**
	 * A custom panel that represents a row of this view.
	 */
	private static class TimeControlRow extends JPanel {
		private final JLabel iconLabel = new JLabel();
		private final JLabel timeLabel = new JLabel();
		private final JLabel nameLabel = new JLabel();

		/** Indicates if this row represents the white player **/
		private boolean isWhite;

		/** The current icon for thw white player **/
		private ImageIcon whiteIcon;

		/** The current icon for thw black player **/
		private ImageIcon blackIcon;

		/**
		 * Creates a new row for the player specified
		 * @param white if {@code true} the new row will represent the white player, otherwise it will represent the
		 *              black player
		 */
		TimeControlRow(UIApplication application, boolean white) {
			// we use a border layout that has the icon on the left and an inner panel in the center
			// the inner panel uses BorderLayout again to position the player's name on the top and the time in the
			// center
			setLayout(new BorderLayout());
			Font font = timeLabel.getFont();
			Font derived = font.deriveFont(20f);
			timeLabel.setForeground(Color.darkGray);
			timeLabel.setFont(derived);
			centerLabel(timeLabel);

			centerLabel(nameLabel);

			// set the isWhite flag before loading the icons. The flag is used when loading the icons to decide which icon to display
			setWhite(white).setTime(0);

			loadThemeIcons(application.getTheme());

			add(iconLabel, BorderLayout.LINE_START);

			// inner panel to display the player's name and time
			JPanel playerPanel = new JPanel(new BorderLayout());
			playerPanel.setBorder(BorderFactory.createEmptyBorder());
			playerPanel.add(nameLabel, BorderLayout.PAGE_START);
			playerPanel.add(timeLabel, BorderLayout.CENTER);

			add(playerPanel, BorderLayout.CENTER);

			setEnabledLabels(false);
		}

		/**
		 * Loads the white and black icons for the theme specified. Note that this method depends on the value
		 * of {@link #isWhite}
		 * @param theme the theme to load the icons for
		 */
		void loadThemeIcons(String theme) {
			whiteIcon = getIcon(theme, Piece.KING_WHITE);
			blackIcon = getIcon(theme, Piece.KING_BLACK);
			refreshIcon();
		}

		/**
		 * Refreshes the icon of the row.
		 * Note that this method depends on the value of {@link #isWhite}
		 */
		void refreshIcon() {
			iconLabel.setIcon(isWhite ? whiteIcon : blackIcon);
		}

		TimeControlRow setWhite(boolean value) {
			this.isWhite = value;
			return this;
		}

		TimeControlRow setTime(String text) {
			timeLabel.setText(text);
			return this;
		}

		TimeControlRow setPlayerName(String text) {
			nameLabel.setText(text);
			return this;
		}

		void setTime(long millis) {
			timeLabel.setText( formatTime(millis) );
		}

		String getTime() {
			return timeLabel.getText();
		}

		String getPlayerName() {
			return nameLabel.getText();
		}

		TimeControlRow setEnabledLabels(boolean value) {
			iconLabel.setEnabled(value);
			nameLabel.setEnabled(value);
			timeLabel.setEnabled(value);
			return this;
		}

		void clear() {
			nameLabel.setText("");
			setTime(0);
			setEnabledLabels(false);
		}
	}
}
