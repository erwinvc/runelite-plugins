package eu.jodelahithit;

import net.runelite.client.config.ConfigManager;

import javax.inject.Inject;
import javax.swing.*;
import java.awt.event.KeyEvent;

import net.runelite.client.input.KeyListener;
import net.runelite.client.util.SwingUtil;

public class SkillingNotificationsListener implements KeyListener {
    @Inject
    SkillingNotificationsPlugin plugin;
    @Inject
    SkillingNotificationsConfig config;
    @Inject
    SkillingNotificationsOverlay overlay;
    @Inject
    ConfigManager configManager;

    @Override
    public void keyPressed(KeyEvent e) {
        if (config.toggle().matches(e)) {
            boolean enabled = !Boolean.parseBoolean(configManager.getConfiguration("Skilling Notifications", "enabled"));
            configManager.setConfiguration("Skilling Notifications", "enabled", enabled);
            plugin.panel.repaintConfigButtons();
            overlay.Notify(enabled ? "Enabled skilling notifications" : "Disabled skilling notifications");
        } else if (config.toggleFlash().matches(e)) {
            boolean enabled = !Boolean.parseBoolean(configManager.getConfiguration("Skilling Notifications", "notificationFlash"));
            configManager.setConfiguration("Skilling Notifications", "notificationFlash", enabled);
            plugin.panel.repaintConfigButtons();
            overlay.Notify(enabled ? "Enabled notification flash" : "Disabled notification flash");

        }
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }
}
