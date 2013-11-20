package org.mustbe.consulo.restclient.actions;

import javax.swing.SwingUtilities;

import org.mustbe.consulo.restclient.ui.RestClientPanel;
import org.wiztools.restclient.bean.HTTPMethod;
import org.wiztools.restclient.bean.RequestBean;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;

/**
 * @author VISTALL
 * @since 20.11.13.
 */
public class CleanAction extends AnAction
{
	@Override
	public void actionPerformed(final AnActionEvent e)
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				RequestBean requestBean = new RequestBean();
				requestBean.setMethod(HTTPMethod.GET);

				RestClientPanel.getInstance(e.getProject()).setRequestBean(requestBean);
			}
		});
	}
}
