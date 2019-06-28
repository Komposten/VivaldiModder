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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import komposten.utilities.logging.Level;
import komposten.utilities.logging.LogUtils;
import komposten.utilities.tools.FileOperations;
import komposten.vivaldi.util.DirectoryUtils;


public class ModConfig
{
	private File configFile;
	private File modDir;
	private File[] vivaldiDirs;
	private List<Instruction> instructions;


	public ModConfig(File file) throws FileNotFoundException, IOException
	{
		this(file, null, null, null);
		parseFile(file);

		Collections.sort(instructions);
	}


	public ModConfig(File configFile, File modDir, File[] vivaldiDirs, List<Instruction> instructions)
	{
		this.configFile = configFile;
		this.modDir = (modDir == null ? new File("").getAbsoluteFile() : modDir);
		this.vivaldiDirs = (vivaldiDirs == null ? new File[0] : vivaldiDirs);
		this.instructions = (instructions == null ? new ArrayList<>() : instructions);
	}
	
	
	public File getConfigFile()
	{
		return configFile;
	}


	public File getModDir()
	{
		return modDir;
	}


	public File[] getVivaldiDirs()
	{
		return vivaldiDirs;
	}


	public List<Instruction> getInstructions()
	{
		return instructions;
	}


	private void parseFile(File file) throws FileNotFoundException, IOException
	{
		try (BufferedReader reader = new BufferedReader(new FileReader(file)))
		{
			String line;
			while ((line = reader.readLine()) != null)
			{
				parseLine(line.trim());
			}
		}
	}


	private void parseLine(String line)
	{
		if (line.startsWith("mod.dir"))
		{
			modDir = new File(line.substring(line.indexOf('=') + 1).trim());
		}
		else if (line.startsWith("vivaldi.dirs"))
		{
			String dirs = line.substring(line.indexOf('=') + 1).trim();
			String[] dirsSplit = splitQuotedArray(dirs);
			vivaldiDirs = new File[dirsSplit.length];

			for (int i = 0; i < dirsSplit.length; i++)
			{
				File vivaldiDir = new File(dirsSplit[i]);

				if (vivaldiDir.exists())
					vivaldiDir = getApplicationFolder(vivaldiDir);

				vivaldiDirs[i] = vivaldiDir;
			}
		}
		else if (line.contains(">"))
		{
			String[] split = line.split(">");

			String source = split[0].trim();
			String target = (split.length > 1 ? split[1].trim() : "");
			instructions.add(new Instruction(source, target));
		}
	}


	private String[] splitQuotedArray(String arrayString)
	{
		if (arrayString.charAt(0) == '"')
		{
			List<String> strings = new ArrayList<>();
			int start = 0;
			int end = -1;

			for (int i = 1; i < arrayString.length(); i++)
			{
				if (arrayString.charAt(i) == '"')
				{
					if (start > end)
					{
						end = i;
						strings.add(arrayString.substring(start + 1, end));
					}
					else
					{
						start = i;
					}
				}
			}

			return strings.toArray(new String[strings.size()]);
		}
		else
		{
			return new String[] { arrayString };
		}
	}


	private File getApplicationFolder(File vivaldiDir)
	{
		File[] applicationFolders = vivaldiDir.listFiles(applicationFolderFilter);

		if (applicationFolders.length > 0)
			vivaldiDir = applicationFolders[0];

		return vivaldiDir;
	}


	public List<String> validate()
	{
		List<String> errors = new ArrayList<>();
		
		boolean modDirValid = validateModDir(errors);

		validateVivaldiDirs(errors);
		validateInstructions(errors, modDirValid);

		return errors;

	}


	private boolean validateModDir(List<String> errors)
	{
		boolean modDirValid = true;

		if (modDir.exists())
		{
			if (!modDir.isDirectory())
			{
				errors.add(String.format("Invalid mod dir: %s is not a directory!", modDir));
				modDirValid = false;
			}
		}
		else
		{
			errors.add(String.format("Invalid mod dir: %s does not exist!", modDir));
			modDirValid = false;
		}
		
		return modDirValid;
	}


	private void validateVivaldiDirs(List<String> errors)
	{
		for (File vivaldiDir : vivaldiDirs)
		{
			if (vivaldiDir.exists())
			{
				if (!vivaldiDir.isDirectory())
				{
					errors.add(
							String.format("Invalid Vivaldi dir: %s is not a directory!", vivaldiDir));
				}

				boolean hasVersionFolder = vivaldiDir
						.listFiles(DirectoryUtils.vivaldiVersionFolderFilter).length > 0;
				
				if (!hasVersionFolder)
				{
					errors.add(String.format("Invalid Vivaldi dir: %s contains no version folders!", vivaldiDir));
				}
			}
			else
			{
				errors.add(String.format("Invalid Vivaldi dir: %s does not exist!", vivaldiDir));
			}
		}
	}


	private void validateInstructions(List<String> errors, boolean validateModFiles)
	{
		for (Instruction instruction : instructions)
		{
			File modFile = new File(modDir, instruction.sourceFile);

			if (validateModFiles)
			{
				if (modFile.exists())
				{
					if (!modFile.isFile())
					{
						errors.add(String.format("Invalid mod file: %s is not a file!", modFile));
					}
				}
				else
				{
					errors.add(String.format("Invalid mod file: %s does not exist!", modFile));
				}
			}
		}
	}


	private FileFilter applicationFolderFilter = file -> file.isDirectory()
			&& file.getName().equals("Application");


	public void save()
	{
		FileOperations fops = new FileOperations();

		try
		{
			fops.createWriter(configFile, false);
			
			fops.printData("mod.dir=" + modDir.getPath(), false);
			fops.printData("\nvivaldi.dirs=" + arrayToString(vivaldiDirs), false);

			StringBuilder builder = new StringBuilder();
			for (Instruction instruction : instructions)
				builder.append('\n').append(instruction.sourceFile).append(">").append(instruction.targetDirectory);

			fops.printData(builder.toString(), false);
		}
		catch (IOException e)
		{
			String msg = "Could not write the patched versions file!";
			LogUtils.log(Level.WARNING, getClass().getSimpleName(), msg, e, false);
		}
		finally
		{
			try
			{
				fops.closeWriter();
			}
			catch (IOException e)
			{
				//Ignore
			}
		}
	}


	private String arrayToString(File[] dirs)
	{
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < dirs.length; i++)
		{
			if (i != 0)
				builder.append(',');
			builder.append('"').append(dirs[i].getPath()).append('"');
		}
		return builder.toString();
	}
}
