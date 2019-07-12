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
	
	
	@Test
	void escapeQuotes_noQuotes_escapeBackslashes()
	{
		String string = "C:\\Windows\\path\\";
		String expected = "C:\\\\Windows\\\\path\\\\";
		assertEquals(expected, Utilities.escapeQuotes(string));
	}
	
	
	@Test
	void escapeQuotes_quotesNoBackslash_escapeQuotes()
	{
		String string = "String with \"funny\" quotes!";
		String expected = "String with \\\"funny\\\" quotes!";
		assertEquals(expected, Utilities.escapeQuotes(string));
	}
	
	
	@Test
	void escapeQuotes_stackedBackslashes_escapeEach()
	{
		String string = "path\\\\\\\\\"quotes\"";
		String expected = "path\\\\\\\\\\\\\\\\\\\"quotes\\\"";
		assertEquals(expected, Utilities.escapeQuotes(string));
	}
	
	
	@Test
	void escapeQuotes_quotesAndBackslash_escapeBackslashesBeforeQuotes()
	{
		String string = "path\\with\\backslashes\\and\\\"quotes\"";
		String expected = "path\\\\with\\\\backslashes\\\\and\\\\\\\"quotes\\\"";
		assertEquals(expected, Utilities.escapeQuotes(string));
	}
	
	
	@Test
	void unescapeQuotes_noQuotes_unescapeBackslashes()
	{
		String string = "C:\\\\Windows\\\\path\\\\";
		String expected = "C:\\Windows\\path\\";
		assertEquals(expected, Utilities.unescapeQuotes(string));
	}
	
	
	@Test
	void unescapeQuotes_quotesNoBackslash_unescapeQuotes()
	{
		String string = "String with \\\"funny\\\" quotes!";
		String expected = "String with \"funny\" quotes!";
		assertEquals(expected, Utilities.unescapeQuotes(string));
	}
	
	
	@Test
	void unescapeQuotes_stackedBackslashes_unescapeEach()
	{
		String string = "path\\\\\\\\\\\\\\\\\\\"quotes\\\"";
		String expected = "path\\\\\\\\\"quotes\"";
		assertEquals(expected, Utilities.unescapeQuotes(string));
	}
	
	
	@Test
	void unescapeQuotes_quotesAndBackslash_unescapeBoth()
	{
		String string = "path\\\\with\\\\backslashes\\\\and\\\\\\\"quotes\\\"";
		String expected = "path\\with\\backslashes\\and\\\"quotes\"";
		assertEquals(expected, Utilities.unescapeQuotes(string));
	}
	
	
	@Test
	void indexOfUnescapedQuote_noQuotes_findNothing()
	{
		assertEquals(-1, Utilities.indexOfUnescapedQuote("no quotes", 0));
	}
	
	
	@Test
	void indexOfUnescapedQuote_unescapedQuotes_findQuotes()
	{
		String string = "I \"have\" quotes!";
		assertEquals(2, Utilities.indexOfUnescapedQuote(string, 0));
		assertEquals(2, Utilities.indexOfUnescapedQuote(string, 2));
		assertEquals(7, Utilities.indexOfUnescapedQuote(string, 3));
	}
	
	
	@Test
	void indexOfUnescapedQuote_escapedQuotes_findNothing()
	{
		String string = "I \\\"have\\\\\\\" quotes!";
		assertEquals(-1, Utilities.indexOfUnescapedQuote(string, 0));
		assertEquals(-1, Utilities.indexOfUnescapedQuote(string, 2));
		assertEquals(-1, Utilities.indexOfUnescapedQuote(string, 3));
	}
	
	
	@Test
	void indexOfUnescapedQuote_escapedAndUnescapedQuotes_findUnescaped()
	{
		String string = "I \\\"have\" \\\"several\" quotes!";
		assertEquals(8, Utilities.indexOfUnescapedQuote(string, 0));
		assertEquals(19, Utilities.indexOfUnescapedQuote(string, 9));
	}
}
