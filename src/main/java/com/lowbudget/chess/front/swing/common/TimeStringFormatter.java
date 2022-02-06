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
import com.lowbudget.util.Strings;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * <p>Utility class that formats time as a counter-like or as human-friendly string.</p>
 * <p>There are two string formats supported: One appropriate to format the time as a digital counter like in the
 * form {@code "01:05:03"} and another one that supports a more human friendly form where the value would be readable as:
 * {@code "1 hour 5 minutes 3 seconds"}</p>
 */
public final class TimeStringFormatter {

	private static final String HOUR = Messages.get("app.time.format.hour");
	private static final String HOURS = Messages.get("app.time.format.hours");
	private static final String MINUTE = Messages.get("app.time.format.minute");
	private static final String MINUTES = Messages.get("app.time.format.minutes");
	private static final String SECOND = Messages.get("app.time.format.second");
	private static final String SECONDS = Messages.get("app.time.format.seconds");


	private TimeStringFormatter() {}

	/**
	 * Formats a value in milliseconds as a string in the format "00:00:00" (hours, minutes, seconds)
	 * @param millis the time to format in millis
	 * @return the formatted string of the milliseconds specified
	 */
	public static String formatTime(long millis) {
		return formatTime(millis, false);
	}

	/**
	 * Formats a value in milliseconds either as a string in the format "00:00:00" (hours, minutes, seconds)
	 * or in its human-readable form i.e. "1 hour 3 seconds" which takes account of plural, skips any zero values etc.
	 * @param millis the time to format in millis
	 * @param isHumanReadable if {@code true} the time will be formatted using the human-readable format, otherwise
	 *                      the "00:00:00" format will be used
	 * @return the formatted string of the milliseconds specified
	 */
	public static String formatTime(long millis, boolean isHumanReadable) {
		long hours = TimeUnit.MILLISECONDS.toHours(millis);
		long remainingMillis = millis - TimeUnit.HOURS.toMillis(hours);
		long minutes = TimeUnit.MILLISECONDS.toMinutes(remainingMillis);
		remainingMillis -= TimeUnit.MINUTES.toMillis(minutes);
		long seconds = TimeUnit.MILLISECONDS.toSeconds(remainingMillis);

		if (!isHumanReadable) {
			String format = "%02d:%02d";
			if (hours > 0) {
				format = "%02d:" + format;
				return String.format(format, hours, minutes, seconds);
			}
			return String.format(format, minutes, seconds);
		}

		List<String> formats = new ArrayList<>();
		List<Long> values = new ArrayList<>();

		appendTimePart(formats, values, hours, HOUR, HOURS);
		appendTimePart(formats, values, minutes, MINUTE, MINUTES);
		appendTimePart(formats, values, seconds, SECOND, SECONDS);

		return String.format(Strings.join(formats, " "), values.toArray());
	}

	private static void appendTimePart(List<String> formats, List<Long> values, long value, String textSingle, String textPlural) {
		if (value > 0) {
			String format = "%d " + (value > 1 ? textPlural : textSingle);
			formats.add(format);
			values.add(value);
		}
	}
}
