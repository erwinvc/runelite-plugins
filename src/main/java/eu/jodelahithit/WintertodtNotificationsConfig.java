package eu.jodelahithit;

import net.runelite.client.config.*;

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
            description = "Disable the \"Wintertodt Notification\" text on the overlay",
            position = 2
    )
    default boolean disableOverlayText() {
        return false;
    }

    @Range(
            min = 0,
            max = 2000
    )
    @Units(Units.MILLISECONDS)
    @ConfigItem(
            keyName = "notificationFade",
            name = "Fade duration",
            description = "Time taken to fade the notification in or out",
            position = 3
    )
    default int notificationFade()
    {
        return 250;
    }
}
