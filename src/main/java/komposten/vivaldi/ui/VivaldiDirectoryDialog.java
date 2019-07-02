package komposten.vivaldi.ui;

import java.awt.Component;
import java.io.File;
import java.util.List;

import javax.swing.JFileChooser;

import komposten.vivaldi.util.DirectoryUtils;


public class VivaldiDirectoryDialog
{
	private JFileChooser chooser;
	
	public VivaldiDirectoryDialog()
	{
		chooser = new JFileChooser();
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		chooser.setDialogTitle("Choose a Vivaldi installation directory");
	}
	
	
	public void setCurrentDirectory(String directory)
	{
		setCurrentDirectory(new File(directory));
	}
	
	
	public void setCurrentDirectory(File directory)
	{
		chooser.setCurrentDirectory(directory);
	}
	
	
	public String[] show(Component parent)
	{
		int result = chooser.showOpenDialog(parent);
		
		if (result == JFileChooser.APPROVE_OPTION)
		{
			return getVivaldiDirs(chooser.getSelectedFile());
		}
		else
		{
			return new String[0];
		}
	}


	private String[] getVivaldiDirs(File file)
	{
		File parentVivaldi = DirectoryUtils.getParentVivaldiDir(file);
		
		if (parentVivaldi != null)
		{
			return new String[] { parentVivaldi.getPath() };
		}
		else
		{
			List<File> vivaldiDirs = DirectoryUtils.findVivaldiDirs(file, 5);
			
			return vivaldiDirs.stream()
					.map(File::getPath)
					.toArray(s -> new String[s]);
		}
	}
}
