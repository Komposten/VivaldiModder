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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import komposten.utilities.data.ObjectPair;
import komposten.utilities.logging.Level;
import komposten.utilities.logging.LogUtils;
import komposten.utilities.logging.Logger;
import komposten.utilities.tools.FileOperations;
import komposten.vivaldi.util.DirectoryUtils;


public class Patcher
{
	public interface PatchProgressListener extends Serializable
	{
		public void onPatchStarted();


		public void filesToPatch(int dirCount, int modFileCount);


		public void onNextInstallation(File directory);


		public void onNextVersion(File versionDirectory);


		public void onNextModFile(String file);


		public void onPatchFinished();
	}
	
	public static final String[] STYLE_SCRIPT_EXTENSIONS = {
			".js",
			".css"
	};


	private Collection<PatchProgressListener> listeners;
	private Map<String, List<String>> patchedVersions;
	private ModConfig modConfig;
	private Logger logger;


	public Patcher(ModConfig modConfig, Logger logger)
	{
		this.modConfig = modConfig;
		this.logger = logger;
		this.listeners = new LinkedList<>();

		loadPatchedVersions();
	}


	public void addProgressListener(PatchProgressListener listener)
	{
		this.listeners.add(listener);
	}
	
	
	public void setModConfig(ModConfig modConfig)
	{
		this.modConfig = modConfig;
	}


	private void loadPatchedVersions()
	{
		try
		{
			patchedVersions = new HashMap<>();
			File patchedFile = new File(Backend.FILE_PATCHED);
			Map<String, String> data = FileOperations.loadConfigFile(patchedFile, false);

			for (Entry<String, String> entry : data.entrySet())
			{
				String key = entry.getKey();
				String[] versions = entry.getValue().split(";");

				List<String> list = new ArrayList<>(versions.length);

				for (String string : versions)
				{
					if (!string.isEmpty())
						list.add(string);
				}

				patchedVersions.put(key, list);
			}
		}
		catch (FileNotFoundException e)
		{
			//Ignore; missing file simply means no patches have been done previously.
		}
	}


	public void applyMods(boolean patchAll)
	{
		notifyPatchStarted();
		applyMods(listVersionDirs(), patchAll);
		notifyPatchFinished();
	}


	public void applyMods(File vivaldiDir, boolean patchAll)
	{
		notifyPatchStarted();
		applyMods(listVersionDirs(vivaldiDir), patchAll);
		notifyPatchFinished();
	}


	private List<ObjectPair<File, File>> listVersionDirs()
	{
		List<ObjectPair<File, File>> dirs = new ArrayList<>();

		for (File vivaldiDir : modConfig.getVivaldiDirs())
		{
			if (vivaldiDir.exists())
			{
				dirs.addAll(listVersionDirs(vivaldiDir));
			}
			else
			{
				logger.log(Level.WARNING,
						String.format("Directory %s does not exist, skipping!", vivaldiDir));
			}
		}

		return dirs;
	}


	private List<ObjectPair<File, File>> listVersionDirs(File vivaldiDir)
	{
		List<ObjectPair<File, File>> dirs = new LinkedList<>();
		File[] versionDirs = vivaldiDir.listFiles(DirectoryUtils.vivaldiVersionFolderFilter);
		for (File versionDir : versionDirs)
			dirs.add(new ObjectPair<File, File>(versionDir, vivaldiDir));

		return dirs;
	}


	private void applyMods(List<ObjectPair<File, File>> versionDirs, boolean patchAll)
	{
		notifyFilesToPatch(versionDirs);

		int successes = 0;
		File vivaldiDir = null;
		for (ObjectPair<File, File> versionDir : versionDirs)
		{
			if (vivaldiDir == null || !versionDir.getSecond().equals(vivaldiDir))
			{
				vivaldiDir = versionDir.getSecond();
				logger.log(Level.INFO, String.format("Patching installation %s...", vivaldiDir));
				notifyNextInstallation(vivaldiDir);
			}

			if (applyMods(versionDir.getFirst(), versionDir.getSecond(), patchAll))
			{
				successes++;
				versionPatched(versionDir.getSecond(), versionDir.getFirst());
			}
		}

		if (successes == versionDirs.size())
			logger.log(Level.INFO, "Patched all Vivaldi installations successfully!");
		else
			logger.log(Level.INFO, String.format(
					"%d/%d versions were not patched successfully! Please review the log above!",
					versionDirs.size() - successes, versionDirs.size()));

		savePatchedVersions();
	}

	
	private boolean applyMods(File versionDir, File vivaldiDir, boolean patchAll)
	{
		for (PatchProgressListener listener : listeners)
			listener.onNextVersion(versionDir);

		boolean success = true;

		if (!patchAll && hasBeenPatchedPreviously(vivaldiDir, versionDir))
		{
			logger.log(Level.INFO, String.format("Version %s already patched, skipping it.",
					versionDir.getName()));
		}
		else
		{
			logger.log(Level.INFO,
					String.format("Patching version %s...", versionDir.getName()));

			boolean hasBrowserHtmlInstruction = false;
			for (Instruction instruction : modConfig.getInstructions())
			{
				if (!executeInstruction(versionDir, instruction))
					success = false;
				
				if (instruction.sourceFile.endsWith("browser.html"))
					hasBrowserHtmlInstruction = true;
			}
			
			if (!hasBrowserHtmlInstruction)
			{
				if (!generateBrowserHtmlFile(versionDir))
					success = false;
			}
		}

		return success;
	}


	private boolean hasBeenPatchedPreviously(File vivaldiDir, File versionDir)
	{
		List<String> previouslyPatched = this.patchedVersions.get(getAbsolutePath(vivaldiDir));
		
		return previouslyPatched != null && previouslyPatched.contains(versionDir.getName());
	}


	private boolean executeInstruction(File versionDir, Instruction instruction)
	{
		notifyNextModFile(instruction);
		File sourceFile = new File(modConfig.getModDir(), instruction.sourceFile);
		File targetDir = new File(versionDir, instruction.targetDirectory);
		File targetFile = new File(targetDir, sourceFile.getName());
		File backupFile = new File(targetDir, sourceFile.getName() + ".bak");

		boolean hasBackup = true;
		
		if (targetFile.exists() && !backupFile.exists())
			hasBackup = backupFile(targetFile, backupFile);

		if (hasBackup)
			return copyModFile(sourceFile, targetFile);
		else
			return false;
	}


	private boolean backupFile(File file, File backupFile)
	{
		try
		{
			logger.log(Level.INFO,
					String.format("Backing up %s...", getAbsolutePath(file)));
			return FileOperations.copyFile(file, backupFile);
		}
		catch (IOException e)
		{
			String message = String.format(
					"Could not back up %s, so it will not be replaced: %s",
					file.getName(), e.getMessage());
			logger.log(Level.WARNING, message);
			
			return false;
		}
	}


	private boolean copyModFile(File sourceFile, File targetFile)
	{
		try
		{
			if (!sourceFile.exists())
				throw new FileNotFoundException(
						String.format("%s does not exist!", sourceFile.getPath()));
			if (!sourceFile.isFile())
				throw new IOException(
						String.format("%s is not a file!", sourceFile.getPath()));
			
			FileOperations.copyFile(sourceFile, targetFile);
		}
		catch (IOException e)
		{
			String message = String.format("Could not copy %s: %s", sourceFile.getName(),
					e.getMessage());
			logger.log(Level.WARNING, message);
			return false;
		}
		
		return true;
	}
	
	
	private boolean generateBrowserHtmlFile(File versionDir)
	{
		List<String> styleFiles = new ArrayList<>();
		List<String> scriptFiles = new ArrayList<>();

		Path pathBrowser = new File(versionDir, "resources/vivaldi/").toPath();
		
		for (Instruction instruction : modConfig.getInstructions())
		{
			if (!instruction.excludeFromBrowserHtml)
			{
				if (instruction.sourceFile.endsWith(".js"))
				{
					Path relative = getPathRelativeToBrowser(versionDir, pathBrowser, instruction);
					scriptFiles.add(relative.toString());
				}
				else if (instruction.sourceFile.endsWith(".css"))
				{
					Path relative = getPathRelativeToBrowser(versionDir, pathBrowser, instruction);
					styleFiles.add(relative.toString());
				}
			}
		}
		
		return addStylesAndScripts(pathBrowser.resolve("browser.html").toFile(), styleFiles, scriptFiles);
	}


	private Path getPathRelativeToBrowser(File versionDir, Path pathBrowser,
			Instruction instruction)
	{
		File source = new File(instruction.sourceFile);
		File targetDir = new File(versionDir, instruction.targetDirectory);
		Path pathTarget = new File(targetDir, source.getName()).toPath();
		
		return pathBrowser.relativize(pathTarget);
	}


	private boolean addStylesAndScripts(File file, List<String> styleFiles,
			List<String> scriptFiles)
	{
		if (styleFiles.isEmpty() && scriptFiles.isEmpty())
			return true;
		
		File backupFile = new File(file.getParentFile(), "browser.html.bak");
		
		if (!backupFile.exists() && !backupFile(file, backupFile))
				return false;
		
		Document document = readBrowserHtml(backupFile);

		if (document != null)
		{
			Element head = document.selectFirst("head");
			Element body = document.selectFirst("body");
			
			for (String styleFile : styleFiles)
			{
				if (head.getElementsByAttributeValue("href", styleFile).isEmpty())
				{
					Element element = document.createElement("link");
					element.attr("rel", "stylesheet");
					element.attr("href", styleFile);
					head.appendChild(element);
				}
			}
			
			for (String scriptFile : scriptFiles)
			{
				if (body.getElementsByAttributeValue("href", scriptFile).isEmpty())
				{
					Element element = document.createElement("script");
					element.attr("src", scriptFile);
					body.appendChild(element);
				}
			}
			
			return saveToFile(document, file);
		}
		
		return false;
	}


	private Document readBrowserHtml(File file)
	{
		Document document = null;
		
		try
		{
			document = Jsoup.parse(file, null);
		}
		catch (IOException e)
		{
			String message = String.format(
					"Could not read %s: %s", file, e.getMessage());
			logger.log(Level.WARNING, message);
		}
		
		return document;
	}


	private boolean saveToFile(Document document, File file)
	{
		try
		{
			FileOperations fops = new FileOperations();
			fops.createWriter(file, false);
			fops.printData(document.html(), false);
			fops.closeWriter();
			return true;
		}
		catch (IOException e)
		{
			String message = String.format(
					"Could not save the modified %s: %s", file, e.getMessage());
			logger.log(Level.WARNING, message);

			return false;
		}
	}


	private void versionPatched(File vivaldiDir, File versionFolder)
	{
		String key = getAbsolutePath(vivaldiDir);

		List<String> versions = patchedVersions.computeIfAbsent(key, k -> new ArrayList<>());
		
		if (!versions.contains(versionFolder.getName()))
			versions.add(versionFolder.getName());
	}


	private String getAbsolutePath(File file)
	{
		try
		{
			return file.getCanonicalPath();
		}
		catch (IOException e)
		{
			return file.getAbsolutePath();
		}
	}


	private void savePatchedVersions()
	{
		FileOperations fops = new FileOperations();

		try
		{
			fops.createWriter(new File(Backend.FILE_PATCHED), false);

			StringBuilder builder = new StringBuilder();
			for (Entry<String, List<String>> entry : patchedVersions.entrySet())
			{
				builder.append(entry.getKey()).append("=");
				for (String version : entry.getValue())
					builder.append(version).append(";");
			}

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


	private void notifyPatchStarted()
	{
		for (PatchProgressListener listener : listeners)
			listener.onPatchStarted();
	}


	private void notifyPatchFinished()
	{
		for (PatchProgressListener listener : listeners)
			listener.onPatchFinished();
	}


	private void notifyFilesToPatch(List<ObjectPair<File, File>> versionDirs)
	{
		for (PatchProgressListener listener : listeners)
			listener.filesToPatch(versionDirs.size(), modConfig.getInstructions().size());
	}


	private void notifyNextInstallation(File vivaldiDir)
	{
		for (PatchProgressListener listener : listeners)
			listener.onNextInstallation(vivaldiDir);
	}


	private void notifyNextModFile(Instruction instruction)
	{
		for (PatchProgressListener listener : listeners)
			listener.onNextModFile(instruction.sourceFile);
	}
}
