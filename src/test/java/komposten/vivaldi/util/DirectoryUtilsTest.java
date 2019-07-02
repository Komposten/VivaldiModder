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
	void getParentVivaldiDir_validPaths_returnVivaldiDir() throws IOException
	{
		File vivaldiDir = getTestFile("testfolders/GetParentVivaldi/");
		File versionDir = getTestFile("testfolders/GetParentVivaldi/Version1");
		File resourceDir = getTestFile("testfolders/GetParentVivaldi/Version1/resources");
		File vivaldiDir2 = getTestFile("testfolders/GetParentVivaldi/Version1/resources/vivaldi");
		File versionFile = getTestFile("testfolders/GetParentVivaldi/Version1/File");
		File vivaldiFile = getTestFile("testfolders/GetParentVivaldi/Version1/resources/vivaldi/File");

		assertAll(
				() -> assertEquals(vivaldiDir, DirectoryUtils.getParentVivaldiDir(versionDir), "/Version1"),
				() -> assertEquals(vivaldiDir, DirectoryUtils.getParentVivaldiDir(resourceDir), "/Version1/resources"),
				() -> assertEquals(vivaldiDir, DirectoryUtils.getParentVivaldiDir(vivaldiDir2), "/Version1/resources/vivaldi"),
				() -> assertEquals(vivaldiDir, DirectoryUtils.getParentVivaldiDir(versionFile), "/Version1/File"),
				() -> assertEquals(vivaldiDir, DirectoryUtils.getParentVivaldiDir(vivaldiFile), "/Version1/resources/vivaldi/File")
		);
	}
	
	
	@Test
	void getParentVivaldiDir_invalidPaths_returnNull() throws IOException
	{
		File styleDir = getTestFile("testfolders/GetParentVivaldi/Version1/resources/vivaldi/style");

		assertNull(DirectoryUtils.getParentVivaldiDir(styleDir));
	}
	
	
	@Test
	void findVivaldiDirs_maxDepth1_dontFindDepth2() throws IOException
	{
		File vivaldiDir = getTestFile("testfolders/FindVivaldiDirs");
		List<File> vivaldiDirs = DirectoryUtils.findVivaldiDirs(vivaldiDir, 1);
		
		assertEquals(3, vivaldiDirs.size());
		assertAll(() -> assertEquals("Vivaldi1", vivaldiDirs.get(0).getName()),
				() -> assertEquals("Vivaldi2", vivaldiDirs.get(1).getName()),
				() -> assertEquals("Vivaldi3", vivaldiDirs.get(2).getName()));
	}
	
	
	@Test
	void findVivaldiDirs_multipleVersions_noDuplicates() throws IOException
	{
		File vivaldiDir = getTestFile("testfolders/FindVivaldiDirs2");
		List<File> vivaldiDirs = DirectoryUtils.findVivaldiDirs(vivaldiDir, 1);
		
		assertEquals(2, vivaldiDirs.size());
		assertAll(() -> assertEquals("Vivaldi1", vivaldiDirs.get(0).getName()),
				() -> assertEquals("Vivaldi2", vivaldiDirs.get(1).getName()));
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
