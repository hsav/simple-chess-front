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

import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>Utility class that provides the functionality of a recent history feature.</p>
 * <p>The class can be configured during runtime for the maximum number of items to remember.</p>
 * <p>It is assumed that it is desirable that the items are kept ordered (i.e. to display them in a menu) with the
 * first item being the oldest one.</p>
 * <p>If a request is made to add an item that already exists in the history, its position is not updated
 * (i.e. it does not become the newest item and thus it is not moved to the end of the list).</p>
 * <p>This class can be serialized to XML using JAXB.</p>
 * @param <T> the type of the item the history remembers
 */
@SuppressWarnings({"WeakerAccess", "unused", "UnusedReturnValue"})
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class RecentHistory<T> implements ReadOnlyRecentHistory<T> {

	/**
	 * <p>Listener that can be registered with the recent history and receive notifications when the history changes.</p>
	 * @param <T> the type of the item in the history
	 */
	public interface RecentHistoryListener<T> {
		/**
		 * <p>Called when an item is added in the history.</p>
		 * @param history the history where the item was added
		 * @param item the item added
		 */
		void onItemAdded(ReadOnlyRecentHistory<T> history, T item);

		/**
		 * <p>Called when an item is removed from the history.</p>
		 * @param history the history where the item was removed from
		 * @param item the item removed
		 */
		void onItemRemoved(ReadOnlyRecentHistory<T> history, T item);
	}

	/**
	 * Stores the most recent items in the history
	 */
	@XmlElement
	private final List<T> items = new ArrayList<>();

	/**
	 * Stores any registered listeners that wish to be notified about history changes.
	 */
	@XmlTransient
	private final ListenerList<RecentHistoryListener<T>> listenerList = new ListenerList<>();

	/**
	 * The max number of items this history should remember.
	 */
	@XmlAttribute
	private int maxCount;

	public RecentHistory() {
		this(3);
	}

	public RecentHistory(int max) {
		this.maxCount = max;
	}

	/**
	 * Registers a new listener that will be notified for future history changes
	 * @param listener the listener to be notified
	 */
	public void addRecentHistoryListener(RecentHistoryListener<T> listener) {
		this.listenerList.add(listener);
	}

	/**
	 * Removes a listener from the list of listeners to notify.
	 * @param listener the listener to remove
	 */
	public void removeRecentHistoryListener(RecentHistoryListener<T> listener) {
		this.listenerList.remove(listener);
	}

	/**
	 * <p>Adds a new item in the recent history if it does not already exist.</p>
	 * <p>If the item already existed then no change occurs. If the item is a new item, then it is added in the list.
	 * If the addition exceeds the history's limit then the oldest item is removed to make space.</p>
	 * @param item the item to add
	 * @return {@code true} if the item was actually added, and {@code false} if the item already existed in the history
	 */
	public boolean addRecentItem(T item) {
		if (!items.contains(item)) {
			items.add(item);
			truncateList(maxCount); // removes items from the start of the list
			listenerList.notifyAllListeners( (listener) -> listener.onItemAdded(this, item) );
			return true;
		}
		return false;
	}

	/**
	 * Returns the item at the index specified.
	 * @param index the index of the item to retrieve.
	 * @return the item at the specified index.
	 */
	public T getRecentItem(int index) {
		checkIndex(index);
		return items.get(index);
	}

	/**
	 * Removes the item at the specified index.
	 * @param index the index of the item to remove.
	 */
	public void removeRecentItem(int index) {
		checkIndex(index);
		T removedItem = items.remove(index);
		listenerList.notifyAllListeners( (listener) -> listener.onItemRemoved(this, removedItem) );
	}

	/**
	 * Removes the item specified.
	 * @param item the item to remove.
	 * @return {@code true} if the item was actually removed, {@code false} otherwise
	 */
	public boolean removeRecentItem(T item) {
		boolean removed = items.remove(item);
		if (removed) {
			listenerList.notifyAllListeners((listener) -> listener.onItemRemoved(this, item));
		}
		return removed;
	}

	/**
	 * Returns the current size of the history from {@code 0} to {@code maxCount-1}
	 * @return the current history size
	 */
	public int getItemCount() {
		return items.size();
	}

	/**
	 * @return the maximum number of items this history will remember.
	 */
	public int getMaxCount() {
		return this.maxCount;
	}

	/**
	 * Sets a new maximum limit for this history. If the new limit is les than the current one, then the history is
	 * truncated to hold only the {@code max} newest items
	 * @param max the new limit
	 */
	public void setMaxCount(int max) {
		if (max < this.maxCount) {
			truncateList(max); // remove items from the start of the list
		}
		this.maxCount = max;
	}

	/**
	 * Adds all the items of the history specified, to this history
	 * @param other the history from which to add all items in this history
	 */
	public void addAll(RecentHistory<T> other) {
		items.addAll(other.items);
		truncateList(maxCount); // remove items from the start of the list
	}

	/**
	 * Checks that the specified index is valid
	 * @param index the index to check
	 * @throws IndexOutOfBoundsException if the index is invalid
	 */
	private void checkIndex(int index) {
		if (index < 0 || index >= items.size()) {
			throw new IndexOutOfBoundsException("Invalid recent item index: " + index);
		}
	}

	/**
	 * Truncates the list by removing items from the start of the list (i.e. the oldest ones) so this history is
	 * reduced to hold the number of items specified.
	 * @param max the maximum number of items the list should have after the truncation.
	 */
	private void truncateList(int max) {
		if (items.size() > max) {
			// delete items from the start of the list (i.e. the oldest ones)
			int removeItems = items.size() - max;
			for (int i = 0; i < removeItems; i++) {
				removeRecentItem(0);
			}
		}
	}
}
