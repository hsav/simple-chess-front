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

import com.lowbudget.common.RecentHistory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.*;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Application's configuration.
 * This file can be serialized to XML with JAXB
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public final class Configuration {
	
	private static final Logger log = LoggerFactory.getLogger(Configuration.class);

	private static final String DEFAULT_PATH = ".";

	/**
	 * Denotes if any of the configuration information is modified
	 */
	@XmlTransient
	private boolean modified = false;

	/**
	 * Last folder used in open file dialog
	 */
	@XmlElement
	private File lastOpenFolderPath;

	/**
	 * History of the most recent games played (first item is the oldest one)
	 */
	@XmlElement(name = "recent-games")
	@XmlJavaTypeAdapter(RecentHistoryAdapter.class)
	private final RecentHistory<GameSetup> recentGames = new RecentHistory<>();

	/**
	 * Profiles of chess engines we know about
	 */
	@XmlElement
	private final EnginesConfig engines = new EnginesConfig();

	/**
	 * The last game setup used when creating a new game
	 */
	@XmlElement
	private GameSetup lastGameSetup;

	/**
	 * Board option that controls if legal moves should be highlighted. Default is {@code true}
	 */
	@XmlElement
	private boolean viewLegalMoves = true;

	/**
	 * Board option that controls if square attacks should be highlighted. Default is {@code true}
	 */
	@XmlElement
	private boolean viewAttacks = true;

	/**
	 * Board option that controls if the squares involving the last move played, should be highlighted.
	 * Default is {@code true}
	 */
	@XmlElement
	private boolean viewLastMove = true;

	/**
	 * The theme the user selected
	 */
	@XmlElement
	private String theme;

	/**
	 * Denotes if we always send the message "register later" in engines that require registration
	 */
	@XmlElement
	private boolean enginesAlwaysRegisterLater;

	/**
	 * The default port that should be used when entering details for starting an engine server
	 */
	@XmlElement
	private int defaultServerPort = 5000;

	@SuppressWarnings("WeakerAccess")
	public Configuration() {
		// Important Note: leave the constructor empty or at least do not attempt to load the configuration
		// from file. The reason is that JAXB uses this constructor when reading the object from file so this could
		// turn out to be a recursive call
	}

	public File getLastOpenFolderPath() {
		return lastOpenFolderPath;
	}

	public void setLastOpenFolderPath(File lastOpenFolderPath) {
		this.lastOpenFolderPath = lastOpenFolderPath;
		modified = true;
	}

	public GameSetup getLastGameSetup() {
		return lastGameSetup;
	}

	private void setLastGameSetup(GameSetup lastGameSetup) {
		this.lastGameSetup = lastGameSetup;
		modified = true;
	}

	public boolean isViewLegalMoves() {
		return viewLegalMoves;
	}

	public void setViewLegalMoves(boolean viewLegalMoves) {
		if (this.viewLegalMoves != viewLegalMoves) {
			this.viewLegalMoves = viewLegalMoves;
			modified = true;
		}
	}

	public boolean isViewAttacks() {
		return viewAttacks;
	}

	public void setViewAttacks(boolean viewAttacks) {
		if (this.viewAttacks != viewAttacks) {
			this.viewAttacks = viewAttacks;
			modified = true;
		}
	}

	public boolean isViewLastMove() {
		return viewLastMove;
	}

	public void setViewLastMove(boolean viewLastMove) {
		if (this.viewLastMove != viewLastMove) {
			this.viewLastMove = viewLastMove;
			modified = true;
		}
	}

	public String getTheme() {
		return theme;
	}

	public void setTheme(String theme) {
		Objects.requireNonNull(theme);
		if (!theme.equals(this.theme)) {
			this.theme = theme;
			modified = true;
		}
	}

	public int getMaxRecentCount() {
		return recentGames.getMaxCount();
	}

	public void setMaxRecentCount(int maxRecentCount) {
		if (recentGames.getMaxCount() != maxRecentCount) {
			recentGames.setMaxCount(maxRecentCount);
			modified = true;
		}
	}

	public void addRecentGameSetup(GameSetup item) {
		boolean added = recentGames.addRecentItem(item);
		if (added) {
			setLastGameSetup(item);
			modified = true;
		}
	}

	public boolean isEnginesAlwaysRegisterLater() {
		return enginesAlwaysRegisterLater;
	}

	public void setEnginesAlwaysRegisterLater(boolean value) {
		if (this.enginesAlwaysRegisterLater != value) {
			this.enginesAlwaysRegisterLater = value;
			modified = true;
		}
	}

	public int getDefaultServerPort() {
		return defaultServerPort;
	}

	public void setDefaultServerPort(int defaultPort) {
		if (this.defaultServerPort != defaultPort) {
			this.defaultServerPort = defaultPort;
			modified = true;
		}
	}

	public RecentHistory<GameSetup> getRecentGames() {
		return recentGames;
	}

	public EnginesConfig getEnginesConfig() {
		return this.engines;
	}

	private boolean isModified() {
		return modified || engines.isModified();
	}

	public void save() {
		if (!isModified()) {
			if (log.isDebugEnabled()) {
				log.debug("Configurations is not modified, no need to save");
			}
			return;
		}
		File configFile = getDefaultConfigFile();
		try {
			JAXBContext ctx = 	JAXBContext.newInstance(Configuration.class);
			Marshaller marshaller = ctx.createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
			marshaller.marshal(this, configFile);

			modified = false;
			engines.setModified(false);

			if (log.isDebugEnabled()) {
				log.debug("Configuration saved to file: " + configFile.getAbsolutePath());
			}
		} catch (JAXBException e) {
			log.error("Could not save config file", e);
		}
	}

	public void load() {
		load(getDefaultConfigFile());
	}

	public void load(File configFile) {
		Objects.requireNonNull(configFile, "Config file cannot be null!");
		if (!configFile.exists()) {
			if (log.isDebugEnabled()) {
				log.debug("Configuration file: {} does not exist. Skipped loading.", configFile.getAbsolutePath());
			}
			return;
		}
		try {
			JAXBContext ctx = 	JAXBContext.newInstance(Configuration.class);
			Unmarshaller unmarshaller = ctx.createUnmarshaller();
			Configuration config = (Configuration) unmarshaller.unmarshal(configFile);
			set(config);

			//load();
			String path = lastOpenFolderPath != null ? lastOpenFolderPath.getAbsolutePath() : null;
			lastOpenFolderPath = resolveLastFolder(path);

			modified = false;
			engines.setModified(false);
			if (log.isDebugEnabled()) {
				log.debug("Configuration loaded from file: " + configFile.getAbsolutePath());
			}
		} catch (JAXBException e) {
			log.error("Could not load config file", e);
		}
	}

	private void set(Configuration other) {
		this.lastOpenFolderPath = other.lastOpenFolderPath;
		this.viewLegalMoves = other.viewLegalMoves;
		this.viewAttacks = other.viewAttacks;
		this.viewLastMove = other.viewLastMove;
		this.theme = other.theme;

		this.enginesAlwaysRegisterLater = other.enginesAlwaysRegisterLater;
		this.defaultServerPort = other.defaultServerPort;

		// first set the desired max, otherwise the list will truncate using its current max
		this.recentGames.setMaxCount(other.recentGames.getMaxCount());
		this.recentGames.addAll(other.recentGames);

		this.engines.clear();
		this.engines.copyFrom(other.engines);

		this.lastGameSetup = other.lastGameSetup;

		this.modified = true;
	}

	private static File resolveLastFolder(String folder) {

		File result = null;
		String[] paths = { folder, System.getProperty("user.home"), DEFAULT_PATH};

		for (String path : paths) {
			if (isValidFolderPath(path)) {
				result = new File(path);
				break;
			}
		}
		if (log.isDebugEnabled()) {
			log.debug("Last folder resolved to: " + result);
		}
		return result;
	}

	private static File getDefaultConfigFile() {
		return new File("./config.xml");
	}

	private static boolean isValidFolderPath(String path) {
		if (path == null) {
			return false;
		}
		File file = new File(path);
		return file.isDirectory() && file.exists();
	}

	/**
	 * This class adapts the recent history object to a class that can be serialized with JAXB using the desired
	 * configuration (i.e. items in the list to be treated as {@link GameSetup} objects).
	 * It is possible to annotate directly the {@link RecentHistory} class however this would not be convenient (e.g.
	 * in case we need a recent history of another type of items) and it would insert a dependency from {@link RecentHistory}
	 * to the {@link GameSetup} class that is specific to our app (while {@link RecentHistory} is not).
	 */
	@XmlRootElement
	private static class GameSetupRecentHistory {
		@XmlAttribute
		private int maxCount;

		@XmlElement(name="game", type=GameSetup.class)
		private final List<GameSetup> items = new ArrayList<>();
	}

	/**
	 * {@link XmlAdapter} implementation for JAXB xml serialization/deserialization between
	 * {@link GameSetupRecentHistory} and {@link RecentHistory} objects
	 */
	private static class RecentHistoryAdapter extends XmlAdapter<GameSetupRecentHistory, RecentHistory<GameSetup>> {

		@Override
		public RecentHistory<GameSetup> unmarshal(GameSetupRecentHistory gameSetupRecentHistory) {
			RecentHistory<GameSetup> history = new RecentHistory<>(gameSetupRecentHistory.maxCount);
			for (GameSetup gs : gameSetupRecentHistory.items) {
				history.addRecentItem(gs);
			}
			return history;
		}

		@Override
		public GameSetupRecentHistory marshal(RecentHistory<GameSetup> recentHistory) {
			GameSetupRecentHistory adapted = new GameSetupRecentHistory();
			adapted.maxCount = recentHistory.getMaxCount();
			for (int i = 0; i < recentHistory.getItemCount(); i++) {
				adapted.items.add(recentHistory.getRecentItem(i));
			}
			return adapted;
		}
	}
}
