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

import java.io.File;
import java.io.Serializable;

import javax.swing.JButton;
import javax.swing.JFileChooser;


public class BrowseButton extends JButton
{
	public interface BrowseListener extends Serializable
	{
		public void onFileChosen(File file);
	}
	
	
	private BrowseListener browseListener;
	private JFileChooser chooser;
	
	
	public BrowseButton(String chooserTitle, int fileSelectionMode)
	{
		this(chooserTitle, fileSelectionMode, null);
	}
	
	
	public BrowseButton(String chooserTitle, int fileSelectionMode, BrowseListener browseListener)
	{
		super("Browse");

		chooser = new JFileChooser();
		chooser.setFileSelectionMode(fileSelectionMode);
		chooser.setDialogTitle(chooserTitle);
		addActionListener(action -> browse());
		setBrowseListener(browseListener);
	}
	
	
	public void setCurrentDirectory(File directory)
	{
		chooser.setCurrentDirectory(directory);
	}
	
	
	public void setBrowseListener(BrowseListener browseListener)
	{
		this.browseListener = browseListener;
	}
	
	
	private void browse()
	{
		int result = chooser.showOpenDialog(getRootPane());
		
		if (result == JFileChooser.APPROVE_OPTION && browseListener != null)
		{
			browseListener.onFileChosen(chooser.getSelectedFile());
		}
	}
}
