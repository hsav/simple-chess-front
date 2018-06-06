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

package com.lowbudget.chess.model.uci.engine.options;

import javax.xml.bind.annotation.*;

/**
 * <p>{@link UCIOption} sub-class for the {@link UCIOption.Type#SPIN}.</p>
 * <p>A spin option represents a numerical value that can be displayed by a spinner (i.e. a component that usually
 * has arrows attached to its edge that can increase/decrease the value). This option additionally supports a minimum,
 * a maximum and a default value.</p>
 */
@XmlRootElement(name = "spin")
@XmlAccessorType(XmlAccessType.FIELD)
public class SpinOption extends UCIOption {

	@XmlAttribute
	private int value;

	@XmlAttribute(name = "default-value")
	private final int defaultValue;

	@XmlAttribute
	private final int min;

	@XmlAttribute
	private final int max;

	// for JAXB
	@SuppressWarnings("unused")
	private SpinOption() {
		this.defaultValue = 0;
		this.min = 0;
		this.max = 0;
	}

	public SpinOption(String name, int defaultValue, int min, int max) {
		super(name, Type.SPIN);
		this.defaultValue = defaultValue;
		this.value = defaultValue;
		this.min = min;
		this.max = max;
	}

	public int getValue() {
		return value;
	}

	public boolean setValue(int newValue) {
		boolean modified = value != newValue;
		this.value = newValue;
		return modified;
	}

	@SuppressWarnings("WeakerAccess")
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
	public void reset() {
		value = defaultValue;
	}

	@Override
	public SpinOption copy() {
		SpinOption option = new SpinOption(getName(), getDefaultValue(), getMin(), getMax());
		option.setValue(getValue());
		return option;
	}

	@Override
	public void accept(OptionVisitor visitor) {
		visitor.visit(this);
	}

	@Override
	public String getValueAsString() {
		return String.valueOf(getValue());
	}

	@Override
	public String toString() {
		return "{'" + getName() + '\'' + ' ' + getType() +
				" value=" + value + ", defaultValue=" + defaultValue + ", min=" + min + ", max=" + max + '}';
	}
}
