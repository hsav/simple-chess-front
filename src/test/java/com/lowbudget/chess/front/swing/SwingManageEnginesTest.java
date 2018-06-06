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

package com.lowbudget.chess.front.swing;

import com.lowbudget.chess.model.uci.client.UCIClient;
import com.lowbudget.chess.model.uci.connect.Connectable.LocalConnectionParams;
import com.lowbudget.chess.front.swing.ComponentNames.DialogManageEngines;
import org.assertj.swing.fixture.DialogFixture;
import org.assertj.swing.fixture.JFileChooserFixture;
import org.assertj.swing.fixture.JListFixture;
import org.junit.Before;
import org.junit.Test;

import static com.lowbudget.chess.front.swing.SwingHelper.*;

public class SwingManageEnginesTest extends BaseSwingTest {

	private static final int DEFAULT_TIME_OUT = 6;

	private DialogFixture dialog;
	private JListFixture list;

	@Before
	@Override
	public void setUp() {
		super.setUp();
		click(manageEnginesAction);
		dialog = assertDialogExists(MANAGE_ENGINES_DIALOG_TITLE);
		list = dialog.list(DialogManageEngines.ENGINES_LIST);
	}

	@Test
	public void testDisplaysManageEnginesDialog() {
		// from our test config there should be 2 items in the list
		list.requireItemCount(2);
	}

	@Test
	public void testDeleteEngineConfig() {
		list.selectItem(1);
		dialog.button(DialogManageEngines.DELETE_BUTTON).click();

		assertAndDismissDialogByTitle(DELETE_ENGINE_DIALOG_TITLE, BUTTON_OK);
		list.requireItemCount(1);
	}

	@Test
	public void testEditEngineConfig() {
		list.selectItem(1);
		dialog.button(DialogManageEngines.EDIT_BUTTON).click();

		assertAndDismissDialogByTitle(ENGINE_SETTINGS_DIALOG_TITLE, BUTTON_OK);
	}

	@Test
	public void testAddEngineConfig() {
		// we need an actual client for this test
		application.setUCIClientFactory(UCIClient.DEFAULT_FACTORY);

		dialog.button(DialogManageEngines.ADD_BUTTON).click();

		DialogFixture addDialog = assertDialogExists(ADD_ENGINE_DIALOG_TITLE);
		addDialog.button(DialogManageEngines.SELECT_ENGINE_FILE_BUTTON).click();

		// we will try to add the first engine that is already added which we know is a local file
		LocalConnectionParams localParams = (LocalConnectionParams) application.getEnginesConfig().get(0).getParams();

		JFileChooserFixture fileChooserFixture = window.fileChooser();
		fileChooserFixture.fileNameTextBox().setText(localParams.getExecutable().getAbsolutePath());
		fileChooserFixture.approve();

		addDialog.button(ComponentNames.OK_BUTTON).click();
		assertAndDismissDialogByTitle(SUCCESS_DIALOG_TITLE, BUTTON_OK);
	}

	@Test
	public void testTestEngineConfigWithSuccess() {
		// we need an actual client for this test
		application.setUCIClientFactory(UCIClient.DEFAULT_FACTORY);

		list.selectItem(0);
		dialog.button(DialogManageEngines.TEST_BUTTON).click();

		// application's connection tester times out after 5 seconds by default
		assertDialogExists(SUCCESS_DIALOG_TITLE, DEFAULT_TIME_OUT);
	}

	@Test
	public void testTestEngineConfigWithTimeOut() {
		list.selectItem(0);
		dialog.button(DialogManageEngines.TEST_BUTTON).click();

		// application's connection tester times out after 5 seconds by default, so there should be a success dialog until then
		assertDialogExists(TEST_ENGINE_TIME_OUT_DIALOG_TITLE, DEFAULT_TIME_OUT);
	}

	@Test
	public void testTestEngineConfigWithError() {
		// we need an actual client for this test
		application.setUCIClientFactory(UCIClient.DEFAULT_FACTORY);

		// second item is the local server which is not started so we should get an error
		list.selectItem(1);
		dialog.button(DialogManageEngines.TEST_BUTTON).click();

		// application's connection tester times out after 5 seconds by default, so there should be a success dialog until then
		assertDialogExists(TEST_ENGINE_UNKNOWN_ERROR_DIALOG_TITLE, DEFAULT_TIME_OUT);
	}
}
