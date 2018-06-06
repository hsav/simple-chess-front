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

/**
 * <p>A {@link JPanel} appropriate to display an error message caused by an exception.</p>
 * <p>The panel displays the stacktrace of an exception in a scrolling {@link JTextArea}.</p>
 * <p>You can display this panel in a dialog using the following code:</p>
 * <pre>
 *     Window window = ... // the window that owns the dialog as required by JOptionPane.showMessageDialog()
 *     try {
 *         // ... code that raises an exception
 *     } catch(Exception e) {
 *        JOptionPane.showMessageDialog(window, ErrorPanel.newErrorPanel(getStackTrace(e)), "Unexpected error", JOptionPane.ERROR_MESSAGE)
 *     }
 * </pre>
 */
@SuppressWarnings("unused")
public class ErrorPanel extends JPanel {

	/**
	 * <p>Creates a new error panel.</p>
	 * <p>The size of the panel is calculated as a ratio of the screen size</p>
	 * @param error the string message to display
	 * @param screenSizeRatioX the horizontal ratio. The resulting panel will have width equal to {@code screenSizeRatioX * screenWidth}
	 * @param screenSizeRatioY the vertical ratio. The resulting panel will have height equal to {@code screenSizeRatioY * screenHeight}
	 */
	private ErrorPanel(String error, double screenSizeRatioX, double screenSizeRatioY) {
		
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		
		int maxWidth = (int) Math.round( screenSize.getWidth() * screenSizeRatioX );
		int maxHeight = (int) Math.round( screenSize.getHeight() * screenSizeRatioY );
		Dimension maxDimension = new Dimension(maxWidth, maxHeight); 
		setMaximumSize(maxDimension);
		
		JTextArea textArea = new JTextArea();
		textArea.setText(error);
		textArea.setEditable(false);
		textArea.setFont(new Font("Courier New", Font.PLAIN, 12));

		// causes the text are to display the first line (otherwise the text area is scrolled to the bottom)
		textArea.setCaretPosition(0);
		
		JScrollPane scroll = new JScrollPane(textArea);
		scroll.setMaximumSize(maxDimension);
		scroll.setPreferredSize(maxDimension);


		add(scroll);
	}

	/**
	 * <p>Static factory to create a new error panel.</p>
	 * <p>The size of the panel is calculated as a fixed ratio of 30% the screen size</p>
	 * @param error the string message to display
	 * @return the new error panel created
	 */
	@SuppressWarnings("WeakerAccess")
	public static ErrorPanel newErrorPanel(String error) {
		return new ErrorPanel(error, 0.3, 0.3);
	}

	/**
	 * <p>Static factory to create a new error panel.</p>
	 * <p>The size of the panel is calculated by the specified ratio of the screen for both width and height</p>
	 * @param error the string message to display
	 * @param screenSizeRatio the horizontal and vertical ratio. The resulting panel will have width equal to {@code screenSizeRatio * screenWidth}
	 * and height equal to {@code screenSizeRatio * screenHeight}
	 * @return the new error panel created
	 */
	public static ErrorPanel newErrorPanel(String error, double screenSizeRatio) {
		return new ErrorPanel(error, screenSizeRatio, screenSizeRatio);
	}

	/**
	 * <p>Static factory to create a new error panel.</p>
	 * <p>The size of the panel is calculated as a ratio of the screen size</p>
	 * @param error the string message to display
	 * @param screenSizeRatioX the horizontal ratio. The resulting panel will have width equal to {@code screenSizeRatioX * screenWidth}
	 * @param screenSizeRatioY the vertical ratio. The resulting panel will have height equal to {@code screenSizeRatioY * screenHeight}
	 * @return the new error panel created
	 */
	public static ErrorPanel newErrorPanel(String error, double screenSizeRatioX, double screenSizeRatioY) {
		return new ErrorPanel(error, screenSizeRatioX, screenSizeRatioY);
	}

}
