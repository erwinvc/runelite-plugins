package eu.jodelahithit;

import java.awt.*;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;

import net.runelite.api.Client;
import net.runelite.api.Point;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.util.ColorUtil;

class WintertodtNotificationsOverlay extends Overlay {
    private static final long INTERRUPTION_DURATION_NANOS = TimeUnit.SECONDS.toNanos(2);

    private static final float TEXT_COLOR_LERP = 0.75f;
    private static final int FLASH_HALF_PERIOD_CYCLES = 20;

    private final Client client;
    private final WintertodtNotificationsPlugin plugin;
    private final WintertodtNotificationsConfig config;

    private long lastFrameNanos;
    private float fadeValue;

    private int flashStartCycle;
    private boolean flashReadyPreviously;

    private long interruptionUntilNanos;
    private String interruptionText = "";

    @Inject
    private WintertodtNotificationsOverlay(Client client, WintertodtNotificationsPlugin plugin, WintertodtNotificationsConfig config) {
        super(plugin);
        setPosition(OverlayPosition.DYNAMIC);
        setLayer(OverlayLayer.ABOVE_SCENE);

        this.client = client;
        this.plugin = plugin;
        this.config = config;
    }

    private Color getFadedColor(Color input, boolean targetVisible) {
        long now = System.nanoTime();

        if (lastFrameNanos == 0L) {
            lastFrameNanos = now;
        }

        long elapsedNanos = now - lastFrameNanos;
        lastFrameNanos = now;

        int fadeDurationMillis = config.notificationFade();

        if (fadeDurationMillis <= 0) {
            fadeValue = targetVisible ? 1.0f : 0.0f;
        } else {
            long fadeDurationNanos =
                    TimeUnit.MILLISECONDS.toNanos(fadeDurationMillis);

            float change = elapsedNanos / (float) fadeDurationNanos;

            fadeValue += targetVisible ? change : -change;
            fadeValue = Utils.clamp01(fadeValue);
        }

        return new Color(
                input.getRed(),
                input.getGreen(),
                input.getBlue(),
                Math.round(input.getAlpha() * fadeValue)
        );
    }

    @Override
    public Dimension render(Graphics2D graphics) {
        boolean targetVisible = plugin.shouldRenderOverlay();

        Color configuredColor = config.overlayColor();
        Color fadedColor = getFadedColor(configuredColor, targetVisible);
        Color textColor = ColorUtil.colorLerp(
                Color.WHITE,
                configuredColor,
                TEXT_COLOR_LERP
        );

        boolean flashReady = targetVisible && (config.notificationFade() == 0 || fadeValue >= 1.0f);

        if (flashReady && !flashReadyPreviously) {
            flashStartCycle = client.getGameCycle();
        }

        flashReadyPreviously = flashReady;

        boolean flashHidden = false;

        if (flashReady && config.flash()) {
            int elapsedCycles = client.getGameCycle() - flashStartCycle;

            flashHidden = elapsedCycles % (FLASH_HALF_PERIOD_CYCLES * 2) >= FLASH_HALF_PERIOD_CYCLES;
        }

        long now = System.nanoTime();
        boolean interruptionActive = interruptionUntilNanos - now > 0L;

        int canvasWidth = client.getCanvasWidth();
        int canvasHeight = client.getCanvasHeight();

        if ((targetVisible || fadeValue > 0.001f) && !flashHidden) {
            Color previousColor = graphics.getColor();

            graphics.setColor(fadedColor);
            graphics.fillRect(0, 0, canvasWidth, canvasHeight);

            graphics.setColor(previousColor);

            if (!config.disableOverlayText() && !interruptionActive) {
                Utils.renderTextCentered(
                        graphics,
                        canvasWidth / 2, canvasHeight / 8 + Utils.getStringHeight(graphics),
                        "Wintertodt Notification",
                        textColor
                );
            }
        }

        if (interruptionActive) {
            Utils.renderTextCentered(
                    graphics,
                    canvasWidth / 2, canvasHeight / 8 + Utils.getStringHeight(graphics),
                    interruptionText,
                    textColor
            );
        }

        return null;
    }

    void showInterruption(String text) {
        interruptionText = text;
        interruptionUntilNanos = System.nanoTime() + INTERRUPTION_DURATION_NANOS;
    }

    void reset() {
        lastFrameNanos = 0L;
        fadeValue = 0.0f;
        flashReadyPreviously = false;
        interruptionUntilNanos = 0L;
        interruptionText = "";
    }
}
