/*
 * Copyright 2019 Jakob Hjelm
 * 
 * This file is part of VivaldiModder.
 * 
 * VivaldiModder is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package komposten.vivaldi.ui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Window;
import java.awt.event.ActionListener;
import java.awt.event.FocusListener;
import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;


public class MultiTextField extends JPanel
{
	private Map<Document, BrowseTextField> docToFieldMap;
	private BrowseTextField lastTextfield;
	private FocusListener textFocusListener;
	private ActionListener openListener;


	public MultiTextField(ActionListener openListener)
	{
		super(new GridBagLayout());
		
		this.openListener = openListener;

		docToFieldMap = new LinkedHashMap<>();

		createRow();
	}
	
	
	public void addTextFocusListener(FocusListener listener)
	{
		for (BrowseTextField textfield : docToFieldMap.values())
		{
			textfield.getTextfield().removeFocusListener(textFocusListener);
			textfield.getTextfield().addFocusListener(listener);
		}
		
		this.textFocusListener = listener;
	}


	public String[] getTexts()
	{
		String[] texts = new String[docToFieldMap.size() - 1];

		int i = 0;
		for (BrowseTextField field : docToFieldMap.values())
		{
			String text = field.getTextfield().getText();
			if (!text.isEmpty())
				texts[i++] = text;
		}

		return texts;
	}


	private void createRow()
	{
		BrowseTextField textfield = new BrowseTextField(
				"Choose a Vivaldi installation directory", JFileChooser.DIRECTORIES_ONLY,
				openListener);
		Document document = textfield.getTextfield().getDocument();
		textfield.getTextfield().addFocusListener(textFocusListener);
		
		addRow(textfield, docToFieldMap.size());

		docToFieldMap.put(document, textfield);
		document.addDocumentListener(documentListener);
		lastTextfield = textfield;

		repackFrame();
	}


	private void repackFrame()
	{
		Window window = SwingUtilities.getWindowAncestor(this);

		if (window != null)
		{
			window.pack();
		}
	}


	private void removeRow(Document document)
	{
		BrowseTextField textfield = docToFieldMap.remove(document);

		textfield.getTextfield().getDocument().removeDocumentListener(documentListener);
		layoutRows();
	}


	private void layoutRows()
	{
		removeAll();

		int i = 0;
		for (BrowseTextField textfield : docToFieldMap.values())
		{
			addRow(textfield, i++);
		}

		repackFrame();
	}


	private void addRow(BrowseTextField textfield, int y)
	{
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.insets.top = (y > 0 ? 6 : 0);
		constraints.gridy = y;
		constraints.fill = GridBagConstraints.BOTH;
		constraints.weightx = 1;
		add(textfield, constraints);
	}


	public void addRows(String... texts)
	{
		SwingUtilities.invokeLater(() -> ignoreChangeEvents = true);
		for (String text : texts)
		{
			lastTextfield.getTextfield().setText(text);
			
			File directory = new File(text);
			if (directory.exists() && directory.isDirectory())
				lastTextfield.setCurrentDirectory(directory);
			createRow();
		}
		SwingUtilities.invokeLater(() -> ignoreChangeEvents = false);
	}


	private boolean ignoreChangeEvents = false;
	private DocumentListener documentListener = new DocumentListener()
	{
		@Override
		public void removeUpdate(DocumentEvent e)
		{
			if (ignoreChangeEvents)
				return;

			BrowseTextField textfield = docToFieldMap.get(e.getDocument());

			if (textfield.getTextfield().getText().trim().length() == 0
					&& docToFieldMap.size() > 1)
			{
				removeRow(e.getDocument());
			}
		}


		@Override
		public void insertUpdate(DocumentEvent e)
		{
			if (ignoreChangeEvents)
				return;

			BrowseTextField textfield = docToFieldMap.get(e.getDocument());

			if (textfield == lastTextfield)
				createRow();
		}


		@Override
		public void changedUpdate(DocumentEvent e)
		{
			//Not needed
		}
	};
}
