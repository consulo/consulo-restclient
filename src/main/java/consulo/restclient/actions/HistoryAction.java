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
import consulo.dataContext.DataContext;
import consulo.platform.base.icon.PlatformIconGroup;
import consulo.project.Project;
import consulo.restclient.RestClientHistoryManager;
import consulo.restclient.ui.RestClientPanel;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.ui.ex.RelativePoint;
import consulo.ui.ex.action.AnAction;
import consulo.ui.ex.action.AnActionEvent;
import consulo.ui.ex.action.DefaultActionGroup;
import consulo.ui.ex.popup.JBPopupFactory;
import consulo.ui.ex.popup.ListPopup;
import consulo.ui.image.Image;
import org.wiztools.restclient.bean.RequestBean;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import javax.swing.*;
import java.awt.event.MouseEvent;
import java.util.Map;

/**
 * @author VISTALL
 * @since 20.11.13.
 */
@ActionImpl(id = "RESTClientToolbarActions.History")
public class HistoryAction extends AnAction
{
	public HistoryAction()
	{
		super("History", null, PlatformIconGroup.vcsHistory());
	}

	@RequiredUIAccess
	@Override
	public void actionPerformed(@Nonnull AnActionEvent e)
	{
		final Project project = e.getData(Project.KEY);
		if(project == null)
		{
			return;
		}

		DefaultActionGroup actionGroup = new DefaultActionGroup();

		RestClientHistoryManager clientHistoryManager = RestClientHistoryManager.getInstance(project);

		final RequestBean requestBean = clientHistoryManager.getRequests().get(RestClientHistoryManager.LAST);

		for(final Map.Entry<String, RequestBean> entry : RestClientHistoryManager.getInstance(project).getRequests().entrySet())
		{
			if(entry.getValue() == requestBean)
			{
				continue;
			}

			AnAction anAction = new AnAction(entry.getKey())
			{
				@RequiredUIAccess
				@Override
				public void actionPerformed(@Nonnull AnActionEvent anActionEvent)
				{

					SwingUtilities.invokeLater(new Runnable()
					{
						@Override
						public void run()
						{
							RestClientPanel.getInstance(project).setRequestBean(entry.getValue());
						}
					});
				}
			};
			actionGroup.add(anAction);
		}

		if(requestBean != null)
		{
			AnAction anAction = new AnAction(RestClientHistoryManager.LAST)
			{
				@RequiredUIAccess
				@Override
				public void actionPerformed(@Nonnull AnActionEvent anActionEvent)
				{
					SwingUtilities.invokeLater(new Runnable()
					{
						@Override
						public void run()
						{
							RestClientPanel.getInstance(project).setRequestBean(requestBean);
						}
					});
				}
			};
			actionGroup.add(anAction);
		}

		DataContext dataContext = e.getDataContext();
		ListPopup popup = JBPopupFactory.getInstance().createActionGroupPopup(null, actionGroup, dataContext,
				JBPopupFactory.ActionSelectionAid.SPEEDSEARCH, false);

		if(e.getInputEvent() instanceof MouseEvent)
		{
			popup.show(new RelativePoint((MouseEvent) e.getInputEvent()));
		}
		else
		{

			popup.showInBestPositionFor(e.getDataContext());
		}
	}
}
