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

import com.lowbudget.common.RecentHistory.RecentHistoryListener;

/**
 * A read-only version of {@link RecentHistory}. The term "read-only" applies to the list of the items not to an
 * individual item itself
 * @param <T> the type of the items this history stores
 */
public interface ReadOnlyRecentHistory<T> {
	/**
	 * Returns the item in the recent history at the specified index
	 * @param index the index of the item
	 * @return the item at the specified index
	 */
	T getRecentItem(int index);

	/**
	 * Returns the number of items in the history
	 * @return the total number of items in the history
	 */
	int getItemCount();

	/**
	 * Adds a listener to the history to be notified when items are added/removed
	 * @param listener the listener to add
	 */
	void addRecentHistoryListener(RecentHistoryListener<T> listener);
}
