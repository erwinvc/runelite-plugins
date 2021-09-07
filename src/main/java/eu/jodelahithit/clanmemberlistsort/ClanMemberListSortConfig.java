package eu.jodelahithit.clanmemberlistsort;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup(ClanMemberListSortPlugin.CONFIG_GROUP)
public interface ClanMemberListSortConfig extends Config
{
    @ConfigItem(
            keyName = "activeSortType",
            name = "Active sort method",
            description = "The active sort method that's used for sorting the clan member list",
            position = 0
    )
    default SortType activeSortType() {
        return SortType.SORT_BY_NAME;
    }

    @ConfigItem(
            keyName = "reverseSort",
            name = "Reverse sort method",
            description = "Reverse the sort method",
            position = 1
    )
    default boolean reverseSort() {
        return false;
    }
}
