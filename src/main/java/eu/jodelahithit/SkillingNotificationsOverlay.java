package eu.jodelahithit;

import com.google.inject.Inject;
import net.runelite.api.Client;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.util.ColorUtil;

import java.awt.*;
import java.time.Instant;

public class SkillingNotificationsOverlay extends Overlay {
    private static final float TEXT_COLOR_LERP = 0.75f;
    private static final int FLASH_HALF_PERIOD = 20;

    private final Client client;
    private final SkillingNotificationsPlugin plugin;
    private final SkillingNotificationsConfig config;

    private long lastFadeTime = System.currentTimeMillis();
    private static Instant notificationInstant = Instant.now();
    private static String notificationText = "";
    private boolean previousShouldRender = false;

    private int flashStartCycle;
    private boolean flashReadyPreviously;

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
        Color fadedColor = getFadedColor(config.overlayColor(), shouldRender);
        Color textColor = ColorUtil.colorLerp(Color.white, config.overlayColor(), TEXT_COLOR_LERP);

        if (config.notificationSound() && shouldRender && !previousShouldRender) {
            Toolkit.getDefaultToolkit().beep();
        }
        previousShouldRender = shouldRender;

        boolean flashReady = shouldRender && (fadeValue >= 1.0f || config.notificationFade() == 0);

        if (flashReady && !flashReadyPreviously) {
            flashStartCycle = client.getGameCycle();
        }

        flashReadyPreviously = flashReady;

        boolean flashHidden = false;

        int canvasWidth = client.getCanvasWidth();
        int canvasHeight = client.getCanvasHeight();

        boolean shouldDisplayNotification = Session.checkInstant(notificationInstant, 2000);

        if (flashReady && config.flash()) {
            int elapsedCycles = client.getGameCycle() - flashStartCycle;
            flashHidden = elapsedCycles % (FLASH_HALF_PERIOD * 2) >= FLASH_HALF_PERIOD;
        }

        if ((shouldRender || fadeValue > 0.05f) && !flashHidden) {
            Color color = graphics.getColor();
            graphics.setColor(fadedColor);

            graphics.fillRect(0, 0, canvasWidth, canvasHeight);

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
