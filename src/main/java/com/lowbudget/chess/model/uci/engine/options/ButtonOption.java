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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * <p>{@link UCIOption} sub-class for the {@link UCIOption.Type#BUTTON}.</p>
 * <p>A button option has no additional fields and no default value. It represents a command that should be sent to
 * the engine when a button in the GUI is pressed.</p>
 */
@XmlRootElement(name = "button")
@XmlAccessorType(XmlAccessType.FIELD)
public class ButtonOption extends UCIOption {

	// for JAXB
	@SuppressWarnings("unused")
	private ButtonOption() {}

	public ButtonOption(String name) {
		super(name, Type.BUTTON);
	}

	@Override
	public String toString() {
		return "{'" + getName() + '\'' + ' ' + getType() + '}';
	}

	@Override
	public void accept(OptionVisitor visitor) {
		visitor.visit(this);
	}

	@Override
	public ButtonOption copy() {
		return new ButtonOption(getName());
	}
}
