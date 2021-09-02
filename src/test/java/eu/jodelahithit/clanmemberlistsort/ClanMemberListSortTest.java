package eu.jodelahithit.clanmemberlistsort;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class ClanMemberListSortTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(ClanMemberListSortPlugin.class);
		RuneLite.main(args);
	}
}
