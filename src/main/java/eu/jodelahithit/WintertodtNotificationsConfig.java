package eu.jodelahithit;

import net.runelite.client.config.Alpha;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

import java.awt.*;

@ConfigGroup("wintertodt-notifications")
public interface WintertodtNotificationsConfig extends Config
{
    @Alpha
    @ConfigItem(
            keyName = "overlayColor",
            name = "Notification color",
            description = "Set the notification overlay color",
            position = 0
    )
    default Color overlayColor() {
        return new Color(1.0f, 0.0f, 0.0f, 0.5f);
    }

    @ConfigItem(
            keyName = "notificationFlash",
            name = "Notification flash",
            description = "Flash the overlay",
            position = 1
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
}
