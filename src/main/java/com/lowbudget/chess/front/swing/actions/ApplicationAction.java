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

package com.lowbudget.chess.front.swing.actions;

import com.lowbudget.chess.front.app.UIApplication;
import com.lowbudget.chess.front.swing.common.UIAction;

import java.awt.*;

/**
 * Base class that should be extended by all our swing actions
 */
public abstract class ApplicationAction extends UIAction {

	protected final UIApplication application;

	protected ApplicationAction(UIApplication application, String titleKey) {
		this(application, titleKey, null, null);
	}

	protected ApplicationAction(UIApplication application, String titleKey, Image icon) {
		this(application, titleKey, icon, null);
	}

	protected ApplicationAction(UIApplication application, String titleKey, Image icon, String tooltipKey) {
		super(titleKey, icon, tooltipKey);
		this.application = application;
	}

}
