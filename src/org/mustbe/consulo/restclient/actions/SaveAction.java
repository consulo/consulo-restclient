package org.mustbe.consulo.restclient.actions;

import org.mustbe.consulo.restclient.RestClientHistoryManager;
import org.mustbe.consulo.restclient.ui.RestClientPanel;
import org.wiztools.restclient.bean.RequestBean;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;

/**
 * @author VISTALL
 * @since 20.11.13.
 */
public class SaveAction extends AnAction
{
	@Override
	public void actionPerformed(AnActionEvent e)
	{
		Project project = e.getProject();
		RequestBean requestBean = RestClientPanel.getInstance(project).getRequestBean();
		if(requestBean == null)
		{
			return;
		}
		String name = Messages.showInputDialog(project, "Name", "Enter Name", null);
		if(name == null)
		{
			return;
		}
		RestClientHistoryManager.getInstance(project).getRequests().put(name, requestBean);
	}

	@Override
	public void update(AnActionEvent e)
	{
		super.update(e);
		Project project = e.getProject();
		if(project == null)
		{
			return;
		}
		e.getPresentation().setEnabled(RestClientPanel.getInstance(project).getRequestBean() != null);
	}
}
