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

import consulo.annotation.component.ActionImpl;
import consulo.platform.base.icon.PlatformIconGroup;
import consulo.project.Project;
import consulo.restclient.ui.RestClientPanel;
import consulo.ui.ex.action.AnAction;
import consulo.ui.ex.action.AnActionEvent;
import consulo.ui.image.Image;
import org.wiztools.restclient.bean.RequestBean;

import javax.annotation.Nullable;
import javax.swing.*;

/**
 * @author VISTALL
 * @since 20.11.13.
 */
@ActionImpl(id = "RESTClientToolbarActions.Clean")
public class CleanAction extends AnAction
{
	public CleanAction()
	{
		super("Clean", null, PlatformIconGroup.actionsGc());
	}

	@Override
	public void actionPerformed(final AnActionEvent e)
	{
		Project project = e.getData(Project.KEY);
		if(project == null)
		{
			return;
		}

		SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				RequestBean requestBean = new RequestBean();

				RestClientPanel.getInstance(project).setRequestBean(requestBean);
			}
		});
	}
}
