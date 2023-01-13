/*
 * Copyright 2013-2015 must-be.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package consulo.restclient.ui;

import consulo.language.editor.ui.awt.TextFieldWithAutoCompletion;
import consulo.language.editor.ui.awt.TextFieldWithAutoCompletionListProvider;
import consulo.project.Project;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.swing.*;
import java.awt.event.KeyEvent;

/**
 * @author VISTALL
 * @since 20.11.13.
 */
public class TextFieldWithAutoCompletionWithEnter extends TextFieldWithAutoCompletion<String>
{
	private Runnable myEnterAction;

	public TextFieldWithAutoCompletionWithEnter(Project project, @Nonnull TextFieldWithAutoCompletionListProvider<String> provider, boolean showAutocompletionIsAvailableHint, @Nullable String text)
	{
		super(project, provider, showAutocompletionIsAvailableHint, text);
	}

	@Override
	protected boolean processKeyBinding(KeyStroke ks, KeyEvent e, int condition, boolean pressed)
	{
		if(super.processKeyBinding(ks, e, condition, pressed))
		{
			return true;
		}

		if(e.getKeyCode() == KeyEvent.VK_ENTER && !pressed)
		{
			if(myEnterAction != null)
			{
				myEnterAction.run();
			}
			return true;
		}

		return false;
	}

	public void setEnterAction(Runnable enterAction)
	{
		myEnterAction = enterAction;
	}
}
