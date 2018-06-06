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

package com.lowbudget.chess.model.uci.engine;

import com.lowbudget.chess.model.uci.connect.Connectable;
import com.lowbudget.chess.model.uci.connect.Connectable.Params;
import com.lowbudget.chess.model.uci.engine.options.*;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

import static java.util.stream.Collectors.toMap;

/**
 * <p>A data transfer object that stores data related to a uci engine.</p>
 * <p>This object can be serialized to XML using JAXB.</p>
 */
@XmlRootElement(name="engine")
@XmlAccessorType(XmlAccessType.FIELD)
public class UCIEngineConfig {

	/**
	 * The name of the engine (received via the {@link com.lowbudget.chess.model.uci.protocol.message.UCIMessage.Type#ID} message)
	 */
	@XmlAttribute
	private String name;

	/**
	 * The author of the engine (received via the {@link com.lowbudget.chess.model.uci.protocol.message.UCIMessage.Type#ID} message)
	 */
	@XmlAttribute
	private String author;

	/**
	 * The {@link Params} used to connect to this engine
	 */
	@XmlElementRefs( {
			@XmlElementRef( type = Connectable.LocalConnectionParams.class ),
			@XmlElementRef( type = Connectable.RemoteConnectionParams.class),
	})
	private final Params params;

	/**
	 * The options available in the engine (received via {@link com.lowbudget.chess.model.uci.protocol.message.UCIMessage.Type#OPTION} messages)
	 */
	@XmlElementWrapper(name = "options")
	@XmlElementRefs( {
			@XmlElementRef( type = ButtonOption.class ),
			@XmlElementRef( type = CheckOption.class),
			@XmlElementRef( type = ComboOption.class ),
			@XmlElementRef( type = SpinOption.class ),
			@XmlElementRef( type = StringOption.class ),
	})
	private final List<UCIOption> options = new ArrayList<>();

	@SuppressWarnings("unused")
	// for JAXB
	private UCIEngineConfig() {
		this.params = null;
	}

	public UCIEngineConfig(Params params) {
		this.params = Objects.requireNonNull(params, "Client params cannot be null");
	}

	public Params getParams() {
		return params;
	}

	/**
	 * Adds a new available option supported by the engine
	 * @param option the option to add
	 */
	public void addOption(UCIOption option) {
		options.add(option);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public List<UCIOption> getOptions() {
		return options;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		UCIEngineConfig other = (UCIEngineConfig) o;
		return Objects.equals(name, other.name) &&
				Objects.equals(params, other.params);
	}

	@Override
	public int hashCode() {
		return Objects.hash(name, params);
	}

	@Override
	public String toString() {
		return "UCIEngineConfig{" +
				"name='" + name + '\'' +
				", author='" + author + '\'' +
				", options=" + options +
				'}';
	}

	/**
	 * <p>Copies the options' values from the specified list to the values of this engine's options.</p>
	 * <p>It is assumed that the list specified, contains the desired values but might be out of date
	 * with the options in this list.</p>
	 * @param desiredOptions a list of options that contain the desired values (e.g. the values used the last time
	 *                       the user has connected to the engine).
	 * @return a list of all options in {@link #options} that were actually modified (if any)
	 */
	public List<UCIOption> copyValuesFrom(List<UCIOption> desiredOptions) {
		// we do not assume that the order of the options in the two lists is the same so we build a map for the
		// stored options to be able to access them by name
		Map<String, UCIOption> desiredOptionMap = desiredOptions.stream().collect(toMap(UCIOption::getName, Function.identity()));

		// then iterate over the engine options and for each option find the stored option with the same name.
		// if we find such an option, copy its value to the engine's option
		List<UCIOption> modifiedOptions = new ArrayList<>();
		OptionToOptionSetter setter = new OptionToOptionSetter();
		for (UCIOption engineOption : options) {
			UCIOption desiredOption = desiredOptionMap.get(engineOption.getName());
			if (desiredOption != null) {
				// the engine option exists in the stored options, copy the stored value
				boolean modified = setter.setValue(desiredOption, engineOption);
				if (modified) {
					modifiedOptions.add(engineOption);
				}
			}
		}
		return modifiedOptions;
	}
}
