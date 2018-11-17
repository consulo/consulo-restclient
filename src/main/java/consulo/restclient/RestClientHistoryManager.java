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

package consulo.restclient;

import java.net.MalformedURLException;
import java.util.LinkedHashMap;
import java.util.Map;

import org.jdom.Element;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.wiztools.restclient.bean.RequestBean;
import org.wiztools.restclient.util.XmlRequestUtil;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.components.StoragePathMacros;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;

/**
 * @author VISTALL
 * @since 20.11.13.
 */
@State(
		name="RestClientHistoryManager",
		storages= {
				@Storage(
						file = StoragePathMacros.WORKSPACE_FILE
				)}
)
public class RestClientHistoryManager implements PersistentStateComponent<Element>
{
	private static final Logger LOGGER = Logger.getInstance(RestClientHistoryManager.class);

	@Nonnull
	public static RestClientHistoryManager getInstance(@Nonnull Project project)
	{
		return ServiceManager.getService(project, RestClientHistoryManager.class);
	}

	public static final String LAST = "~ Last";

	private Map<String, RequestBean> myHistory = new LinkedHashMap<String, RequestBean>();

	public Map<String, RequestBean> getRequests()
	{
		return myHistory;
	}

	@Nullable
	@Override
	public Element getState()
	{
		if(myHistory.isEmpty())
		{
			return null;
		}
		Element element = new Element("state");
		for(Map.Entry<String, RequestBean> entry : myHistory.entrySet())
		{
			Element requestElement = XmlRequestUtil.getRequestElement(entry.getValue());
			requestElement.setAttribute("name", entry.getKey());
			element.addContent(requestElement);
		}
		return element;
	}

	@Override
	public void loadState(Element element)
	{
		for(Element child : element.getChildren())
		{
			try
			{
				RequestBean requestBean = XmlRequestUtil.getRequestBean(child);

				String name = child.getAttributeValue("name");

				myHistory.put(name, requestBean);
			}
			catch(MalformedURLException e)
			{
				RestClientHistoryManager.LOGGER.error(e);
			}
		}
	}
}
