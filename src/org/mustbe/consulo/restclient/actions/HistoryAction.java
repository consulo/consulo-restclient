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

package org.mustbe.consulo.restclient.actions;

import java.awt.event.MouseEvent;

import javax.swing.SwingUtilities;

import org.mustbe.consulo.restclient.RestClientHistoryManager;
import org.mustbe.consulo.restclient.ui.RestClientPanel;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.ui.popup.ListPopup;
import com.intellij.ui.awt.RelativePoint;
import lombok.val;

/**
 * @author VISTALL
 * @since 20.11.13.
 */
public class HistoryAction extends AnAction
{
	@Override
	public void actionPerformed(AnActionEvent e)
	{
		val project = e.getProject();

		val actionGroup = new DefaultActionGroup();

		val clientHistoryManager = RestClientHistoryManager.getInstance(e.getProject());

		val requestBean = clientHistoryManager.getRequests().get(RestClientHistoryManager.LAST);

		for(val entry : RestClientHistoryManager.getInstance(e.getProject()).getRequests().entrySet())
		{
			if(entry.getValue() == requestBean)
			{
				continue;
			}

			AnAction anAction = new AnAction(entry.getKey())
			{
				@Override
				public void actionPerformed(AnActionEvent anActionEvent)
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
				@Override
				public void actionPerformed(AnActionEvent anActionEvent)
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
