package eu.jodelahithit;

import java.awt.*;
import javax.inject.Inject;

import net.runelite.api.Client;
import net.runelite.api.Point;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.util.ColorUtil;

class WintertodtNotificationsOverlay extends Overlay
{
	private final Client client;
	private final WintertodtNotificationsPlugin plugin;
	private final WintertodtNotificationsConfig config;
	private final float TEXT_COLOR_LERP = 0.75f;

	@Inject
	private WintertodtNotificationsOverlay(Client client, WintertodtNotificationsPlugin plugin, WintertodtNotificationsConfig config)
	{
		super(plugin);
		setPosition(OverlayPosition.DYNAMIC);
		setLayer(OverlayLayer.ABOVE_SCENE);
		this.client = client;
		this.plugin = plugin;
		this.config = config;
	}

	@Override
	public Dimension render(Graphics2D graphics) {
		if (plugin.shouldRenderOverlay()) {
			if(config.flash() && client.getGameCycle() % 40 >= 20) return null;
			Color color = graphics.getColor();
			graphics.setColor(config.overlayColor());
			graphics.fill(new Rectangle(client.getCanvas().getSize()));
			graphics.setColor(color);
			if (!config.disableOverlayText()) {
				net.runelite.api.Point location = new Point(client.getCanvasWidth() / 2, client.getCanvasHeight() / 8);
				Utils.renderTextCentered(graphics, location, "Wintertodt Notification", ColorUtil.colorLerp(Color.white, config.overlayColor(), TEXT_COLOR_LERP));
			}
		}
		return null;
	}
}
