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

import java.awt.Color;
import java.awt.Graphics;
import java.io.File;

import javax.swing.JPanel;

import komposten.vivaldi.backend.Backend;
import komposten.vivaldi.backend.Patcher.PatchProgressListener;


public class PatchProgressBar extends JPanel
{
	private enum State
	{
		PATCHING,
		SUCCESS,
		FAIL
	}
	
	private float progress = 0;
	private State state;
	

	public PatchProgressBar(Backend backend)
	{
		backend.registerProgressListener(progressListener);
	}


	@Override
	protected void paintComponent(Graphics g)
	{
		super.paintComponent(g);

		Color colour = (state == State.PATCHING ? Color.YELLOW : Color.RED);
		if (state == State.SUCCESS)
			colour = Color.GREEN;
		
		g.setColor(colour);
		g.fillRect(0, 0, (int) (getWidth() * progress), getHeight());
	}


	private PatchProgressListener progressListener = new PatchProgressListener()
	{
		private float total;
		private int fileCounter;


		@Override
		public void onPatchStarted()
		{
			this.fileCounter = 0;

			state = State.PATCHING;
			progress = 0;
			repaint();
		}
		
		
		@Override
		public void filesToPatch(int dirCount, int modFileCount)
		{
			this.total = dirCount * (float)modFileCount;
		}


		@Override
		public void onNextInstallation(File directory)
		{
			//Currently not used.
		}


		@Override
		public void onNextVersion(File versionDirectory)
		{
			//Currently not used.
		}


		@Override
		public void onNextModFile(String file)
		{
			progress = fileCounter / total;
			fileCounter++;
			repaint();
		}


		@Override
		public void onPatchFinished(boolean success)
		{
			progress = 1;
			state = (success ? State.SUCCESS : State.FAIL);
			repaint();
		}
	};
}
