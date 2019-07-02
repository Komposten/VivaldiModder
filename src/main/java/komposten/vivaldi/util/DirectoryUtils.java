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

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;


public final class DirectoryUtils
{
	private DirectoryUtils()
	{}


	public static List<File> findVivaldiVersionDirs(File vivaldiDir)
	{
		return findVivaldiVersionDirs(vivaldiDir, 0);
	}
	
	
	public static List<File> findVivaldiVersionDirs(File vivaldiDir, int maxDepth)
	{
		List<File> dirs = new LinkedList<>();
		File[] children = vivaldiDir.listFiles();

		if (children != null)
		{
			for (File child : children)
			{
				if (isVersionDir(child))
					dirs.add(child);
				else if (maxDepth > 0)
					dirs.addAll(findVivaldiVersionDirs(child, maxDepth-1));
			}
		}

		return dirs;
	}
	
	
	public static boolean isVersionDir(File directory)
	{
		File[] resources = directory.listFiles((dir, name) -> name.equals("resources"));

		if (resources != null && resources.length > 0)
		{
			File[] vivaldi = resources[0].listFiles((dir, name) -> name.equals("vivaldi"));

			if (vivaldi != null && vivaldi.length > 0)
			{
				for (File file : vivaldi)
				{
					if (file.isDirectory())
						return true;
				}
			}
		}
		
		return false;
	}
}
