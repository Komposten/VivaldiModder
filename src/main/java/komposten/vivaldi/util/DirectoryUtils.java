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
import java.util.Arrays;
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
	
	
	public static List<File> findVivaldiDirs(File dir, int maxDepth)
	{
		return findVivaldiVersionDirs(dir, maxDepth+1)
				.stream()
				.map(File::getParentFile)
				.filter(Objects::nonNull)
				.distinct()
				.collect(Collectors.toList());
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
	
	
	/**
	 * Checks if <code>file</code> represents a directory, or a file in directory,
	 * in the first 3 levels below a Vivaldi directory (i.e. a version folder or a
	 * folder 1 or 2 steps below a version folder).
	 * 
	 * @param file
	 * @return <code>null</code> if the <code>file</code> is not in a Vivaldi dir,
	 *         otherwise a <code>File</code> instance for the Vivaldi directory.
	 */
	public static File getParentVivaldiDir(File file)
	{
		if (file.isFile())
			file = file.getParentFile();
		
		for (int i = 0; i < 3; i++)
		{
			if (file.getParentFile() == null)
				return null;
			
			if (DirectoryUtils.isVersionDir(file))
				return file.getParentFile();
			
			file = file.getParentFile();
		}
		
		return null;
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
	
	
	public static String assemblePath(String... elements)
	{
		StringBuilder builder = new StringBuilder();
		
		for (int i = 0; i < elements.length; i++)
		{
			String element = elements[i];
			
			if (element.isEmpty())
			{
				continue;
			}
			else if (i == 0 || !(elements[i-1].endsWith("/") || elements[i-1].endsWith("\\")))
			{
				if (element.startsWith("/") || element.startsWith("\\"))
					builder.append(element);
				else
					builder.append("/").append(element);
			}
			else if (element.startsWith("/") || element.startsWith("\\"))
			{
				builder.append(element.substring(1));
			}
			else
			{
				builder.append(element);
			}
		}
		
		return builder.toString();
	}
}
