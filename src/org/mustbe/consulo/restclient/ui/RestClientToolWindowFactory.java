package org.mustbe.consulo.restclient.ui;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.ContentFactory;

/**
 * @author VISTALL
 * @since 20.11.13.
 */
public class RestClientToolWindowFactory implements ToolWindowFactory
{
	@Override
	public void createToolWindowContent(Project project, ToolWindow toolWindow)
	{
		ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();

		RestClientPanel restClientPanel = RestClientPanel.getInstance(project);

		toolWindow.getContentManager().addContent(contentFactory.createContent(restClientPanel.getRootPanel(), "", true));
	}
}
