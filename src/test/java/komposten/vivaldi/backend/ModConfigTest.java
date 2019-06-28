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
package komposten.vivaldi.backend;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.junit.jupiter.api.Test;


class ModConfigTest
{
	private ModConfig getModConfig(String file)
			throws FileNotFoundException, IOException
	{
		String path = Thread.currentThread().getContextClassLoader()
				.getResource(file).getFile().replace("%20", " ");
		ModConfig config = new ModConfig(new File(path));
		return config;
	}


	@Test
	void loadConfig_singleInstructionNoTarget()
			throws FileNotFoundException, IOException
	{
		ModConfig config = getModConfig("single_file_1.ini");

		Instruction instruction = new Instruction("File1.txt", "", false);

		assertAll(
				() -> assertEquals(new File("C:\\Users\\Some User\\Desktop\\Mod"),
						config.getModDir()),
				() -> assertEquals(1, config.getVivaldiDirs().length),
				() -> assertEquals(1, config.getInstructions().size()));

		assertAll(
				() -> assertEquals("C:\\Users\\Some User\\AppData\\Local\\Vivaldi",
						config.getVivaldiDirs()[0].getPath()),
				() -> assertEquals(instruction, config.getInstructions().get(0)));
	}


	@Test
	void loadConfig_singleInstructionWithTarget()
			throws FileNotFoundException, IOException
	{
		ModConfig config = getModConfig("single_file_2.ini");

		Instruction instruction = new Instruction("File1.txt", "Subdir/Subsubdir", false);

		assertEquals(1, config.getInstructions().size());
		assertEquals(instruction, config.getInstructions().get(0));
	}


	@Test
	void loadConfig_multipleInstructions()
			throws FileNotFoundException, IOException
	{
		ModConfig config = getModConfig("multiple_files.ini");

		Instruction instruction1 = new Instruction("File1.txt", "Subdir/Subsubdir1", false);
		Instruction instruction2 = new Instruction("File2.txt", "Subdir/Subsubdir2", false);
		Instruction instruction3 = new Instruction("File3.txt", "Subdir/Subsubdir3", false);

		assertEquals(3, config.getInstructions().size());

		assertAll(
				() -> assertEquals(instruction1, config.getInstructions().get(0)),
				() -> assertEquals(instruction2, config.getInstructions().get(1)),
				() -> assertEquals(instruction3, config.getInstructions().get(2)));
	}


	@Test
	void loadConfig_multipleInstructionsAndExclude()
			throws FileNotFoundException, IOException
	{
		ModConfig config = getModConfig("multiple_files_2.ini");

		Instruction instruction1 = new Instruction("File1.txt", "", false);
		Instruction instruction2 = new Instruction("File2.txt", "", true);
		Instruction instruction3 = new Instruction("File3.txt", "Subdir/Subsubdir2", false);
		Instruction instruction4 = new Instruction("File4.txt", "Subdir/Subsubdir2", true);
		Instruction instruction5 = new Instruction("File5.txt", "Subdir/Subsubdir2", false);

		assertEquals(5, config.getInstructions().size());

		assertAll(
				() -> assertEquals(instruction1, config.getInstructions().get(0)),
				() -> assertEquals(instruction2, config.getInstructions().get(1)),
				() -> assertEquals(instruction3, config.getInstructions().get(2)),
				() -> assertEquals(instruction4, config.getInstructions().get(3)),
				() -> assertEquals(instruction5, config.getInstructions().get(4)));
	}


	@Test
	void loadConfig_multipleVivaldis()
			throws FileNotFoundException, IOException
	{
		ModConfig config = getModConfig("multiple_vivaldis.ini");

		Instruction instruction = new Instruction("File1.txt", "Subdir/Subsubdir", false);

		assertAll(
				() -> assertEquals(new File("C:\\Users\\Some User\\Desktop\\Mod"),
						config.getModDir()),
				() -> assertEquals(2, config.getVivaldiDirs().length),
				() -> assertEquals(1, config.getInstructions().size()));

		assertAll(
				() -> assertEquals("C:\\Users\\Some User\\AppData\\Local\\Vivaldi",
						config.getVivaldiDirs()[0].getPath()),
				() -> assertEquals("C:\\Users\\Some User\\AppData\\Local\\Vivaldi2",
						config.getVivaldiDirs()[1].getPath()),
				() -> assertEquals(instruction, config.getInstructions().get(0)));
	}
}
