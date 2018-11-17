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

import java.awt.BorderLayout;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;
import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import javax.annotation.Nullable;
import org.wiztools.restclient.bean.RequestBean;
import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.hint.HintUtil;
import com.intellij.lang.Language;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionPlaces;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.application.ApplicationInfo;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.Result;
import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.editor.event.DocumentAdapter;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.FileTypeRegistry;
import com.intellij.openapi.fileTypes.LanguageFileType;
import com.intellij.openapi.fileTypes.PlainTextFileType;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.ColoredListCellRenderer;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.ui.TabbedPaneWrapper;
import com.intellij.ui.TextFieldWithAutoCompletion;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.util.concurrency.AppExecutorUtil;
import com.intellij.util.containers.ArrayListSet;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.UIUtil;
import consulo.restclient.HttpHeader;
import consulo.restclient.RestClientHistoryManager;
import okhttp3.Call;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * @author VISTALL
 * @since 20.11.13.
 */
public class RestClientPanel extends Ref<Project> implements Disposable
{
	private static final String ourToolwindowId = "REST Client";

	@Nonnull
	public static RestClientPanel getInstance(@Nonnull Project project)
	{
		return ServiceManager.getService(project, RestClientPanel.class);
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
	private final JComboBox<Protocol> myHttpVersionBox;

	private RestRequestOrResponsePanel myRequestPanel;
	private RestRequestOrResponsePanel myResponsePanel;

	public RestClientPanel(@Nonnull final Project project)
	{
		super(project);

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
		panel2.add(myMethodComboBox, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints
				.SIZEPOLICY_FIXED, null, null, null, 0, false));
		myUrlTextField = new TextFieldWithAutoCompletionWithEnter(get(), new TextFieldWithAutoCompletion.StringsCompletionProvider(null, null)
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
				for(RequestBean o : RestClientHistoryManager.getInstance(get()).getRequests().values())
				{
					list.add(o.getUrl().toString());
				}
				return list;
			}
		}, false, null);
		myUrlTextField.setPlaceholder("URL. Hit 'Enter' for execute");
		panel2.add(myUrlTextField, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints
				.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
		myHttpVersionBox = new JComboBox<>();
		panel2.add(myHttpVersionBox, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints
				.SIZEPOLICY_FIXED, null, null, null, 0, false));

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

		myHttpVersionBox.setRenderer(new ColoredListCellRenderer<Protocol>()
		{
			@Override
			protected void customizeCellRenderer(@Nonnull JList list, Protocol value, int index, boolean selected, boolean hasFocus)
			{
				if(index == -1)
				{
					append("Protocol: ", SimpleTextAttributes.GRAY_ATTRIBUTES);
				}
				append(value.toString());
			}
		});

		for(Protocol httpVersion : Protocol.values())
		{
			if(httpVersion == Protocol.HTTP_1_0)
			{
				continue;
			}
			myHttpVersionBox.addItem(httpVersion);
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
			@Override
			public void run(@Nonnull ProgressIndicator progressIndicator)
			{
				final RequestBean request = getRequestBean();
				if(request == null)
				{
					return;
				}
				Request.Builder builder = new Request.Builder();
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
					builder = builder.addHeader(name, value);
				}
				builder = builder.header("User-Agent", ApplicationInfo.getInstance().getVersionName());

				String methodType = (String) myMethodComboBox.getSelectedItem();
				switch(methodType)
				{
					case "GET":
					case "HEAD":
					case "DELETE":
						builder = builder.method(methodType, null);
						break;
					case "PUT":
					case "POST":
					case "PATCH":
					case "TAG":
						String text = myRequestPanel.getText();
						RequestBody body = RequestBody.create(contentType == null ? null : MediaType.parse(contentType), text);
						builder = builder.method(methodType, body);
						break;
				}

				builder = builder.url(request.getUrl());

				Request build = builder.build();

				OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder();
				Set<Protocol> protocols = new ArrayListSet<>();
				protocols.add(request.getHttpVersion());
				protocols.add(Protocol.HTTP_1_1);

				clientBuilder.protocols(new ArrayList<>(protocols));
				clientBuilder.connectTimeout(1, TimeUnit.HOURS);
				clientBuilder.readTimeout(1, TimeUnit.HOURS);
				clientBuilder.writeTimeout(1, TimeUnit.HOURS);

				OkHttpClient client = clientBuilder.build();
				Call call = client.newCall(build);

				ScheduledFuture<?> cancelCheckFuture = AppExecutorUtil.getAppScheduledExecutorService().scheduleWithFixedDelay(() ->
				{
					try
					{
						progressIndicator.checkCanceled();
					}
					catch(ProcessCanceledException e)
					{
						call.cancel();
					}
				}, 1, 1, TimeUnit.SECONDS);

				long time = System.currentTimeMillis();
				try (Response response = call.execute())
				{
					showBallon("successed", time, response.code(), response.message(), MessageType.INFO);

					cancelCheckFuture.cancel(false);

					ApplicationManager.getApplication().invokeLater(() ->
					{
						myResponsePanel.setStatusLabel(String.valueOf(response.code()) + " " + response.message());

						List<HttpHeader> headersAsList = new ArrayList<>();
						Headers headers = response.headers();
						for(String headerName : headers.names())
						{
							headersAsList.add(new HttpHeader(headerName, StringUtil.join(headers.values(headerName), ";")));
						}

						myResponsePanel.setHeaders(headersAsList);
					});

					new WriteAction<Object>()
					{
						@Override
						protected void run(Result<Object> objectResult) throws Throwable
						{
							FileType fileType = null;

							ResponseBody body = response.body();
							if(body != null)
							{
								MediaType contentType = body.contentType();
								if(contentType != null)
								{
									String mime = contentType.type() + "/" + contentType.subtype();
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

								myResponsePanel.setText(fileType, body.string());

								RestClientHistoryManager.getInstance(project).getRequests().put(RestClientHistoryManager.LAST, request);

								SwingUtilities.invokeLater(() -> tabbedPaneWrapper.setSelectedComponent(myResponsePanel));
							}
						}
					}.execute();
				}
				catch(Throwable e)
				{
					cancelCheckFuture.cancel(false);

					if(!call.isCanceled())
					{
						showBallon("timeouted", time, -1, null, MessageType.ERROR);
					}
					else
					{
						showBallon("canceled", time, -1, null, MessageType.WARNING);
					}
				}
			}
		}.queue());

		AnAction restClientToolbarActions = ActionManager.getInstance().getAction("RESTClientToolbarActions");

		ActionToolbar actionToolbar = ActionManager.getInstance().createActionToolbar(ActionPlaces.UNKNOWN, (ActionGroup) restClientToolbarActions, false);

		myRootPanel.add(actionToolbar.getComponent(), BorderLayout.WEST);
	}

	private void showBallon(String reason, long startTime, int code, String message, MessageType type)
	{
		long l = System.currentTimeMillis();
		UIUtil.invokeLaterIfNeeded(() ->
		{
			String body = "Request " + reason + " in " + StringUtil.formatDuration(l - startTime);
			if(code != -1)
			{
				body += ". Status " + code + " " + message;
			}
			ToolWindowManager.getInstance(get()).notifyByBalloon(ourToolwindowId, type, body);
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
		request.setHttpVersion((Protocol) myHttpVersionBox.getSelectedItem());
		return request;
	}

	public void setRequestBean(RequestBean requestBean)
	{
		myHttpVersionBox.setSelectedItem(requestBean.getHttpVersion());
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
