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

package com.lowbudget.chess.front.app.impl;

import com.lowbudget.chess.front.app.Player;
import com.lowbudget.chess.front.app.UIApplication.DialogCancelListener;
import com.lowbudget.chess.front.app.UIApplication.RegistrationDialogSuccessListener;
import com.lowbudget.chess.front.app.UIApplication.WaitDialogHolder;
import com.lowbudget.chess.model.PlayerColor;
import com.lowbudget.chess.model.uci.engine.UCIEngineConfig;
import com.lowbudget.chess.model.uci.engine.options.UCIOption;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A listener for player events used by the application.
 * Displays appropriate dialogs (like an informative message or a warning) appropriate for each player event and
 * manages any {@link WaitDialogHolder} objects used by each player (applies only for engine players).
 *
 * Important note: when displaying dialogs in this listener it is important to use {@link HeadlessApplication#runInUIThread(Runnable)}
 * to allow the rest of the game model's listeners to be executed. Failure to do that will most probably result in
 * undesired behaviour
 */
public class ApplicationPlayerListener extends Player.PlayerListenerAdapter {

	private final HeadlessApplication application;
	private final Map<Player, WaitDialogHolder> waitDialogs = new HashMap<>();

	ApplicationPlayerListener(HeadlessApplication application) {
		this.application = application;
	}

	@Override
	public void onPlayerStarted(Player player) {
		if (player.isEngine()) {
			WaitDialogHolder dialogHolder = application.createWaitDialogHolder();
			waitDialogs.put(player, dialogHolder);
			application.runInUIThread( () ->
				dialogHolder.openDialog(application.getGameModel()::stopGame)
			);
		}
	}

	@Override
	public void onPlayerStopped(Player player) {
		closeDialogHolder(player);
	}

	@Override
	public void onPlayerReady(Player player) {
		closeDialogHolder(player);
	}

	@Override
	public void onPlayerError(Exception exception) {
		application.runInUIThread( () ->
			application.showExceptionMessage(exception)
		);
	}

	@Override
	public void onPlayerUnexpectedDisconnection() {
		application.runInUIThread(
			application::showEnginePlayerUnexpectedDisconnectionErrorMessage
		);
	}

	@Override
	public void onPlayerCopyProtectionError(String name, PlayerColor color) {
		application.runInUIThread( () ->
			application.showEnginePlayerCopyProtectionWarningMessage(name, color)
		);
	}

	@Override
	public void onPlayerRegistrationRequired(Player player, RegistrationDialogSuccessListener successListener, DialogCancelListener cancelListener) {
		application.runInUIThread( () ->
			application.showEnginePlayerRegistrationDialog(player, successListener, cancelListener)
		);
	}

	@Override
	public void onEngineInfoAvailable(String info) {
		application.publishOnEngineInfoAvailable(info);
	}

	@Override
	public List<UCIOption> onEngineConfigAvailable(UCIEngineConfig engineConfig) {
		return application.mergeEngineConfig(engineConfig);
	}

	private void closeDialogHolder(Player player) {
		if (player.isEngine()) {
			WaitDialogHolder dialogHolder = waitDialogs.remove(player);
			if (dialogHolder != null) {
				dialogHolder.close();
			}
		}
	}
}
