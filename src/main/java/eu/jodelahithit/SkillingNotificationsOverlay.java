package eu.jodelahithit;

import com.google.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.Point;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.util.ColorUtil;
import org.apache.commons.lang3.StringUtils;

import java.awt.*;

public class SkillingNotificationsOverlay extends Overlay {
    private final Client client;
    private final SkillingNotificationsPlugin plugin;
    private final SkillingNotificationsConfig config;
    private final float TEXT_COLOR_LERP = 0.75f;

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
        if (plugin.shouldRenderOverlay()) {
            if(config.flash() && client.getGameCycle() % 40 >= 20) return null;
            Color color = graphics.getColor();
            graphics.setColor(config.overlayColor());
            graphics.fill(new Rectangle(client.getCanvas().getSize()));
            graphics.setColor(color);
            if (!config.disableOverlayText()) {
                Point location = new Point(client.getCanvasWidth() / 2, client.getCanvasHeight() / 8);
                Utils.renderTextCentered(graphics, location, "Skilling Notification", ColorUtil.colorLerp(Color.white, config.overlayColor(), TEXT_COLOR_LERP));
            }
        }
        return null;
    }
}
