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

import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import komposten.utilities.logging.Level;
import komposten.utilities.logging.LogUtils;
import komposten.vivaldi.backend.Backend;
import komposten.vivaldi.backend.Instruction;
import komposten.vivaldi.backend.ModConfig;
import komposten.vivaldi.util.DirectoryUtils;


public class ModPanel extends JPanel
{
	private Backend backend;

	private JLabel labelModDir;
	private JLabel labelVivaldiDirs;
	private BrowseTextField fieldModDir;
	private VivaldiDirList listVivaldiDirs;
	private InstructionTable instructionsTable;
	private JPanel buttonPanel;
	private JPanel buttonPanel2;
	private JButton buttonAdd;
	private JButton buttonEdit;
	private JButton buttonRemove;
	private JButton buttonEditFile;

	private JButton buttonShowLog;
	private JButton buttonPatchAll;
	private JButton buttonPatchUnpatched;

	private PatchProgressBar progressBar;
	
	private EditInstructionDialog editDialog;
	private VivaldiDirectoryDialog vivaldiDialog;

	public ModPanel(String configPath)
	{
		super(new GridBagLayout());

		backend = new Backend(configPath);

		labelModDir = new JLabel("Mod directory:");
		labelVivaldiDirs = new JLabel("Vivaldi directories:");
		fieldModDir = new BrowseTextField("Choose your mod directory",
				JFileChooser.DIRECTORIES_ONLY, this::open);
		listVivaldiDirs = new VivaldiDirList(this::showVivaldiDirSelector,
				this::open);

		instructionsTable = new InstructionTable();
		instructionsTable.getSelectionModel().addListSelectionListener(selectionListener);
		instructionsTable.setClickListener(this::editInstruction);

		JScrollPane scrollPane = new JScrollPane(instructionsTable);
		scrollPane.setPreferredSize(new Dimension(600, 300));

		buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		buttonPanel2 = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		buttonAdd = new JButton("Add");
		buttonEdit = new JButton("Edit");
		buttonRemove = new JButton("Remove");
		buttonEditFile = new JButton("Edit file");
		buttonShowLog = new JButton("Show log");
		buttonPatchAll = new JButton("Patch all");
		buttonPatchUnpatched = new JButton("Patch unpatched");

		buttonEditFile.addActionListener(editFileListener);
		buttonAdd.addActionListener(action -> addInstruction());
		buttonRemove.addActionListener(action -> removeSelectedInstructions());
		buttonEdit.addActionListener(e -> editSelectedInstruction());
		buttonShowLog.addActionListener(e -> showPatchLog());
		buttonPatchAll.addActionListener(e -> saveAndPatch(true));
		buttonPatchUnpatched.addActionListener(e -> saveAndPatch(false));

		buttonPanel.add(buttonAdd);
		buttonPanel.add(buttonEdit);
		buttonPanel.add(buttonRemove);
		buttonPanel.add(buttonEditFile);

		buttonPanel2.add(buttonShowLog);
		buttonPanel2.add(buttonPatchAll);
		buttonPanel2.add(buttonPatchUnpatched);

		progressBar = new PatchProgressBar(backend);

		GridBagConstraints constraints = new GridBagConstraints();

		labelVivaldiDirs.setVerticalAlignment(SwingConstants.TOP);

		constraints.insets.set(3, 2, 3, 2);
		constraints.gridx = 0;
		constraints.gridy = 0;
		constraints.fill = GridBagConstraints.BOTH;
		add(labelModDir, constraints);
		constraints.gridx = 1;
		constraints.fill = GridBagConstraints.BOTH;
		constraints.weightx = 1;
		constraints.gridwidth = 2;
		add(fieldModDir, constraints);
		constraints.insets.top = 7;
		constraints.gridx = 0;
		constraints.gridy++;
		constraints.gridwidth = 1;
		constraints.fill = GridBagConstraints.BOTH;
		constraints.weightx = 0;
		add(labelVivaldiDirs, constraints);
		constraints.insets.top = 3;
		constraints.gridx = 1;
		constraints.gridwidth = 2;
		constraints.fill = GridBagConstraints.HORIZONTAL;
		add(listVivaldiDirs, constraints);
		constraints.gridx = 0;
		constraints.gridy++;
		constraints.gridwidth = 3;
		constraints.fill = GridBagConstraints.BOTH;
		constraints.weighty = 1;
		add(scrollPane, constraints);
		constraints.gridy++;
		constraints.gridx = 0;
		constraints.gridwidth = 3;
		constraints.weighty = 0;
		constraints.fill = GridBagConstraints.BOTH;
		add(progressBar, constraints);
		constraints.gridy++;
		constraints.gridwidth = 2;
		add(buttonPanel, constraints);
		constraints.gridx = 2;
		constraints.gridwidth = 1;
		add(buttonPanel2, constraints);

		addInitialData();

		backend.start();
	}


	private void addInitialData()
	{
		fieldModDir.getTextfield().setText(backend.getModConfig().getModDir().getPath());
		fieldModDir.setCurrentDirectory(backend.getModConfig().getModDir());
		instructionsTable.setInstructions(backend.getModConfig().getInstructions());
		instructionsTable.getSelectionModel().setSelectionInterval(0, 0);

		File[] dirFiles = backend.getModConfig().getVivaldiDirs();
		String[] dirs = new String[dirFiles.length];

		for (int i = 0; i < dirFiles.length; i++)
			dirs[i] = dirFiles[i].getPath();

		listVivaldiDirs.addDirectories(dirs, false);
	}


	private boolean saveData()
	{
		String modDirString = fieldModDir.getTextfield().getText();
		String[] vivaldiDirStrings = listVivaldiDirs.getDirectories();

		File modDir = new File(modDirString);
		File[] vivaldiDirs = Arrays.stream(vivaldiDirStrings)
				.map(File::new)
				.toArray(s -> new File[s]);

		List<Instruction> instructions = instructionsTable.getInstructions();

		ModConfig config = new ModConfig(backend.getModConfig().getConfigFile(), modDir, vivaldiDirs, instructions);

		List<String> errors = config.validate();

		if (errors.isEmpty())
		{
			backend.setModConfig(config);
			backend.saveModConfig();
			return true;
		}
		else
		{
			StringBuilder message = new StringBuilder();

			message.append(String.format("The config contains %d errors:", errors.size()));
			for (String error : errors)
				message.append("\n* " + error);

			JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(this),
					message.toString(), "Invalid config!", JOptionPane.ERROR_MESSAGE);
			
			return false;
		}
	}


	private void addInstruction()
	{
		if (!checkCanEdit())
			return;
		
		if (editDialog == null)
			editDialog = new EditInstructionDialog(SwingUtilities.getWindowAncestor(this));
		
		if (showEditDialog(null) == EditInstructionDialog.RESULT_OK)
		{
			Instruction instruction = editDialog.getInstruction();
			
			if (isModFileDirectory(instruction))
			{
				addDirectoryInstruction(instruction, editDialog.getOnlyFolderContent(),
						editDialog.getIncludeSubfolders());
			}
			else
			{
				instructionsTable.addInstruction(instruction);
			}
		}
	}


	private boolean isModFileDirectory(Instruction instruction)
	{
		File modFile = new File(fieldModDir.getTextfield().getText(), instruction.sourceFile);
		return modFile.isDirectory();
	}


	private boolean checkCanEdit()
	{
		if (listVivaldiDirs.getDirectories().length == 0)
		{
			String title = "Please add a Vivaldi directory!";
			String msg = "You cannot add or edit instructions until have you have added a Vivaldi directory!";
			JOptionPane.showMessageDialog(this, msg, title, JOptionPane.INFORMATION_MESSAGE);
			
			return false;
		}
		
		return true;
	}
	

	private void addDirectoryInstruction(Instruction instruction, boolean onlyContent, 
			boolean recursive)
	{
		File modDir = new File(fieldModDir.getTextfield().getText());
		File directory = new File(modDir, instruction.sourceFile);
		String target = instruction.targetDirectory;
		
		addInstructions(directory, directory, modDir, target,
				instruction.excludeFromBrowserHtml, onlyContent, recursive);
	}


	private void addInstructions(File directory, File relativeTo, File modDir,
			String targetDir, boolean excludeFromBrowserHtml, boolean onlyFolderContent,
			boolean recursive)
	{
		File[] children = directory.listFiles();
		
		if (children != null)
		{
			for (File child : children)
			{
				if (child.isFile())
				{
					String modFile = modDir.toPath().relativize(child.toPath()).toString();
					String target = relativeTo.toPath().relativize(directory.toPath()).toString();
					
					if (!onlyFolderContent)
						target = DirectoryUtils.assemblePath(targetDir, relativeTo.getName(), target);
					else
						target = DirectoryUtils.assemblePath(targetDir, target);
					
					Instruction instruction = new Instruction(modFile, target, excludeFromBrowserHtml);
					instructionsTable.addInstruction(instruction);
				}
				else if (child.isDirectory() && recursive)
				{
					addInstructions(child, relativeTo, modDir, targetDir, excludeFromBrowserHtml, onlyFolderContent, recursive);
				}
			}
		}
	}


	private int showEditDialog(Instruction instruction)
	{
		File modDir = new File(fieldModDir.getTextfield().getText());
		File vivaldiDir = new File(listVivaldiDirs.getDirectories()[0]);
		return editDialog.show(instruction, modDir, vivaldiDir);
	}


	private void editSelectedInstruction()
	{
		Instruction[] instructions = instructionsTable.getSelectedInstructions();
		
		if (instructions.length == 1)
			editInstruction(instructions[0]);
	}
	
	
	private void editInstruction(Instruction instruction)
	{
		if (!checkCanEdit())
			return;
		
		if (editDialog == null)
			editDialog = new EditInstructionDialog(SwingUtilities.getWindowAncestor(this));
		
		if (showEditDialog(instruction) == EditInstructionDialog.RESULT_OK)
		{
			Instruction newInstruction = editDialog.getInstruction();
			
			if (isModFileDirectory(newInstruction))
			{
				removeInstructions(instruction);
				addDirectoryInstruction(newInstruction, editDialog.getOnlyFolderContent(),
						editDialog.getIncludeSubfolders());
			}
			else
			{
				instructionsTable.replaceInstruction(instruction, newInstruction);
			}
		}
	}


	private void removeSelectedInstructions()
	{
		removeInstructions(instructionsTable.getSelectedInstructions());
	}
	
	
	private void removeInstructions(Instruction... instructions)
	{
		if (instructions.length > 0)
			instructionsTable.removeInstructions(instructions);
	}
	
	
	private String[] showVivaldiDirSelector()
	{
		if (vivaldiDialog == null)
		{
		  String[] vivaldiDirs = listVivaldiDirs.getDirectories();
		  
		  vivaldiDialog = new VivaldiDirectoryDialog();
		  if (vivaldiDirs.length > 0)
		  	vivaldiDialog.setCurrentDirectory(vivaldiDirs[vivaldiDirs.length-1]);
		}
		
		return vivaldiDialog.show(this);
	}
	
	
	private void showPatchLog()
	{
		try
		{
			Desktop.getDesktop().open(new File(Backend.FILE_PATCHLOG));
		}
		catch (IllegalArgumentException e)
		{
			String title = "Could not open the log!";
			String msg = "The patch log file doesn't exist! Look in log.txt for more details.";
			JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(ModPanel.this),
					msg, title, JOptionPane.ERROR_MESSAGE);
			
			LogUtils.log(Level.ERROR, ModPanel.class.getSimpleName(), msg, e, false);
		}
		catch (IOException e)
		{
			String title = "Could not open the log!";
			String msg = "The patch log could not be opened!\nReason: " + e.getMessage();
			JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(ModPanel.this),
					msg, title, JOptionPane.ERROR_MESSAGE);
			
			LogUtils.log(Level.ERROR, ModPanel.class.getSimpleName(), msg, e, false);
		}
	}
	
	
	private void saveAndPatch(boolean patchAll)
	{
		if (saveData())
			backend.applyMods(true, patchAll);
	}


	private ListSelectionListener selectionListener = new ListSelectionListener()
	{
		@Override
		public void valueChanged(ListSelectionEvent e)
		{
			buttonEdit.setEnabled(instructionsTable.getSelectedRowCount() == 1);
		}
	};


	private ActionListener editFileListener = new ActionListener()
	{
		@Override
		public void actionPerformed(ActionEvent event)
		{
			Instruction[] instructions = instructionsTable.getSelectedInstructions();

			for (Instruction instruction : instructions)
			{
				File modDir = backend.getModConfig().getModDir();
				File file = new File(modDir, instruction.sourceFile);

				try
				{
					Desktop.getDesktop().edit(file);
				}
				catch (IllegalArgumentException e)
				{
					String title = "Failed to open file editor!";
					String msg = "The selected file does not exist!";
					JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(ModPanel.this),
							msg, title, JOptionPane.ERROR_MESSAGE);
				}
				catch (IOException e)
				{
					String title = "Failed to open file editor!";
					String msg = "The file editor could not be opened!\nReason: " + e.getMessage();
					JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(ModPanel.this),
							msg, title, JOptionPane.ERROR_MESSAGE);
					
					LogUtils.log(Level.ERROR, ModPanel.class.getSimpleName(), msg, e, false);
				}
			}
		}
	};


	private void open(ActionEvent event)
	{
		File directory = null;
		if (event.getSource() == fieldModDir)
		{
			directory = backend.getModConfig().getModDir();
		}
		else
		{
			RemovableListItem listItem = (RemovableListItem) event.getSource();
			directory = new File(listItem.getText());
		}
		
		try
		{
			Desktop.getDesktop().open(directory);
		}
		catch (IllegalArgumentException e)
		{
			String title = "Failed to open the directory!";
			String msg = "The selected directory does not exist!";
			JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(ModPanel.this),
					msg, title, JOptionPane.ERROR_MESSAGE);
		}
		catch (IOException e)
		{
			String title = "Failed to open the directory!";
			String msg = "The directory could not be opened!\nReason: " + e.getMessage();
			JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(ModPanel.this),
					msg, title, JOptionPane.ERROR_MESSAGE);
			
			LogUtils.log(Level.ERROR, ModPanel.class.getSimpleName(), msg, e, false);
		}
	}
}
