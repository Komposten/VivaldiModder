package komposten.vivaldi;

import java.io.File;
import java.io.IOException;
import java.net.URL;

public class TestUtils
{
	private TestUtils()
	{}
	
	
	public static File getTestFile(String path) throws IOException
	{
		URL resource = Thread.currentThread().getContextClassLoader().getResource(path);
		
		if (resource != null)
		{
			String fixedPath = resource.getFile().replace("%20", " ");
			File file = new File(fixedPath);
			return file;
		}
		else
		{
			String msg = String.format("Could not load %s: File does not exist or access was denied!", path);
			throw new IOException(msg);
		}
	}
	
}
