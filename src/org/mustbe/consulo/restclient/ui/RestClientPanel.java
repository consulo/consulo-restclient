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

package org.mustbe.consulo.restclient.ui;

import java.awt.BorderLayout;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;

import org.consulo.lombok.annotations.ProjectService;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.restclient.RestClientHistoryManager;
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
import com.intellij.openapi.application.Result;
import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.event.DocumentAdapter;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.editor.impl.EditorFactoryImpl;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.FileTypeRegistry;
import com.intellij.openapi.fileTypes.LanguageFileType;
import com.intellij.openapi.fileTypes.PlainTextFileType;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.ui.ColoredListCellRendererWrapper;
import com.intellij.ui.EditorTextField;
import com.intellij.ui.JBSplitter;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.ui.TextFieldWithAutoCompletion;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.table.JBTable;
import com.intellij.util.ui.ColumnInfo;
import com.intellij.util.ui.ListTableModel;
import com.intellij.util.ui.UIUtil;
import com.squareup.okhttp.Call;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.Headers;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Protocol;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.squareup.okhttp.ResponseBody;
import lombok.val;

/**
 * @author VISTALL
 * @since 20.11.13.
 */
@ProjectService
public class RestClientPanel extends Ref<Project>  implements Disposable
{
	private static final String[] ourSupportedMethods = new String[] {
			"GET",
			"DELETE",
			"HEAD",
			//TODO [VISTALL] support request body
			//"POST",
			//"PUT",
			//"PATCH"
	};
	private JComboBox myMethodComboBox;
	private JPanel myRootPanel;
	private JTabbedPane myTabPanel;
	private JPanel myResponseTab;
	private JBSplitter myResponseSplitter;

	private TextFieldWithAutoCompletionWithEnter myUrlTextField;
	private JLabel myResultLabel;
	private JComboBox myHttpVersionBox;

	private List<Pair<String, String>> myHeaders = new ArrayList<Pair<String, String>>();

	private EditorTextField myEditorTextField;

	public RestClientPanel(final Project project)
	{
		super(project);

		myMethodComboBox.setRenderer(new ColoredListCellRendererWrapper<String>()
		{
			@Override
			protected void doCustomize(JList list, String value, int index, boolean selected, boolean hasFocus)
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

		myHttpVersionBox.setRenderer(new ColoredListCellRendererWrapper<Protocol>() {
			@Override
			protected void doCustomize(JList list, Protocol value, int index, boolean selected, boolean hasFocus)
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

		myResponseSplitter.setProportion(0.2f);
		myResponseSplitter.setSplitterProportionKey("RestClientPanel#response");

		ListTableModel<Pair<String, String>> model = new ListTableModel<Pair<String, String>>(new ColumnInfo[]{
				new ColumnInfo<Pair<String, String>, String>("HTTP Header")
				{
					@Nullable
					@Override
					public String valueOf(Pair<String, String> pair)
					{
						return pair.getFirst();
					}
				},
				new ColumnInfo<Pair<String, String>, String>("Value")
				{
					@Nullable
					@Override
					public String valueOf(Pair<String, String> pair)
					{
						return pair.getSecond();
					}
				}
		}, myHeaders, 0);


		final JBTable table = new JBTable(model);
		table.setShowColumns(true);

		myResponseSplitter.setFirstComponent(new JBScrollPane(table));

		myEditorTextField = new EditorTextField(EditorFactory.getInstance().createDocument(""), project, PlainTextFileType.INSTANCE, true, false)
		{
			@Override
			protected EditorEx createEditor()
			{
				EditorEx editor = super.createEditor();
				editor.getScrollPane().setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
				return editor;
			}
		};
		myEditorTextField.setFontInheritedFromLAF(false);

		myResponseSplitter.setSecondComponent(new JBScrollPane(myEditorTextField));

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

		myUrlTextField.setEnterAction(new Runnable()
		{
			@Override
			public void run()
			{
				new Task.Backgroundable(project, "Executing request to: " + myUrlTextField.getText(), false)
				{
					@Override
					public void run(@NotNull ProgressIndicator progressIndicator)
					{
						val request = getRequestBean();
						if(request == null)
						{
							return;
						}
						Request.Builder builder = new Request.Builder();
						builder = builder.method((String) myMethodComboBox.getSelectedItem(), null);
						builder = builder.url(request.getUrl());
						builder = builder.header("User-Agent", ApplicationInfo.getInstance().getVersionName());
						Request build = builder.build();

						OkHttpClient client = new OkHttpClient();
						client.setProtocols(Arrays.<Protocol>asList(request.getHttpVersion()));
						Call call = client.newCall(build);
						call.enqueue(new Callback()
						{
							@Override
							public void onFailure(Request request, final IOException e)
							{
								SwingUtilities.invokeLater(new Runnable()
								{
									@Override
									public void run()
									{
										Messages.showErrorDialog(e.getMessage(), "Error While Processing Request");
									}
								});
							}

							@Override
							public void onResponse(final Response response) throws IOException
							{
								new WriteAction<Object>()
								{
									@Override
									protected void run(Result<Object> objectResult) throws Throwable
									{
										myResultLabel.setText(String.valueOf(response.code()));
										myHeaders.clear();

										Headers headers = response.headers();
										for(String headerName : headers.names())
										{
											myHeaders.add(new Pair<String, String>(headerName, StringUtil.join(headers.values(headerName), ";")));
										}

										table.revalidate();
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

											EditorFactoryImpl editorFactory = (EditorFactoryImpl) EditorFactory.getInstance();
											Document document = editorFactory.createDocument(body.string(), true,
													true);

											myEditorTextField.setNewDocumentAndFileType(fileType, document);

											RestClientHistoryManager.getInstance(project).getRequests().put(RestClientHistoryManager.LAST, request);

											SwingUtilities.invokeLater(new Runnable()
											{
												@Override
												public void run()
												{
													myTabPanel.setSelectedComponent(myResponseTab);
												}
											});
										}
									}
								}.execute();
							}
						});
					}
				}.queue();
			}
		});

		AnAction restClientToolbarActions = ActionManager.getInstance().getAction("RESTClientToolbarActions");

		ActionToolbar actionToolbar = ActionManager.getInstance().createActionToolbar(ActionPlaces.UNKNOWN, (ActionGroup) restClientToolbarActions,
				false);

		myRootPanel.add(actionToolbar.getComponent(), BorderLayout.WEST);
	}

	@Nullable
	public RequestBean getRequestBean()
	{
		URL url = null;
		try
		{
			url = new URL(myUrlTextField.getText());
		}
		catch(MalformedURLException e)
		{
			return null;
		}
		RequestBean request = new RequestBean();
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

	private void createUIComponents()
	{
		myUrlTextField = new TextFieldWithAutoCompletionWithEnter(get(), new TextFieldWithAutoCompletion.StringsCompletionProvider(null, null)
		{
			@NotNull
			@Override
			public Collection<String> getItems(String prefix, boolean cached, CompletionParameters parameters)
			{
				if(prefix == null)
				{
					return Collections.emptyList();
				}

				List<String> list = new ArrayList<String>();
				for(RequestBean o : RestClientHistoryManager.getInstance(get()).getRequests().values())
				{
					list.add(o.getUrl().toString());
				}
				return list;
			}
		}, false, null);
		myUrlTextField.setPlaceholder("URL");

	}

	@Override
	public void dispose()
	{
		myEditorTextField.setDocument(null);
	}
}
