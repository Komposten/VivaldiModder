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
package komposten.vivaldi.backend;

import java.util.Objects;


public class Instruction implements Comparable<Instruction>
{
	public final String sourceFile;
	public final String targetDirectory;
	public final boolean excludeFromBrowserHtml;


	public Instruction(String source, String target, boolean excludeFromBrowserHtml)
	{
		this.sourceFile = source;
		this.targetDirectory = target;
		this.excludeFromBrowserHtml = excludeFromBrowserHtml;
	}
	
	
	public Instruction(Instruction copyFrom)
	{
		this.sourceFile = copyFrom.sourceFile;
		this.targetDirectory = copyFrom.targetDirectory;
		this.excludeFromBrowserHtml = copyFrom.excludeFromBrowserHtml;
	}


	@Override
	public int hashCode()
	{
		return Objects.hash(sourceFile, targetDirectory, excludeFromBrowserHtml);
	}


	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Instruction other = (Instruction) obj;
		if (sourceFile == null)
		{
			if (other.sourceFile != null)
				return false;
		}
		else if (!sourceFile.equals(other.sourceFile))
			return false;
		if (targetDirectory == null)
		{
			if (other.targetDirectory != null)
				return false;
		}
		else if (!targetDirectory.equals(other.targetDirectory))
			return false;
		return excludeFromBrowserHtml == other.excludeFromBrowserHtml;
	}
	
	
	@Override
	public String toString()
	{
		return String.format("[%s > %s]", sourceFile, targetDirectory);
	}


	@Override
	public int compareTo(Instruction o)
	{
		if (!targetDirectory.equals(o.targetDirectory))
			return targetDirectory.compareTo(o.targetDirectory);
		else
			return (sourceFile.compareTo(o.sourceFile));
	}
}
