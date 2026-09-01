package eu.jodelahithit;

import com.google.common.base.Strings;
import net.runelite.client.util.ColorUtil;

import java.awt.*;

public class Utils {
    public static int getStringWidth(Graphics graphics, String text) {
        FontMetrics metrics = graphics.getFontMetrics();
        return metrics.stringWidth(text);
    }

    public static int getStringHeight(Graphics graphics) {
        FontMetrics metrics = graphics.getFontMetrics();
        return metrics.getHeight();
    }

    public static void renderTextCentered(Graphics2D graphics, int x, int y, String text, Color color) {
        if (Strings.isNullOrEmpty(text)) {
            return;
        }

        int halfWidth = graphics.getFontMetrics().stringWidth(text) / 2;

        graphics.setColor(Color.BLACK);
        graphics.drawString(text, x - halfWidth + 1, y + 1);

        graphics.setColor(ColorUtil.colorWithAlpha(color, 255));
        graphics.drawString(text, x - halfWidth, y);
    }

    public static float clamp(float val, float min, float max) {
        return Math.max(min, Math.min(max, val));
    }

    public static float clamp01(float val) {
        return Math.max(0.0f, Math.min(1.0f, val));
    }
}
