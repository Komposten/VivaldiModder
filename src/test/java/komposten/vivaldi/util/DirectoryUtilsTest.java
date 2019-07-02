package komposten.vivaldi.util;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;

import org.junit.jupiter.api.Test;

class DirectoryUtilsTest
{
	private File getTestFile(String path) throws IOException
	{
		URL resource = Thread.currentThread().getContextClassLoader().getResource(path);
		
		if (resource != null)
		{
			String fixedPath = resource.getFile().replace("%20", " ");
			File validDir = new File(fixedPath);
			return validDir;
		}
		else
		{
			String msg = String.format("Could not load %s: File does not exist or access was denied!", path);
			throw new IOException(msg);
		}
	}
	
	
	@Test
	void isVersionDir_validDir_true() throws IOException
	{
		File validDir = getTestFile("testfolders/IsVersionDir/Valid");
		File invalidDir = getTestFile("testfolders/IsVersionDir/Invalid");
		
		assertTrue(DirectoryUtils.isVersionDir(validDir));
		assertFalse(DirectoryUtils.isVersionDir(invalidDir));
	}
	
	
	@Test
	void isVersionDir_filesInsteadOfDirs_returnFalse() throws IOException
	{
		File dirIsFile = getTestFile("testfolders/IsVersionDir2/DirIsFile");
		File withResourceFile = getTestFile("testfolders/IsVersionDir2/WithResourceFile");
		File withVivaldiFile = getTestFile("testfolders/IsVersionDir2/WithVivaldiFile");

		assertFalse(DirectoryUtils.isVersionDir(dirIsFile));
		assertFalse(DirectoryUtils.isVersionDir(withResourceFile));
		assertFalse(DirectoryUtils.isVersionDir(withVivaldiFile));
	}
	
	
	@Test
	void findVivaldiVersionDirs_noDepth_dontFindDeep() throws IOException
	{
		File vivaldiDir = getTestFile("testfolders/FindVersionDirs");
		List<File> versionDirs = DirectoryUtils.findVivaldiVersionDirs(vivaldiDir);
		
		assertEquals(3, versionDirs.size());
		assertAll(() -> assertEquals("Version1", versionDirs.get(0).getName()),
				() -> assertEquals("Version2", versionDirs.get(1).getName()),
				() -> assertEquals("Version3", versionDirs.get(2).getName()));
	}
	
	
	@Test
	void findVivaldiVersionDirs_maxDepth1_dontFindDepth2() throws IOException
	{
		File vivaldiDir = getTestFile("testfolders/FindVersionDirs");
		List<File> versionDirs = DirectoryUtils.findVivaldiVersionDirs(vivaldiDir, 1);
		
		assertEquals(4, versionDirs.size());
		assertAll(() -> assertEquals("Version1", versionDirs.get(0).getName()),
				() -> assertEquals("Version2", versionDirs.get(1).getName()),
				() -> assertEquals("Version3", versionDirs.get(2).getName()),
				() -> assertEquals("Version4", versionDirs.get(3).getName()));
	}
	
	
	@Test
	void findVivaldiVersionDirs_fileInsteadOfDir_returnEmpty() throws IOException
	{
		File vivaldiDir = getTestFile("testfolders/FindVersionDirsFile");
		List<File> versionDirs = DirectoryUtils.findVivaldiVersionDirs(vivaldiDir, 1);
		
		assertEquals(0, versionDirs.size());
	}
}
