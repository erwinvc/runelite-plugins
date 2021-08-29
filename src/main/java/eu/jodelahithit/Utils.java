package eu.jodelahithit;

import net.runelite.api.Client;
import net.runelite.api.Player;

import java.awt.*;

import static eu.jodelahithit.Constants.*;

public class Utils {
    static boolean IsInAnimation(Skill skill, Client client) {
        Player player = client.getLocalPlayer();
        if(player == null) return false;
        int anim = player.getAnimation();
        switch (skill) {
            case COOKING:       return COOKING_ANIMATIONS.contains(anim);
            case FISHING:       return FISHING_ANIMATIONS.contains(anim);
            case FLETCHING:     return FLETCHING_ANIMATIONS.contains(anim);
            case MINING:        return MINING_ANIMATIONS.contains(anim);
            case WOODCUTTING:   return WOODCUTTING_ANIMATIONS.contains(anim);
        }
        return false;
    }

    /*Force alpha level*/
    static Color EnsureAlphaLevel(Color color){
        int r = color.getRed();
        int g = color.getGreen();
        int b = color.getBlue();
        return new Color(r, g, b, 128);
    }
}
