package eu.jodelahithit;

import com.google.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.Point;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayUtil;
import net.runelite.client.util.ColorUtil;
import org.apache.commons.lang3.StringUtils;

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
            Color overlayColor = ColorUtil.colorWithAlpha(config.OverlayColor(), 128);
            graphics.setColor(overlayColor);
            graphics.fill(new Rectangle(client.getCanvas().getSize()));
            graphics.setColor(color);
            if(!config.DisableOverlayText())OverlayUtil.renderTextLocation(graphics, new Point(client.getCanvasWidth() / 2, client.getCanvasHeight() / 8), StringUtils.capitalize(plugin.GetSelectedSkill().name().toLowerCase()) + " Notification", ColorUtil.colorLerp(Color.white, overlayColor, 0.75f));
        }
        return null;
    }
}
