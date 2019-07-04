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
package komposten.vivaldi.util;

public class Utilities
{
	private static final String[] SCRIPT_EXTENSIONS = {
			".js",
	};
	
	private static final String[] STYLE_EXTENSIONS = {
			".css"
	};
	
	private Utilities() {}
	
	
	public static boolean isStyle(String file)
	{
		return fileHasExtension(file, STYLE_EXTENSIONS);
	}
	
	
	public static boolean isScript(String file)
	{
		return fileHasExtension(file, SCRIPT_EXTENSIONS);
	}
	
	
	private static boolean fileHasExtension(String file, String[] extensions)
	{
		for (String extension : extensions)
		{
			if (file.toLowerCase().endsWith(extension))
				return true;
		}
		
		return false;
	}
}
