package eu.jodelahithit;

import net.runelite.client.config.ConfigManager;

import javax.inject.Inject;
import java.awt.event.KeyEvent;

import net.runelite.client.input.KeyListener;

public class SkillingNotificationsListener implements KeyListener {
    @Inject
    SkillingNotificationsPlugin plugin;
    @Inject
    SkillingNotificationsConfig config;
    @Inject
    ConfigManager configManager;

    @Override
    public void keyPressed(KeyEvent e) {
        if (config.toggle().matches(e)) {
            boolean enabled = !Boolean.parseBoolean(configManager.getConfiguration(SkillingNotificationsConfig.CONFIG_GROUP, "enabled"));
            configManager.setConfiguration(SkillingNotificationsConfig.CONFIG_GROUP, "enabled", enabled);
            plugin.getPanel().repaintConfigButtons();
            plugin.showNotification(enabled ? "Enabled skilling notifications" : "Disabled skilling notifications");
        } else if (config.toggleFlash().matches(e)) {
            boolean enabled = !Boolean.parseBoolean(configManager.getConfiguration(SkillingNotificationsConfig.CONFIG_GROUP, "notificationFlash"));
            configManager.setConfiguration(SkillingNotificationsConfig.CONFIG_GROUP, "notificationFlash", enabled);
            plugin.getPanel().repaintConfigButtons();
            plugin.showNotification(enabled ? "Enabled notification flash" : "Disabled notification flash");
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }
}
