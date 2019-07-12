package komposten.vivaldi.util;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;

import org.junit.jupiter.api.Test;

class UtilitiesTest
{

	@Test
	void isStyle_styleFiles_true()
	{
		String[] extensions = Utilities.getStyleExtensions();
		
		Arrays.stream(extensions)
			.map(e -> "folder/file"+e)
			.forEach(f -> assertTrue(Utilities.isStyle(f), f + " should be a style file!"));
	}

	
	@Test
	void isStyle_scriptFiles_false()
	{
		String[] extensions = Utilities.getScriptExtensions();
		
		Arrays.stream(extensions)
			.map(e -> "folder/file"+e)
			.forEach(f -> assertFalse(Utilities.isStyle(f), f + " should not be a style file!"));
	}

	@Test
	void isScript_styleFiles_false()
	{
		String[] extensions = Utilities.getStyleExtensions();
		
		Arrays.stream(extensions)
			.map(e -> "folder/file"+e)
			.forEach(f -> assertFalse(Utilities.isScript(f), f + " should not be a script file!"));
	}

	
	@Test
	void isScript_scriptFiles_true()
	{
		String[] extensions = Utilities.getScriptExtensions();
		
		Arrays.stream(extensions)
			.map(e -> "folder/file"+e)
			.forEach(f -> assertTrue(Utilities.isScript(f), f + " should be a script file!"));
	}
}
