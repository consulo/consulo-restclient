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

package consulo.restclient.actions;

import consulo.restclient.RestClientHistoryManager;
import consulo.restclient.ui.RestClientPanel;
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
