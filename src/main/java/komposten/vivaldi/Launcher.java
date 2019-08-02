package komposten.vivaldi;

import java.io.IOException;

import komposten.vivaldi.backend.Backend;

public class Launcher
{
	public static void main(String[] args) throws IOException
	{
		new Backend(null);
	}
}
