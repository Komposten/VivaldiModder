package komposten.vivaldi.util;

import java.text.DateFormat;
import java.util.Arrays;
import java.util.Calendar;

import komposten.utilities.logging.Level;
import komposten.utilities.logging.LogFormatter;

public class PatchLogFormatter implements LogFormatter
{
	@Override
	public String format(Level logLevel, Calendar date, String location, String message,
			Throwable throwable, boolean includeStackTrace)
	{
		DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.LONG);
		
		StringBuilder builder = new StringBuilder();
		
		builder.append(dateFormat.format(date.getTime())).append(" | ");
		
		if (logLevel != null)
		{
			builder.append(logLevel.getName());
			
			if (location != null && !location.isEmpty())
				builder.append(" in ").append(location).append(": ");
			else
				builder.append(": ");
		}
		else
		{
			if (location != null && !location.isEmpty())
				builder.append("In ").append(location).append(": ");
		}
		
		builder.append(message).append("\r\n");
		
		if (throwable != null)
		{
			String indent = createIndentString(builder);
			
			while (throwable != null)
			{
				builder.append(indent).append("Cause: ").append(throwable.getMessage()).append("\r\n");
				throwable = throwable.getCause();
			}
		}
		
		return builder.toString();
	}

	private String createIndentString(StringBuilder builder)
	{
		int indentSize = builder.indexOf("|");
		char[] indentChars = new char[indentSize+4];
		Arrays.fill(indentChars, ' ');
		indentChars[indentSize] = '|';
		indentChars[indentSize+2] = '>';
		
		return new String(indentChars);
	}
}
