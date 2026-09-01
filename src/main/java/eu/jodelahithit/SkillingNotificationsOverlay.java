package eu.jodelahithit;

import com.google.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.Point;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.util.ColorUtil;

import java.awt.*;
import java.time.Instant;

public class SkillingNotificationsOverlay extends Overlay {
    private final Client client;
    private final SkillingNotificationsPlugin plugin;
    private final SkillingNotificationsConfig config;
    private final float TEXT_COLOR_LERP = 0.75f;
    private long lastFadeTime = System.currentTimeMillis();
    private static Instant notificationInstant = Instant.now();
    private static String notificationText = "";
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

        long now = System.currentTimeMillis();
        float difference = (now - lastFadeTime) / (float) fadeDuration;
        lastFadeTime = now;

        fadeValue += overlayEnabled ? difference : -difference;

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
        if (config.notificationSound() && shouldRender && !previousShouldRender) {
            Toolkit.getDefaultToolkit().beep();
        }
        previousShouldRender = shouldRender;

        Color fadedColor = getFadedColor(config.overlayColor(), shouldRender);
        Color textColor = ColorUtil.colorLerp(Color.white, config.overlayColor(), TEXT_COLOR_LERP);

        int canvasWidth = client.getCanvasWidth();
        int canvasHeight = client.getCanvasHeight();

        boolean shouldDisplayNotification = Session.checkInstant(notificationInstant, 2000);

        if (shouldRender || fadeValue > 0.05f) {
            boolean canFlash = fadeValue > 0.95f || config.notificationFade() == 0;
            boolean shouldFlash = canFlash && config.flash() && client.getGameCycle() % 40 >= 20;
            if (shouldFlash) {
                Color color = graphics.getColor();
                graphics.setColor(fadedColor);

                graphics.fillRect(0, 0, canvasWidth, canvasHeight);

                graphics.fill(new Rectangle(client.getCanvas().getSize()));
                graphics.setColor(color);

                if (!config.disableOverlayText() && !shouldDisplayNotification) {
                    Utils.renderTextCentered(
                            graphics,
                            canvasWidth / 2,
                            canvasHeight / 8 + Utils.getStringHeight(graphics),
                            "Skilling Notification",
                            textColor
                    );
                }
            }
        }
        if (shouldDisplayNotification) {
            Utils.renderTextCentered(
                    graphics,
                    canvasWidth / 2,
                    canvasHeight / 8 + Utils.getStringHeight(graphics),
                    notificationText,
                    textColor
            );
        }
        return null;
    }

    public void notify(String text) {
        notificationInstant = Instant.now();
        notificationText = text;
    }
}
