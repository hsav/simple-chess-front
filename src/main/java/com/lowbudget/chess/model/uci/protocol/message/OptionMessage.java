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

package com.lowbudget.chess.model.uci.protocol.message;

import com.lowbudget.chess.model.uci.protocol.EngineMessage;
import com.lowbudget.chess.model.uci.protocol.EngineMessageHandler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public abstract class OptionMessage extends UCIMessage implements EngineMessage {

	private final String name;

	private final String optionType;

	OptionMessage(String name, String optionType) {
		super(Type.OPTION);
		this.name = Objects.requireNonNull(name, "Option name cannot be null");
		this.optionType = Objects.requireNonNull(optionType, "Option type cannot be null");
	}

	public String getName() {
		return name;
	}

	public String getOptionType() {
		return optionType;
	}

	@Override
	public void accept(EngineMessageHandler handler) {
		handler.handle(this);
	}

	public static class ButtonOptionMessage extends OptionMessage {

		ButtonOptionMessage(String name) {
			super(name, Token.BUTTON);
		}

		@Override
		public String toCommand() {
			return Type.OPTION.command() + ' ' + Token.NAME + ' ' + getName() + ' ' + Token.TYPE + ' ' + Token.BUTTON;
		}
	}

	public static class CheckOptionMessage extends OptionMessage {

		private final boolean defaultValue;

		CheckOptionMessage(String name, boolean defaultValue) {
			super(name, Token.CHECK);
			this.defaultValue = defaultValue;
		}

		public boolean getDefaultValue() {
			return this.defaultValue;
		}

		@Override
		public String toCommand() {
			return Type.OPTION.command() + ' ' + Token.NAME + ' ' + getName() + ' ' + Token.TYPE + ' ' + Token.CHECK + ' ' + Token.DEFAULT +  ' ' + defaultValue;
		}
	}

	public static class ComboOptionMessage extends OptionMessage {

		private final String defaultValue;

		private final List<String> comboOptions;

		ComboOptionMessage(String name, String defaultValue, List<String> comboOptions) {
			super(name, Token.COMBO);
			this.defaultValue = defaultValue;

			List<String> options = new ArrayList<>(comboOptions);
			if (!options.contains(defaultValue)) {
				options.add(0, defaultValue);
			}
			this.comboOptions = Collections.unmodifiableList(options);
		}

		public String getDefaultValue() {
			return defaultValue;
		}

		public List<String> getComboOptions() {
			return comboOptions;
		}

		@Override
		public String toCommand() {
			StringBuilder sb = new StringBuilder(Type.OPTION.command())
					.append(' ').append(Token.NAME).append(' ').append(getName())
					.append(' ').append(Token.TYPE).append(' ').append(Token.COMBO)
					.append(' ').append(Token.DEFAULT).append(' ').append(defaultValue);

			for (String comboOption: comboOptions) {
				sb.append(' ').append(Token.VAR).append(' ').append(comboOption);
			}
			return sb.toString();
		}
	}

	public static class SpinOptionMessage extends OptionMessage {

		private final int defaultValue;

		private final int min;

		private final int max;

		SpinOptionMessage(String name, int defaultValue, int min, int max) {
			super(name, Token.SPIN);
			this.defaultValue = defaultValue;
			this.min = min;
			this.max = max;
		}

		public int getDefaultValue() {
			return defaultValue;
		}

		public int getMin() {
			return min;
		}

		public int getMax() {
			return max;
		}

		@Override
		public String toCommand() {
			return Type.OPTION.command() + ' ' + Token.NAME + ' ' + getName() + ' ' + Token.TYPE + ' ' + Token.SPIN + ' '
					+ Token.DEFAULT +  ' ' + defaultValue + ' ' + Token.MIN + ' ' + min + ' ' + Token.MAX + ' ' + max;
		}
	}

	public static class StringOptionMessage extends OptionMessage {
		private static final String EMPTY = "<empty>";

		private final String defaultValue;

		StringOptionMessage(String name, String defaultValue) {
			super(name, Token.STRING);
			this.defaultValue = (defaultValue == null || EMPTY.equals(defaultValue)) ? "" : defaultValue;
		}

		public String getDefaultValue() {
			return defaultValue;
		}

		@Override
		public String toCommand() {
			return Type.OPTION.command() + ' ' + Token.NAME + ' ' + getName() + ' ' + Token.TYPE + ' ' + Token.STRING + ' ' + Token.DEFAULT +  ' ' + defaultValue;
		}
	}
}
