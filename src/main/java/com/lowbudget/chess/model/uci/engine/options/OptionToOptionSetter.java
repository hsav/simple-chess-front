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

/**
 * A visitor that sets the value of an option to the value of another option
 */
public class OptionToOptionSetter implements OptionVisitor {

	/**
	 * These members are used only temporarily during the method call of {@link #setValue(UCIOption, UCIOption)}
	 */
	private UCIOption oldOption;
	private boolean modified;

	/**
	 * Sets an option's new value to another option's value
	 * @param newOption the option with the new value
	 * @param oldOption the corresponding option with the old value
	 * @return {@code true} if setting the value caused an actual modification, {@code false} otherwise
	 */
	public boolean setValue(UCIOption newOption, UCIOption oldOption) {
		if (!newOption.getName().equals(oldOption.getName()) || newOption.getType() != oldOption.getType()) {
			throw new IllegalArgumentException("Options do not refer to the same option! new: " + newOption + ", old: " + oldOption);
		}
		this.oldOption = oldOption;
		this.modified = false;
		newOption.accept(this);
		return modified;
	}

	@Override
	public void visit(ButtonOption option) {
		// nothing to do
		modified = false;
	}

	@Override
	public void visit(StringOption option) {
		StringOption stringOption = (StringOption) oldOption;
		modified = stringOption.setValue(option.getValue());
	}

	@Override
	public void visit(CheckOption option) {
		CheckOption checkOption = (CheckOption) oldOption;
		modified = checkOption.setValue(option.getValue());
	}

	@Override
	public void visit(SpinOption option) {
		SpinOption spinOption = (SpinOption) oldOption;
		modified = spinOption.setValue(option.getValue());
	}

	@Override
	public void visit(ComboOption option) {
		ComboOption comboOption = (ComboOption) oldOption;
		modified = comboOption.setValue(option.getValue());
	}
}
