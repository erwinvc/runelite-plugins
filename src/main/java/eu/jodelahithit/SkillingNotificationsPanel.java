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

public class SkillingNotificationsPanel extends PluginPanel {
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

        RepaintTabs();
        add(tabGroup, c);
    }

    @Override
    public void onActivate() {
        RepaintTabs();
    }

    private void RepaintTabs() {
        tabGroup.removeAll();
        MaterialTab toSelect = null;
        for (Skill skill : Skill.values()) {
            if (skill == Skill.NONE) continue;
            String skillIcon = "/skill_icons/" + skill.name().toLowerCase() + ".png";

            MaterialTab tab = new MaterialTab(new ImageIcon(ImageUtil.loadImageResource(getClass(), skillIcon)), tabGroup, null);
            tab.setToolTipText(StringUtils.capitalize(skill.name().toLowerCase()));
            tab.addMouseListener(new MouseAdapter()
            {
                @Override
                public void mousePressed(MouseEvent mouseEvent)
                {
                    if (plugin.GetSelectedSkill() == skill) {
                        tab.unselect();
                        plugin.SetSkillInConfig(Skill.NONE);
                    }else plugin.SetSkillInConfig(skill);
                }
            });

            tabGroup.addTab(tab);
            if (plugin.GetSelectedSkill() == skill) toSelect = tab;
        }
        if (toSelect != null) toSelect.select();
    }
}
