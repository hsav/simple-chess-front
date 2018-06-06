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

package com.lowbudget.chess.front.app;

import com.lowbudget.chess.front.app.impl.HeadlessApplication;
import com.lowbudget.common.Messages;
import org.assertj.swing.fixture.AbstractComponentFixture;
import org.assertj.swing.fixture.FrameFixture;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public final class Helper {

	private Helper() {}

	public static final File TEST_CONFIG_FILE = new File("./src/test/resources/config-test.xml");
	public static final File TEST_SAVE_GAME_FILE = new File("./test-game.pgn");

	@SuppressWarnings("SpellCheckingInspection")
	//public static final String DEFAULT_ENGINE_NAME_PREFIX = "Stockfish";

	@FunctionalInterface
	public interface Locator {
		AbstractComponentFixture locate(FrameFixture window);

		class Factory {
			public static Locator button(String name) {
				return (window) -> window.button(name);
			}
			public static Locator menu(String... path) {
				return (window) -> window.menuItemWithPath( menuPath(path));
			}
		}
	}

	private static String[] menuPath(String... keys) {
		List<String> children = new ArrayList<>();
		for (String key : keys) {
			children.add(Messages.get(key));
		}
		return children.toArray(new String[0]);
	}

	public static class TestApplicationFactory implements UIApplication.UIApplicationFactory {

		@Override
		public UIApplication createApplication() {
			UIApplication app = new HeadlessApplication();
			app.initialize();
			return app;
		}
	}

	@SuppressWarnings("WeakerAccess")
	public static class CallableAction {
		private final Supplier<Boolean> action;

		CallableAction(Supplier<Boolean> action) {
			this.action = action;
		}

		public boolean isAllowed() {
			return action.get();
		}

		public static CallableAction of(Supplier<Boolean> action) {
			return new CallableAction(action);
		}
	}
}
