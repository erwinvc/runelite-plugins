package eu.jodelahithit;

import lombok.extern.slf4j.Slf4j;
import com.google.common.base.Strings;
import net.runelite.api.*;
import net.runelite.api.Point;
import net.runelite.client.game.ItemManager;
import net.runelite.client.game.ItemStats;
import net.runelite.client.util.ColorUtil;

import java.awt.*;
import java.util.Set;

import static net.runelite.api.gameval.InterfaceID.Wornitems.EQUIPMENT;

@Slf4j
public class Utils {

    static void printAnimation(Client client){
        Player player = client.getLocalPlayer();
        if(player == null) return;
        int anim = player.getAnimation();
        log.debug("Skilling-notifications: " + anim);
    }

    public static int getStringWidth(Graphics graphics, String text) {
        FontMetrics metrics = graphics.getFontMetrics();
        return metrics.stringWidth(text);
    }

    public static int getStringHeight(Graphics graphics) {
        FontMetrics metrics = graphics.getFontMetrics();
        return metrics.getHeight();
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

    public static int getAttackSpeed(Client client, ItemManager itemManager) {
        final ItemContainer equipment = client.getItemContainer(EQUIPMENT);
        if (equipment != null) {
            Item[] items = equipment.getItems();
            int weaponSlot = EquipmentInventorySlot.WEAPON.getSlotIdx();
            if (items.length > weaponSlot) {
                Item weapon = items[weaponSlot];
                if (weapon != null && weapon.getId() > 0) {
                    final ItemStats stats = itemManager.getItemStats(weapon.getId());
                    if (stats != null && stats.getEquipment() != null) {
                        return stats.getEquipment().getAspeed();
                    }
                }
            }
        }
        return 4; // default unarmed
    }

    public static float clamp(float val, float min, float max) {
        return Math.max(min, Math.min(max, val));
    }

    public static float clamp01(float val) {
        return Math.max(0.0f, Math.min(1.0f, val));
    }
}
