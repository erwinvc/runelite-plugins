package eu.jodelahithit;

import net.runelite.client.config.*;

import java.awt.*;

@ConfigGroup("Skilling Notifications")
public interface SkillingNotificationsConfig extends Config {
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

    @ConfigItem(
            keyName = "disableWhenWalking",
            name = "Disable overlay while walking",
            description = "Disable the idle overlay when the player is walking or running",
            position = 4
    )
    default boolean disableWhenWalking() {
        return true;
    }

    @ConfigSection(
            name = "Selected skills",
            description = "Set the selected skills that will cause a notification when idling",
            position = 20
    )
    String selectedSkills = "Selected skills";

    @ConfigItem(
            keyName = "COOKING",
            name = "Cooking",
            description = "Causes notifications when the player is not actively cooking",
            position = 21,
            section = selectedSkills
    )
    default boolean cooking() {
        return false;
    }


    @ConfigItem(
            keyName = "CRAFTING",
            name = "Crafting",
            description = "Causes notifications when the player is not actively crafting",
            position = 22,
            section = selectedSkills
    )
    default boolean crafting() {
        return false;
    }

    @ConfigItem(
            keyName = "FISHING",
            name = "Fishing",
            description = "Causes notifications when the player is not actively fishing",
            position = 23,
            section = selectedSkills
    )
    default boolean fishing() {
        return false;
    }

    @ConfigItem(
            keyName = "FLETCHING",
            name = "Fletching ",
            description = "Causes notifications when the player is not actively fletching",
            position = 24,
            section = selectedSkills
    )
    default boolean fletching() {
        return false;
    }

    @ConfigItem(
            keyName = "HERBLORE",
            name = "Herblore",
            description = "Causes notifications when the player is not actively doing herblore",
            position = 25,
            section = selectedSkills
    )
    default boolean herblore() {
        return false;
    }

    @ConfigItem(
            keyName = "MINING",
            name = "Mining",
            description = "Causes notifications when the player is not actively mining",
            position = 26,
            section = selectedSkills
    )
    default boolean mining() {
        return false;
    }

    @ConfigItem(
            keyName = "WOODCUTTING",
            name = "Woodcutting",
            description = "Causes notifications when the player is not actively woodcutting",
            position = 27,
            section = selectedSkills
    )
    default boolean woodcutting() {
        return false;
    }

    @ConfigItem(
            keyName = "SMITHING",
            name = "Smithing",
            description = "Causes notifications when the player is not actively smithing",
            position = 28,
            section = selectedSkills
    )
    default boolean smithing() {
        return false;
    }

    @ConfigSection(
            name = "Extra Delays",
            description = "Set notification delays for individual skills. This means that it'll take longer for the notification to appear after the player started idling",
            position = 50
    )
    String delays = "Extra Delays";

    @ConfigItem(
            keyName = "COOKINGDELAY",
            name = "Cooking delay",
            description = "Add an extra delay before the cooking notification",
            position = 51,
            section = delays
    )
    default int cookingDelay() {
        return 0;
    }

    @ConfigItem(
            keyName = "CRAFTINGDELAY",
            name = "Crafting delay",
            description = "Add an extra delay before the crafting notification",
            position = 52,
            section = delays
    )
    default int craftingDelay() {
        return 0;
    }

    @ConfigItem(
            keyName = "FISHINGDELAY",
            name = "Fishing delay",
            description = "Add an extra delay before the fishing notification",
            position = 53,
            section = delays
    )
    default int fishingDelay() {
        return 0;
    }

    @ConfigItem(
            keyName = "FLETCHINGDELAY",
            name = "Fletching delay",
            description = "Add an extra delay before the fletching notification",
            position = 54,
            section = delays
    )
    default int fletchingDelay() {
        return 0;
    }

    @ConfigItem(
            keyName = "HERBLOREDELAY",
            name = "Herblore delay",
            description = "Add an extra delay before the herblore notification",
            position = 55,
            section = delays
    )
    default int herbloreDelay() {
        return 0;
    }

    @ConfigItem(
            keyName = "MININGDELAY",
            name = "Mining delay",
            description = "Add an extra delay before the mining notification",
            position = 56,
            section = delays
    )
    default int miningDelay() {
        return 0;
    }

    @ConfigItem(
            keyName = "WOODCUTTINGDELAY",
            name = "Woodcutting delay",
            description = "Add an extra delay before the woodcutting notification",
            position = 57,
            section = delays
    )
    default int woodcuttingDelay() {
        return 0;
    }

    @ConfigItem(
            keyName = "SMITHINGDELAY",
            name = "Smithing delay",
            description = "Add an extra delay before the smithing notification",
            position = 58,
            section = delays
    )
    default int smithingDelay() {
        return 0;
    }
}
