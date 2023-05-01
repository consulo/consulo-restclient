/*
 * Copyright 2013-2016 must-be.org
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

import consulo.codeEditor.EditorEx;
import consulo.codeEditor.EditorFactory;
import consulo.document.Document;
import consulo.language.editor.ui.awt.EditorTextField;
import consulo.language.plain.PlainTextFileType;
import consulo.project.Project;
import consulo.restclient.HttpHeader;
import consulo.ui.ex.awt.*;
import consulo.ui.ex.awt.table.ListTableModel;
import consulo.ui.ex.awt.table.TableView;
import consulo.util.lang.StringUtil;
import consulo.virtualFileSystem.fileType.FileType;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * @author VISTALL
 * @since 27-Sep-16
 */
public class RestRequestOrResponsePanel extends JPanel
{
	private EditorTextField myEditorTextField;

	private ListTableModel<HttpHeader> myModel;

	private JLabel myStatusPanel;

	public RestRequestOrResponsePanel(@Nonnull Project project, boolean request)
	{
		super(new BorderLayout());

		OnePixelSplitter splitter = new OnePixelSplitter();
		splitter.setProportion(0.2f);
		splitter.setSplitterProportionKey("RestClientPanel#request=" + request);

		add(splitter, BorderLayout.CENTER);

		if(!request)
		{
			myStatusPanel = new JLabel();
			add(myStatusPanel, BorderLayout.SOUTH);
		}

		myModel = new ListTableModel<>(new ColumnInfo<HttpHeader, String>("Header Name")
		{
			@Override
			public boolean isCellEditable(HttpHeader stringCouple)
			{
				return true;
			}

			@Override
			public void setValue(HttpHeader stringCouple, String value)
			{
				stringCouple.setName(value);
			}

			@Nullable
			@Override
			public String valueOf(HttpHeader stringCouple)
			{
				return stringCouple.getName();
			}
		}, new ColumnInfo<HttpHeader, String>("Value")
		{
			@Override
			public boolean isCellEditable(HttpHeader stringCouple)
			{
				return true;
			}

			@Override
			public void setValue(HttpHeader httpHeader, String value)
			{
				httpHeader.setValue(value);
			}

			@Nullable
			@Override
			public String valueOf(HttpHeader stringCouple)
			{
				return stringCouple.getValue();
			}
		}
		);
		TableView<HttpHeader> headersView = new TableView<>(myModel);

		JComponent tableView;
		if(request)
		{
			headersView.setBorder(null);
			ToolbarDecorator toolbarDecorator = ToolbarDecorator.createDecorator(headersView, new ElementProducer<HttpHeader>()
			{
				@Override
				public HttpHeader createElement()
				{
					return new HttpHeader("", "");
				}

				@Override
				public boolean canCreateElement()
				{
					return true;
				}
			}).disableUpDownActions();
			tableView = toolbarDecorator.createPanel();
			tableView.setBorder(null);
		}
		else
		{
			tableView = ScrollPaneFactory.createScrollPane(headersView, SideBorder.BOTTOM | SideBorder.LEFT);
		}

		splitter.setFirstComponent(tableView);

		myEditorTextField = new EditorTextField(EditorFactory.getInstance().createDocument(""), project, PlainTextFileType.INSTANCE, !request, false)
		{
			@Override
			protected EditorEx createEditor()
			{
				EditorEx editor = super.createEditor();
				editor.getScrollPane().setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
				return editor;
			}
		};
		myEditorTextField.setBorder(null);
		myEditorTextField.setFontInheritedFromLAF(false);
		splitter.setSecondComponent(myEditorTextField);
	}

	public void setStatusLabel(String label)
	{
		myStatusPanel.setText(label);
	}

	public void setHeaders(List<HttpHeader> headers)
	{
		myModel.setItems(headers);
	}

	@Nonnull
	public List<HttpHeader> getHeaders()
	{
		return myModel.getItems();
	}

	public String getText()
	{
		return myEditorTextField.getText();
	}

	public void setText(FileType fileType, String body)
	{
		EditorFactory editorFactory = EditorFactory.getInstance();
		Document document = editorFactory.createDocument(StringUtil.convertLineSeparators(body));

		myEditorTextField.setNewDocumentAndFileType(fileType, document);
	}
}
