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

package com.lowbudget.chess.front.swing.components.forms;

import com.lowbudget.chess.front.app.UIApplication;
import com.lowbudget.chess.front.app.config.GameSetup;
import com.lowbudget.chess.front.app.config.PlayerSetup;
import com.lowbudget.chess.front.app.Player;
import com.lowbudget.common.Messages;
import com.lowbudget.chess.model.uci.engine.UCIEngineConfig;
import com.lowbudget.chess.front.swing.ComponentNames;
import com.lowbudget.chess.front.swing.ComponentNames.DialogNewGame;
import com.lowbudget.chess.front.swing.common.FormDialog;
import com.lowbudget.chess.model.ChessConstants;
import com.lowbudget.chess.model.PlayerColor;
import com.lowbudget.chess.front.swing.components.forms.DialogHelper.EngineComboBoxModel;
import com.lowbudget.chess.front.swing.components.forms.DialogHelper.EngineListCellRenderer;

import javax.swing.*;
import javax.swing.text.DefaultFormatterFactory;
import java.awt.*;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static com.lowbudget.chess.front.swing.common.TimeStringFormatter.formatTime;
import static com.lowbudget.chess.front.swing.components.forms.DialogHelper.createBorderlessRadio;

/**
 * <p>Dialog to setup a new chess game</p>
 */
public class NewGameDialog extends ApplicationFormDialog {

	/* Message keys used by this class */
	private static final String KEY_DIALOG_TITLE = "app.dialog.new.game.title";
	private static final String KEY_HUMAN_RADIO = "app.dialog.new.game.player.type.human";
	private static final String KEY_ENGINE_RADIO = "app.dialog.new.game.player.type.engine";
	private static final String KEY_TIME_AVAILABLE = "app.dialog.new.game.player.time.available";
	private static final String KEY_WHITE_PANEL_TITLE = "app.dialog.new.game.player.white.title";
	private static final String KEY_BLACK_PANEL_TITLE = "app.dialog.new.game.player.black.title";
	private static final String KEY_USE_RANDOM_PLAYER_COLORS_LABEL = "app.dialog.new.game.use.random.player.colors";
	private static final String KEY_USE_EXISTING_BOARD_LABEL = "app.dialog.new.game.use.existing.board.label";
	private static final String KEY_USE_INITIAL_BOARD_LABEL = "app.dialog.new.game.use.initial.board.label";
	private static final String KEY_USE_SETUP_BOARD_LABEL = "app.dialog.new.game.use.setup.board.label";
	private static final String KEY_VALIDATION_ERROR_TITLE = "app.dialog.new.game.validation.error.title";
	private static final String KEY_VALIDATION_ERROR_NAME = "app.dialog.new.game.validation.error.name";
	private static final String KEY_VALIDATION_ERROR_ENGINE = "app.dialog.new.game.validation.error.engine";
	private static final String KEY_VALIDATION_ERROR_TIME = "app.dialog.new.game.validation.error.time";
	private static final String KEY_BUTTON_OK = "app.button.ok";
	private static final String KEY_BUTTON_CANCEL = "app.button.cancel";

	/** Stores the components of the white's player panel */
	private final PlayerPanel whitePanel;

	/** Stores the components of the black's player panel */
	private final PlayerPanel blackPanel;

	/**
	 * Indicates if there was an existing board from a previous game when this dialog was opened.
	 * In that case we display an additional radio to allow the user to start the game using that position
	 */
	private final boolean gameBoardExists;

	/**
	 * Indicates if there was a valid board in the setup screen when this dialog was opened.
	 * In that case we display an additional radio to allow the user to start the game using that position
	 */
	private final boolean setupBoardExists;

	/** A radio that allows the user to use a position from the last played game */
	private final JRadioButton useExistingGameBoard;

	/** A radio that allows the user to use a position specified via setup board */
	private final JRadioButton useSetupBoard;

	/** A radio that allows the user to use the standard initial board position (default) */
	private final JRadioButton useInitialBoard;

	/**	A check box that allows the user to select random colors to the players specified */
	private final JCheckBox useRandomPlayerColors;

	public NewGameDialog(UIApplication application) {
		super(application, Messages.get(KEY_DIALOG_TITLE));

		// create white and black panels. Each panel contains the same type of components and provides the same
		// behaviour e.g. when the "Human" radio is selected the engines combo box is disabled and the name text field
		// is enabled and vice-versa
		whitePanel = new PlayerPanel(application, KEY_WHITE_PANEL_TITLE, true);
		blackPanel = new PlayerPanel(application, KEY_BLACK_PANEL_TITLE, false);

		useExistingGameBoard = new JRadioButton(Messages.get(KEY_USE_EXISTING_BOARD_LABEL));
		useExistingGameBoard.setName(DialogNewGame.USE_GAME_POSITION);

		useSetupBoard = new JRadioButton(Messages.get(KEY_USE_SETUP_BOARD_LABEL));
		useSetupBoard.setName(DialogNewGame.USE_SETUP_POSITION);

		useInitialBoard = new JRadioButton(Messages.get(KEY_USE_INITIAL_BOARD_LABEL));

		ButtonGroup buttonGroup = new ButtonGroup();
		buttonGroup.add(useExistingGameBoard);
		buttonGroup.add(useInitialBoard);
		buttonGroup.add(useSetupBoard);

		gameBoardExists = application.getGameModel().hasBoard();

		// we currently allow any board position even the two kings alone. Engines do not have a problem with that,
		// however a more reasonable approach might be to check if there is sufficient material
		setupBoardExists = true;

		useRandomPlayerColors = new JCheckBox(Messages.get(KEY_USE_RANDOM_PLAYER_COLORS_LABEL));
		useRandomPlayerColors.addActionListener( event -> updatePlayerNamesForRandomColor() );

		// Set the labels and names for ok and cancel buttons
		setOkButtonLabel(Messages.get(KEY_BUTTON_OK));
		setCancelButtonLabel(Messages.get(KEY_BUTTON_CANCEL));
		setOkButtonComponentName(ComponentNames.OK_BUTTON);
		setCancelButtonComponentName(ComponentNames.CANCEL_BUTTON);
	}

	/**
	 * Updates the titles of the players' {@link JPanel}s depending on the value of {@link #useRandomPlayerColors} check box
	 */
	private void updatePlayerNamesForRandomColor() {
		boolean isRandom = useRandomPlayerColors.isSelected();
		whitePanel.setPlayerTitle(isRandom);
		blackPanel.setPlayerTitle(isRandom);
	}

	@Override
	protected  void populateForm() {
		if (application.isSetupMode()) {
			useSetupBoard.setSelected(true);
		} else {
			useInitialBoard.setSelected(true);
		}

		GameSetup lastGameSetup = application.getLastGameSetup();

		if (lastGameSetup != null) {
			whitePanel.set(lastGameSetup.getWhiteSetup());
			blackPanel.set(lastGameSetup.getBlackSetup());
			useRandomPlayerColors.setSelected(lastGameSetup.isRandomColor());
			updatePlayerNamesForRandomColor();
		}
	}

	/**
	 * Override parent behaviour and create our own top panel, without using {@link FormDialog.Row}s
	 * or the {@link FormDialog.RowsFactory}.
	 * @return the top panel of the dialog (i.e. the whole area excluding the buttons at the bottom). This panel has
	 * three sub-panels one for the white information, one for the black information and one for the game options
	 */
	@Override
	protected JPanel createTopPanel() {
		// Our main panel leaves some space around the borders
		JPanel topPanel = new JPanel(new GridLayout(3, 1, 10, 10));
		topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

		topPanel.add( whitePanel.panel );
		topPanel.add( blackPanel.panel );
		topPanel.add( createOptionsPanel() );
		return topPanel;
	}

	/**
	 * Creates the options panel at the bottom with the various options on how to start the new game
	 * @return the newly created options panel
	 */
	private JPanel createOptionsPanel() {
		JPanel optionsPanel = new JPanel();
		optionsPanel.setBorder(BorderFactory.createTitledBorder("Options"));
		optionsPanel.setLayout(new BoxLayout(optionsPanel, BoxLayout.PAGE_AXIS) );

		optionsPanel.add(useInitialBoard);
		optionsPanel.add(useSetupBoard);
		optionsPanel.add(useExistingGameBoard);
		optionsPanel.add(useRandomPlayerColors);

		// if there was a board of a previous game when this dialog opened allow the user to select it
		useExistingGameBoard.setEnabled(gameBoardExists);

		// if there was a valid board in setup mode when this dialog opened allow the user to select it
		useSetupBoard.setEnabled(setupBoardExists);

		return optionsPanel;
	}

	@Override
	protected FormDialog.ValidationResult validateForm() {
		FormDialog.ValidationResult result = whitePanel.validate();
		if (result.isSuccess()) {
			result = blackPanel.validate();
		}
		return result;
	}

	@Override
	protected void onValidationError(FormDialog.ValidationResult validationResult) {
		application.showValidationFailedWarningMessage(KEY_VALIDATION_ERROR_TITLE, validationResult.getError());
	}

	/**
	 * @return a {@link GameSetup} object that contains the necessary information to setup the game parameters for
	 * the two players
	 */
	public GameSetup getGameSetup() {
		PlayerSetup whiteSetup = whitePanel.createPlayerSetup(PlayerColor.WHITE);
		PlayerSetup blackSetup = blackPanel.createPlayerSetup(PlayerColor.BLACK);

		// decide the fen position to be used for the new game
		String fen = ChessConstants.INITIAL_FEN;
		if (gameBoardExists && useExistingGameBoard.isSelected()) {
			fen = application.getGameModel().getFenPosition();
		} else if (setupBoardExists && useSetupBoard.isSelected()) {
			fen = application.getSetupBoardModel().getFenPosition();
		}
		return new GameSetup(whiteSetup, blackSetup, fen, useRandomPlayerColors.isSelected());
	}

	/**
	 * Helper class that stores the components of each player's panel
	 */
	private static class PlayerPanel {
		/** Radio that indicates that the player is human. When this is selected the user needs to provide the player's name */
		final JRadioButton humanRadio;

		/** Radio that indicates that the player is an engine. When this is selected the user needs to select one of the available engines */
		final JRadioButton engineRadio;

		/** The player's name in case the player is of human type */
		final JTextField nameField;

		/** The selected engine config in case the player is of engine type */
		final JComboBox<UCIEngineConfig> engineConfigsCombo;

		/** The time available for this player */
		final JSpinner time;

		/** {@code true} when this panel represents the first player, {@code false} otherwise */
		final boolean firstPlayer;

		final String playerTitle;

		/** The {@link JPanel} represented by this class */
		final JPanel panel;

		PlayerPanel(UIApplication application, String panelTitleKey, boolean firstPlayer) {
			this.firstPlayer = firstPlayer;
			this.playerTitle = Messages.get(panelTitleKey);

			// component names are required for our swing tests
			String humanRadioName = firstPlayer ? DialogNewGame.FIRST_PLAYER_HUMAN_RADIO : DialogNewGame.SECOND_PLAYER_HUMAN_RADIO;
			String engineRadioName = firstPlayer ? DialogNewGame.FIRST_PLAYER_ENGINE_RADIO : DialogNewGame.SECOND_PLAYER_ENGINE_RADIO;
			String textFieldName = firstPlayer ? DialogNewGame.FIRST_PLAYER_NAME : DialogNewGame.SECOND_PLAYER_NAME;
			String engineComboName = firstPlayer ? DialogNewGame.FIRST_PLAYER_ENGINE_COMBO : DialogNewGame.SECOND_PLAYER_ENGINE_COMBO;
			String timeName = firstPlayer ? DialogNewGame.FIRST_PLAYER_TIME : DialogNewGame.SECOND_PLAYER_TIME;

			// create the radios with the specified title, selected status and assigned action listener
			// Each radio is configured to have an empty border for better alignment with the rest of the components
			humanRadio = createBorderlessRadio(humanRadioName, Messages.get(KEY_HUMAN_RADIO), true, event -> updateEnabledFieldsForRadioSelection() );
			engineRadio = createBorderlessRadio(engineRadioName, Messages.get(KEY_ENGINE_RADIO), false, event -> updateEnabledFieldsForRadioSelection() );

			// The two radios belong to the same group (so only one of them can be selected)
			ButtonGroup group = new ButtonGroup();
			group.add(humanRadio);
			group.add(engineRadio);

			// player's name (in case of a human player)
			nameField = new JTextField();
			nameField.setName(textFieldName);

			// Combo box with available engines. The combo box uses a custom renderer to display only the engine's name in the list
			engineConfigsCombo = new JComboBox<>( new EngineComboBoxModel(application) );
			engineConfigsCombo.setRenderer( new EngineListCellRenderer() );
			engineConfigsCombo.setName(engineComboName);

			// A custom spinner that allows to provide the available time in the "00:00:00" format
			time = new JSpinner( new SpinnerNumberModel(0L, (Comparable) 0L, Long.MAX_VALUE, 1000L) );
			time.setEditor(new TimeControlEditor(time));
			time.setName(timeName);

			// create the panel
			panel = FormDialog.createFieldsPanel(Arrays.asList(
					FormDialog.Row.with(humanRadio).andComponent(nameField),
					FormDialog.Row.with(engineRadio).andComponent(engineConfigsCombo),
					FormDialog.Row.withLabel(Messages.get(KEY_TIME_AVAILABLE)).andComponent(time)
			));
			setPlayerTitle(false);

			// update the status of the components to agree with the default selection of the human type
			updateEnabledFieldsForRadioSelection();
		}

		void setPlayerTitle(boolean isRandom) {
			panel.setBorder(BorderFactory.createTitledBorder( !isRandom ? playerTitle : Messages.get("app.dialog.new.game.player.unknown.title")) );
		}

		/**
		 * Creates a {@link PlayerSetup} object that contains the configuration extracted from this panel
		 * @param color the color of the player this panel represents (i.e. white or black)
		 * @return a configured {@link PlayerSetup} object that can be used to create a new {@link Player}
		 */
		@SuppressWarnings("SpellCheckingInspection")
		PlayerSetup createPlayerSetup(PlayerColor color) {
			if (humanRadio.isSelected()) {
				return new PlayerSetup(nameField.getText(), color, getTime());
			}
			UCIEngineConfig engineConfig = (UCIEngineConfig) engineConfigsCombo.getSelectedItem();
			Objects.requireNonNull(engineConfig);
			return new PlayerSetup(engineConfig.getName(), color, engineConfig.getParams(), getTime());
		}

		/**
		 * Sets the panel from the player setup specified
		 * @param playerSetup the player setup that should be used to initialize this panel
		 */
		void set(PlayerSetup playerSetup) {
			if (playerSetup.isHuman()) {
				nameField.setText(playerSetup.getName());
			} else {
				// create a dummy engine object with the desired name and connection params
				UCIEngineConfig dummy = new UCIEngineConfig(playerSetup.getConnectParams());
				dummy.setName(playerSetup.getName());

				EngineComboBoxModel model = (EngineComboBoxModel) engineConfigsCombo.getModel();

				// find the index of the item - note that this takes advantage of the UCIEngineConfig.equals() method
				int itemIndex = model.indexOf(dummy);
				// set the desired selected index. The selected UCIEngineConfig object will have all its options
				// initialized unlike our dummy object
				engineConfigsCombo.setSelectedIndex(itemIndex);
			}
			humanRadio.setSelected(playerSetup.isHuman());
			engineRadio.setSelected(!playerSetup.isHuman());
			updateEnabledFieldsForRadioSelection();

			time.setValue(playerSetup.getTime());
		}

		/**
		 * Validates the fields of this panel.
		 * @return {@link FormDialog.ValidationResult#VALIDATION_OK if all the fields of the panel are valid, otherwise it returns
		 * the appropriate {@link FormDialog.ValidationResult } which includes the error message
		 */
		FormDialog.ValidationResult validate() {
			// the title of the player the error is about (use terminology similar to first/second player, avoid the
			// terms "white"/"black", since when random color is selected we do not know the color of each player)
			String playerTitle = Messages.get(firstPlayer ? "app.dialog.new.game.player.first.title" : "app.dialog.new.game.player.second.title");

			if (humanRadio.isSelected() && nameField.getText().isEmpty()) {
				return new FormDialog.ValidationResult(false, Messages.get(KEY_VALIDATION_ERROR_NAME, playerTitle));
			}
			if (engineRadio.isSelected() && engineConfigsCombo.getSelectedItem() == null) {
				return new FormDialog.ValidationResult(false, Messages.get(KEY_VALIDATION_ERROR_ENGINE, playerTitle));
			}

			Number whiteTime = (Number) time.getValue();
			if (whiteTime.longValue() <= 0) {
				return new FormDialog.ValidationResult(false, Messages.get(KEY_VALIDATION_ERROR_TIME, playerTitle));
			}
			return FormDialog.ValidationResult.VALIDATION_OK;
		}

		/**
		 * Returns the time in millis represented by the {@link #time} spinner
		 */
		long getTime() {
			Number numberValue = (Number) time.getValue();
			return numberValue.longValue();
		}

		/**
		 * Updates the enabled status for the name and the combo fields based on the selected radio (either the
		 * {@link #humanRadio} or {@link #engineRadio})
		 */
		private void updateEnabledFieldsForRadioSelection() {
			nameField.setEnabled(humanRadio.isSelected());
			engineConfigsCombo.setEnabled(engineRadio.isSelected());
		}
	}

	/**
	 * A custom editor for a {@link JSpinner} that allows to enter time information in the format of "00:00:00".
	 */
	private static class TimeControlEditor extends JSpinner.DefaultEditor {

		/** This editor only knows how to handle numbers */
		private final SpinnerNumberModel model;

		TimeControlEditor(JSpinner spinner) {
			super(spinner);
			if (!(spinner.getModel() instanceof SpinnerNumberModel)) {
				throw new IllegalArgumentException("model not a SpinnerNumberModel");
			}
			model = (SpinnerNumberModel) spinner.getModel();
			getTextField().setEditable(true);
			getTextField().setHorizontalAlignment(JTextField.RIGHT);	// align the text on the right
			getTextField().setFormatterFactory(new DefaultFormatterFactory(new TimeControlFormatter(model)));	// use our custom formatter
		}

		/**
		 * Custom formatter for the text field. It converts the value from string to number and from number to string
		 */
		private static class TimeControlFormatter extends JFormattedTextField.AbstractFormatter {
			private final SpinnerNumberModel model;

			TimeControlFormatter(SpinnerNumberModel model) {
				this.model = model;
			}

			@Override
			public Object stringToValue(String text) {
				// invalid/empty text return 0
				if (text == null || text.isEmpty()) {
					return 0L;
				}
				// we expect 3 parts - if not return current value (i.e. ignore the text)
				String[] parts = text.split(":");
				if (parts.length != 3) {
					return model.getValue();
				}

				// if the value is not parsable again ignore the value and return the model's current value
				try {
					long hours = Integer.parseInt(parts[0]);
					long minutes = Integer.parseInt(parts[1]);
					long seconds = Integer.parseInt(parts[2]);

					return TimeUnit.HOURS.toMillis(hours) + TimeUnit.MINUTES.toMillis(minutes) + TimeUnit.SECONDS.toMillis(seconds);
				} catch (NumberFormatException e) {
					return model.getValue();
				}
			}

			@Override
			public String valueToString(Object value) {
				if (value == null) {
					return "";
				}
				if ( ! (value instanceof Number) ) {
					throw new IllegalArgumentException("value is not a Number");
				}
				return formatTime( ((Number) value).longValue() );
			}
		}
	}
}
