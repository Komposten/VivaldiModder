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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

		createEmptyRow(true);
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
		List<String> filteredTexts = docToFieldMap.values().stream()
			.map(x -> x.getTextfield().getText())
			.filter(x -> !x.trim().isEmpty())
			.collect(Collectors.toList());
		
		return filteredTexts.toArray(new String[filteredTexts.size()]);
	}
	
	
	private void createEmptyRow(boolean repack)
	{
		createRow(null, repack);
	}


	private void createRow(String text, boolean repack)
	{
		BrowseTextField textfield = new BrowseTextField(
				"Choose a Vivaldi installation directory", JFileChooser.DIRECTORIES_ONLY,
				openListener);
		
		if (text != null)
		{
			textfield.getTextfield().setText(text);
			File directory = new File(text);
			if (directory.exists() && directory.isDirectory())
				textfield.setCurrentDirectory(directory);
		}
		
		textfield.getTextfield().addFocusListener(textFocusListener);
		
		addRow(textfield, docToFieldMap.size());

		Document document = textfield.getTextfield().getDocument();
		document.addDocumentListener(documentListener);
		docToFieldMap.put(document, textfield);
		lastTextfield = textfield;

		if (repack)
			repackFrame();
	}


	private void repackFrame( )
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
	
	
	private void removeAllRows(boolean layout)
	{
		for (Document document : docToFieldMap.keySet())
			document.removeDocumentListener(documentListener);
		
		docToFieldMap.clear();
		
		if (layout)
			layoutRows();
		else
			removeAll();
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
		String[] oldTexts = getTexts();
		String[] newTexts = distinct(texts, oldTexts);
		
		if (newTexts.length == 0)
			return;
		
		SwingUtilities.invokeLater(() -> ignoreChangeEvents = true);
		removeAllRows(false);
		
		for (int i = 0; i < oldTexts.length + newTexts.length; i++)
		{
			String text = (i < oldTexts.length ? oldTexts[i] : newTexts[i - oldTexts.length]);
			createRow(text, false);
		}

		createEmptyRow(true);
		SwingUtilities.invokeLater(() -> ignoreChangeEvents = false);
	}


	private String[] distinct(String[] texts, String[] checkAgainst)
	{
		List<String> distinct = new LinkedList<>();
		
		String[] originalTexts = texts;
		texts = normalisePaths(texts);
		checkAgainst = normalisePaths(checkAgainst);
		
		for (int i = 0; i < texts.length; i++)
		{
			boolean match = false;
			for (String check : checkAgainst)
			{
				if (texts[i].equals(check))
				{
					match = true;
					break;
				}
			}
			
			if (!match)
				distinct.add(originalTexts[i]);
		}
		
		return distinct.toArray(new String[distinct.size()]);
	}


	private String[] normalisePaths(String[] texts)
	{
		String[] normals = new String[texts.length];
		
		for (int i = 0; i < texts.length; i++)
		{
			normals[i] = texts[i].trim().replace('\\', '/');
			
			if (normals[i].endsWith("/"))
				normals[i] = normals[i].substring(0, normals[i].length()-1);
		}
		
		return normals;
	}


	private boolean ignoreChangeEvents = false;
	private DocumentListener documentListener = new DocumentListener()
	{
		@Override
		public void removeUpdate(DocumentEvent e)
		{
			//FIXME Browsing for a vivaldi dir replaces the text, which first removes it and then inserts new.
			//				Because all text is first removed, the text field is removed!
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
				createEmptyRow(true);
		}


		@Override
		public void changedUpdate(DocumentEvent e)
		{
			//Not needed
		}
	};
}
