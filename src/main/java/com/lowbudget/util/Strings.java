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

package com.lowbudget.util;

import java.util.List;
import java.util.Objects;

/**
 * Contains utility methods related to strings
 */
public final class Strings {

	private Strings() {}

	/**
	 * Converts a list to string
	 * @param list the list to convert to string
	 * @param delimiter the string to be used as a delimiter to separate list's items
	 * @return a string where each item in the list is appended one after the other, delimited by the delimiter
	 * specified. Each item is converted to a {@code String} using the {@link StringBuilder#append(Object)} method.
	 */
	public static String join(List<?> list, String delimiter) {
		Objects.requireNonNull(list, "List argument cannot be null!");
		Objects.requireNonNull(delimiter, "Delimiter argument cannot be null!");

		int size = list.size();
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < size; i++) {
			Object listItem = list.get(i);
			sb.append(listItem);
			if (i < size - 1) {
				sb.append(delimiter);
			}
		}
		return sb.toString();
	}
}
