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
 * <p>{@link UCIOption} sub-class for the {@link UCIOption.Type#CHECK}.</p>
 * <p>A check option can be represented by a checkbox. It has a boolean value that can be true or false and it also has
 * a default value</p>
 */
@XmlRootElement(name = "check")
@XmlAccessorType(XmlAccessType.FIELD)
public class CheckOption extends UCIOption {

	@XmlAttribute
	private boolean value;

	@XmlAttribute(name = "default-value")
	private final boolean defaultValue;

	// for JAXB
	@SuppressWarnings("unused")
	private CheckOption() {
		this.defaultValue = false;
	}

	public CheckOption(String name, boolean defaultValue) {
		super(name, Type.CHECK);
		this.defaultValue = defaultValue;
		this.value = defaultValue;
	}

	public boolean getValue() {
		return value;
	}

	public boolean setValue(boolean newValue) {
		boolean modified = value != newValue;
		value = newValue;
		return modified;
	}

	@SuppressWarnings("WeakerAccess")
	public boolean getDefaultValue() {
		return defaultValue;
	}

	@Override
	public void accept(OptionVisitor visitor) {
		visitor.visit(this);
	}

	@Override
	public String getValueAsString() {
		return String.valueOf(getValue()).toLowerCase();
	}

	@Override
	public void reset() {
		value = defaultValue;
	}

	@Override
	public CheckOption copy() {
		CheckOption option = new CheckOption(getName(), getDefaultValue());
		option.setValue(getValue());
		return option;
	}

	@Override
	public String toString() {
		return "{'" + getName() + '\'' + ' ' + getType() + " value=" + value + ", defaultValue=" + defaultValue + '}';
	}
}
