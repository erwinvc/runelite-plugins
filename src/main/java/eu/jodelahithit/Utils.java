package eu.jodelahithit;

import net.runelite.api.Client;
import net.runelite.api.Player;

import java.awt.*;

import static eu.jodelahithit.Constants.*;

public class Utils {
    static boolean IsInAnimation(Skill skill, Client client) {
        if(skill == Skill.NONE) return false;
        Player player = client.getLocalPlayer();
        if(player == null) return false;
        int anim = player.getAnimation();
        return skill.animations.contains(anim);
    }
}
