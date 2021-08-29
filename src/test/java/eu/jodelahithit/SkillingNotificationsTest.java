package eu.jodelahithit;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class SkillingNotificationsTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(SkillingNotificationsPlugin.class);
		RuneLite.main(args);
	}
}