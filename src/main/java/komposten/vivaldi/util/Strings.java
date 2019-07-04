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

public class Strings
{
	public static final String EDIT_INSTRUCTION_EXCLUDE_TOOLTIP = 
			"<html>"
			+ "If this file or folder should be excluded from the auto-generated browser.html.<br />"
			+ "This <i>only</i> applies if there is no copy instruction for a browser.html file,<br />"
			+ "in which case a new file will be generated to include mod scripts and styles."
			+ "</html>";
	public static final String INSTRUCTION_TABLE_INCLUDE_TOOLTIP = 
			"<html>"
			+ "If this file should be included in the auto-generated browser.html.<br />"
			+ "This <i>only</i> applies if there is no copy instruction for a browser.html file,<br />"
			+ "in which case a new file will be generated to include mod scripts and styles."
			+ "</html>";
	public static final String EDIT_INSTRUCTION_INCLUDE_SUBFOLDERS = 
			"<html>"
			+ "If files in sub-folders should be included."
			+ "</html>";
	public static final String EDIT_INSTRUCTION_FOLDER_CONTENT = 
			"<html>"
			+ "If only the content of the folder should be copied, and not the folder itself."
			+ "</html>";
}
