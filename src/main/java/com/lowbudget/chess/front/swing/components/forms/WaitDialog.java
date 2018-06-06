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

import com.lowbudget.common.Messages;

import javax.swing.*;
import java.awt.*;

/**
 * <p>Displays a waiting dialog with a title, a message and an indeterminant progress bar.</p>
 * <p>The dialog stays open while we wait for an operation to finish possibly informing the user of what
 * is happening.</p>
 */
public class WaitDialog extends JDialog {

	public WaitDialog(String titleKey, String messageKey) {
		setTitle(Messages.get(titleKey));

		setModal(true);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		setMinimumSize(new Dimension(300, 50));

		JPanel mainPanel = new JPanel();
		// use a grid layout with 3 rows and 1 column to vertically center the components. First row contains the
		// label with the dialog's message, second row contains the progress bar and the third row is empty to leave
		// some space from the bottom of the dialog.
		// Note: if we use 2 rows only, the progress bar has a slightly larger height
		mainPanel.setLayout( new GridLayout(3, 1) );

		JLabel label = new JLabel(Messages.get(messageKey));
		label.setHorizontalAlignment(SwingConstants.CENTER);
		mainPanel.add(label);

		// do not add the progress bar directly but use an internal panel with an empty border. This way the
		// progress bar leaves some space from the dialog edges
		JPanel progressPanel = new JPanel();
		progressPanel.setLayout(new GridLayout());
		JProgressBar progressBar = new JProgressBar();
		progressBar.setIndeterminate(true);
		progressPanel.add(progressBar);
		progressPanel.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));
		mainPanel.add(progressPanel);

		add(mainPanel);

		pack();
		setResizable(false);
		setLocationRelativeTo(null);
	}
}
