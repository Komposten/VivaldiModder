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

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class RemovableListItem extends JPanel
{
	private JTextField field;
	private JButton buttonOpen;
	private JButton buttonRemove;
	
	
	public RemovableListItem(String text, ActionListener openListener, ActionListener removeListener)
	{
		field = new JTextField(text);
		buttonOpen = new JButton("Open");
		buttonRemove = new JButton("Remove");
		
		field.setEditable(false);
		
		if (openListener != null)
		{
			buttonOpen.addActionListener(
					action -> openListener.actionPerformed(new ActionEvent(this, 0, "open")));
		}
		
		if (removeListener != null)
		{
			buttonRemove.addActionListener(
					action -> removeListener.actionPerformed(new ActionEvent(this, 0, "remove")));
		}
		
		createLayout();
	}


	private void createLayout()
	{
		setLayout(new GridBagLayout());
		
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.weightx = 1;
		constraints.fill = GridBagConstraints.BOTH;
		add(field, constraints);
		constraints.weightx = 0;
		constraints.insets.left = 4;
		add(buttonOpen, constraints);
		add(buttonRemove, constraints);
	}
	
	
	public String getText()
	{
		return field.getText();
	}
}
