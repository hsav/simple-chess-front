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

package com.lowbudget.chess.front.swing.common;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * <p>A base dialog class that uses a two-column layout with labels on the left and fields on the right.</p>
 * <p>It additionally uses two buttons that utilize "ok" and "cancel" functionality providing the necessary hooks
 * for descendants to plug-in dialog specific behaviour.</p>
 * <p>The expected life-cycle of the dialog is the following:</p>
 * <ol>
 *     <li>Dialog creation: It is expected to provide a {@link RowsFactory} for the rows with components for the first
 *     column (that are usually {@link JLabel}s) and components for the second column. If you don't wish to use the
 *     two-column layout with rows you should override the {@link #createTopPanel()} and fully create the dialog's
 *     content (i.e. everything else beside the buttons).
 *     You will also probably want to set the labels of the buttons by calling {@link #setOkButtonLabel(String)} and
 *     {@link #setCancelButtonLabel(String)}. If additional buttons are needed you can override {@link #createExtraButtons()}.
 *     If on the other hand you need to hide the ok or cancel buttons you can use {@link #setDisplayOkButton(boolean)}
 *     and {@link #setDisplayCancelButton(boolean)}</li>
 *     <li>Dialog display: When {@link #setVisible(boolean) setVisible(true)} is called for the first time, the various
 *     fields will be created. Before the dialog is displayed the {@link #populateForm()} will be called which can be
 *     overridden to set the initial values of the fields.</li>
 *     <li>Dialog cancellation: If the user clicks the cancel button the dialog will be closed and any changes in the
 *     fields' values will be lost. In case you need to perform an additional action before cancellation you can
 *     override {@link #onDialogCancelled()}</li>
 *     <li>Dialog acceptance: If the user clicks the ok button then {@link #validateForm()} will be called. Depending
 *     on its result the following methods will be called:
 *     <ul>
 *         <li>If the {@link ValidationResult} reports no error then {@link #onValidationSuccess()} will be called.
 *         You can override this method to persist any changes made to the fields before the dialog is closed.</li>
 *         <li>If the {@link ValidationResult} reports an error then {@link #onValidationError(ValidationResult)}
 *         will be called and the dialog will stay opened. You can override this method to report the error to the user
 *         i.e. display a warning message
 *     </ul>
 *     </li>
 * </ol>
 */
@SuppressWarnings("WeakerAccess")
public class FormDialog extends JDialog {

	/**
	 * Creates the components used by the first and second columns
	 */
	@FunctionalInterface
	public interface RowsFactory {
		List<Row> createRows();
	}

	/**
	 * <p>Holder class that groups a label and its corresponding component that reside in the same row.</p>
	 * <p>Note: while we use the term "label" for the first component it does not actually have to be a {@link JLabel}
	 * it can be any component.</p>
	 */
	@SuppressWarnings("WeakerAccess")
	public static class Row {
		private final Component label;
		private final Component component;

		private Row(Component label, Component component) {
			this.label = label;
			this.component = component;
		}

		public Component getLabel() {
			return label;
		}

		public Component getComponent() {
			return component;
		}

		public static Builder withLabel(String label) {
			return new Builder(label);
		}
		public static Builder with(Component component) {
			return new Builder(component);
		}

		@SuppressWarnings("unused")
		public static class Builder {
			private final Component label;

			private Builder(String label) {
				this.label = new JLabel(label);
			}
			private Builder(Component component) {
				this.label = component;
			}

			public Row andLabel(String label) {
				return new Row(this.label, new JLabel(label));
			}
			public Row andTextField() {
				return new Row(this.label, new JTextField());
			}
			public Row andNamedTextField(String componentName) {
				JTextField textField = new JTextField();
				textField.setName(componentName);
				return new Row(this.label, textField);
			}

			public Row andComponent(Component component) {
				return new Row(this.label, component);
			}
		}

	}

	/**
	 * Encapsulates the result of a validation. Descendants can extend this class to provide additional
	 * information about the error in case this is not enough
	 */
	@SuppressWarnings("WeakerAccess")
	public static class ValidationResult {
		// when the validation succeeds there is no need to return a different instance
		public static final ValidationResult VALIDATION_OK = new ValidationResult(true);

		// Denotes if this is a successful validation result
		private final boolean success;

		// The error message (if any)
		private final String error;

		public ValidationResult(boolean success) {
			this(success, null);
		}

		public ValidationResult(boolean success, String error) {
			this.success = success;
			this.error = error;
		}

		public boolean isSuccess() {
			return success;
		}

		public String getError() {
			return error;
		}
	}

	// we set the button names to key-like names to draw the developer's attention in case we forget to set them
	// to more meaningful values
    private final JButton okButton = new JButton("button.ok");
    private final JButton cancelButton = new JButton("button.cancel");

	/**
	 * Indicates if this dialog was cancelled
	 */
	private boolean cancelled = false;

	/**
	 * Indicates if this dialog was initialized
	 */
	private boolean initialized = false;

	/**
	 * The factory to create the rows with the labels and fields of our layout
	 */
	private RowsFactory rowsFactory;

	/**
	 * Indicates if the ok button should be displayed. If another value is desired, it must be set
	 * before displaying the dialog
	 */
	private boolean displayOkButton = true;

	/**
	 * Indicates if the cancel button should be displayed. If another value is desired, it must be set
	 * before displaying the dialog
	 */
	private boolean displayCancelButton = true;


	/**
	 * Creates a new dialog with the title specified
	 * @param title the dialog's title
	 */
    public FormDialog(String title) {
        setTitle(title);
        setModal(true);
        setResizable(false);
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

		cancelButton.addActionListener( (event) -> cancelDialog() );
		okButton.addActionListener( (event) -> acceptDialog() );

		// add a window listener. If the user closes the window we treat this the same as cancelling the dialog
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				cancelDialog();
			}
		});
	}

	/**
	 * Creates a new dialog with the title and the rows' factory specified
	 * @param title the dialog's title
	 * @param rowsFactory the factory that creates the rows with the components of the first and second columns
	 */
	@SuppressWarnings("unused")
	public FormDialog(String title, RowsFactory rowsFactory) {
    	this(title);
    	this.rowsFactory = rowsFactory;
	}

	/**
	 * <p>Displays/hides the dialog.</p>
	 * <p>The first time this method is called with a parameter of {@code true} it will invoke {@link #prepareDialog()}.</p>
	 * @param value if the dialog should be visible or hidden
	 */
    @Override
    public void setVisible(boolean value) {
    	if (value && !initialized) {
			prepareDialog();
			initialized = true;
		}
    	super.setVisible(value);
	}

	/**
	 * <p>Specifies if the dialog was cancelled.</p>
	 * @return {@code true} if the dialog was cancelled, {@code false} otherwise
	 */
	@SuppressWarnings({"unused", "BooleanMethodIsAlwaysInverted"})
	public boolean isCancelled() {
		return cancelled;
	}

	/**
	 * Sets the label of the ok button
	 * @param label the new label to set
	 */
	public void setOkButtonLabel(String label) {
    	okButton.setText(label);
	}

	/**
	 * Sets the label of the cancel button
	 * @param label the new label to set
	 */
	public void setCancelButtonLabel(String label) {
		cancelButton.setText(label);
	}

	/**
	 * Sets the component name of ok button (required for AssertJ-swing tests)
	 * @param name the name to set
	 */
	public void setOkButtonComponentName(String name) {
		okButton.setName(name);
	}

	/**
	 * Sets the component name of the cancel button (required for AssertJ-swing tests)
	 * @param name the name to set
	 */
	public void setCancelButtonComponentName(String name) {
		cancelButton.setName(name);
	}

	/**
	 * Sets the {@link RowsFactory} to use for the rows of the layout
	 * @param rowsFactory the factory to use
	 */
	public void setRowsFactory(RowsFactory rowsFactory) {
		this.rowsFactory = rowsFactory;
	}

	/**
	 * @return {@code true} if the ok button is configured to be displayed, {@code false} otherwise
	 */
	public boolean isDisplayOkButton() {
		return displayOkButton;
	}

	/**
	 * Controls the display of the ok button. If set, this should be called before displaying the dialog
	 * @param displayOkButton {@code true} if the ok button should be displayed, {@code false} otherwise
	 */
	public void setDisplayOkButton(boolean displayOkButton) {
		this.displayOkButton = displayOkButton;
	}

	/**
	 * @return {@code true} if the cancel button is configured to be displayed, {@code false} otherwise
	 */
	public boolean isDisplayCancelButton() {
		return displayCancelButton;
	}

	/**
	 * Controls the display of the cancel button. If set, this should be called before displaying the dialog
	 * @param displayCancelButton {@code true} if the cancel button should be displayed, {@code false} otherwise
	 */
	@SuppressWarnings("unused")
	public void setDisplayCancelButton(boolean displayCancelButton) {
		this.displayCancelButton = displayCancelButton;
	}

	/**
	 * <p>Creates the top panel of the dialog.</p>
	 * <p>The default implementation here uses a panel with a two-column layout however descendants may override this
	 * method to create any panel they desire and keep the buttons at the bottom of the dialog as long as the rest of
	 * functionality for cancelling, validating etc.</p>
	 * @return the dialog that will be used as the top panel of the dialog (i.e. the one with all fields without the buttons)
	 */
	protected JPanel createTopPanel() {
		// check that we have the factory assigned before proceeding
		Objects.requireNonNull(rowsFactory, "Rows factory is required");

		return createFieldsPanel(rowsFactory.createRows());
	}

	/**
	 * <p>Validates the current values of the fields of the form.</p>
	 * <p>Descendants are expected to override this method to perform meaningful validation. If the validation is
	 * successful then the method {@link ValidationResult#isSuccess()} must return {@code true}, otherwise it must
	 * return {@code false}.</p>
	 * @return {@link ValidationResult} the result of the validation
	 */
	protected ValidationResult validateForm() {
		return ValidationResult.VALIDATION_OK;
	}

	/**
	 * <p>Populates all the fields in the form with their initial values.</p>
	 * <p>This method is called after all the fields are created but before the dialog is displayed.</p>
	 * <p>Descendants are expected to override this method to populate the form's fields.</p>
	 */
	protected  void populateForm() {
	}

	/**
	 * <p>Called after the validation is successful but before the dialog is closed.</p>
	 * <p>Descendants are expected to override this method to use the new values of the fields in some way i.e.
	 * possibly to persist them.</p>
	 */
	protected void onValidationSuccess() {
	}

	/**
	 * <p>Called after the validation fails.</p>
	 * <p>When the validation fails the dialog is not closed so the user can fix any errors in the form.</p>
	 * <p>Descendants are expected to override this method to respond to the user possibly by displaying a warning
	 * message.</p>
	 * @param validationResult the {@link ValidationResult} returned from {@link #validateForm()}.
	 */
	protected void onValidationError(ValidationResult validationResult) {
	}

	/**
	 * <p>Called when the user clicked at the cancel button (or closed the modal dialog) before the dialog is closed.</p>
	 * <p>Descendants may override this method in case they need to perform an action when the dialog is cancelled.</p>
	 * <p>Note however that any action executed here should not prevent the dialog from closing.</p>
	 */
	protected void onDialogCancelled() {
	}

	/**
	 * <p>Creates extra buttons to be displayed along with the ok and cancel buttons at the bottom of the form.</p>
	 * <p>Descendants may override this method in case they need additional buttons i.e. a "reset" button.</p>
	 * @return an array of {@link JButton} that will be additionally displayed in the dialog. The buttons need to be
	 * fully configured with their corresponding actions etc.
	 */
	protected List<JButton> createExtraButtons() {
    	return new ArrayList<>();
	}

	/**
	 * <p>Cancels the dialog.</p>
	 * <p>This is the default handler of the cancel button and will perform the following operations:</p>
	 * <ol>
	 *     <li>invoke {@link #onDialogCancelled()} for the descendant to perform some action as a result of the cancellation.</li>
	 *     <li>set the {@link #cancelled} flag to {@code true}</li>
	 *     <li>hide and dispose the dialog</li>
	 * </ol>
	 */
	private void cancelDialog() {
    	onDialogCancelled();
		this.cancelled = true;
		closeDialog();
	}

	/**
	 * Hides and disposes the dialog
	 */
	private void closeDialog() {
		setVisible(false);
		dispose();
	}

	/**
	 * <p>Prepares the dialog for display.</p>
	 * <p>This is called on the first time {@link #setVisible(boolean)} is called with a value of {@code true}.</p>
	 * <p>The following operations will be performed:</p>
	 * <ol>
	 *     <li>The fields will be created using the assigned {@link #rowsFactory}.
	 *     During the field creation, {@link #createExtraButtons()} will be called to give the descendant a chance to
	 *     create extra buttons that should be displayed.</li>
	 *     <li>{@link #populateForm()} will be invoked so the descendant populates the various fields.</li>
	 * </ol>
	 */
	private void prepareDialog() {
		JPanel mainPanel = createDialogPanel();
		JScrollPane scroll = new JScrollPane();

		// the scroll pane has a preferred size of its viewport. In case there are many settings this can exceed the
		// size of the screen in the vertical direction, so we need to restrict the height (and possibly the width)
		// In order to do that we restrict the width and height to a percentage of the screen size plus some extra space
		// to accommodate for any scrollbars
		// If the main panel does not exceed these max values, its own preferred size will be used.
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		Dimension maxSize = new Dimension(Math.round(screenSize.width * 0.7f), Math.round(screenSize.height * 0.7f));
		Dimension currentPreferredSize = mainPanel.getPreferredSize();

		if (currentPreferredSize.width >= maxSize.width || currentPreferredSize.height >= maxSize.height) {

			// we are not interested in calculating the exact size of the scrollbar here, we just need to leave enough
			// space to avoid displaying the horizontal scrollbar just because the vertical scrollbar is shown
			// doubling its preferred width seems to do the trick

			// The same trick would not work quite as well for height though. If we define a height larger than needed
			// our components (i.e. text fields, combo boxes etc) would have an increased height and would not look natural
			JScrollBar verticalScrollBar = scroll.getVerticalScrollBar();
			int verticalScrollbarWidth = 2 * verticalScrollBar.getPreferredSize().width;

			JScrollBar horizontalScrollBar = scroll.getHorizontalScrollBar();
			int horizontalScrollbarHeight = horizontalScrollBar.getPreferredSize().height;

			Dimension scrollPreferredSize = new Dimension(
					Math.min(currentPreferredSize.width + verticalScrollbarWidth, maxSize.width),
					Math.min(currentPreferredSize.height + horizontalScrollbarHeight, maxSize.height));

			scroll.setMaximumSize(maxSize);
			scroll.setPreferredSize(scrollPreferredSize);
		}
		scroll.setViewportView(mainPanel);

		add(scroll);
		populateForm();
		pack();
		setLocationRelativeTo(null);
	}

	/**
	 * <p>The default handler of the ok button.</p>
	 * <p>It will invoke {@link #validateForm()} and depending on the {@link ValidationResult} it will call either
	 * {@link #onValidationSuccess()} after which the dialog will be closed or
	 * {@link #onValidationError(ValidationResult)} after which the dialog will remain opened.</p>
	 */
	private void acceptDialog() {
    	ValidationResult result = validateForm();
		if (result != null && result.isSuccess()) {
			onValidationSuccess();
			closeDialog();
		} else {
			onValidationError(result);
		}
	}

	/**
	 * <p>Creates the whole dialog's panel that contains the dialog's content.</p>
	 * <p>The dialog's panel in turn consists of two panels: the top panel, that contains any fields of the form and
	 * the bottom panel or "buttons" panel that contains the ok and cancel buttons as long as any extra buttons the
	 * descendants might add.</p>
	 * <p>The default implementation (implemented in {@link #createTopPanel()} uses a two-column layout.</p>
	 * @return the main {@link JPanel} that will be displayed as the content of the dialog
	 */
    private JPanel createDialogPanel() {
        // create a panel with two inner panels: the top panel that contain the fields (labels and components) and
		// the bottom panel that contain the buttons
        JPanel dialogPanel = new JPanel();
        dialogPanel.setLayout(new BoxLayout(dialogPanel, BoxLayout.PAGE_AXIS));

        JPanel topPanel = createTopPanel();
        dialogPanel.add(topPanel);

        List<JButton> buttons = createExtraButtons();
        if (isDisplayCancelButton()) {
			buttons.add(0, cancelButton);
		}
		if (isDisplayOkButton()) {
			buttons.add(0, okButton);
		}

        JPanel buttonPanel = createButtonsPanel(buttons);
		dialogPanel.add(buttonPanel);

        return dialogPanel;
    }

	/**
	 * <p>Creates a panel with two columns using a {@link GroupLayout}.</p>
	 * <p>The code is based on the Java tutorial: <a href="https://docs.oracle.com/javase/tutorial/uiswing/layout/group.html">How to Use GroupLayout</a></p>
	 * @param rows the rows with the labels and components that will be displayed for the first and second columns
	 * @return a {@link JPanel} where the labels and components are laid out in a two-column format.
	 */
    public static JPanel createFieldsPanel(List<Row> rows) {
		Objects.requireNonNull(rows);

        JPanel topPanel = new JPanel();
        GroupLayout layout = new GroupLayout(topPanel);
        topPanel.setLayout(layout);

        // Turn on automatically adding gaps between components
        layout.setAutoCreateGaps(true);

        // Turn on automatically creating gaps between components that touch
        // the edge of the container and the container.
        layout.setAutoCreateContainerGaps(true);

        GroupLayout.SequentialGroup hGroup = layout.createSequentialGroup();

        GroupLayout.ParallelGroup labelsGroup = layout.createParallelGroup();

        GroupLayout.ParallelGroup componentsGroup = layout.createParallelGroup();

		for (Row row : rows) {
			labelsGroup.addComponent(row.getLabel());
			componentsGroup.addComponent(row.getComponent());
		}
        hGroup.addGroup(labelsGroup);
        hGroup.addGroup(componentsGroup);
        layout.setHorizontalGroup(hGroup);

        GroupLayout.SequentialGroup vGroup = layout.createSequentialGroup();

		for (Row row : rows) {
			Component label = row.getLabel();
			Component component = row.getComponent();
			vGroup.addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER).addComponent(label).addComponent(component));
		}
        layout.setVerticalGroup(vGroup);
        return topPanel;
    }

	/**
	 * <p>Creates a buttons panel appropriate to be displayed at the bottom of a dialog.</p>
	 * <p>The buttons are positioned using some space between them and all have equal widths.</p>
	 * @param buttons a list of the {@link JButton}s that should be displayed.
	 * @return the {@link JPanel} that contains all the buttons.
	 */
	public static JPanel createButtonsPanel(List<JButton> buttons) {
		Objects.requireNonNull(buttons);

		JPanel buttonPanel = new JPanel();
		//buttonPanel.setBackground(Color.green);

		// use an internal panel to place the buttons. This internal panel will be centered horizontally
		JPanel internalPanel = new JPanel();
		//internalPanel.setBackground(Color.red);

		// use a grid layout so all the buttons have equal width
		int buttonCount = buttons.size();

		// We leave a fixed space of 10px between buttons (this might be problematic if the dialog becomes too wide)
		internalPanel.setLayout(new GridLayout(1, buttonCount, 10, 10));

		for (JButton button : buttons) {
			internalPanel.add(button);
		}
		buttonPanel.add(internalPanel);
		return buttonPanel;
	}

}
