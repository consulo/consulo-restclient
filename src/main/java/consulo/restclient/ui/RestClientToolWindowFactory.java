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

import consulo.annotation.component.ExtensionImpl;
import consulo.application.dumb.DumbAware;
import consulo.localize.LocalizeValue;
import consulo.platform.base.icon.PlatformIconGroup;
import consulo.project.Project;
import consulo.project.ui.wm.ToolWindowFactory;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.ui.ex.content.ContentFactory;
import consulo.ui.ex.toolWindow.ToolWindow;
import consulo.ui.ex.toolWindow.ToolWindowAnchor;
import consulo.ui.image.Image;

import javax.annotation.Nonnull;

/**
 * @author VISTALL
 * @since 20.11.13.
 */
@ExtensionImpl
public class RestClientToolWindowFactory implements ToolWindowFactory, DumbAware
{
	@Nonnull
	@Override
	public String getId()
	{
		return RestClientPanel.ourToolwindowId;
	}

	@RequiredUIAccess
	@Override
	public void createToolWindowContent(@Nonnull Project project, @Nonnull ToolWindow toolWindow)
	{
		ContentFactory contentFactory = toolWindow.getContentManager().getFactory();

		RestClientPanel restClientPanel = RestClientPanel.getInstance(project);

		toolWindow.getContentManager().addContent(contentFactory.createContent(restClientPanel.getRootPanel(), "", true));
	}

	@Nonnull
	@Override
	public ToolWindowAnchor getAnchor()
	{
		return ToolWindowAnchor.BOTTOM;
	}

	@Override
	public boolean canCloseContents()
	{
		return false;
	}

	@Nonnull
	@Override
	public Image getIcon()
	{
		return PlatformIconGroup.toolwindowsWebtoolwindow();
	}

	@Nonnull
	@Override
	public LocalizeValue getDisplayName()
	{
		return LocalizeValue.localizeTODO("REST Client");
	}
}
