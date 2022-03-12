package eu.jodelahithit;

import java.util.Set;

public enum Skill {
    NONE(null),
    COOKING(Constants.COOKING_ANIMATIONS),
    CRAFTING(Constants.CRAFTING_ANIMATIONS),
    FISHING(Constants.FISHING_ANIMATIONS),
    FIREMAKING(Constants.FIREMAKING_ANIMATIONS),
    FLETCHING(Constants.FLETCHING_ANIMATIONS),
    HERBLORE(Constants.HERBLORE_ANIMATIONS),
    MINING(Constants.MINING_ANIMATIONS),
    WOODCUTTING(Constants.WOODCUTTING_ANIMATIONS),
    SMITHING(Constants.SMITHING_ANIMATIONS),
    COMBAT(null);

    Set<Integer> animations;

    Skill(Set<Integer> animations) {
        this.animations = animations;
    }
}