package eu.jodelahithit;

import com.google.common.base.Strings;
import net.runelite.api.Client;
import net.runelite.api.Player;
import net.runelite.api.Point;
import net.runelite.client.util.ColorUtil;

import java.awt.*;

public class Utils {
    static boolean isInAnimation(Skill skill, Client client) {
        if(skill == Skill.NONE) return false;
        Player player = client.getLocalPlayer();
        if(player == null) return false;
        int anim = player.getAnimation();
        return skill.animations.contains(anim);
    }

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
}
