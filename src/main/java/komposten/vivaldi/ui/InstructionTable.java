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

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;

import komposten.vivaldi.backend.Instruction;


public class InstructionTable extends JTable
{
	public interface InstructionClickListener extends Serializable
	{
		public void onDoubleClick(Instruction instruction);
	}
	
	private static final int PADDING = 3;
	private static final int COL_SOURCE = 0;
	private static final int COL_ARROW = 1;
	private static final int COL_TARGET = 2;
	private InstructionTableModel tableModel;
	private InstructionClickListener clickListener;


	public InstructionTable()
	{
		super(new InstructionTableModel());
		tableModel = (InstructionTableModel) getModel();

		InstructionCellRenderer cellRenderer = new InstructionCellRenderer();
		TableColumn colArrow = getColumnModel().getColumn(COL_ARROW);
		TableColumn colSource = getColumnModel().getColumn(COL_SOURCE);
		TableColumn colTarget = getColumnModel().getColumn(COL_TARGET);

		setShowVerticalLines(false);
		setIntercellSpacing(new Dimension(0, getRowMargin()));

		getTableHeader().setReorderingAllowed(false);

		colSource.setCellRenderer(cellRenderer);
		colArrow.setCellRenderer(cellRenderer);
		colTarget.setCellRenderer(cellRenderer);
		setRowHeight(getRowHeight() + PADDING * 2);
		colArrow.setMaxWidth(25);
		colArrow.setMinWidth(25);
		
		addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseClicked(MouseEvent e)
			{
				if (e.getClickCount() == 2 && clickListener != null)
				{
					Instruction clicked = tableModel.getInstructions().get(getSelectedRow());
					clickListener.onDoubleClick(clicked);
				}
			}
		});
	}
	
	
	public void setClickListener(InstructionClickListener clickListener)
	{
		this.clickListener = clickListener;
	}


	public void setInstructions(List<Instruction> instructions)
	{
		List<Instruction> copy = new ArrayList<>(instructions.size());
		for (int i = 0; i < instructions.size(); i++)
			copy.add(new Instruction(instructions.get(i)));

		tableModel.setList(copy);
	}
	
	
	public List<Instruction> getInstructions()
	{
		return tableModel.getInstructions();
	}
	
	
	public void addInstruction(Instruction instruction)
	{
		tableModel.add(instruction);
	}
	
	
	public void replaceInstruction(Instruction oldInstruction, Instruction newInstruction)
	{
		tableModel.replace(oldInstruction, newInstruction);
	}
	
	
	public void removeInstructions(Instruction... instructions)
	{
		tableModel.remove(instructions);
	}
	
	
	public Instruction[] getSelectedInstructions()
	{
		Instruction[] result = new Instruction[getSelectedRowCount()];
		
		for (int i = 0; i < getSelectedRowCount(); i++)
		{
			int row = getSelectedRows()[i];
			result[i] = tableModel.getInstructions().get(row);
		}
		
		return result;
	}


	private static class InstructionTableModel extends AbstractTableModel
	{
		private List<InstructionPair> instructionPairs;
		private List<Instruction> instructionList = new ArrayList<>();


		public void setList(List<Instruction> instructions)
		{
			instructionPairs = new ArrayList<>(instructions.size());
			
			for (Instruction instruction : instructions)
			{
				Instruction prefixed = fixInstructionPaths(instruction);
				instructionPairs.add(new InstructionPair(instruction, prefixed));
			}

			listChanged();
		}
		
		
		public void add(Instruction instruction)
		{
			Instruction modified = fixInstructionPaths(instruction);
			instructionPairs.add(new InstructionPair(instruction, modified));
			
			listChanged();
		}
		
		
		public void replace(Instruction oldInstruction, Instruction newInstruction)
		{
			int index = instructionList.indexOf(oldInstruction);
			instructionPairs.remove(index);
			add(newInstruction);
		}
		
		
		public void remove(Instruction... instructions)
		{
			for (Instruction instruction : instructions)
			{
				int index = instructionList.indexOf(instruction);
				instructionPairs.remove(index);
			}
			
			listChanged();
		}


		private Instruction fixInstructionPaths(Instruction original)
		{
				String prefix = "[VERSION]";
				String dir = original.targetDirectory.replace('\\', '/');
				String src = original.sourceFile.replace('\\', '/');

				if (src.startsWith("/"))
					src = src.substring(1);
				if (!dir.startsWith("/"))
					prefix = prefix + "/";
				if (!dir.isEmpty() && !dir.endsWith("/"))
					dir = dir + "/";

				String result = prefix + dir;

				int navUpIndex;
				while ((navUpIndex = result.indexOf("../")) != -1)
				{
					int previousSlash = result.lastIndexOf('/', navUpIndex);

					if (previousSlash != -1)
						result = result.substring(0, previousSlash)
								+ result.substring(navUpIndex + 2);
				}

				return new Instruction(src, result);
		}
		
		
		private void listChanged()
		{
			Collections.sort(instructionPairs);
			
			instructionList = createInstructionList();

			fireTableDataChanged();
		}
		
		
		public List<Instruction> createInstructionList()
		{
			instructionList = new ArrayList<>(instructionPairs.size());
			
			for (InstructionPair instructionPair : instructionPairs)
				instructionList.add(instructionPair.original);
			
			return instructionList;
		}
		
		
		public List<Instruction> getInstructions()
		{
			return instructionList;
		}


		@Override
		public int getRowCount()
		{
			return instructionPairs == null ? 0 : instructionPairs.size();
		}


		@Override
		public int getColumnCount()
		{
			return 3;
		}


		@Override
		public String getColumnName(int column)
		{
			switch (column)
			{
				case COL_SOURCE :
					return "Mod file";
				case COL_TARGET :
					return "Destination folder";
				default :
					return "";
			}
		}


		@Override
		public Object getValueAt(int rowIndex, int columnIndex)
		{
			if (columnIndex == 1)
				return ">>";

			if (instructionPairs == null)
				return "";

			if (columnIndex == COL_SOURCE)
				return instructionPairs.get(rowIndex).modified.sourceFile;
			else if (columnIndex == COL_TARGET)
				return instructionPairs.get(rowIndex).modified.targetDirectory;
			return null;
		}
	}


	private class InstructionCellRenderer extends DefaultTableCellRenderer
	{
		@Override
		public Component getTableCellRendererComponent(JTable table, Object value,
				boolean isSelected, boolean hasFocus, int row, int column)
		{
			JLabel component = (JLabel) super.getTableCellRendererComponent(table,
					value, isSelected, false, row, column);

			EmptyBorder paddingBorder = new EmptyBorder(PADDING, PADDING, PADDING,
					PADDING);
			Border actualBorder = component.getBorder();
			component.setBorder(new CompoundBorder(actualBorder, paddingBorder));

			if (column == COL_ARROW)
			{
				component.setHorizontalAlignment(SwingConstants.CENTER);
			}
			else
			{
				component.setHorizontalAlignment(SwingConstants.LEADING);
			}

			return component;
		}
	}
	
	
	private static class InstructionPair implements Comparable<InstructionPair>
	{
		private final Instruction original;
		private final Instruction modified;
		
		private InstructionPair(Instruction original, Instruction modified)
		{
			this.original = original;
			this.modified = modified;
		}
		
		
		@Override
		public int hashCode()
		{
			return Objects.hash(original, modified);
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
			
			InstructionPair other = (InstructionPair) obj;
			if (modified == null)
			{
				if (other.modified != null)
					return false;
			}
			else if (!modified.equals(other.modified))
				return false;
			if (original == null)
			{
				if (other.original != null)
					return false;
			}
			else if (!original.equals(other.original))
				return false;
			return true;
		}
		
		
		@Override
		public int compareTo(InstructionPair o)
		{
			return modified.compareTo(o.modified);
		}
	}
}
