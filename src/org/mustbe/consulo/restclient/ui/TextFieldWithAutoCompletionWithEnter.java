package org.mustbe.consulo.restclient.ui;

import java.awt.event.KeyEvent;

import javax.swing.KeyStroke;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.intellij.openapi.project.Project;
import com.intellij.ui.TextFieldWithAutoCompletion;
import com.intellij.ui.TextFieldWithAutoCompletionListProvider;

/**
 * @author VISTALL
 * @since 20.11.13.
 */
public class TextFieldWithAutoCompletionWithEnter extends TextFieldWithAutoCompletion<String>
{
	private Runnable myEnterAction;

	public TextFieldWithAutoCompletionWithEnter(Project project, @NotNull TextFieldWithAutoCompletionListProvider<String> provider,
			boolean showAutocompletionIsAvailableHint, @Nullable String text)
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
