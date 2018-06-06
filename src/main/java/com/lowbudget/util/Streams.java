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

import java.io.Closeable;
import java.io.IOException;

/**
 * Contains utility methods related to streams and IO
 */
public final class Streams {
	private Streams() {}

	/**
	 * <p>Closes a {@link Closeable} resource. </p>
	 * <p>This method tolerates {@code null} arguments. If the resource in question throws an {@link IOException} the
	 * exception will be caught and re-thrown as a {@link RuntimeException}.</p>
	 * @param resource the resource to close. It can be {@code null} in which case the call has no effect
	 * @throws RuntimeException if an {@link IOException} occurs when closing the resource
	 */
	public static void close(Closeable resource) {
		try {
			if (resource != null) {
				resource.close();
			}
		} catch(IOException e) {
			throw new RuntimeException(e);
		}
	}
}
