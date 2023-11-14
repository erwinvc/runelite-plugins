package eu.jodelahithit;

import com.google.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.Point;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.util.ColorUtil;

import java.awt.*;
import java.time.Duration;
import java.time.Instant;

public class SkillingNotificationsOverlay extends Overlay {
    private final Client client;
    private final SkillingNotificationsPlugin plugin;
    private final SkillingNotificationsConfig config;
    private final float TEXT_COLOR_LERP = 0.75f;
    private Instant fadeInstant = Instant.now();
    private static Instant notificationInstant = Instant.now();
    private static String notificationText;
    private boolean previousShouldRender = false;

    @Inject
    private SkillingNotificationsOverlay(Client client, SkillingNotificationsPlugin plugin, SkillingNotificationsConfig config) {
        super(plugin);
        setPosition(OverlayPosition.DYNAMIC);
        setLayer(OverlayLayer.ABOVE_SCENE);
        this.client = client;
        this.plugin = plugin;
        this.config = config;
    }

    float fadeValue = 0.0f;

    private Color getFadedColor(Color input, boolean overlayEnabled) {
        int fadeDuration = config.notificationFade();
        if (fadeDuration == 0) {
            fadeValue = 0.0f;
            return input;
        }

        Instant now = Instant.now();
        float difference = (float) Duration.between(fadeInstant, now).toMillis() / fadeDuration;

        fadeValue += overlayEnabled ? difference : -difference;
        fadeInstant = now;

        fadeValue = Utils.clamp01(fadeValue);

        int r = input.getRed();
        int g = input.getGreen();
        int b = input.getBlue();
        int a = input.getAlpha();
        return new Color(r, g, b, (int) (a * fadeValue));
    }

    @Override
    public Dimension render(Graphics2D graphics) {
        boolean shouldRender = plugin.shouldRenderOverlay();
        if(config.notificationSound() && shouldRender && !previousShouldRender){
            Toolkit.getDefaultToolkit().beep();
        }
        previousShouldRender = shouldRender;

        Color fadedColor = getFadedColor(config.overlayColor(), shouldRender);
        if (shouldRender || fadeValue > 0.05f) {
            boolean canFlash = fadeValue > 0.95f || config.notificationFade() == 0;
            if (canFlash && config.flash() && client.getGameCycle() % 40 >= 20) return null;
            Color color = graphics.getColor();
            graphics.setColor(fadedColor);
            graphics.fill(new Rectangle(client.getCanvas().getSize()));
            graphics.setColor(color);
            if (!config.disableOverlayText()) {
                Point locationOffset = new Point(client.getCanvasWidth() / 2, client.getCanvasHeight() / 8 + Utils.getStringHeight(graphics));
                Utils.renderTextCentered(graphics, locationOffset, "Skilling Notification", ColorUtil.colorLerp(Color.white, config.overlayColor(), TEXT_COLOR_LERP));
            }
        }
        if (Session.checkInstant(notificationInstant, 2000)) {
            Point location = new Point(client.getCanvasWidth() / 2, client.getCanvasHeight() / 8);
            Utils.renderTextCentered(graphics, location, notificationText, ColorUtil.colorLerp(Color.white, config.overlayColor(), TEXT_COLOR_LERP));
        }
        return null;
    }

    public void Notify(String text) {
        notificationInstant = Instant.now();
        notificationText = text;
    }
}
