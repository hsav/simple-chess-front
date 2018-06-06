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
import java.util.ArrayList;
import java.util.List;

/**
 * <p>{@link UCIOption} sub-class for the {@link UCIOption.Type#COMBO}.</p>
 * <p>A combo option represents a list of string values that the option can take (for example an option named "Level"
 * could have the values "Easy", "Medium", "Hard"). It additionally supports a default value.</p>
 */
@XmlRootElement(name = "combo")
@XmlAccessorType(XmlAccessType.FIELD)
public class ComboOption extends UCIOption {

	@XmlAttribute
	private String value;

	@XmlAttribute(name = "default-value")
	private final String defaultValue;

	@XmlElementWrapper(name = "values")
	@XmlElement(name = "value")
	private final List<String> availableValues;

	// for JAXB
	@SuppressWarnings("unused")
	private ComboOption() {
		this.defaultValue = null;
		this.availableValues = new ArrayList<>();
	}

	public ComboOption(String name, String defaultValue, List<String> availableValues) {
		super(name, Type.COMBO);
		this.defaultValue = defaultValue;
		this.availableValues = new ArrayList<>(availableValues);
		this.value = defaultValue;
	}

	public String getValue() {
		return value;
	}

	public boolean setValue(String newValue) {
		boolean modified = UCIOption.areDifferentValues(value, newValue);
		value = newValue;
		return modified;
	}

	@SuppressWarnings("WeakerAccess")
	public String getDefaultValue() {
		return defaultValue;
	}

	@Override
	public void reset() {
		value = defaultValue;
	}

	@Override
	public ComboOption copy() {
		ComboOption option = new ComboOption(getName(), getDefaultValue(), getAvailableValues());
		option.setValue(getValue());
		return option;
	}

	public List<String> getAvailableValues() {
		return availableValues;
	}

	@Override
	public void accept(OptionVisitor visitor) {
		visitor.visit(this);
	}

	@Override
	public String getValueAsString() {
		return getValue();
	}

	@Override
	public String toString() {
		return "{'" + getName() + '\'' +
				' ' + getType() +
				" value='" + value + '\'' + ", defaultValue='" + defaultValue + '\'' +
				", available values=" + availableValues.toString() +
				'}';
	}
}
