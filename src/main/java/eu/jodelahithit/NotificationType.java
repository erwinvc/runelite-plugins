package eu.jodelahithit;

import java.util.Set;

public enum NotificationType {
    NONE(null, null),
    COOKING(Constants.COOKING_ANIMATIONS, null),
    CRAFTING(Constants.CRAFTING_ANIMATIONS, null),
    FISHING(Constants.FISHING_ANIMATIONS, null),
    FIREMAKING(Constants.FIREMAKING_ANIMATIONS, null),
    FLETCHING(Constants.FLETCHING_ANIMATIONS, null),
    HERBLORE(Constants.HERBLORE_ANIMATIONS, null),
    MINING(Constants.MINING_ANIMATIONS, null),
    WOODCUTTING(Constants.WOODCUTTING_ANIMATIONS, null),
    SMITHING(Constants.SMITHING_ANIMATIONS, null),
    MANIACALMONKEYS(null, "/eu/jodelahithit/monkey.png"),
    LUNAR(Constants.LUNAR_ANIMATIONS, "/eu/jodelahithit/lunar.png"),
    COMBAT(null, null),
    CUSTOMXP(null, null);


    Set<Integer> animations;
    String customImage;

    NotificationType(Set<Integer> animations, String customImage) {
        this.animations = animations;
        this.customImage = customImage;
    }
}