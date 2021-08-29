package eu.jodelahithit;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;

import java.awt.*;

@ConfigGroup("Skilling Notifications")
public interface SkillingNotificationsConfig extends Config {

    @ConfigItem(
            keyName = "selectedSkill",
            name = "Selected skill",
            description = "The selected skill that will cause a notification when not actively performing it",
            position = 0
    )
    default Skill SelectedSkill() {
        return Skill.NONE;
    }

    @ConfigItem(
            keyName = "overlayColor",
            name = "Notification color",
            description = "Set the notification overlay color",
            position = 1
    )
    default Color OverlayColor() {
        return new Color(1.0f, 0.0f, 0.0f, 1.0f);
    }

    @ConfigSection(
            name = "Extra Delays",
            description = "Set notification delays for individual skills",
            position = 2
    )
    String delays = "Extra Delays";

    @ConfigItem(
            keyName = "COOKING",
            name = "Cooking delay",
            description = "Add an extra delay before the cooking notification",
            position = 2,
            section = delays
    )
    default int CookingDelay() {
        return 0;
    }

    @ConfigItem(
            keyName = "FISHING",
            name = "Fishing delay",
            description = "Add an extra delay before the fishing notification",
            position = 2,
            section = delays
    )
    default int FishingDelay() {
        return 0;
    }

    @ConfigItem(
            keyName = "FLETCHING",
            name = "Fletching delay",
            description = "Add an extra delay before the fletching notification",
            position = 2,
            section = delays
    )
    default int FletchingDelay() {
        return 0;
    }

    @ConfigItem(
            keyName = "MINING",
            name = "Mining delay",
            description = "Add an extra delay before the mining notification",
            position = 2,
            section = delays
    )
    default int MiningDelay() {
        return 0;
    }

    @ConfigItem(
            keyName = "WOODCUTTING",
            name = "Woodcutting delay",
            description = "Add an extra delay before the woodcutting notification",
            position = 2,
            section = delays
    )
    default int WoodcuttingDelay() {
        return 0;
    }
}
