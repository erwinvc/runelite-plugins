package eu.jodelahithit;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class WintertodtNotificationsTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(WintertodtNotificationsPlugin.class);
		RuneLite.main(args);
	}
}
