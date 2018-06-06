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

package com.lowbudget.chess.front.swing.components.painter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.Properties;

/**
 * Loads and stores any necessary resources required by an application's theme.
 */
class Theme {
	private static final Logger log = LoggerFactory.getLogger(Theme.class);

	/**
	 * The width of the border in case the border is drawn using an image. Note here that the image is expected to
	 * contain all four edges of the board so we don't have a way to know which pixels of this image are actually used
	 */
	private int borderWidth;

	/**
	 * The image used to paint the border (if any)
	 */
	private String borderImage;

	/**
	 * The color of the border in case a border image is not used
	 */
	private Color borderColor;

	/**
	 * The color of the border's border - in case the board's border is drawn with simple colors a border is supposed
	 * to be drawn around that border in order to achieve a better contrast with the background
	 */
	private Color borderBorderColor;

	/**
	 * The color of the background (if any). A background can be drawn either using a simple color or by drawing
	 * a background image (in case an image is declared it is preferred)
	 */
	private Color backgroundColor;

	/**
	 * The background image (if any)
	 */
	private String backgroundImage;

	/**
	 * The color of the black squares. Similarly to the background squares can be drawn either using a color or an
	 * image (the image is preferred)
	 */
	private Color squareBlackColor;

	/**
	 * The color of the white squares
	 */
	private Color squareWhiteColor;

	/**
	 * An image to use when drawing the black squares
	 */
	private String squareBlackImage;

	/**
	 * An image to use when drawing the white squares
	 */
	private String squareWhiteImage;

	/**
	 * The color to use when a square is selected
	 */
	private Color squareSelectedColor;

	/**
	 * The color to use when a square is the landing square of a legal move
	 */
	private Color squareHighlightLegalMoveColor;

	/**
	 * The color to use when drawing an attack between two squares (i.e. if an arrow is drawn this is the color
	 * of the arrow)
	 */
	private Color squareHighlightAttackColor;

	/**
	 * The color to use when a square is a square involved in the last move player on the board
	 */
	private Color squareHighlightColor;

	/**
	 * The color of the font used to draw the subscripts
	 */
	private Color subscriptFontColor;

	/**
	 * The name of the theme.
	 */
	private final String name;

	Theme(String name) {
		this.name = Objects.requireNonNull(name, "Theme name cannot be null");
	}

	boolean hasBorderImage() {
		return borderImage != null;
	}

	boolean hasBackgroundImage() {
		return backgroundImage != null;
	}

	boolean hasSquareImages() {
		return getSquareBlackImage() != null && getSquareWhiteImage() != null;
	}

	private Color readColor(String value, Color defaultValue) {
		if (value == null) {
			return defaultValue;
		}
		Color result = defaultValue;
		try {
			if (!value.startsWith("#")) {
				value = "#" + value;
			}
			result = Color.decode(value);
		} catch (Exception e) {
			log.error("Warning: Value {} is not a valid color value", value, e);
		}
		return result;
	}

	@SuppressWarnings("SameParameterValue")
	private int readInt(String value, int defaultValue) {
		if (value == null) {
			return defaultValue;
		}
		int result = defaultValue;
		try {
			result = Integer.parseInt(value);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return result;
	}

	void load(String resource) {
		Properties p = new Properties();
		try {
			InputStream in = getClass().getClassLoader().getResourceAsStream(resource);
			p.load(in);

			this.borderWidth = readInt(p.getProperty("border-width"), 1);
			this.borderColor = readColor(p.getProperty("border-color"), Color.decode("#105573"));
			this.borderBorderColor = readColor(p.getProperty("border-border-color"), Color.decode("#77b4c9"));
			this.borderImage = p.getProperty("border-image");

			this.backgroundColor = readColor(p.getProperty("background-color"), Color.decode("#707070"));
			this.backgroundImage = p.getProperty("background-image");

			this.squareBlackImage = p.getProperty("square-black-image");
			this.squareBlackColor = readColor(p.getProperty("square-black-color"), Color.decode("#adb6b5"));
			this.squareWhiteImage = p.getProperty("square-white-image");
			this.squareWhiteColor = readColor(p.getProperty("square-white-color"), Color.decode("#d6d6d6"));

			this.squareSelectedColor = readColor(p.getProperty("square-selected-color"), Color.blue);
			this.squareHighlightLegalMoveColor = readColor(p.getProperty("square-highlight-legal-moves-color"), Color.orange);
			this.squareHighlightColor = readColor(p.getProperty("square-highlight-color"), Color.cyan);
			this.squareHighlightAttackColor = readColor(p.getProperty("square-highlight-attack-color"), Color.pink);

			this.subscriptFontColor = readColor(p.getProperty("subscript-color"), Color.green);


			in.close();
		} catch (IOException e) {
			log.error("Warning: Could not load theme properties from {}:{}", resource, e.getMessage(), e);
		}
	}

	public String getName() {
		return name;
	}

	String getBorderImage() {
		return borderImage;
	}

	Color getBackgroundColor() {
		return backgroundColor;
	}

	Color getSquareBlackColor() {
		return squareBlackColor;
	}

	Color getSquareWhiteColor() {
		return squareWhiteColor;
	}

	Color getSquareSelectedColor() {
		return squareSelectedColor;
	}

	Color getSquareHighlightLegalMoveColor() {
		return squareHighlightLegalMoveColor;
	}

	Color getSquareHighlightAttackColor() {
		return squareHighlightAttackColor;
	}

	String getSquareBlackImage() {
		return squareBlackImage;
	}

	String getSquareWhiteImage() {
		return squareWhiteImage;
	}

	Color getSubscriptFontColor() {
		return subscriptFontColor;
	}

	int getBorderWidth() {
		return borderWidth;
	}

	Color getSquareHighlightColor() {
		return squareHighlightColor;
	}

	Color getBorderColor() {
		return borderColor;
	}

	Color getBorderBorderColor() {
		return borderBorderColor;
	}

	String getBackgroundImage() {
		return backgroundImage;
	}
}
