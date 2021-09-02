package eu.jodelahithit.clanlistsort;

import com.google.inject.Provides;

import javax.inject.Inject;

import net.runelite.client.config.ConfigManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

@PluginDescriptor(
        name = "Clan List Sorting",
        description = "Adds a sort button to the clan member list",
        tags = {"clan", "list", "sort", "sorting", "members", "alphabetically"}
)
public class ClanListSortPlugin extends Plugin {
    @Inject
    private ClanListSortConfig config;

    @Provides
    ClanListSortConfig getConfig(ConfigManager configManager) {
        return configManager.getConfig(ClanListSortConfig.class);
    }

    @Override
    public void startUp() {

    }

    @Override
    public void shutDown() {

    }
}
