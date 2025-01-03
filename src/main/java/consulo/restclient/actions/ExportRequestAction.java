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
import consulo.ui.annotation.RequiredUIAccess;
import consulo.ui.ex.action.AnAction;
import consulo.ui.ex.action.AnActionEvent;
import consulo.ui.image.Image;

import jakarta.annotation.Nullable;

/**
 * @author VISTALL
 * @since 20.11.13.
 */
@ActionImpl(id = "RESTClientToolbarActions.Export")
public class ExportRequestAction extends AnAction
{
	public ExportRequestAction()
	{
		super("Export", null, PlatformIconGroup.actionsExport());
	}

	@RequiredUIAccess
	@Override
	public void actionPerformed(AnActionEvent e)
	{

	}
}
