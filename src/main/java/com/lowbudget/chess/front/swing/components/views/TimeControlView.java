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
import com.lowbudget.chess.model.PlayerColor;

import javax.swing.*;
import java.awt.*;

import static com.lowbudget.chess.front.swing.common.TimeStringFormatter.formatTime;

/**
 * <p>Displays a single players' time controls.</p>
 * <p>The information consists of an icon that indicates the player's color, the time left in seconds
 * and the player's name.</p>
 */
class TimeControlView extends JPanel {

	/**
	 * The panel displaying the actual information
	 **/
	private final TimeControlRow row;

	@SuppressWarnings("WeakerAccess")
	public TimeControlView(UIApplication application, PlayerColor playerColor) {

		// use a grid layout of one row
		setLayout(new GridLayout(1, 1));

		// by default black is on the top and white is on the bottom
		row = new TimeControlRow(application, PlayerColor.WHITE == playerColor);
		add(row);

		// listen for application changes
		application.addModelListener(new DefaultModelListener());

		// listen for game status changes
		application.getGameModel().addGameModelListener(new DefaultGameModelListener());
	}

	public boolean isWhite() {
		return row.isWhite;
	}

	public String getTime() {
		return this.row.getTime();
	}

	public String getPlayerName() {
		return this.row.getPlayerName();
	}

	public void setInfo(PlayerColor color, String time, String playerName) {
		row.setWhite(color.isWhite())
			.setTime(time)
			.setPlayerName(playerName)
			.refreshIcon();
	}

	/**
	 * Default model listener to listen for model's theme changes
	 */
	private class DefaultModelListener extends UIApplication.ModelAdapter {
		@Override
		public void onThemeChanged(String newTheme) {
			// theme changed reload the white and black icons
			row.loadThemeIcons(newTheme);
		}
	}

	private class DefaultGameModelListener extends GameModelAdapter {

		@Override
		public void onGameStarted(Game game) {
			String name = row.isWhite ? game.getWhiteName() : game.getBlackName();
			long time = row.isWhite ? game.getWhiteTime() : game.getBlackTime();

			row.setEnabledLabels(true)
				.setPlayerName(name)
				.setTime(time);
		}

		@Override
		public void onGameStopped(Game game) {
			row.setEnabledLabels(false);
		}

		@Override
		public void onGamePaused(Game game) {
			row.setEnabledLabels(false);
		}

		@Override
		public void onGameResumed(Game game) {
			row.setEnabledLabels(true);
		}

		@Override
		public void onGameClosed(Game game) {
			row.clear();
		}

		@Override
		public void onGameLoaded(Game game) {
			row.setPlayerName(row.isWhite ? game.getWhiteName() : game.getBlackName());
		}

		@Override
		public void onTimeControlChanged(long whiteRemainingTime, long blackRemainingTime) {
			// time control changed, update the timings
			row.setTime(row.isWhite ? whiteRemainingTime : blackRemainingTime);
		}

		@Override
		public void onGameInformationChanged(String whiteName, String blackName) {
			// player names have changed, re-set them
			row.setPlayerName(row.isWhite ? whiteName : blackName);
		}
	}

	/**
	 * <p>Loads the icon that corresponds to a piece's type by taking into account the specified theme</p>
	 * <p>The icon is scaled to a hard-coded value of {@code 48} pixels if it is larger/smaller</p>
	 *
	 * @param theme the theme for which to load the icon
	 * @param piece the piece for the type of which we should load the icon
	 * @return an {@link ImageIcon} that corresponds to the {@code piece}'s type for the {@code theme} specified
	 */
	private static ImageIcon getIcon(String theme, Piece piece) {
		String imageName = SwingUIApplication.getPieceImageName(theme, piece);
		return new ImageIcon(UIUtils.getImage(imageName, 32));
	}

	/**
	 * Sets the alignment of the specified labels to {@link SwingConstants#CENTER}
	 *
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

		/**
		 * Indicates if this row represents the white player
		 **/
		private boolean isWhite;

		/**
		 * The current icon for thw white player
		 **/
		private ImageIcon whiteIcon;

		/**
		 * The current icon for thw black player
		 **/
		private ImageIcon blackIcon;

		/**
		 * Creates a new row for the player specified
		 *
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
			playerPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
			playerPanel.add(nameLabel, BorderLayout.LINE_START);
			playerPanel.add(timeLabel, BorderLayout.LINE_END);


			add(playerPanel, BorderLayout.CENTER);

			setEnabledLabels(false);
		}

		/**
		 * Loads the white and black icons for the theme specified. Note that this method depends on the value
		 * of {@link #isWhite}
		 *
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
			timeLabel.setText(formatTime(millis));
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
