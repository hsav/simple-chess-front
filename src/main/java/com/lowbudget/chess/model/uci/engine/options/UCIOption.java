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

import javax.xml.bind.annotation.XmlAttribute;
import java.util.Objects;

/**
 * <p>Data transfer object that stores the data of an engine option.</p>
 */
public abstract class UCIOption implements OptionVisitable {

	/**
	 * Available option types
	 */
	public enum Type {
		BUTTON, STRING, CHECK, SPIN, COMBO
	}

	@XmlAttribute
	private final String name;

	@XmlAttribute
	private final Type type;

	// for JAXB
	UCIOption() {
		this.name = null;
		this.type = null;
	}

	UCIOption(String name, Type type) {
		this.name = Objects.requireNonNull(name);
		this.type = Objects.requireNonNull(type);
	}

	public String getName() {
		return name;
	}

	public Type getType() {
		return type;
	}

	/**
	 * Resets the value of this option to its default value (if a default value is supported)
	 */
	public void reset() {}

	/**
	 * Creates a copy of this option.
	 * @return the newly created copy
	 */
	public abstract UCIOption copy();

	/**
	 * <p>Returns a string representation of the option's value. If a value is not meaningful for this option
	 * (i.e. as is the case for a button option) then {@code null} should be returned.</p>
	 * @return a string representation of the option's value.
	 */
	public String getValueAsString() {
		return null;
	}

	static boolean areDifferentValues(String value1, String value2) {
		return (value1 != null || value2 != null) && (value1 == null || !value1.equals(value2));

	}
}
