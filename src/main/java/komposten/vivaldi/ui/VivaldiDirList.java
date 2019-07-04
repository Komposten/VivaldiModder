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
import java.util.LinkedList;
import java.util.List;
import java.util.function.Supplier;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

public class VivaldiDirList extends JPanel
{
	private Supplier<String[]> directorySupplier;
	private ActionListener openListener;
	
	private List<RemovableListItem> list;
	private JPanel listPanel;
	private JButton buttonAdd;

	
	public VivaldiDirList(Supplier<String[]> directorySupplier, ActionListener openListener)
	{
		this.directorySupplier = directorySupplier;
		this.openListener = openListener;
		
		list = new LinkedList<>();
		listPanel = new JPanel(new GridBagLayout());
		buttonAdd = new JButton("Add");
		buttonAdd.addActionListener(action -> add());
		
		createLayout();
	}


	private void createLayout()
	{
		setLayout(new GridBagLayout());
		
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.weightx = 1;
		constraints.weighty = 1;
		constraints.fill = GridBagConstraints.BOTH;
		add(listPanel, constraints);
		constraints.gridy = 1;
		constraints.weighty = 0;
		constraints.fill = GridBagConstraints.VERTICAL;
		constraints.anchor = GridBagConstraints.EAST;
		add(buttonAdd, constraints);
	}
	
	
	public String[] getDirectories()
	{
		return list.stream()
				.map(RemovableListItem::getText)
				.toArray(size -> new String[size]);
	}
	
	
	public void addDirectories(String[] directories, boolean repack)
	{
		for (String directory : directories)
		{
			if (!hasDirectory(directory))
				addListItem(new RemovableListItem(directory, openListener, removeListener));
		}
		
		if (repack)
			repackWindow();
	}
	
	
	private boolean hasDirectory(String directory)
	{
		for (RemovableListItem listItem : list)
		{
			if (listItem.getText().equals(directory))
				return true;
		}
		
		return false;
	}


	private void add()
	{
		addDirectories(directorySupplier.get(), true);
	}


	private void addListItem(RemovableListItem listItem)
	{
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.gridy = list.size();
		constraints.insets.bottom = 6;
		constraints.weightx = 1;
		constraints.fill = GridBagConstraints.BOTH;

		listPanel.add(listItem, constraints);
		list.add(listItem);
	}
	
	

	private void repackWindow()
	{
		Window window = SwingUtilities.getWindowAncestor(this);

		if (window != null)
			window.pack();
	}
	
	
	private ActionListener removeListener = action ->
	{
		RemovableListItem listItem = (RemovableListItem) action.getSource();
		list.remove(listItem);
		listPanel.remove(listItem);
		
		repackWindow();
	};
}
