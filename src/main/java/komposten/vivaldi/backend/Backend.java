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
import java.nio.file.ClosedWatchServiceException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.swing.JOptionPane;

import komposten.utilities.data.Settings;
import komposten.utilities.logging.Level;
import komposten.utilities.logging.LogUtils;
import komposten.utilities.logging.Logger;
import komposten.utilities.tools.FileOperations;
import komposten.vivaldi.backend.Patcher.PatchProgressListener;
import komposten.vivaldi.util.PatchLogFormatter;


public class Backend
{
	private static final String SETTING_APPLY_ON_START = "applyOnStart";
	private static final String SETTING_WATCH = "watchDirectories";

	static final String VERSION_PATTERN = "(\\d+\\.)+\\d+";

	private static final String FILE_SETTINGS = "settings.ini";
	static final String FILE_CONFIG = "config.ini";
	static final String FILE_PATCHED = "PATCHED";
	public static final String FILE_PATCHLOG = "patchlog.txt";
	
	private final String configPath;

	private WatchService watchService;
	private Map<WatchKey, File> keyToDirMap;

	private Logger patchLogger;
	private Patcher patcher;
	private ModConfig modConfig;
	private Settings appConfig;

	private Thread watcherThread;
	private WatcherRunnable watcherRunnable;
	private WorkerThread workerThread;


	public Backend(String configPath)
	{
		if (!LogUtils.hasInitialised())
			LogUtils.writeToFile("log.txt");
		
		this.configPath = (configPath != null ? configPath : FILE_CONFIG);

		workerThread = new WorkerThread();
		workerThread.start();

		patchLogger = new Logger(FILE_PATCHLOG);
		patchLogger.setFormatter(new PatchLogFormatter());
		clearLog();
		loadConfigs();
		patcher = new Patcher(modConfig, patchLogger);

	}


	public void start()
	{
		boolean configValid = validateModConfig();

		if (configValid)
		{
			if (appConfig.getBoolean(SETTING_APPLY_ON_START))
				applyMods(false, false);

			if (appConfig.getBoolean(SETTING_WATCH, true) && addFileWatchers())
				startFileWatch();
		}
	}


	public void registerProgressListener(PatchProgressListener progressListener)
	{
		patcher.addProgressListener(progressListener);
	}


	public ModConfig getModConfig()
	{
		return modConfig;
	}
	
	
	public void setModConfig(ModConfig newConfig)
	{
		workerThread.postRunnable(() ->
		{
			boolean watchDirs = appConfig.getBoolean(SETTING_WATCH);
			if (watchDirs)
				removeFileWatchers();
			
			this.modConfig = newConfig;
			patcher.setModConfig(modConfig);
			
			if (watchDirs)
				addFileWatchers();
		});
	}
	
	
	public void saveModConfig()
	{
		workerThread.postRunnable(() -> modConfig.save());
	}


	private void loadConfigs()
	{
		appConfig = new Settings(FILE_SETTINGS);

		try
		{
			modConfig = new ModConfig(new File(configPath));
		}
		catch (FileNotFoundException e)
		{
			//FIXME Don't show UI messages in the backend!
			String title = "Could not load the config!";
			String msg = String.format("The config file (%s) could not be found!"
					+ "%nStarting with an empty config.", configPath);
			JOptionPane.showMessageDialog(null, msg, title, JOptionPane.ERROR_MESSAGE);
			LogUtils.log(Level.ERROR, getClass().getSimpleName(), msg, e, false);

			modConfig = new ModConfig(new File(configPath), null, null, null);
		}
		catch (IOException e)
		{
			String title = "Could not load the config!";
			String msg = String.format("The config file (%s) could not be read:"
					+ "%n%s", configPath, e.getMessage());
			JOptionPane.showMessageDialog(null, msg, title, JOptionPane.ERROR_MESSAGE);
			LogUtils.log(Level.ERROR, getClass().getSimpleName(), msg, e, false);
		}
	}


	private boolean validateModConfig()
	{
		List<String> errors = modConfig.validate();

		if (!errors.isEmpty())
		{
			StringBuilder builder = new StringBuilder();
			builder.append(String.format("The config contains %d errors:", errors.size()));

			for (String error : errors)
				builder.append("\n" + error);

			LogUtils.log(Level.ERROR, builder.toString());

			//FIXME Don't show UI messages in the backend!
			JOptionPane.showMessageDialog(null, builder.toString(), "Invalid config!",
					JOptionPane.ERROR_MESSAGE);
		}

		return errors.isEmpty();
	}


	public void applyMods(boolean clearLog, boolean patchAll)
	{
		if (clearLog)
			clearLog();

		workerThread.postRunnable(() -> patcher.applyMods(patchAll));
	}


	private void applyMods(File vivaldiDir, boolean patchAll)
	{
		workerThread.postRunnable(() -> patcher.applyMods(vivaldiDir, patchAll));
	}


	private void clearLog()
	{
		File logFile = new File(FILE_PATCHLOG);

		if (logFile.exists())
			FileOperations.deleteFileOrFolder(logFile);
	}


	private boolean addFileWatchers()
	{
		try
		{
			watchService = FileSystems.getDefault().newWatchService();
			keyToDirMap = new HashMap<>();

			for (File dir : modConfig.getVivaldiDirs())
			{
				WatchKey key = dir.toPath().register(watchService,
						StandardWatchEventKinds.ENTRY_CREATE);
				keyToDirMap.put(key, dir);
			}
		}
		catch (IOException e)
		{
			String msg = "Failed to setup the Watch Service!";
			LogUtils.log(Level.WARNING, getClass().getSimpleName(), msg, e, false);

			return false;
		}

		return true;
	}


	private void removeFileWatchers()
	{
		if (watchService != null)
		{
			try
			{
				watchService.close();
			}
			catch (IOException e)
			{
				watcherThread.interrupt();
			}
	
			for (WatchKey key : keyToDirMap.keySet())
			{
				key.cancel();
			}
		}
	}


	private void startFileWatch()
	{
		watcherRunnable = new WatcherRunnable();
		watcherThread = new Thread(watcherRunnable, "FileWatcherThread");

		watcherThread.start();
	}


	public void setWatchDirectories(boolean watch)
	{
		boolean current = appConfig.getBoolean(SETTING_WATCH);

		if (watch != current)
		{
			appConfig.set(SETTING_WATCH, Boolean.toString(watch));

			try
			{
				appConfig.saveToFile();
			}
			catch (IOException e)
			{
				String msg = "Could not save the application settings!";
				LogUtils.log(Level.WARNING, getClass().getSimpleName(), msg, e, false);
			}

			if (watch)
				addFileWatchers();
			else
				removeFileWatchers();
		}
	}


	private class WatcherRunnable implements Runnable
	{
		boolean running = true;


		@Override
		public void run()
		{
			while (running)
			{
				WatchKey key;
				try
				{
					key = watchService.take();
				}
				catch (InterruptedException | ClosedWatchServiceException e)
				{
					Thread.currentThread().interrupt();
					break;
				}

				for (WatchEvent<?> event : key.pollEvents())
				{
					WatchEvent.Kind<?> kind = event.kind();

					if (kind == StandardWatchEventKinds.OVERFLOW)
						continue;

					@SuppressWarnings("unchecked")
					WatchEvent<Path> pathEvent = (WatchEvent<Path>) event;
					File file = pathEvent.context().toFile();

					if (file.getName().matches(VERSION_PATTERN))
					{
						applyMods(keyToDirMap.get(key), false);
					}
				}
			}
		}
	}


	private class WorkerThread extends Thread
	{
		private BlockingQueue<Runnable> runnables;


		public WorkerThread()
		{
			super("PatchThread");

			this.runnables = new LinkedBlockingQueue<>();
		}


		void postRunnable(Runnable runnable)
		{
			runnables.add(runnable);
		}


		@Override
		public void run()
		{
			while (true)
			{
				try
				{
					runnables.take().run();
				}
				catch (InterruptedException e)
				{
					Thread.currentThread().interrupt();
					break;
				}
			}
		}
	}
}
