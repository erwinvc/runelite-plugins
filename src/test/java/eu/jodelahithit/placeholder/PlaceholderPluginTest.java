package eu.jodelahithit.placeholder;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class PlaceholderPluginTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(PlaceholderPlugin.class);
		RuneLite.main(args);
	}
}
