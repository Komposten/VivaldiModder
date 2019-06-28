package komposten.vivaldi.util;

public class Utilities
{
	private static final String[] SCRIPT_EXTENSIONS = {
			".js",
	};
	
	private static final String[] STYLE_EXTENSIONS = {
			".css"
	};
	
	private Utilities() {}
	
	
	public static boolean isStyle(String file)
	{
		return fileHasExtension(file, STYLE_EXTENSIONS);
	}
	
	
	public static boolean isScript(String file)
	{
		return fileHasExtension(file, SCRIPT_EXTENSIONS);
	}
	
	
	private static boolean fileHasExtension(String file, String[] extensions)
	{
		for (String extension : extensions)
		{
			if (file.toLowerCase().endsWith(extension))
				return true;
		}
		
		return false;
	}
}
