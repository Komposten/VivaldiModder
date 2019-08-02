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

import java.awt.Dimension;
import java.io.File;
import java.io.IOException;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import komposten.utilities.logging.Level;
import komposten.utilities.logging.LogUtils;

public class VivaldiModderUI extends JFrame
{
	private ModPanel modPanel;
	
	public VivaldiModderUI(String configPath)
	{
		super("VivaldiModder");

		LogUtils.writeToFile("log.txt");
		
		checkConfigExists(configPath);
		
		try
		{
			modPanel = new ModPanel(configPath);
		}
		catch (IOException e)
		{
			String title = "Could not load the config!";
			String msg = String.format("The config file (%s) could not be read!"
					+ "%nReason: %s"
					+ "%n%nThe program will exit.", configPath, e.getMessage());
			LogUtils.log(Level.ERROR, getClass().getSimpleName(), title, e, false);
			JOptionPane.showMessageDialog(null, msg, title, JOptionPane.ERROR_MESSAGE);
			System.exit(1);
		}
		setContentPane(modPanel);
		
		pack();
		setMinimumSize(new Dimension(600, 280));
		setLocationRelativeTo(null);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setVisible(true);
	}
	
	
	private void checkConfigExists(String configPath)
	{
		File file = new File(configPath);
		
		if (!file.exists())
		{
			String title = "Could not load the config!";
			String msg = String.format("The config file (%s) could not be found!"
					+ "%nStarting with an empty config instead.", configPath);
			JOptionPane.showMessageDialog(null, msg, title, JOptionPane.ERROR_MESSAGE);
		}
		else if (!file.isFile())
		{
			String title = "Could not load the config!";
			String msg = String.format("The config file (%s) is not a file!"
					+ "%nThe program will exit.", configPath);
			JOptionPane.showMessageDialog(null, msg, title, JOptionPane.ERROR_MESSAGE);
			System.exit(1);
		}
	}


	public static void main(String[] args)
	{
		if (args.length > 0)
			new VivaldiModderUI(args[0]);
		else
			new VivaldiModderUI("config.ini");
	}
}
