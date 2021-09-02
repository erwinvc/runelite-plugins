package eu.jodelahithit.clanlistsort;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class ClanListSortTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(ClanListSortPlugin.class);
		RuneLite.main(args);
	}
}
