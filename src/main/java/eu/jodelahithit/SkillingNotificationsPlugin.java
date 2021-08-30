package eu.jodelahithit;

import com.google.inject.Provides;

import javax.inject.Inject;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.events.ClientTick;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.config.ConfigPlugin;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.ImageUtil;

import java.awt.image.BufferedImage;

@Slf4j
@PluginDescriptor(
        name = "Skilling Notifications",
        tags = {"notifications", "skilling"},
        description = "Provides visual notifications when no longer actively performing the selected skill"
)
public class SkillingNotificationsPlugin extends Plugin {
    private Session session;

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

    private SkillingNotificationsPanel panel;
    private NavigationButton navigationButton;

    public final BufferedImage ICON = ImageUtil.loadImageResource(SkillingNotificationsPlugin.class, "icon.png");

    @Override
    protected void startUp() throws Exception {
        panel = new SkillingNotificationsPanel(this);
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
        Skill skill = config.SelectedSkill();
        if (skill != null)
            if (Utils.IsInAnimation(skill, client)) session.UpdateInstant(skill);
    }

    boolean IsSelectedSkillActive() {
        return session.IsSkillActive(config.SelectedSkill());
    }

    boolean ShouldRenderOverlay() {
        return config.SelectedSkill() != Skill.NONE && !IsSelectedSkillActive();
    }

    int GetExtraDelay(Skill skill) {
        return Integer.parseInt(configManager.getConfiguration("Skilling Notifications", skill.name()));
    }

    void SetSkillInConfig(Skill skill) {
        configManager.setConfiguration("Skilling Notifications", "selectedSkill", skill);
    }

    public Skill GetSelectedSkill() {
        return config.SelectedSkill();
    }
}
