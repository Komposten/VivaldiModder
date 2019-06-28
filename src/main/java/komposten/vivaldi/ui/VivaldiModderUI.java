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

import javax.swing.JFrame;

import komposten.utilities.logging.LogUtils;

public class VivaldiModderUI extends JFrame
{
	private ModPanel modPanel;
	
	public VivaldiModderUI(String configPath)
	{
		super("VivaldiModder");

		LogUtils.writeToFile("log.txt");
		
		modPanel = new ModPanel(configPath);
		setContentPane(modPanel);
		
		pack();
		setMinimumSize(new Dimension(600, 280));
		setLocationRelativeTo(null);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setVisible(true);
	}
	
	
	public static void main(String[] args)
	{
		if (args.length > 0)
			new VivaldiModderUI(args[0]);
		else
			new VivaldiModderUI(null);
	}
}
