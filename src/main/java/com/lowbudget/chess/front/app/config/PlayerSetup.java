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

import com.lowbudget.chess.front.app.Constants;
import com.lowbudget.chess.model.PlayerColor;
import com.lowbudget.chess.model.uci.connect.Connectable;

import javax.xml.bind.annotation.*;
import java.util.Objects;

/**
 * Stores the configuration for a player in a new game (i.e. its name, color and time control information)
 */
@XmlRootElement(name="player")
@XmlAccessorType(XmlAccessType.FIELD)
public class PlayerSetup {

	@XmlAttribute
	private final String name;

	@XmlElementRefs( {
			@XmlElementRef( type = Connectable.LocalConnectionParams.class ),
			@XmlElementRef( type = Connectable.RemoteConnectionParams.class),
	})
	private final Connectable.Params connectParams;

	@XmlAttribute
	private final PlayerColor color;

	@XmlAttribute
	private final long time;

	@SuppressWarnings("unused")
	// for JAXB
	private PlayerSetup() {
		name = null;
		connectParams = null;
		color = null;
		time = 0;
	}

	public PlayerSetup(PlayerColor color) {
		this(Constants.UNKNOWN_NAME, color, null, 0);
	}

	public PlayerSetup(String name, PlayerColor color, long time) {
		this(name, color, null, time);
	}

	public PlayerSetup ofOppositeColor() {
		return new PlayerSetup(name, color.opposite(), connectParams, time);
	}

	public PlayerSetup(String name, PlayerColor color, Connectable.Params connectParams, long time) {
		this.name = Objects.requireNonNull(name);
		this.color = Objects.requireNonNull(color);
		this.connectParams = connectParams;
		this.time = time;
	}

	public boolean isHuman() {
		return connectParams == null;
	}

	public String getName() {
		return name;
	}

	public PlayerColor getColor() {
		return color;
	}

	public Connectable.Params getConnectParams() {
		return connectParams;
	}

	public long getTime() {
		return time;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		PlayerSetup that = (PlayerSetup) o;
		return time == that.time &&
				Objects.equals(name, that.name) &&
				Objects.equals(connectParams, that.connectParams) &&
				color == that.color;
	}

	@Override
	public int hashCode() {
		return Objects.hash(name, connectParams, color, time);
	}

}
