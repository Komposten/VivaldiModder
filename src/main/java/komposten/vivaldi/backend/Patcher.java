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
import komposten.vivaldi.util.Utilities;


public class Patcher
{
	public interface PatchProgressListener extends Serializable
	{
		public void onPatchStarted();


		public void filesToPatch(int dirCount, int modFileCount);


		public void onNextInstallation(File directory);


		public void onNextVersion(File versionDirectory);


		public void onNextModFile(String file);


		public void onPatchFinished(boolean success);
	}


	private Collection<PatchProgressListener> listeners;
	private Map<String, List<String>> patchedVersions;
	private ModConfig modConfig;
	private Logger logger;
	
	private List<LogMessage> errors;


	public Patcher(ModConfig modConfig, Logger logger)
	{
		this.modConfig = modConfig;
		this.logger = logger;
		this.listeners = new LinkedList<>();
		this.errors = new LinkedList<>();

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
		boolean success = applyMods(listVersionDirs(), patchAll);
		notifyPatchFinished(success);
	}


	public void applyMods(File vivaldiDir, boolean patchAll)
	{
		notifyPatchStarted();
		boolean success = applyMods(listVersionDirs(vivaldiDir), patchAll);
		notifyPatchFinished(success);
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
		
		List<File> versionDirs = DirectoryUtils.findVivaldiVersionDirs(vivaldiDir);
		
		for (File dir : versionDirs)
			dirs.add(new ObjectPair<File, File>(dir, vivaldiDir));

		return dirs;
	}


	private boolean applyMods(List<ObjectPair<File, File>> versionDirs, boolean patchAll)
	{
		notifyFilesToPatch(versionDirs);

		int successes = 0;
		File vivaldiDir = null;
		String headerSeparator = "=================================";
		
		for (ObjectPair<File, File> versionDir : versionDirs)
		{
			if (vivaldiDir == null || !versionDir.getSecond().equals(vivaldiDir))
			{
				vivaldiDir = versionDir.getSecond();
				logger.log(null, headerSeparator);
				logger.log(null, String.format("Patching installation %s...", vivaldiDir));
				logger.log(null, headerSeparator);
				notifyNextInstallation(vivaldiDir);
			}

			if (applyMods(versionDir.getFirst(), versionDir.getSecond(), patchAll))
			{
				successes++;
				versionPatched(versionDir.getSecond(), versionDir.getFirst());
			}
		}

		logger.log(null, headerSeparator);
		if (successes == versionDirs.size())
			logger.log(null, "Patched all Vivaldi installations successfully!");
		else
			logger.log(Level.WARNING, String.format(
					"%d/%d versions were not patched successfully! Please review the log above!",
					versionDirs.size() - successes, versionDirs.size()));
		logger.log(null, headerSeparator);

		savePatchedVersions();
		
		return successes == versionDirs.size();
	}

	
	private boolean applyMods(File versionDir, File vivaldiDir, boolean patchAll)
	{
		for (PatchProgressListener listener : listeners)
			listener.onNextVersion(versionDir);

		boolean success = true;
		String headerSeparator = "---------------------------------";

		logger.log(null, headerSeparator);
		if (!patchAll && hasBeenPatchedPreviously(vivaldiDir, versionDir))
		{
			logger.log(null, String.format("Version %s already patched, skipping it.",
					versionDir.getName()));
			logger.log(null, headerSeparator);
		}
		else
		{
			logger.log(null,String.format("Patching version %s...", versionDir.getName()));
			logger.log(null, headerSeparator);

			logger.log(null, "BACKING UP FILES");
			List<Instruction> successfulBackups = backupFiles(versionDir);
			if (successfulBackups.size() != modConfig.getInstructions().size())
				success = false;
			
			logger.log(null, "");
			logger.log(null, "COPYING MOD FILES");
			if (!copyFiles(successfulBackups, versionDir))
				success = false;

			if (!hasBrowserHtmlInstruction())
			{
				logger.log(null, "");
				logger.log(null, "UPDATING BROWSER.HTML");
				if (!generateBrowserHtmlFile(versionDir))
					success = false;
			}
			
			if (!errors.isEmpty())
			{
				logger.log(null, "");
				logErrors();
			}

			logger.log(null, "");
		}

		return success;
	}


	private boolean hasBeenPatchedPreviously(File vivaldiDir, File versionDir)
	{
		List<String> previouslyPatched = this.patchedVersions.get(getAbsolutePath(vivaldiDir));
		
		return previouslyPatched != null && previouslyPatched.contains(versionDir.getName());
	}


	/**
	 * @return A list of all instructions that were backed up successfully,
	 *         already had backups, or did not exist (and where thus not in need
	 *         of being backed up).
	 */
	private List<Instruction> backupFiles(File versionDir)
	{
		List<Instruction> instructions = new LinkedList<>();
		boolean anyNeededBackup = false;
		
		for (Instruction instruction : modConfig.getInstructions())
		{
			File sourceFile = new File(modConfig.getModDir(), instruction.sourceFile);
			File targetDir = new File(versionDir, instruction.targetDirectory);
			File targetFile = new File(targetDir, sourceFile.getName());
			File backupFile = new File(targetDir, sourceFile.getName() + ".bak");
			
			if (targetFile.exists() && !backupFile.exists())
			{
				anyNeededBackup = true;
				if (backupFile(targetFile, backupFile, versionDir))
					instructions.add(instruction);
			}
			else
			{
				instructions.add(instruction);
			}
		}
		
		File browser = new File(versionDir, "resources/vivaldi/browser.html");
		File browserBackup = new File(browser.getParentFile(), "browser.html.bak");
		
		if (browser.exists() && !browserBackup.exists())
		{
			anyNeededBackup = true;
			backupFile(browser, browserBackup, versionDir);
		}
		
		if (!anyNeededBackup)
			logger.log(null, String.format("  %s All files already had back-ups!", getResultString(true)));
		
		return instructions;
	}


	private boolean copyFiles(List<Instruction> instructions, File versionDir)
	{
		boolean allSuccessful = true;
		for (Instruction instruction : instructions)
		{
			File sourceFile = new File(modConfig.getModDir(), instruction.sourceFile);
			File targetDir = new File(versionDir, instruction.targetDirectory);
			File targetFile = new File(targetDir, sourceFile.getName());
			
			if (!copyFile(sourceFile, targetFile, modConfig.getModDir()))
				allSuccessful = false;
		}
		
		return allSuccessful;
	}


	private boolean backupFile(File file, File backupFile, File relativeTo)
	{
		String relativePath = relativeTo.toPath().relativize(file.toPath()).toString();
		boolean success;
		
		try
		{
			success = FileOperations.copyFile(file, backupFile);
		}
		catch (IOException e)
		{
			String message = String.format(
					"Could not back up %s, so it will not be replaced!", relativePath);
			errors.add(new LogMessage(Level.ERROR, "", message, e));
			
			success = false;
		}
		
		logger.log(null, String.format("  %s %s", getResultString(success), relativePath));
		return success;
	}


	private boolean copyFile(File file, File targetFile, File relativeTo)
	{
		String relativePath = relativeTo.toPath().relativize(file.toPath()).toString();
		boolean success;
		
		try
		{
			if (!file.exists())
				throw new FileNotFoundException(
						String.format("%s does not exist!", file.getPath()));
			if (!file.isFile())
				throw new IOException(
						String.format("%s is not a file!", file.getPath()));
			
			success = FileOperations.copyFile(file, targetFile);
		}
		catch (IOException e)
		{
			String message = String.format("Could not copy %s", relativePath);
			errors.add(new LogMessage(Level.ERROR, "", message, e));
			success = false;
		}

		logger.log(null, String.format("  %s %s", getResultString(success), relativePath));
		return success;
	}
	
	
	private boolean hasBrowserHtmlInstruction()
	{
		for (Instruction instruction : modConfig.getInstructions())
		{
			if (instruction.sourceFile.toLowerCase().endsWith("browser.html"))
				return true;
		}
		
		return false;
	}
	
	
	private boolean generateBrowserHtmlFile(File versionDir)
	{
		List<String> styleFiles = new ArrayList<>();
		List<String> scriptFiles = new ArrayList<>();

		Path pathVivaldi = new File(versionDir, "resources/vivaldi/").toPath();
		File fileBrowserHtml = pathVivaldi.resolve("browser.html").toFile();
		
		if (!fileBrowserHtml.exists())
		{
			logger.log(null, String.format("  %s Reading resources/vivaldi/browser.html",
							getResultString(false)));
			String message = "resources/vivaldi/browser.html does not exist!";
			errors.add(new LogMessage(Level.ERROR, message));
			return false;
		}
		
		for (Instruction instruction : modConfig.getInstructions())
		{
			if (!instruction.excludeFromBrowserHtml)
			{
				if (Utilities.isScript(instruction.sourceFile))
				{
					Path relative = getPathRelativeToBrowser(versionDir, pathVivaldi, instruction);
					scriptFiles.add(relative.toString());
				}
				else if (Utilities.isStyle(instruction.sourceFile))
				{
					Path relative = getPathRelativeToBrowser(versionDir, pathVivaldi, instruction);
					styleFiles.add(relative.toString());
				}
			}
		}
		
		return addStylesAndScripts(fileBrowserHtml, styleFiles, scriptFiles);
	}


	private Path getPathRelativeToBrowser(File versionDir, Path pathBrowser,
			Instruction instruction)
	{
		File source = new File(instruction.sourceFile);
		File targetDir = new File(versionDir, instruction.targetDirectory);
		Path pathTarget = new File(targetDir, source.getName()).toPath();
		
		return pathBrowser.relativize(pathTarget);
	}


	private boolean addStylesAndScripts(File file, List<String> styleFiles, List<String> scriptFiles)
	{
		if (styleFiles.isEmpty() && scriptFiles.isEmpty())
		{
			logger.log(null, String.format("  %s No files to add",
					getResultString(true)));
			return true;
		}

		File backupFile = new File(file.getParentFile(), "browser.html.bak");
		Document document = readBrowserHtml(backupFile);

		if (document != null)
		{
			updateHtmlDocument(document, styleFiles, scriptFiles);
			
			if (saveToFile(document, file))
			{
				for (String scriptFile : scriptFiles)
					logger.log(null, String.format("  %s Added %s", getResultString(true), scriptFile));
				for (String styleFile : styleFiles)
					logger.log(null, String.format("  %s Added %s", getResultString(true), styleFile));
				
				return true;
			}
		}
		else
		{
			logger.log(null, String.format("  %s Reading resources/vivaldi/browser.html",
							getResultString(false)));
		}
		
		return false;
	}


	private void updateHtmlDocument(Document document, List<String> styleFiles,
			List<String> scriptFiles)
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
			String message = "Could not read resources/vivaldi/browser.html!";
			errors.add(new LogMessage(Level.ERROR, "", message, e));
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
			String message = "Could not save the modified resources/vivaldi/browser.html!";
			errors.add(new LogMessage(Level.ERROR, "", message, e));

			return false;
		}
	}


	private void logErrors()
	{
		logger.log(null, String.format("%s ERROR%s OCCURRED", errors.size(), errors.size() != 1 ? "S" : ""));
		for (LogMessage error : errors)
		{
			String message = String.format("  %s %s", getResultString(false), error.message);
			logger.log(error.logLevel, error.location, message, error.throwable, false);
		}
		errors.clear();
	}
	
	
	private String getResultString(boolean success)
	{
		return success ? "[+]" : "[!]";
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
				builder.append("\n");
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


	private void notifyPatchFinished(boolean success)
	{
		for (PatchProgressListener listener : listeners)
			listener.onPatchFinished(success);
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


	@SuppressWarnings("unused")
	private void notifyNextModFile(Instruction instruction)
	{
		for (PatchProgressListener listener : listeners)
			listener.onNextModFile(instruction.sourceFile);
	}
	
	
	private class LogMessage
	{
		private final Level logLevel;
		private final String location;
		private final String message;
		private final Throwable throwable;
		
		public LogMessage(Level logLevel, String message)
		{
			this(logLevel, null, message, null);
		}

		public LogMessage(Level logLevel, String location, String message, Throwable t)
		{
			this.logLevel = logLevel;
			this.location = location;
			this.message = message;
			this.throwable = t;
		}
	}
}
