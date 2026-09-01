package eu.jodelahithit;

import com.google.inject.Inject;
import net.runelite.api.Client;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.util.ColorUtil;

import java.awt.*;
import java.util.concurrent.TimeUnit;

public class SkillingNotificationsOverlay extends Overlay {
    private static final long NOTIFICATION_DURATION_NANOS = TimeUnit.SECONDS.toNanos(2);
    private static final float TEXT_COLOR_LERP = 0.75f;
    private static final int FLASH_HALF_PERIOD = 20;

    private final Client client;
    private final SkillingNotificationsPlugin plugin;
    private final SkillingNotificationsConfig config;

    private long lastFadeTime = System.currentTimeMillis();
    private long notificationUntil = 0;
    private String notificationText = "";
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
        long now = System.currentTimeMillis();
        long elapsed = now - lastFadeTime;
        lastFadeTime = now;

        int fadeDuration = config.notificationFade();
        if (fadeDuration == 0)
        {
            fadeValue = 0.0f;
            return input;
        }

        float difference = elapsed / (float) fadeDuration;
        fadeValue += overlayEnabled ? difference : -difference;
        fadeValue = Utils.clamp01(fadeValue);

        return new Color(
                input.getRed(),
                input.getGreen(),
                input.getBlue(),
                (int) (input.getAlpha() * fadeValue)
        );
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

        boolean shouldDisplayNotification = notificationUntil - System.nanoTime() > 0;

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
        notificationUntil = System.nanoTime() + NOTIFICATION_DURATION_NANOS;
        notificationText = text;
    }
}
