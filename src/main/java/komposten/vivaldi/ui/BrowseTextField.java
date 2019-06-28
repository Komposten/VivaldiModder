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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;

import komposten.vivaldi.ui.BrowseButton.BrowseListener;

public class BrowseTextField extends JPanel
{
	private JTextField textfield;
	private JButton openButton;
	private BrowseButton browseButton;
	private BrowseListener listener;
	
	
	public BrowseTextField(String chooserTitle, int fileSelectionMode, ActionListener openListener)
	{
		this(chooserTitle, fileSelectionMode, true, openListener);
	}
	
	
	public BrowseTextField(String chooserTitle, int fileSelectionMode,
			boolean showOpenButton, ActionListener openListener)
	{
		super(new GridBagLayout());
		
		textfield = new JTextField();
		browseButton = new BrowseButton(chooserTitle, fileSelectionMode, this::updateText);
		openButton = new JButton("Open");
		
		openButton.addActionListener(action -> openListener.actionPerformed(new ActionEvent(this, 0, "open")));
		
		GridBagConstraints constraints = new GridBagConstraints();
		
		constraints.fill = GridBagConstraints.BOTH;
		constraints.weightx = 1;
		add(textfield, constraints);
		constraints.weightx = 0;
		constraints.insets.left = 4;
		if (showOpenButton)
			add(openButton, constraints);
		add(browseButton, constraints);
	}
	
	
	public void setBrowseListener(BrowseListener listener)
	{
		this.listener = listener;
	}
	
	
	public void addTextFocusListener(FocusListener listener)
	{
		textfield.addFocusListener(listener);
	}
	
	
	public void setCurrentDirectory(File directory)
	{
		browseButton.setCurrentDirectory(directory);
	}
	
	
	public JTextField getTextfield()
	{
		return textfield;
	}
	
	
	private void updateText(File file)
	{
		textfield.setText(file.getPath());
		
		if (listener != null)
			listener.onFileChosen(file);
	}
}
