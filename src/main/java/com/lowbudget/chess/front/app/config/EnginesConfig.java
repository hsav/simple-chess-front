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

package com.lowbudget.chess.front.app.config;

import com.lowbudget.chess.model.uci.engine.UCIEngineConfig;
import com.lowbudget.chess.model.uci.engine.options.UCIOption;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>A helper class that is essentially an {@link ArrayList} wrapper that keeps track if any changes were made to the
 * internal list when adding/removing items.</p>
 * <p>This is used in the {@link Configuration} class where we need to know if any information is modified so we can
 * know if the configuration needs to be saved.</p>
 */
@XmlRootElement(name="engines-config")
@XmlAccessorType(XmlAccessType.FIELD)
public class EnginesConfig implements ReadOnlyEnginesConfig {

	@XmlElement(name="engine")
	private final List<UCIEngineConfig> configs = new ArrayList<>();

	@XmlTransient
	private boolean modified = false;

	public boolean isModified() {
		return modified;
	}

	public void setModified(boolean modified) {
		this.modified = modified;
	}

	/*package*/ void copyFrom(EnginesConfig other) {
		clear();
		boolean changed = configs.addAll(other.configs);
		if (changed) {
			modified = true;
		}
	}

	public void clear() {
		int size = configs.size();
		configs.clear();
		if (size > 0) {
			modified = true;
		}
	}

	private void add(UCIEngineConfig item) {
		boolean added = this.configs.add(item);
		if (added) {
			modified = true;
		}
	}

	public UCIEngineConfig get(int index) {
		return this.configs.get(index);
	}

	public void remove(int index) {
		this.configs.remove(index);
		modified = true;
	}

	public int size() {
		return configs.size();
	}

	/**
	 * <p>Adds the engine config to the configuration if it does not already exist.</p>
	 * <p>If the engine config already exists, then any stored option values are copied to the values of
	 * the {@code newEngine}.</p>
	 * @param newEngine the {@link UCIEngineConfig} for which its options will possibly be modified, to set its option values
	 *               to any preferences that already exist from a previous connection to the same engine
	 * @return a list of the options that were actually modified by this process (if any)
	 */
	public List<UCIOption> merge(UCIEngineConfig newEngine) {
		List<UCIOption> modifiedOptions = new ArrayList<>();

		// search if the engine already exists. If yes, then set the new engine's option values using the
		// "old" engine's option values. This way an engine remembers any user modified settings used
		int index = configs.indexOf(newEngine);
		if (index < 0) {
			// the new engine does not exist - add it
			add(newEngine);
			return modifiedOptions;
		}

		// the engine exists - we need to merge the option values
		UCIEngineConfig oldEngine = configs.get(index);

		// copy the option values from the existing engine to the new engine and return a list with the options
		// modified (if any)
		modifiedOptions = newEngine.copyValuesFrom(oldEngine.getOptions());

		// replace the old object reference with the new one, so both of them (engine object specified and the one
		// stored in configuration) refer to the same object.
		// This way any changes in the client will be saved by the configuration when the application exits.
		this.configs.set(index, newEngine);

		if (modifiedOptions.size() > 0) {
			modified = true;
		}

		return modifiedOptions;
	}

}
