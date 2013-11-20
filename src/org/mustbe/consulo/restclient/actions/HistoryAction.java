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
