package eu.jodelahithit;

import com.google.inject.Inject;
import net.runelite.api.Client;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;

import java.awt.*;

public class SkillingNotificationsOverlay extends Overlay {
    private final Client client;
    private final SkillingNotificationsPlugin plugin;
    private final SkillingNotificationsConfig config;

    @Inject
    private SkillingNotificationsOverlay(Client client, SkillingNotificationsPlugin plugin, SkillingNotificationsConfig config) {
        super(plugin);
        setPosition(OverlayPosition.DYNAMIC);
        setLayer(OverlayLayer.ABOVE_SCENE);
        this.client = client;
        this.plugin = plugin;
        this.config = config;
    }

    @Override
    public Dimension render(Graphics2D graphics) {
        if (plugin.ShouldRenderOverlay()) {
            Color color = graphics.getColor();
            graphics.setColor(Utils.EnsureAlphaLevel(config.OverlayColor()));
            graphics.fill(new Rectangle(client.getCanvas().getSize()));
            graphics.setColor(color);
        }
        return null;
    }
}
