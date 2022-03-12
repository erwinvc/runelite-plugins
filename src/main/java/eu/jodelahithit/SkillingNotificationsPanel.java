package eu.jodelahithit;

import net.runelite.client.config.ConfigManager;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.PluginPanel;
import net.runelite.client.util.ImageUtil;
import org.apache.commons.lang3.StringUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.Dictionary;
import java.util.Hashtable;

public class SkillingNotificationsPanel extends PluginPanel {
    private Dictionary<String, BufferedImage> iconsCache = new Hashtable<>();
    private SkillingNotificationsPlugin plugin;
    private ConfigManager configManager;
    private JPanel group;
    private JTextArea textLabel;
    private JToggleButton walkingButton;

    SkillingNotificationsPanel(SkillingNotificationsPlugin plugin, ConfigManager configManager) {
        super();
        this.plugin = plugin;
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

        group = new JPanel();
        group.setLayout(new GridLayout(0, 2 , 7, 7));

        walkingButton = new JToggleButton("Disable overlay while walking");

        JPanel descriptionPanel = new JPanel();
        descriptionPanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        descriptionPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JTextArea description = new JTextArea(0, 25);
        description.setText("This plugin will display an overlay when the player isn't actively performing any of the following selected skills.\n\nExtra notification delays can be customized in the plugin configuration.");
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
        add(group, c);
        c.gridy++;
        add(walkingButton, c);
    }

    @Override
    public void onActivate() {
        repaintConfigButtons();
    }

    private void repaintConfigButtons() {
        group.removeAll();
        for (Skill skill : Skill.values()) {
            if (skill == Skill.NONE) continue;
            String skillIcon = "/skill_icons/" + skill.name().toLowerCase() + ".png";

            boolean isActive = Boolean.parseBoolean(configManager.getConfiguration("Skilling Notifications", skill.name()));
            JToggleButton toggleButton = new JToggleButton(new ImageIcon(GetIcon(skillIcon)), isActive);
            toggleButton.setToolTipText(StringUtils.capitalize(skill.name().toLowerCase()));
            toggleButton.setFocusable(false);
            toggleButton.addItemListener(new ItemListener() {

                public void itemStateChanged(ItemEvent ev) {
                    configManager.setConfiguration("Skilling Notifications", skill.name(), ev.getStateChange() == ItemEvent.SELECTED);
                }
            });

            group.add(toggleButton);
        }

        walkingButton = new JToggleButton("Disable overlay while walking", Boolean.parseBoolean(configManager.getConfiguration("Skilling Notifications", "disableWhenWalking")));
        walkingButton.setFocusable(false);
        walkingButton.setToolTipText("Forces the notification overlay to be disabled while walking or running");
        walkingButton.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent ev) {
                configManager.setConfiguration("Skilling Notifications", "disableWhenWalking", ev.getStateChange() == ItemEvent.SELECTED);
            }
        });
    }

    private BufferedImage GetIcon(String path) {
        BufferedImage iconImage = iconsCache.get(path);
        if (iconImage != null) return iconImage;
        iconImage = ImageUtil.loadImageResource(getClass(), path);
        iconsCache.put(path, iconImage);
        return iconImage;
    }
}
