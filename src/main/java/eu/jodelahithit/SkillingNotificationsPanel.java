package eu.jodelahithit;

import net.runelite.client.config.ConfigManager;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.PluginPanel;
import net.runelite.client.util.ImageUtil;
import org.apache.commons.lang3.StringUtils;

import javax.inject.Inject;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.Dictionary;
import java.util.Hashtable;

public class SkillingNotificationsPanel extends PluginPanel {
    private final Dictionary<String, BufferedImage> iconsCache = new Hashtable<>();
    private final ConfigManager configManager;
    private final JPanel skillsPanel, enabledPanel, flashingPanel, soundPanel, walkingPanel, customPanel;

    @Inject
    SkillingNotificationsPanel(ConfigManager configManager) {
        super();
        this.configManager = configManager;
        setBorder(new EmptyBorder(5, 5, 5, 5));
        setBackground(ColorScheme.DARK_GRAY_COLOR);
        setLayout(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        c.weighty = 0;
        c.insets = new Insets(0, 0, 10, 0);

        final JLabel welcomeText = new JLabel("Skilling Notifications");
        welcomeText.setFont(FontManager.getRunescapeBoldFont());
        welcomeText.setHorizontalAlignment(JLabel.CENTER);

        skillsPanel = new JPanel();
        skillsPanel.setLayout(new GridLayout(0, 2 , 7, 7));

        enabledPanel = new JPanel();
        enabledPanel.setLayout(new GridLayout(1, 1, 0, 0));
        flashingPanel = new JPanel();
        flashingPanel.setLayout(new GridLayout(1, 1, 0, 0));
        walkingPanel = new JPanel();
        walkingPanel.setLayout(new GridLayout(1, 1, 0, 0));
        soundPanel = new JPanel();
        soundPanel.setLayout(new GridLayout(1, 2, 0, 0));
        customPanel = new JPanel();
        customPanel.setLayout(new GridLayout(1, 2, 0, 0));

        JPanel descriptionPanel = new JPanel();
        descriptionPanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        descriptionPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JTextArea description = new JTextArea(0, 25);
        description.setText("This plugin will display an overlay when the player is not actively performing any of the following selected skills.\n\nExtra notification delays can be configured in the plugin configuration.");
        description.setWrapStyleWord(true);
        description.setLineWrap(true);
        description.setOpaque(false);
        description.setEditable(false);
        description.setFocusable(false);
        description.setBackground(ColorScheme.DARK_GRAY_COLOR);
        description.setFont(FontManager.getRunescapeSmallFont());
        description.setBorder(new EmptyBorder(0, 0, 0, 0));

        descriptionPanel.add(description);

        repaintConfigButtons();

        add(welcomeText, c);
        c.gridy++;
        add(descriptionPanel, c);
        c.gridy++;
        add(enabledPanel, c);
        c.gridy++;
        add(walkingPanel, c);
        c.gridy++;
        add(flashingPanel, c);
        c.gridy++;
        add(soundPanel, c);
        c.gridy++;
        add(skillsPanel, c);
        c.gridy++;
        add(customPanel, c);
    }

    @Override
    public void onActivate() {
        repaintConfigButtons();
    }

    public void repaintConfigButtons() {
        setVisible(false);
        skillsPanel.removeAll();
        for (NotificationType notificationType : NotificationType.values()) {
            if (notificationType == NotificationType.NONE || notificationType == NotificationType.CUSTOMXP) continue;
            String skillIcon = "/skill_icons/" + notificationType.name().toLowerCase() + ".png";
            ImageIcon icon = new ImageIcon(GetIcon(notificationType.customImage == null ? skillIcon : notificationType.customImage));
            boolean isActive = Boolean.parseBoolean(configManager.getConfiguration("Skilling Notifications", notificationType.name()));
            JToggleButton toggleButton = new JToggleButton(icon, isActive);
            toggleButton.setToolTipText(StringUtils.capitalize(notificationType.name().toLowerCase()));
            toggleButton.setFocusable(false);
            toggleButton.addItemListener(ev -> configManager.setConfiguration("Skilling Notifications", notificationType.name(), ev.getStateChange() == ItemEvent.SELECTED));
            skillsPanel.add(toggleButton);
        }

        AddButton(enabledPanel, "enabled", "Enabled", "Toggles the overlay and plugin functionality");
        AddButton(walkingPanel, "disableWhenWalking", "Disable overlay while walking", "Forces the notification overlay to be disabled while walking or running");
        AddButton(flashingPanel, "notificationFlash", "Notification flash", "Flashes notifications at the configured interval");
        AddButton(soundPanel, "notificationSound", "Notification sound", "Plays a sound when the player is idle");
        AddButton(customPanel, "CUSTOMXP", "Custom XP", "Displays notifications when XP drops of the configured threshold are not received");

        SpinnerModel model = new SpinnerNumberModel(Integer.parseInt(configManager.getConfiguration("Skilling Notifications", "customXPValue")), 1, Integer.MAX_VALUE, 10);
        JSpinner spinner = new JSpinner(model);
        spinner.addChangeListener(ev -> configManager.setConfiguration("Skilling Notifications", "customXPValue", spinner.getValue()));
        customPanel.add(spinner);

        setVisible(true);
    }

    private void AddButton(JPanel panel, String configKey, String name, String description){
        panel.removeAll();
        JToggleButton button = new JToggleButton(name, Boolean.parseBoolean(configManager.getConfiguration("Skilling Notifications", configKey)));
        button.setFocusable(false);
        button.setToolTipText(description);
        button.addItemListener(ev -> configManager.setConfiguration("Skilling Notifications", configKey, ev.getStateChange() == ItemEvent.SELECTED));
        panel.add(button);
    }

    private BufferedImage GetIcon(String path) {
        BufferedImage iconImage = iconsCache.get(path);
        if (iconImage != null) return iconImage;
        iconImage = ImageUtil.loadImageResource(getClass(), path);
        iconsCache.put(path, iconImage);
        return iconImage;
    }
}
