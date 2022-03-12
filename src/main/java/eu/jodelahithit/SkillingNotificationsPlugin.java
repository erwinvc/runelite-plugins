package eu.jodelahithit;

import com.google.inject.Provides;

import javax.inject.Inject;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.events.ClientTick;
import net.runelite.api.events.HitsplatApplied;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.ui.PluginPanel;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.ImageUtil;
import net.runelite.http.api.item.ItemStats;

import java.awt.image.BufferedImage;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@PluginDescriptor(
        name = "Skilling Notifications",
        tags = {"notifications", "skilling"},
        description = "Provides visual notifications when no longer actively performing the selected skill"
)
public class SkillingNotificationsPlugin extends Plugin {
    private LocalPoint lastPlayerLocation;
    private Session session;
    private SkillingNotificationsPanel panel;
    private NavigationButton navigationButton;
    private List<Skill> selectedSkills = new ArrayList<>();

    @Inject
    Client client;
    @Inject
    ConfigManager configManager;
    @Inject
    SkillingNotificationsConfig config;
    @Inject
    OverlayManager overlayManager;
    @Inject
    SkillingNotificationsOverlay overlay;
    @Inject
    ClientToolbar clientToolbar;
    @Inject
    private ItemManager itemManager;

    public final BufferedImage ICON = ImageUtil.loadImageResource(SkillingNotificationsPlugin.class, "icon.png");

    @Override
    protected void startUp() throws Exception {
        updateSelectedSkills();
        panel = new SkillingNotificationsPanel(this, configManager);
        navigationButton = NavigationButton.builder()
                .tooltip("Skilling Notifications")
                .icon(ICON).priority(10).panel(panel)
                .build();

        clientToolbar.addNavigation(navigationButton);
        overlayManager.add(overlay);
        session = new Session(this);
        log.info("Skilling notifications plugin started!");
    }

    @Override
    protected void shutDown() throws Exception {
        clientToolbar.removeNavigation(navigationButton);
        overlayManager.remove(overlay);
        log.info("Skilling notifications plugin stopped!");
    }

    @Provides
    SkillingNotificationsConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(SkillingNotificationsConfig.class);
    }

    @Subscribe
    public void onClientTick(ClientTick clientTick) {
        for (Skill skill : selectedSkills) {
            if (Utils.isInAnimation(skill, client)) session.updateInstant(skill);
        }
        Player player = client.getLocalPlayer();
        if (player != null) {
            LocalPoint playerLocation = player.getLocalLocation();
            if (!playerLocation.equals(lastPlayerLocation)) {
                session.updateWalkingInstant();
            }
            lastPlayerLocation = playerLocation;
        }
    }

    @Subscribe
    public void onConfigChanged(ConfigChanged configChanged) {
        if (configChanged.getGroup().equals("Skilling Notifications")) {
            updateSelectedSkills();
        }
    }

    @Subscribe
    public void onHitsplatApplied(HitsplatApplied hitsplatApplied) {
        Actor actor = hitsplatApplied.getActor();
        Hitsplat hitsplat = hitsplatApplied.getHitsplat();
        Hitsplat.HitsplatType type = hitsplat.getHitsplatType();
        boolean skip = type == Hitsplat.HitsplatType.POISON || type == Hitsplat.HitsplatType.VENOM || type == Hitsplat.HitsplatType.DISEASE || type == Hitsplat.HitsplatType.HEAL;
        if (hitsplat.isMine() && !skip) {
            session.updateInstant(Skill.COMBAT);
        }
    }

    boolean areSelectedSkillsActive() {
        boolean isActive = false;
        for (Skill skill : selectedSkills) {
            isActive |= session.isSkillActive(skill);
        }
        return isActive;
    }

    boolean shouldRenderOverlay() {
        return selectedSkills.size() != 0 && !areSelectedSkillsActive() && !(config.disableWhenWalking() && session.isWalking(config.walkDelay()));
    }

    public List<Skill> getSelectedSkills() {
        return selectedSkills;
    }

    void updateSelectedSkills() {
        selectedSkills.clear();
        for (Skill skill : Skill.values()) {
            if (Boolean.parseBoolean(configManager.getConfiguration("Skilling Notifications", skill.name().toUpperCase()))) {
                selectedSkills.add(skill);
            }
        }
    }

    int getExtraSkillDelay(Skill skill) {
        int delay = Integer.parseInt(configManager.getConfiguration("Skilling Notifications", skill.name() + "DELAYV2"));;
        if(skill == Skill.COMBAT) return Utils.getAttackSpeed(client, itemManager) * 600 + delay;
        return delay;
    }

    void setSkillInConfig(Skill skill) {
        configManager.setConfiguration("Skilling Notifications", "selectedSkill", skill);
    }
}
