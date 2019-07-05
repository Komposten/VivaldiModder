package komposten.vivaldi.util;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import komposten.vivaldi.TestUtils;

class DirectoryUtilsTest
{
	@Test
	void isVersionDir_validDir_true() throws IOException
	{
		File validDir = TestUtils.getTestFile("testfolders/IsVersionDir/Valid");
		File invalidDir = TestUtils.getTestFile("testfolders/IsVersionDir/Invalid");
		
		assertTrue(DirectoryUtils.isVersionDir(validDir));
		assertFalse(DirectoryUtils.isVersionDir(invalidDir));
	}
	
	
	@Test
	void isVersionDir_filesInsteadOfDirs_returnFalse() throws IOException
	{
		File dirIsFile = TestUtils.getTestFile("testfolders/IsVersionDir2/DirIsFile");
		File withResourceFile = TestUtils.getTestFile("testfolders/IsVersionDir2/WithResourceFile");
		File withVivaldiFile = TestUtils.getTestFile("testfolders/IsVersionDir2/WithVivaldiFile");

		assertFalse(DirectoryUtils.isVersionDir(dirIsFile));
		assertFalse(DirectoryUtils.isVersionDir(withResourceFile));
		assertFalse(DirectoryUtils.isVersionDir(withVivaldiFile));
	}
	
	
	@Test
	void getParentVivaldiDir_validPaths_returnVivaldiDir() throws IOException
	{
		File vivaldiDir = TestUtils.getTestFile("testfolders/GetParentVivaldi/");
		File versionDir = TestUtils.getTestFile("testfolders/GetParentVivaldi/Version1");
		File resourceDir = TestUtils.getTestFile("testfolders/GetParentVivaldi/Version1/resources");
		File vivaldiDir2 = TestUtils.getTestFile("testfolders/GetParentVivaldi/Version1/resources/vivaldi");
		File versionFile = TestUtils.getTestFile("testfolders/GetParentVivaldi/Version1/File");
		File vivaldiFile = TestUtils.getTestFile("testfolders/GetParentVivaldi/Version1/resources/vivaldi/File");

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
		File styleDir = TestUtils.getTestFile("testfolders/GetParentVivaldi/Version1/resources/vivaldi/style");

		assertNull(DirectoryUtils.getParentVivaldiDir(styleDir));
	}
	
	
	@Test
	void findVivaldiDirs_maxDepth1_dontFindDepth2() throws IOException
	{
		File vivaldiDir = TestUtils.getTestFile("testfolders/FindVivaldiDirs");
		List<File> vivaldiDirs = DirectoryUtils.findVivaldiDirs(vivaldiDir, 1);
		List<String> vivaldiNames = vivaldiDirs.stream().map(File::getName).collect(Collectors.toList());
		
		assertEquals(3, vivaldiDirs.size());

		assertAll(() -> assertTrue(vivaldiNames.contains("Vivaldi1")),
				() -> assertTrue(vivaldiNames.contains("Vivaldi2")),
				() -> assertTrue(vivaldiNames.contains("Vivaldi3")));
	}
	
	
	@Test
	void findVivaldiDirs_multipleVersions_noDuplicates() throws IOException
	{
		File vivaldiDir = TestUtils.getTestFile("testfolders/FindVivaldiDirs2");
		List<File> vivaldiDirs = DirectoryUtils.findVivaldiDirs(vivaldiDir, 1);
		List<String> vivaldiNames = vivaldiDirs.stream().map(File::getName).collect(Collectors.toList());
		
		assertEquals(2, vivaldiDirs.size());

		assertAll(() -> assertTrue(vivaldiNames.contains("Vivaldi1")),
				() -> assertTrue(vivaldiNames.contains("Vivaldi2")));
	}
	
	
	@Test
	void findVivaldiVersionDirs_noDepth_dontFindDeep() throws IOException
	{
		File vivaldiDir = TestUtils.getTestFile("testfolders/FindVersionDirs");
		List<File> versionDirs = DirectoryUtils.findVivaldiVersionDirs(vivaldiDir);
		List<String> versionNames = versionDirs.stream().map(File::getName).collect(Collectors.toList());
		
		assertEquals(3, versionDirs.size());
		assertAll(() -> assertTrue(versionNames.contains("Version1")),
				() -> assertTrue(versionNames.contains("Version2")),
				() -> assertTrue(versionNames.contains("Version3")));
	}
	
	
	@Test
	void findVivaldiVersionDirs_maxDepth1_dontFindDepth2() throws IOException
	{
		File vivaldiDir = TestUtils.getTestFile("testfolders/FindVersionDirs");
		List<File> versionDirs = DirectoryUtils.findVivaldiVersionDirs(vivaldiDir, 1);
		List<String> versionNames = versionDirs.stream().map(File::getName).collect(Collectors.toList());
		
		assertEquals(4, versionDirs.size());
		assertAll(() -> assertTrue(versionNames.contains("Version1")),
				() -> assertTrue(versionNames.contains("Version2")),
				() -> assertTrue(versionNames.contains("Version3")),
				() -> assertTrue(versionNames.contains("Version4")));
	}
	
	
	@Test
	void findVivaldiVersionDirs_fileInsteadOfDir_returnEmpty() throws IOException
	{
		File vivaldiDir = TestUtils.getTestFile("testfolders/FindVersionDirsFile");
		List<File> versionDirs = DirectoryUtils.findVivaldiVersionDirs(vivaldiDir, 1);
		
		assertEquals(0, versionDirs.size());
	}
	
	
	@Test
	void assemblePath()
	{
		String expected = "/aaa/bbb/ccc/ddd";
		assertAll(
			() -> assertEquals(expected, DirectoryUtils.assemblePath("aaa", "bbb", "ccc", "ddd")),
			() -> assertEquals(expected, DirectoryUtils.assemblePath("aaa/", "bbb/", "ccc/", "ddd")),
			() -> assertEquals(expected, DirectoryUtils.assemblePath("/aaa", "/bbb", "/ccc", "/ddd")),
			() -> assertEquals(expected, DirectoryUtils.assemblePath("/aaa/", "/bbb/", "/ccc/", "/ddd")),
			() -> assertEquals(expected, DirectoryUtils.assemblePath("/aaa", "bbb/", "/ccc", "ddd"))
		);
	}
	
	
	@Test
	void assemblePath_backslashAndForwardSlash_treatAsTheSame()
	{
		String pattern = "[/\\\\]aaa[/\\\\]bbb[/\\\\]ccc[/\\\\]ddd";

		assertAll(
				() -> assertTrue(Pattern.matches(pattern,
						DirectoryUtils.assemblePath("aaa/", "bbb\\", "ccc\\", "ddd"))),
				() -> assertTrue(Pattern.matches(pattern,
						DirectoryUtils.assemblePath("aaa\\", "/bbb/", "\\ccc\\", "/ddd"))));
	}
	
	
	@Test
	void assemblePath_noArguments_emptyString()
	{
		assertEquals("", DirectoryUtils.assemblePath());
	}
	
	
	@Test
	void truncatePath_zeroChars_removeNothing()
	{
		String path = "this/is/a/path/";
		assertEquals(path, DirectoryUtils.truncatePath(path, 0));
	}
	
	
	@Test
	void truncatePath_someChars_removeMiddlePartsFirst()
	{
		String path = "this/is/a/path/with/many/elements/";
		String expected = "this/.../with/many/elements/";
		assertEquals(expected, DirectoryUtils.truncatePath(path, 9));
	}
	
	
	@Test
	void truncatePath_mostChars_removeFromBeginningToPreserveEnd()
	{
		String path = "this/is/a/path/with/many/elements/";
		String expected = "...y/elements/";
		assertEquals(expected, DirectoryUtils.truncatePath(path, 23));
	}
}
