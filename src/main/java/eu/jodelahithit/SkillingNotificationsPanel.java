package eu.jodelahithit;

import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.PluginPanel;
import net.runelite.client.ui.components.materialtabs.MaterialTab;
import net.runelite.client.ui.components.materialtabs.MaterialTabGroup;
import net.runelite.client.util.ImageUtil;
import org.apache.commons.lang3.StringUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.Dictionary;
import java.util.Hashtable;

public class SkillingNotificationsPanel extends PluginPanel {
    private Dictionary<String, BufferedImage> iconsCache = new Hashtable<>();
    private SkillingNotificationsPlugin plugin;
    private MaterialTabGroup tabGroup;
    private JTextArea textLabel;

    SkillingNotificationsPanel(SkillingNotificationsPlugin plugin) {
        super();

        this.plugin = plugin;
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

        textLabel = new JTextArea();
        textLabel.setWrapStyleWord(true);
        textLabel.setLineWrap(true);
        textLabel.setEditable(false);
        textLabel.setOpaque(false);
        textLabel.setFocusable(false);

        textLabel.setText("Selected skill");
        add(textLabel, c);
        c.gridy++;

        tabGroup = new MaterialTabGroup();
        tabGroup.setLayout(new GridLayout(5, 1, 7, 7));

        repaintTabs();
        add(tabGroup, c);
    }

    @Override
    public void onActivate() {
        repaintTabs();
    }

    private void repaintTabs() {
        tabGroup.removeAll();
        MaterialTab toSelect = null;
        for (Skill skill : Skill.values()) {
            if (skill == Skill.NONE) continue;
            String skillIcon = "/skill_icons/" + skill.name().toLowerCase() + ".png";

            MaterialTab tab = new MaterialTab(new ImageIcon(GetIcon(skillIcon)), tabGroup, null);
            tab.setToolTipText(StringUtils.capitalize(skill.name().toLowerCase()));
            tab.addMouseListener(new MouseAdapter()
            {
                @Override
                public void mousePressed(MouseEvent mouseEvent)
                {
                    if (plugin.getSelectedSkill() == skill) {
                        tab.unselect();
                        plugin.setSkillInConfig(Skill.NONE);
                    }else plugin.setSkillInConfig(skill);
                }
            });

            tabGroup.addTab(tab);
            if (plugin.getSelectedSkill() == skill) toSelect = tab;
        }
        if (toSelect != null) toSelect.select();
    }

    private BufferedImage GetIcon(String path){
        BufferedImage iconImage = iconsCache.get(path);
        if(iconImage != null) return iconImage;
        iconImage = ImageUtil.loadImageResource(getClass(), path);
        iconsCache.put(path, iconImage);
        return iconImage;
    }
}
