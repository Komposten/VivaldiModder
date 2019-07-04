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
		
		if (location != null && !location.isEmpty())
			builder.append("In ").append(location).append(": ");
		
		builder.append(message).append("\r\n");
		
		if (throwable != null)
		{
			String indent = createIndentString(builder);
			
			while (throwable != null)
			{
				builder.append(indent).append("        Cause: ").append(throwable.getMessage()).append("\r\n");
				throwable = throwable.getCause();
			}
		}
		
		return builder.toString().replace('\\', '/');
	}

	private String createIndentString(StringBuilder builder)
	{
		int indentSize = builder.indexOf("|");
		char[] indentChars = new char[indentSize+2];
		Arrays.fill(indentChars, ' ');
		indentChars[indentSize] = '|';
		
		return new String(indentChars);
	}
}
