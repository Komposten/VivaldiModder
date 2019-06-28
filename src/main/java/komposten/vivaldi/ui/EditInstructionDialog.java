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
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Window;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import komposten.utilities.logging.Level;
import komposten.utilities.logging.LogUtils;
import komposten.vivaldi.backend.Instruction;
import komposten.vivaldi.util.DirectoryUtils;


public class EditInstructionDialog extends JDialog
{
	public static final int RESULT_OK = 0;
	public static final int RESULT_CANCEL = 1;
	
	private int result;
	
	private JLabel labelMod;
	private JLabel labelTarget;
	private BrowseTextField fieldMod;
	private BrowseTextField fieldTarget;
	private JButton buttonOk;
	private JButton buttonCancel;
	private File modDir;
	private File vivaldiDir;


	public EditInstructionDialog(Window owner)
	{
		super(owner);

		setModalityType(ModalityType.DOCUMENT_MODAL);
		setLayout(new GridBagLayout());

		labelMod = new JLabel("Mod file:");
		labelTarget = new JLabel("Target directory:");
		fieldMod = new BrowseTextField("Choose a mod file", JFileChooser.FILES_ONLY, false, null);
		fieldTarget = new BrowseTextField("Choose a target directory", JFileChooser.DIRECTORIES_ONLY, false, null);
		buttonOk = new JButton("Ok");
		buttonCancel = new JButton("Cancel");
		
		fieldTarget.setPreferredSize(new Dimension(340, fieldTarget.getPreferredSize().height));
		
		fieldMod.setBrowseListener(file -> setRelativePath(fieldMod, file, modDir));
		fieldTarget.setBrowseListener(file -> setRelativePath(fieldTarget, file, vivaldiDir));
		buttonOk.addActionListener(action -> close(true));
		buttonCancel.addActionListener(action -> close(false));
		
		JPanel panelButtons = new JPanel(new FlowLayout(FlowLayout.TRAILING));
		panelButtons.add(buttonOk);
		panelButtons.add(buttonCancel);
		
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.insets.set(2, 3, 2, 3);
		constraints.gridx = 0;
		constraints.gridy = 0;
		constraints.fill = GridBagConstraints.BOTH;
		add(labelMod, constraints);
		constraints.gridx++;
		constraints.weightx = 1;
		constraints.fill = GridBagConstraints.BOTH;
		add(fieldMod, constraints);
		constraints.gridx = 0;
		constraints.gridy++;
		constraints.weightx = 0;
		constraints.fill = GridBagConstraints.BOTH;
		add(labelTarget, constraints);
		constraints.gridx++;
		constraints.weightx = 1;
		constraints.fill = GridBagConstraints.BOTH;
		add(fieldTarget, constraints);
		constraints.gridx = 0;
		constraints.gridy++;
		constraints.weightx = 0;
		constraints.gridwidth = 2;
		constraints.fill = GridBagConstraints.BOTH;
		add(panelButtons, constraints);
		
		pack();
		setLocationRelativeTo(owner);
	}
	
	
	public Instruction getInstruction()
	{
		String modFile = fieldMod.getTextfield().getText();
		String targetDir = fieldTarget.getTextfield().getText();
		
		return new Instruction(modFile, targetDir);
	}
	
	
	public int show(Instruction instruction, File modDir, File vivaldiDir)
	{
		this.modDir = modDir;
		this.vivaldiDir = findVersionDir(vivaldiDir);
		
		if (instruction == null)
		{
			fieldMod.getTextfield().setText("");
			fieldTarget.getTextfield().setText("");
			setTitle("Add new instruction");
		}
		else
		{
			fieldMod.getTextfield().setText(instruction.sourceFile.replace('\\', '/'));
			fieldTarget.getTextfield().setText(instruction.targetDirectory.replace('\\', '/'));
			setTitle("Edit instruction");
		}
		
		fieldMod.setCurrentDirectory(this.modDir);
		fieldTarget.setCurrentDirectory(this.vivaldiDir);

		result = RESULT_CANCEL;
		
		setVisible(true);
		
		return result;
	}
	
	
	private File findVersionDir(File vivaldiDir)
	{
		File[] versionFolders = vivaldiDir.listFiles(DirectoryUtils.vivaldiVersionFolderFilter);
		return versionFolders[0];
	}


	private boolean validateData()
	{
		File modFile = new File(fieldMod.getTextfield().getText());
		File targetFile = new File(fieldTarget.getTextfield().getText());
		
		if (!modFile.isAbsolute())
			modFile = new File(modDir, fieldMod.getTextfield().getText());
		if (!targetFile.isAbsolute())
			targetFile = new File(vivaldiDir, fieldTarget.getTextfield().getText());
		
		List<String> errors = new ArrayList<>(2);
		if (!isFileInDirectory(modFile, modDir, false))
			errors.add("The mod file is not in the mod directory!");
		if (!isFileInDirectory(targetFile, vivaldiDir, false))
			errors.add("The target directory is not in the Vivaldi directory!");
		
		if (!errors.isEmpty())
		{
			StringBuilder builder = new StringBuilder();
			for (int i = 0; i < errors.size(); i++)
			{
				if (i != 0)
					builder.append('\n');
				builder.append(errors.get(i));
			}
			
			JOptionPane.showMessageDialog(this, builder.toString(), "Invalid input!", JOptionPane.ERROR_MESSAGE);
			return false;
		}
		
		return true;
	}


	private boolean isFileInDirectory(File file, File directory, boolean areFilesCanonical)
	{
		if (!areFilesCanonical)
		{
			try
			{
				file = file.getCanonicalFile();
				directory = directory.getCanonicalFile();
			}
			catch (IOException e)
			{
				String msg = "Could not access file system to get canonical paths!";
				LogUtils.log(Level.WARNING, getClass().getSimpleName(), msg, e, false);
			}
		}
		
		File parent = file;
		while (parent != null)
		{
			if (parent.equals(directory))
			{
				return true;
			}
			
			parent = parent.getParentFile();
		}
		
		return false;
	}
	
	
	private void setRelativePath(BrowseTextField browseField, File file, File relativeTo)
	{
		try 
		{
			file = file.getCanonicalFile();
			relativeTo = relativeTo.getCanonicalFile();
		}
		catch (IOException e)
		{
			String msg = "Could not access file system to get canonical paths!";
			LogUtils.log(Level.WARNING, getClass().getSimpleName(), msg, e, false);
		}
		
		String path = file.getAbsolutePath();
		
		if (isFileInDirectory(file, relativeTo, true))
		{
			path = path.replace(relativeTo.getAbsolutePath(), "");
		}
		
		browseField.getTextfield().setText(path.replace('\\', '/'));
	}
	
	
	private void close(boolean wasOkPressed)
	{
		if (wasOkPressed)
		{
			result = RESULT_OK;
			if (validateData())
				setVisible(false);
		}
		else
		{
			result = RESULT_CANCEL;
			setVisible(false);
		}
	}
}
