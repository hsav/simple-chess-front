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

import com.lowbudget.common.Messages;
import com.lowbudget.util.Exceptions;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * <p>Utility class that contains common methods that we might need in the UI part of the application.</p>
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public final class UIUtils {

	private static final Map<String, BufferedImage> IMAGE_CACHE = new HashMap<>();

	// prohibit instantiation
	private UIUtils() {}

	/**
	 * Displays a dialog with an informative message
	 * @param window the parent of the dialog
	 * @param titleKey a key that will be used to obtain the dialog's title translation
	 * @param msgKey a key that will be used to obtain the dialog's message translation
	 * @param args additional arguments that might be needed by the {@code msgKey}
	 */
	public static void showInfoMessage(Window window, String titleKey, String msgKey, Object... args) {
		showDialog(window, Messages.get(titleKey), Messages.get(msgKey, args), JOptionPane.INFORMATION_MESSAGE);
	}

	/**
	 * Displays a dialog with a warning message
	 * @param window the parent of the dialog
	 * @param titleKey a key that will be used to obtain the dialog's title translation
	 * @param msgKey a key that will be used to obtain the dialog's message translation
	 * @param args additional arguments that might be needed by the {@code msgKey}
	 */
	public static void showWarningMessage(Window window, String titleKey, String msgKey, Object... args) {
		showDialog(window, Messages.get(titleKey), Messages.get(msgKey, args), JOptionPane.WARNING_MESSAGE);
	}

	/**
	 * Displays a dialog with an error message
	 * @param window the parent of the dialog
	 * @param titleKey a key that will be used to obtain the dialog's title translation
	 * @param msgKey a key that will be used to obtain the dialog's message translation
	 * @param args additional arguments that might be needed by the {@code msgKey}
	 */
	public static void showErrorMessage(Window window, String titleKey, String msgKey, Object... args) {
		showDialog(window, Messages.get(titleKey), Messages.get(msgKey, args), JOptionPane.ERROR_MESSAGE);
	}

	/**
	 * Displays a dialog with an exception stacktrace as the message
	 * @param window the parent of the dialog
	 * @param titleKey a key that will be used to obtain the dialog's title translation
	 * @param e the exception to be displayed
	 */
	public static void showExceptionMessage(Window window, String titleKey, Exception e) {
		showDialog(window, Messages.get(titleKey), ErrorPanel.newErrorPanel(Exceptions.getStackTrace(e)), JOptionPane.ERROR_MESSAGE);
	}

	/**
	 * Displays a warning message where the user is requested to verify if the action should proceed by clicking one
	 * of the ok or cancel buttons
	 * @param window the parent of the dialog
	 * @param titleKey a key that will be used to obtain the dialog's title translation
	 * @param msgKey a key that will be used to obtain the dialog's message translation
	 * @return an int with the same semantics as the return values of {@link JOptionPane} confirm dialog (i.e. either
	 * {@link JOptionPane#OK_OPTION} or {@link JOptionPane#CANCEL_OPTION})
	 */
	public static int showOkCancelDialog(Window window, String titleKey, String msgKey) {
		return showDialog(window,
				Messages.get(titleKey),
				Messages.get(msgKey),
				JOptionPane.OK_CANCEL_OPTION,
				JOptionPane.WARNING_MESSAGE,
				new String[] { Messages.get("app.button.ok"), Messages.get("app.button.cancel")});
	}

	/**
	 * Displays a warning message where the user is requested to verify if the action should proceed by clicking one
	 * of the yes, no or cancel buttons
	 * @param window the parent of the dialog
	 * @param titleKey a key that will be used to obtain the dialog's title translation
	 * @param msgKey a key that will be used to obtain the dialog's message translation
	 * @return an int with the same semantics as the return values of {@link JOptionPane} confirm dialog (i.e. either
	 * {@link JOptionPane#YES_OPTION},  {@link JOptionPane#NO_OPTION} or {@link JOptionPane#CANCEL_OPTION})
	 */
	public static int showYesNoCancelDialog(Window window, String titleKey, String msgKey) {
		return showDialog(window,
				Messages.get(titleKey),
				Messages.get(msgKey),
				JOptionPane.YES_NO_CANCEL_OPTION,
				JOptionPane.WARNING_MESSAGE,
				new String[] { Messages.get("app.button.yes"), Messages.get("app.button.no"), Messages.get("app.button.cancel")}
		);
	}


	/**
	 * <p>Finds a {@link Window} of the component that is the source of the event specified.</p>
	 * <p>This method will traverse the ancestor hierarchy event if it encounters a {@code {@link JPopupMenu}}.</p>
	 * @param event the current event
	 * @return the corresponding window
	 * @throws IllegalStateException if a window could not be found
	 */
	public static Window findWindowAncestor(ActionEvent event) {
		return findWindowAncestor(event, true);
	}

	/**
	 * <p>Finds a {@link Window} of the component that is the source of the event specified.</p>
	 * <p>This method will traverse the ancestor hierarchy event if it encounters a {@code {@link JPopupMenu}}.</p>
	 * @param event the current event
	 * @param required a flag that indicates if we expect the window to exist
	 * @return the corresponding window if it exists. In case {@code required} was specified as {@code false} and the
	 * window could not be found then {@code null} will be returned.
	 * @throws IllegalStateException if {@code required} was specified as {@code true} but a window could not be found
	 */
	public static Window findWindowAncestor(ActionEvent event, boolean required) {
		Component component = (Component) event.getSource();
		return findWindowAncestor(component, required);
	}

	/**
	 * <p>Finds a {@link Window} ancestor in the ancestor hierarchy of the component specified.</p>
	 * <p>This method will traverse the ancestor hierarchy event if it encounters a {@code {@link JPopupMenu}}.</p>
	 * @param component the component hierarchy to search
	 * @return the corresponding window if exists.
	 * @throws IllegalStateException if a window could not be found
	 */
	public static Window findWindowAncestor(Component component) {
		return findWindowAncestor(component, true);
	}

	/**
	 * <p>Finds a {@link Window} ancestor in the ancestor hierarchy of the component specified.</p>
	 * <p>This method will traverse the ancestor hierarchy event if it encounters a {@code {@link JPopupMenu}}.</p>
	 * @param component the component hierarchy to search
	 * @param required a flag that indicates if we expect the window to exist
	 * @return the corresponding window if it exists. In case {@code required} was specified as {@code false} and the
	 * window could not be found then {@code null} will be returned.
	 * @throws IllegalStateException if {@code required} was specified as {@code true} but a window could not be found
	 */
	public static Window findWindowAncestor(Component component, boolean required) {
		if (component != null) {
			Container container = component.getParent();
			while (container != null) {
				if (container instanceof Window) {
					return (Window) container;
				}
				// if the current container is in a JPopupMenu we need to use the invoker as a parent to be able to
				// traverse the hierarchy upwards
				if (container instanceof JPopupMenu) {
					Component invoker = ((JPopupMenu) container).getInvoker();
					container = invoker.getParent();
				} else {
					container = container.getParent();
				}
			}
		}
		// frame is null here
		if (required) {
			throw new IllegalStateException("A parent JFrame was expected to be found but could not find any");
		}
		return null;
	}

	/**
	 * <p>Loads the image with the specified name from the class path.</p>
	 * <p>the image will be loaded only the first time it is requested, and it will be retrieved from a cache on
	 * future requests.</p>
	 * @param name the name of the image
	 * @return the image that corresponds to the specified name
	 */
	public static BufferedImage getImage(String name) {
		BufferedImage image = IMAGE_CACHE.get(name);
		if (image == null) {
			try (InputStream in = UIUtils.class.getClassLoader().getResourceAsStream(name)) {
				image = ImageIO.read(in);
			} catch (IOException e) {
				throw new RuntimeException("Could not load image", e);
			}
			IMAGE_CACHE.put(name, image);
		}
		return image;
	}

	/**
	 * <p>Loads a rectangular image with the specified name from the class path scaling with the scale factor specified.</p>
	 * <p>the image will be loaded only the first time it is requested, and it will be retrieved from a cache on
	 * future requests.</p>
	 * @param name the name of the image
	 * @param scale the number of pixels for the width/height of the scaled image
	 * @return the image that corresponds to the specified name
	 */
	public static BufferedImage getImage(String name, int scale) {
		BufferedImage image = getImage(name);

		BufferedImage scaledImage = new BufferedImage(scale, scale, BufferedImage.TYPE_INT_ARGB);

		Graphics2D g = scaledImage.createGraphics();
		try {
			g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g.drawImage(image, 0, 0, scale, scale, null);
		} finally {
			g.dispose();
		}
		int dotIndex = name.indexOf('.');
		String prefix = name.substring(0, dotIndex);
		String suffix = name.substring(dotIndex + 1);
		IMAGE_CACHE.put(prefix + '_' + scale + suffix, scaledImage);
		return scaledImage;
	}

	private static void showDialog(Window window, String title, Object message, int messageType) {
		showDialog(
				window,
				title,
				message,
				JOptionPane.DEFAULT_OPTION,
				messageType,
				new String[] {Messages.get("app.button.ok")});
	}

	private static int showDialog(Window window, String title, Object message, int messageType, String[] buttons) {
		return showDialog(window, title, message, JOptionPane.DEFAULT_OPTION, messageType, buttons);
	}
	private static int showDialog(Window window, String title, Object message, int dialogType, int messageType, String[] buttons) {
		return JOptionPane.showOptionDialog(
				window,
				message,
				title,
				dialogType,
				messageType,
				null,
				buttons,
				null);
	}

}
