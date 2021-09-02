package eu.jodelahithit.placeholder;

import com.google.inject.Provides;
import javax.inject.Inject;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

@PluginDescriptor(
	name = "Placeholder",
	description = "Placeholder",
	tags = {}
)
public class PlaceholderPlugin extends Plugin
{
	@Inject
	private PlaceholderConfig config;

	@Inject
	private PlaceholderOverlay overlay;

	@Inject
	private OverlayManager overlayManager;

	@Provides
	PlaceholderConfig getConfig(ConfigManager configManager)
	{
		return configManager.getConfig(PlaceholderConfig.class);
	}

	@Override
	public void startUp()
	{
		overlayManager.add(overlay);
	}

	@Override
	public void shutDown()
	{
		overlayManager.remove(overlay);
	}
}
