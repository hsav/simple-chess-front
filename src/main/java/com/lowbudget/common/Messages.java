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

package com.lowbudget.common;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * <p>Utility class that wraps the functionality provided by a {@link ResourceBundle} to be able to easily retrieve
 * key translations.</p>
 * <p>The property file with the translated keys should be named {@code messages_en.properties} for the default english
 * locale. For other locales the file naming convention follows the conventions used by {@link ResourceBundle}.</p>
 * <p>This class manages a single instance of a {@link ResourceBundle} and does not allow instantiation.</p>
 */
public class Messages {

	// the single resource bundle instance
	private static final ResourceBundle MESSAGES = ResourceBundle.getBundle("messages", Locale.ENGLISH);

	private Messages() {}

	/**
	 * <p>Retrieves the translation of the key specified.</p>
	 * @param key the key for which the translation is desired.
	 * @return the key itself if a translation is not found, otherwise the corresponding translation is returned.
	 */
	public static String get(String key) {
		String result;
		try {
			result = MESSAGES.getString(key);
		} catch (MissingResourceException e) {
			result = key;
		}
		return result;
	}

	/**
	 * <p>Retrieves the translation of the key specified.</p>
	 * @param key the key for which the translation is desired.
	 * @param args arguments that should be used to replace placeholders in the translation (placeholders are specified
	 *             via {0}, {1} etc inside the translation). The rules of the replacement are the same as
	 *             {@link MessageFormat#format(String, Object...)}
	 * @return the key itself if a translation is not found, otherwise the corresponding translation is returned.
	 */
	public static String get(String key, Object... args) {
		String result;
		try {
			result = MESSAGES.getString(key);
			result = MessageFormat.format(result, args);
		} catch (MissingResourceException e) {
			result = key;
		}
		return result;
	}

}
