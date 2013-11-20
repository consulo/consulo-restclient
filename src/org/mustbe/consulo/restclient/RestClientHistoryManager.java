package org.mustbe.consulo.restclient;

import java.net.MalformedURLException;
import java.util.LinkedHashMap;
import java.util.Map;

import org.consulo.lombok.annotations.Logger;
import org.consulo.lombok.annotations.ProjectService;
import org.jdom.Element;
import org.jetbrains.annotations.Nullable;
import org.wiztools.restclient.bean.RequestBean;
import org.wiztools.restclient.util.XmlRequestUtil;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.components.StoragePathMacros;

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
@ProjectService
@Logger
public class RestClientHistoryManager implements PersistentStateComponent<Element>
{
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
				LOGGER.error(e);
			}
		}
	}
}
