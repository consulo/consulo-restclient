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

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import consulo.annotation.component.ComponentScope;
import consulo.annotation.component.ServiceAPI;
import consulo.annotation.component.ServiceImpl;
import consulo.application.Application;
import consulo.application.WriteAction;
import consulo.application.progress.ProgressIndicator;
import consulo.application.progress.Task;
import consulo.application.util.concurrent.AppExecutorUtil;
import consulo.component.ProcessCanceledException;
import consulo.disposer.Disposable;
import consulo.document.event.DocumentAdapter;
import consulo.document.event.DocumentEvent;
import consulo.language.Language;
import consulo.language.editor.completion.CompletionParameters;
import consulo.language.editor.ui.awt.HintUtil;
import consulo.language.editor.ui.awt.TextFieldWithAutoCompletion;
import consulo.language.file.LanguageFileType;
import consulo.language.plain.PlainTextFileType;
import consulo.project.Project;
import consulo.project.ui.wm.ToolWindowManager;
import consulo.restclient.HttpHeader;
import consulo.restclient.RestClientHistoryManager;
import consulo.ui.NotificationType;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.ui.ex.JBColor;
import consulo.ui.ex.SimpleTextAttributes;
import consulo.ui.ex.action.*;
import consulo.ui.ex.awt.*;
import consulo.util.lang.StringUtil;
import consulo.virtualFileSystem.fileType.FileType;
import consulo.virtualFileSystem.fileType.FileTypeRegistry;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.apache.hc.client5.http.async.methods.SimpleBody;
import org.apache.hc.client5.http.async.methods.SimpleHttpRequest;
import org.apache.hc.client5.http.async.methods.SimpleHttpResponse;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;
import org.apache.hc.client5.http.impl.async.HttpAsyncClientBuilder;
import org.apache.hc.client5.http.impl.async.HttpAsyncClients;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.Header;
import org.wiztools.restclient.bean.RequestBean;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import javax.swing.*;
import java.awt.*;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * @author VISTALL
 * @since 20.11.13.
 */
@Singleton
@ServiceAPI(ComponentScope.PROJECT)
@ServiceImpl
public class RestClientPanel implements Disposable
{
	public static final String ourToolwindowId = "REST Client";

	@Nonnull
	public static RestClientPanel getInstance(@Nonnull Project project)
	{
		return project.getInstance(RestClientPanel.class);
	}

	private static final String[] ourSupportedMethods = new String[]{
			"GET",
			"DELETE",
			"HEAD",
			"POST",
			"PUT",
			"PATCH"
	};

	private final JComboBox<String> myMethodComboBox;
	private final JPanel myRootPanel;

	private final TextFieldWithAutoCompletionWithEnter myUrlTextField;

	private RestRequestOrResponsePanel myRequestPanel;
	private RestRequestOrResponsePanel myResponsePanel;

	private final Project myProject;

	@Inject
	public RestClientPanel(@Nonnull final Project project)
	{
		myProject = project;

		myRootPanel = new JPanel();
		myRootPanel.setLayout(new BorderLayout(0, 0));
		myRootPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(0, 0, 0, 5), null));
		final JPanel panel1 = new JPanel();
		panel1.setLayout(new BorderLayout(0, 0));
		myRootPanel.add(panel1, BorderLayout.CENTER);
		final JPanel panel2 = new JPanel();
		panel2.setLayout(new GridLayoutManager(1, 3, JBUI.emptyInsets(), -1, -1));
		panel1.add(panel2, BorderLayout.NORTH);
		myMethodComboBox = new JComboBox<>();
		Wrapper comboWrapper = new Wrapper(myMethodComboBox);
		comboWrapper.setBorder(JBUI.Borders.empty(0, 9, 0, 0));

		panel2.add(comboWrapper, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints
				.SIZEPOLICY_FIXED, null, null, null, 0, false));
		myUrlTextField = new TextFieldWithAutoCompletionWithEnter(myProject, new TextFieldWithAutoCompletion.StringsCompletionProvider(null, null)
		{
			@Nonnull
			@Override
			public Collection<String> getItems(String prefix, boolean cached, CompletionParameters parameters)
			{
				if(prefix == null)
				{
					return Collections.emptyList();
				}

				List<String> list = new ArrayList<>();
				for(RequestBean o : RestClientHistoryManager.getInstance(myProject).getRequests().values())
				{
					list.add(o.getUrl().toString());
				}
				return list;
			}
		}, false, null);
		myUrlTextField.setPlaceholder("URL. Hit 'Enter' for execute");
		panel2.add(myUrlTextField, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints
				.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));

		final TabbedPaneWrapper tabbedPaneWrapper = new TabbedPaneWrapper(this);
		panel1.add(tabbedPaneWrapper.getComponent(), BorderLayout.CENTER);

		myRequestPanel = new RestRequestOrResponsePanel(project, true);
		myResponsePanel = new RestRequestOrResponsePanel(project, false);

		tabbedPaneWrapper.addTab("Request", myRequestPanel);
		tabbedPaneWrapper.addTab("Response", myResponsePanel);
		tabbedPaneWrapper.setSelectedComponent(myResponsePanel);

		myMethodComboBox.setRenderer(new ColoredListCellRenderer<String>()
		{
			@Override
			protected void customizeCellRenderer(@Nonnull JList list, String value, int index, boolean selected, boolean hasFocus)
			{
				if(index == -1)
				{
					append("Method: ", SimpleTextAttributes.GRAY_ATTRIBUTES);
				}
				append(value);
			}
		});

		for(String httpMethod : ourSupportedMethods)
		{
			myMethodComboBox.addItem(httpMethod);
		}

		myUrlTextField.addDocumentListener(new DocumentAdapter()
		{
			@Override
			public void documentChanged(DocumentEvent documentEvent)
			{
				try
				{
					new URL(documentEvent.getDocument().getText());
					myUrlTextField.setBackground(UIUtil.getTextFieldBackground());
				}
				catch(MalformedURLException e)
				{
					myUrlTextField.setBackground(HintUtil.ERROR_COLOR);
				}
			}
		});

		myUrlTextField.setEnterAction(() -> new Task.Backgroundable(project, "Executing request to: " + myUrlTextField.getText(), true)
		{
			private SimpleBody myBody;
			private RequestBean myRequestBean;

			@Override
			public void run(@Nonnull ProgressIndicator progressIndicator)
			{
				final RequestBean request = getRequestBean();
				if(request == null)
				{
					return;
				}
				myRequestBean = request;

				String methodType = (String) myMethodComboBox.getSelectedItem();
				SimpleHttpRequest httpRequest;
				try
				{
					httpRequest = new SimpleHttpRequest(methodType, request.getUrl().toURI());
				}
				catch(URISyntaxException e)
				{
					return;
				}

				String contentType = null;
				for(HttpHeader httpHeader : myRequestPanel.getHeaders())
				{
					String name = httpHeader.getName();
					String value = httpHeader.getValue();
					if(name.equals("Content-Type"))
					{
						contentType = value;
					}
					if(StringUtil.isEmpty(name))
					{
						continue;
					}
					httpRequest.setHeader(name, value);
				}

				switch(methodType)
				{
					case "GET":
					case "HEAD":
					case "DELETE":
						break;
					case "PUT":
					case "POST":
					case "PATCH":
					case "TAG":
						String text = myRequestPanel.getText();
						ContentType c = contentType == null ? null : ContentType.parse(contentType);

						if(text != null)
						{
							httpRequest.setBody(text, c);
						}
						break;
				}

				httpRequest.setHeader("User-Agent", Application.get().getName().get());

				HttpAsyncClientBuilder httpClientBuilder = HttpAsyncClients.custom();
				RequestConfig.Builder requestConfigBuilder = RequestConfig.custom();
				requestConfigBuilder.setConnectTimeout(1, TimeUnit.HOURS);
				requestConfigBuilder.setResponseTimeout(1, TimeUnit.HOURS);
				requestConfigBuilder.setConnectionRequestTimeout(1, TimeUnit.HOURS);

				httpClientBuilder.setDefaultRequestConfig(requestConfigBuilder.build());

				Future<?> cancelFuture = CompletableFuture.completedFuture(null);
				Future<SimpleHttpResponse> callFuture = CompletableFuture.completedFuture(null);

				long time = System.currentTimeMillis();
				try (CloseableHttpAsyncClient httpClient = httpClientBuilder.build())
				{
					httpClient.start();

					callFuture = httpClient.execute(httpRequest, null);

					final Future<SimpleHttpResponse> finalCallFuture = callFuture;
					cancelFuture = AppExecutorUtil.getAppScheduledExecutorService().scheduleWithFixedDelay(() ->
					{
						try
						{
							progressIndicator.checkCanceled();
						}
						catch(ProcessCanceledException e)
						{
							finalCallFuture.cancel(true);
						}
					}, 1, 1, TimeUnit.SECONDS);

					SimpleHttpResponse response = callFuture.get(1, TimeUnit.HOURS);

					showBallon("success", time, response.getCode(), response.getReasonPhrase(), NotificationType.INFO);

					cancelFuture.cancel(false);

					Application.get().invokeLater(() ->
					{
						myResponsePanel.setStatusLabel(String.valueOf(response.getCode()) + " " + response.getReasonPhrase());

						List<HttpHeader> headersAsList = new ArrayList<>();
						Header[] headers = response.getHeaders();
						for(Header header : headers)
						{
							headersAsList.add(new HttpHeader(header.getName(), header.getValue()));
						}

						myResponsePanel.setHeaders(headersAsList);
					});

					myBody = response.getBody();
				}
				catch(Throwable e)
				{
					e.printStackTrace();
					cancelFuture.cancel(false);

					if(e instanceof CancellationException)
					{
						showBallon("canceled", time, -1, null, NotificationType.WARNING);
					}
					else
					{
						showBallon("timeouted", time, -1, null, NotificationType.ERROR);
					}
				}
			}

			@RequiredUIAccess
			@Override
			public void onSuccess()
			{
				if(myBody == null)
				{
					return;
				}

				WriteAction.run(() ->
				{
					FileType fileType = null;

					ContentType contType = myBody.getContentType();
					if(contType != null)
					{
						String mime = contType.getMimeType();
						Collection<Language> languages = Language.findInstancesByMimeType(mime);
						if(!languages.isEmpty())
						{
							for(FileType type : FileTypeRegistry.getInstance().getRegisteredFileTypes())
							{
								if(type instanceof LanguageFileType && languages.contains(((LanguageFileType) type).getLanguage()))
								{
									fileType = type;
								}
							}
						}
					}

					if(fileType == null)
					{
						fileType = PlainTextFileType.INSTANCE;
					}

					myResponsePanel.setText(fileType, myBody.getBodyText());

					RestClientHistoryManager.getInstance(project).getRequests().put(RestClientHistoryManager.LAST, myRequestBean);

					SwingUtilities.invokeLater(() -> tabbedPaneWrapper.setSelectedComponent(myResponsePanel));
				});
			}
		}.queue());

		AnAction restClientToolbarActions = ActionManager.getInstance().getAction("RESTClientToolbarActions");

		ActionToolbar actionToolbar = ActionManager.getInstance().createActionToolbar(ActionPlaces.UNKNOWN, (ActionGroup) restClientToolbarActions, false);
		actionToolbar.setTargetComponent(myRootPanel);
		actionToolbar.getComponent().setBorder(JBUI.Borders.customLine(JBColor.border(), 0, 0, 0, 1));
		myRootPanel.add(actionToolbar.getComponent(), BorderLayout.WEST);
	}

	private void showBallon(String reason, long startTime, int code, String message, NotificationType type)
	{
		long l = System.currentTimeMillis();
		UIUtil.invokeLaterIfNeeded(() ->
		{
			String body = "Request " + reason + " in " + StringUtil.formatDuration(l - startTime);
			if(code != -1)
			{
				body += ". Status " + code + " " + message;
			}
			ToolWindowManager.getInstance(myProject).notifyByBalloon(ourToolwindowId, type, body);
		});
	}

	@Nullable
	public RequestBean getRequestBean()
	{
		URL url;
		try
		{
			url = new URL(myUrlTextField.getText());
		}
		catch(MalformedURLException e)
		{
			return null;
		}
		RequestBean request = new RequestBean();
		for(HttpHeader header : myRequestPanel.getHeaders())
		{
			request.addHeader(header.getName(), header.getValue());
		}
		request.setUrl(url);
		request.setMethod((String) myMethodComboBox.getSelectedItem());
		return request;
	}

	public void setRequestBean(RequestBean requestBean)
	{
		myMethodComboBox.setSelectedItem(requestBean.getMethod());
		URL url = requestBean.getUrl();
		myUrlTextField.setText(url == null ? "" : url.toString());
	}

	public JPanel getRootPanel()
	{
		return myRootPanel;
	}

	@Override
	public void dispose()
	{
	}
}
