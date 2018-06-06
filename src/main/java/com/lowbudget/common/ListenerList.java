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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * <p>Utility class to manage a set of listeners for classes that wish to be able to notify clients not in the form
 * of events but by invoking an appropriate action on each listener instead.</p>
 * <p>The invoked action can be expressed in the form of a lambda.</p>
 * <p>Example usage:</p>
 * <pre>
 *     // interface that listens for theme changes
 *     interface ThemeListener {
 *         void onThemeCreated();
 *         void onThemeChanged(String newTheme);
 *     }
 *
 *     // application
 *     class Application {
 *         ListenerList&lt;ThemeListener&gt; themeListeners = new ListenerList&lt;&gt;();
 *
 *         // allow to register a listener that listens for theme changes
 *         void addThemeListener(ThemeListener listener) {
 *             themeListeners.add(listener);
 *         }
 *
 *         void createTheme(String newTheme) {
 *             // ... create the new theme, then notify the listeners
 *             themeListeners.notifyAllListeners(listener -&gt; listener.onThemeCreated());
 *             // or with a method reference: themeListeners.notifyAllListeners(ThemeListener::onThemeCreated);
 *         }
 *
 *         void changeTheme(String newTheme) {
 *             // ... change the theme to the new theme, then notify the listeners
 *             themeListeners.notifyAllListeners(listener -&gt; listener.onThemeChanged(newTheme));
 *         }
 *     }
 * </pre>
 * @param <T> the type of the listener this object manages
 */
public class ListenerList<T> {
	private final List<T> list = new ArrayList<>();

	/**
	 * <p>Add a new listener to be notified for events.</p>
	 * @param listener the listener to add.
	 * @throws NullPointerException if the specified listener is {@code null}
	 */
	public void add(T listener) {
		list.add( Objects.requireNonNull(listener, "Cannot add null listener") );
	}

	/**
	 * <p>Removes a listener.</p>
	 * @param listener the listener to remove
	 * @throws NullPointerException if the specified listener is {@code null}
	 */
	public void remove(T listener) {
		list.remove( Objects.requireNonNull(listener, "Cannot remove null listener") );
	}

	/**
	 * <p>Removes all listeners.</p>
	 */
	public void clear() {
		list.clear();
	}

	/**
	 * <p>Notifies all listeners to perform the specified action.</p>
	 * @param action a {@link Consumer} representing the action to be invoked on all the listener objects.
	 */
	public void notifyAllListeners(Consumer<T> action) {
		// iterate the list from the end so listeners can remove themselves during this loop
		for (int i = list.size() - 1; i >= 0; i--) {
			T listener = list.get(i);
			action.accept(listener);
		}
	}
}
