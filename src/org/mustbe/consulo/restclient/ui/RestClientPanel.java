package org.mustbe.consulo.restclient.ui;

import java.awt.BorderLayout;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.wiztools.restclient.HTTPClientRequestExecuter;
import org.wiztools.restclient.ViewAdapter;
import org.wiztools.restclient.bean.ContentType;
import org.wiztools.restclient.bean.HTTPMethod;
import org.wiztools.restclient.bean.HTTPVersion;
import org.wiztools.restclient.bean.RequestBean;
import org.wiztools.restclient.bean.RequestExecuter;
import org.wiztools.restclient.bean.Response;
import com.intellij.codeInsight.hint.HintUtil;
import com.intellij.icons.AllIcons;
import com.intellij.lang.Language;
import com.intellij.openapi.actionSystem.ActionPlaces;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.EmptyAction;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.actionSystem.impl.ActionButton;
import com.intellij.openapi.application.Result;
import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.event.DocumentAdapter;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.FileTypeRegistry;
import com.intellij.openapi.fileTypes.LanguageFileType;
import com.intellij.openapi.fileTypes.PlainTextFileType;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.VerticalFlowLayout;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.ui.EditorTextField;
import com.intellij.ui.JBSplitter;
import com.intellij.ui.TextFieldWithAutoCompletion;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.table.JBTable;
import com.intellij.util.containers.MultiMap;
import com.intellij.util.ui.ColumnInfo;
import com.intellij.util.ui.ListTableModel;
import com.intellij.util.ui.UIUtil;
import lombok.val;

/**
 * @author VISTALL
 * @since 20.11.13.
 */
public class RestClientPanel extends Ref<Project>
{
	private JComboBox myMethodComboBox;
	private JPanel myRootPanel;
	private JTabbedPane myTabPanel;
	private JPanel myResponseTab;
	private JBSplitter myResponseSplitter;
	private JPanel myActionPanel;
	private TextFieldWithAutoCompletionWithEnter myUrlTextField;
	private JLabel myResultLabel;
	private JComboBox myHttpVersionBox;

	private List<Pair<String, String>> myHeaders = new ArrayList<Pair<String, String>>();

	public RestClientPanel(final Project project)
	{
		super(project);

		for(HTTPMethod httpMethod : HTTPMethod.values())
		{
			myMethodComboBox.addItem(httpMethod);
		}

		for(HTTPVersion httpVersion : HTTPVersion.values())
		{
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

		final EditorTextField editorTextField = new EditorTextField(EditorFactory.getInstance().createDocument(""), project,
				PlainTextFileType.INSTANCE, true, false)
		{
			@Override
			protected EditorEx createEditor()
			{
				EditorEx editor = super.createEditor();
				editor.getScrollPane().setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
				return editor;
			}
		};
		editorTextField.setFontInheritedFromLAF(false);

		myResponseSplitter.setSecondComponent(editorTextField);

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
						URL url = null;
						try
						{
							url = new URL(myUrlTextField.getText());
						}
						catch(MalformedURLException e)
						{
							return;
						}

						RequestExecuter requestExecuter = new HTTPClientRequestExecuter();

						RequestBean request = new RequestBean();
						request.setUrl(url);
						request.setMethod((HTTPMethod) myMethodComboBox.getSelectedItem());
						request.setHttpVersion((HTTPVersion) myHttpVersionBox.getSelectedItem());
						requestExecuter.execute(request, new ViewAdapter()
						{
							@Override
							public void doError(final String error)
							{
								SwingUtilities.invokeLater(new Runnable()
								{
									@Override
									public void run()
									{
										Messages.showErrorDialog(error, "Error While Processing Request");
									}
								});
							}

							@Override
							public void doResponse(final Response response)
							{
								new WriteAction<Object>()
								{
									@Override
									protected void run(Result<Object> objectResult) throws Throwable
									{
										myResultLabel.setText(response.getStatusLine());
										myHeaders.clear();

										MultiMap<String, String> headers = response.getHeaders();
										for(Map.Entry<String, Collection<String>> entry : headers.entrySet())
										{
											myHeaders.add(new Pair<String, String>(entry.getKey(), StringUtil.join(entry.getValue(), ";")));
										}
										table.revalidate();
										FileType fileType = null;

										ContentType contentType = response.getContentType();
										Collection<Language> languages = Language.findInstancesByMimeType(contentType.getContentType());
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

										if(fileType == null)
										{
											fileType = PlainTextFileType.INSTANCE;
										}

										editorTextField.setNewDocumentAndFileType(fileType, EditorFactory.getInstance().createDocument(new String
												(response.getResponseBody(), contentType.getCharset())));

										SwingUtilities.invokeLater(new Runnable()
										{
											@Override
											public void run()
											{
												myTabPanel.setSelectedComponent(myResponseTab);
											}
										});
									}
								}.execute();
							}
						});
					}
				}.queue();
			}
		});

		JPanel panel = new JPanel(new VerticalFlowLayout());
		myActionPanel.add(panel, BorderLayout.CENTER);

		Presentation presentation = new Presentation();
		presentation.setIcon(AllIcons.General.MessageHistory);

		val button = new ActionButton(new EmptyAction(), presentation, ActionPlaces.UNKNOWN, ActionToolbar.DEFAULT_MINIMUM_BUTTON_SIZE);

		myActionPanel.add(button);
	}

	public JPanel getRootPanel()
	{
		return myRootPanel;
	}

	private void createUIComponents()
	{
		myUrlTextField = new TextFieldWithAutoCompletionWithEnter(get(), new TextFieldWithAutoCompletion.StringsCompletionProvider(Arrays.asList
				("test"), null), false, null);
		myUrlTextField.setPlaceholder("URL");

	}
}
