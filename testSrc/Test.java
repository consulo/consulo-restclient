import java.net.URL;

import org.wiztools.restclient.HTTPClientRequestExecuter;
import org.wiztools.restclient.View;
import org.wiztools.restclient.bean.HTTPMethod;
import org.wiztools.restclient.bean.Request;
import org.wiztools.restclient.bean.RequestBean;
import org.wiztools.restclient.bean.RequestExecuter;
import org.wiztools.restclient.bean.Response;

/**
 * @author VISTALL
 * @since 19.11.13.
 */
public class Test
{
	public static void main(String[] args) throws Exception
	{
		RequestExecuter requestExecuter = new HTTPClientRequestExecuter();

		RequestBean request = new RequestBean();
		request.setUrl(new URL("http://must-be.org"));
		request.setMethod(HTTPMethod.GET);

		requestExecuter.execute(request, new View()
		{
			@Override
			public void doStart(Request request)
			{
				System.out.println("start");
			}

			@Override
			public void doResponse(Response response)
			{
				System.out.println(response.getStatusLine());
				System.out.println(response.getContentType());
				System.out.println(new String(response.getResponseBody()));
			}

			@Override
			public void doCancelled()
			{

			}

			@Override
			public void doEnd()
			{

			}

			@Override
			public void doError(String error)
			{
				System.out.println(error);
			}
		});
	}
}
