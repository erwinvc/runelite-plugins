package eu.jodelahithit;

import net.runelite.client.config.*;

import java.awt.*;

@ConfigGroup("Skilling Notifications")
public interface SkillingNotificationsConfig extends Config {

    @ConfigItem(
            keyName = "selectedSkill",
            name = "Selected skill",
            description = "Select the skill that will cause a notification when not actively performing it",
            position = 0
    )
    default Skill selectedSkill() {
        return Skill.NONE;
    }

    @Alpha
    @ConfigItem(
            keyName = "overlayColor",
            name = "Notification color",
            description = "Set the notification overlay color",
            position = 1
    )
    default Color overlayColor() {
        return new Color(1.0f, 0.0f, 0.0f, 0.5f);
    }

    @ConfigItem(
            keyName = "notificationFlash",
            name = "Notification flash",
            description = "Flash the idle overlay",
            position = 2
    )
    default boolean flash() {
        return false;
    }

    @ConfigItem(
            keyName = "disableOverlayText",
            name = "Disable overlay text",
            description = "Disable the \"Skill Notification\" text on the overlay",
            position = 3
    )
    default boolean disableOverlayText() {
        return false;
    }

    @ConfigSection(
            name = "Extra Delays",
            description = "Set notification delays for individual skills",
            position = 4
    )
    String delays = "Extra Delays";

    @ConfigItem(
            keyName = "COOKING",
            name = "Cooking delay",
            description = "Add an extra delay before the cooking notification",
            position = 5,
            section = delays
    )
    default int cookingDelay() {
        return 0;
    }

    @ConfigItem(
            keyName = "CRAFTING",
            name = "Crafting delay",
            description = "Add an extra delay before the crafting notification",
            position = 6,
            section = delays
    )
    default int craftingDelay() {
        return 0;
    }

    @ConfigItem(
            keyName = "FISHING",
            name = "Fishing delay",
            description = "Add an extra delay before the fishing notification",
            position = 7,
            section = delays
    )
    default int fishingDelay() {
        return 0;
    }

    @ConfigItem(
            keyName = "FLETCHING",
            name = "Fletching delay",
            description = "Add an extra delay before the fletching notification",
            position = 8,
            section = delays
    )
    default int fletchingDelay() {
        return 0;
    }

    @ConfigItem(
            keyName = "HERBLORE",
            name = "Herblore delay",
            description = "Add an extra delay before the herblore notification",
            position = 9,
            section = delays
    )
    default int herbloreDelay() {
        return 0;
    }

    @ConfigItem(
            keyName = "MINING",
            name = "Mining delay",
            description = "Add an extra delay before the mining notification",
            position = 10,
            section = delays
    )
    default int miningDelay() {
        return 0;
    }

    @ConfigItem(
            keyName = "WOODCUTTING",
            name = "Woodcutting delay",
            description = "Add an extra delay before the woodcutting notification",
            position = 11,
            section = delays
    )
    default int woodcuttingDelay() {
        return 0;
    }

    @ConfigItem(
            keyName = "SMITHING",
            name = "Smithing delay",
            description = "Add an extra delay before the smithing notification",
            position = 12,
            section = delays
    )
    default int smithingDelay() {
        return 0;
    }
}
