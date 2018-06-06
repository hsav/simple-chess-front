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
import java.io.File;

/**
 * <p>Custom swing component to select a file.</p>
 * <p>The component is a combination of anon-editable {@link JTextField} and icons that allow either to open
 * a {@link JFileChooser} to select the file or clear the field.</p>
 */
public class JFileField extends JPanel {

	/** The text box field that displays the currently selected file's path (if any) */
	private final JTextField textField;

	/** Button that invokes a file chooser to select the desired file */
	private final JButton selectFolderButton;

	/** Button to clear the {@link #textField} */
	private final JButton clearButton;

	/**
	 * The currently selected {@code File} (if any)
	 */
	private File file;

	/** If the component should select a directory instead of a simple file (default is {@code false} */
	private final boolean directorySelection;

	/** The title of the file chooser dialog */
	private String fileChooserDialogTitle;

	/** The current directory that should be set to the file chooser */
	private File fileChooserCurrentDirectory;

	public JFileField() {
		this(null, false);
	}

	@SuppressWarnings("unused")
	public JFileField(boolean isDirectory) {
		this(null, isDirectory);
	}
	
	@SuppressWarnings("unused")
	public JFileField(String filePath) {
		this(new File(filePath), false);
	}
	
	@SuppressWarnings("WeakerAccess")
	public JFileField(File f, boolean isDirectory) {
		this.file = f;
		this.directorySelection = isDirectory;

		// set empty border and a border layout
		setBorder(BorderFactory.createEmptyBorder());
		setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));

		// create the text field and make it non-editable. This field always contains a valid File or null
		textField = new JTextField();
		textField.setEditable(false);

		// the select button opens a JFileChooser for the user to select the file/folder
		selectFolderButton = new JButton();

		// cancel button just clears the field
		clearButton = new JButton();

		// place the buttons in a toolbar
		JToolBar toolBar = new JToolBar();
		toolBar.setBorderPainted(true);
		toolBar.setFloatable(false);
		toolBar.setFocusable(false);
		toolBar.setLayout(new GridLayout(1, 2));
		toolBar.add(selectFolderButton);
		toolBar.add(clearButton);

		// add the text field and the toolbar to the main panel
		add(textField);
		add(toolBar);


		if (this.file != null) {
			if ((isDirectory && ! file.isDirectory()) || (!isDirectory && file.isDirectory()) ) {
				throw new IllegalArgumentException("File " + file);
			}
		}
		
		selectFolderButton.addActionListener(event -> {
			// open dialog and select the file
			if (selectFolderButton.isEnabled()) {
				chooseFile();
			}
		});
		
		clearButton.addActionListener(event -> {
			// clear the file
			if (clearButton.isEnabled()) {
				setFile((String) null);
			}
		});
	}

	@Override
	public void setEnabled(boolean value) {
		super.setEnabled(value);
		if (value) {
			selectFolderButton.setEnabled(true);
			clearButton.setEnabled(true);
		} else {
			selectFolderButton.setEnabled(false);
			clearButton.setEnabled(false);
		}
	}

	public void setSelectButtonName(String name) {
		selectFolderButton.setName(name);
	}

	public File getFile() {
		return file;
	}

	public void setFile(File file) {
		this.file = file;
		textField.setText( getFilePath(file) );
	}

	public void setFile(String filePath) {
		this.file = (filePath == null || filePath.length() == 0) ? null : new File(filePath);
		textField.setText( getFilePath(file) );
	}

	public void setFileChooserDialogTitle(String fileChooserDialogTitle) {
		this.fileChooserDialogTitle = fileChooserDialogTitle;
	}

	public void setFileChooserCurrentDirectory(File fileChooserCurrentDirectory) {
		this.fileChooserCurrentDirectory = fileChooserCurrentDirectory;
	}

	public void setColumns(int columns) {
		this.textField.setColumns(columns);
	}

	public void setSelectButtonIcon(ImageIcon icon) {
		this.selectFolderButton.setIcon(icon);
	}

	public void setClearButtonIcon(ImageIcon icon) {
		this.clearButton.setIcon(icon);
	}

	private void chooseFile() {
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setFileSelectionMode(directorySelection ? JFileChooser.DIRECTORIES_ONLY : JFileChooser.FILES_ONLY);
		fileChooser.setDialogTitle(fileChooserDialogTitle);
		
		if (file != null) {
			File folder = file.isDirectory() ? file : file.getParentFile();
			fileChooser.setCurrentDirectory(folder);
		} else {
			fileChooser.setCurrentDirectory( fileChooserCurrentDirectory != null ? fileChooserCurrentDirectory : new File(System.getProperty("user.home")));
		}
		if (fileChooser.showOpenDialog(JFileField.this) == JFileChooser.APPROVE_OPTION) {
			setFile(fileChooser.getSelectedFile());
		}
	}

	private static String getFilePath(File f) {
		return f == null ? null : f.getAbsolutePath();
	}

}
