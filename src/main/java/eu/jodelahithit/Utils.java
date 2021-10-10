package eu.jodelahithit;

import com.google.common.base.Strings;
import net.runelite.api.Point;
import net.runelite.client.util.ColorUtil;

import java.awt.*;
import java.time.Duration;
import java.time.Instant;

public class Utils {
    public static int getStringWidth(Graphics graphics, String text) {
        FontMetrics metrics = graphics.getFontMetrics();
        return metrics.stringWidth(text);
    }

    public static void renderTextCentered(Graphics2D graphics, Point txtLoc, String text, Color color) {
        if (!Strings.isNullOrEmpty(text)) {
            int x = txtLoc.getX();
            int y = txtLoc.getY();
            int halfStringWidth = getStringWidth(graphics, text) / 2;
            graphics.setColor(Color.BLACK);
            graphics.drawString(text, x - halfStringWidth+ 1, y + 1);
            graphics.setColor(ColorUtil.colorWithAlpha(color, 255));
            graphics.drawString(text, x - halfStringWidth, y);
        }
    }

    public static boolean checkInstant(Instant instant, float timeout) {
        return Duration.between(instant, Instant.now()).toMillis() < (timeout * 1000);
    }
}
