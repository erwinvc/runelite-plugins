package eu.jodelahithit;

import com.google.inject.Provides;

import javax.inject.Inject;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.*;
import net.runelite.api.gameval.InventoryID;
import net.runelite.api.gameval.ItemID;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.game.ItemManager;
import net.runelite.client.input.KeyManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.ImageUtil;
import org.apache.commons.lang3.ArrayUtils;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@PluginDescriptor(
        name = "Skilling Notifications",
        tags = {"notifications", "skilling"},
        description = "Provides visual notifications when no longer actively performing the selected skill"
)
public class SkillingNotificationsPlugin extends Plugin {
    private static final String WALK_HERE = "Walk here";
    private static final String DROP = "Drop";
    private static final String SET_TRAP = "Set-trap";
    private static final String CHECK = "Check";
    private static final String SET_HEADING = "Set heading";
    private static final int MANIACAL_MONKEYS_REGION_ID = 11662;
    private LocalPoint lastPlayerLocation;
    private Session session;
    private NavigationButton navigationButton;
    private List<NotificationType> selectedNotificationTypes = new ArrayList<>();
    private Tile lastManiacalMonkeyRockTile = null;
    private int[] xpCache;
    private int lastBananas = 0;

    @Getter
    private SkillingNotificationsPanel panel;

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
    SkillingNotificationsListener inputListener;
    @Inject
    ClientToolbar clientToolbar;
    @Inject
    ItemManager itemManager;
    @Inject
    KeyManager keyManager;

    public final BufferedImage ICON = ImageUtil.loadImageResource(SkillingNotificationsPlugin.class, "icon.png");

    @Override
    protected void startUp() throws Exception {
        keyManager.registerKeyListener(inputListener);
        updateSelectedSkills();
        panel = new SkillingNotificationsPanel(configManager);
        navigationButton = NavigationButton.builder()
                .tooltip("Skilling Notifications")
                .icon(ICON).priority(10).panel(panel)
                .build();

        clientToolbar.addNavigation(navigationButton);
        overlayManager.add(overlay);
        session = new Session(this);

        Skill[] skills = Skill.values();
        xpCache = new int[skills.length];
        for (Skill s : skills) {
            xpCache[s.ordinal()] = client.getSkillExperience(s);
        }
    }

    @Override
    protected void shutDown() throws Exception {
        clientToolbar.removeNavigation(navigationButton);
        overlayManager.remove(overlay);
        keyManager.unregisterKeyListener(inputListener);
    }

    @Provides
    SkillingNotificationsConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(SkillingNotificationsConfig.class);
    }

    @Subscribe
    public void onClientTick(ClientTick clientTick) {
        //Utils.printAnimation(client);
        if (!config.enabled()) return;
        for (NotificationType notificationType : selectedNotificationTypes) {
            if (Utils.isInAnimation(notificationType, client)) session.updateInstant(notificationType);
        }
        Player player = client.getLocalPlayer();
        if (player != null) {
            LocalPoint playerLocation = player.getLocalLocation();
            if (!playerLocation.equals(lastPlayerLocation)) {
                session.updateWalkingInstant();
            }
            lastPlayerLocation = playerLocation;
        }

        if (Utils.isInAnimation(Constants.SAILING_HELM_ANIMATIONS, client)) {
            session.updateSailingInstant();
        }

        if (Utils.isInAnimation(Constants.MONKEY_ANIMS, client)) {
            session.updateInstant(NotificationType.MANIACALMONKEYS);
        }

        boolean isInManiacalMonkeysArea = isInManiacalMonkeysArea();
        if (!isInManiacalMonkeysArea || lastManiacalMonkeyRockTile != null) {
            session.updateInstant(NotificationType.MANIACALMONKEYS);
        }
    }

    @Subscribe
    public void onConfigChanged(ConfigChanged configChanged) {
        if (configChanged.getGroup().equals(SkillingNotificationsConfig.CONFIG_GROUP)) {
            updateSelectedSkills();
        }
    }

    @Subscribe
    public void onStatChanged(StatChanged statChanged) {
        Skill skill = statChanged.getSkill();
        int currentXp = statChanged.getXp();
        int skillIdx = skill.ordinal();
        int cachedXP = xpCache[skillIdx];
        if (cachedXP < currentXp) {
            int xpAmount = currentXp - cachedXP;
            if (xpAmount >= config.customXPValue()) {
                session.updateInstant(NotificationType.CUSTOMXP);
            }
        }

        xpCache[skillIdx] = currentXp;
    }

    @Subscribe
    public void onHitsplatApplied(HitsplatApplied hitsplatApplied) {
        if (!config.enabled()) return;
        Hitsplat hitsplat = hitsplatApplied.getHitsplat();
        if (hitsplat.isMine()) {
            session.updateInstant(NotificationType.COMBAT);
        }
    }

    @Subscribe
    public void onGameObjectSpawned(GameObjectSpawned event) {
        final GameObject gameObject = event.getGameObject();
        final int id = gameObject.getId();
        final WorldPoint trapLocation = gameObject.getWorldLocation();

        if (id == ObjectID.MONKEY_TRAP || id == ObjectID.LARGE_BOULDER_28825) {
            Player localPlayer = client.getLocalPlayer();
            if (localPlayer != null
                    && localPlayer.getWorldLocation().distanceTo(trapLocation) <= 2) {
                lastManiacalMonkeyRockTile = event.getTile();
            }
            return;
        }

        if (Constants.MONKEY_ROCKS.contains(id)) {
            if (lastManiacalMonkeyRockTile != null && event.getTile() == lastManiacalMonkeyRockTile)
                lastManiacalMonkeyRockTile = null;
        }
    }

    @Subscribe
    private void onWorldChanged(WorldChanged ev) {
        lastManiacalMonkeyRockTile = null;
    }

    @Subscribe
    public void onMenuOptionClicked(MenuOptionClicked event) {
        if (event.getMenuOption().equals(WALK_HERE) || event.getMenuOption().equals(SET_HEADING))
            session.updateWalkingInstant();
        else {
            if (!config.enabled() || (config.enabled() && (!config.maniacalMonkeys() || !isInManiacalMonkeysArea())))
                return;
            else if (event.getMenuOption().equals(DROP) && (event.getItemId() == ItemID.BASKET_EMPTY || event.getItemId() == ItemID.DAMAGED_BALLISTA_ROPE))
                session.updateInstant(NotificationType.MANIACALMONKEYS);
            else if (event.getMenuOption().equals(SET_TRAP) || event.getMenuOption().equals(CHECK))
                session.updateInstant(NotificationType.MANIACALMONKEYS);
        }
    }

    //Subscribe to item container event for inventory banana checks for maniacal monkeys
    @Subscribe
    public void onItemContainerChanged(ItemContainerChanged event) {
        if (!config.enabled() || (config.enabled() && (!config.maniacalMonkeys() || !isInManiacalMonkeysArea())))
            return;

        if (event.getContainerId() != InventoryID.INV) return;

        ItemContainer inv = event.getItemContainer();
        if (inv == null)
            return;

        int bananas = 0;
        for (Item item : inv.getItems())
            if (item != null && item.getId() == ItemID.BANANA)
                bananas += 1;

        //Check for any difference in bananas - player could be buggering around in inv
        if (bananas != lastBananas)
            session.updateInstant(NotificationType.MANIACALMONKEYS);

        lastBananas = bananas;
    }

    boolean areSelectedSkillsActive() {
        for (NotificationType notificationType : selectedNotificationTypes) {
            if (notificationType == NotificationType.MANIACALMONKEYS && !isInManiacalMonkeysArea()) {
                continue;
            }
            if (session.isSkillActive(notificationType)) {
                return true;
            }
        }
        return false;
    }

    boolean shouldRenderOverlay() {
        if (!config.enabled()) return false;
        final boolean skills = !selectedNotificationTypes.isEmpty() && !areSelectedSkillsActive();
        final boolean notMoving = !(config.disableWhenWalking() && (session.isWalking(config.walkDelay()) || session.isSailing()));
        return skills && notMoving;
    }

    public List<NotificationType> getSelectedSkills() {
        return selectedNotificationTypes;
    }

    void updateSelectedSkills() {
        selectedNotificationTypes.clear();
        for (NotificationType notificationType : NotificationType.values()) {
            if (Boolean.parseBoolean(configManager.getConfiguration(SkillingNotificationsConfig.CONFIG_GROUP, notificationType.name().toUpperCase()))) {
                selectedNotificationTypes.add(notificationType);
            }
        }

    }

    int getExtraSkillDelay(NotificationType notificationType) {
        int delay = Integer.parseInt(configManager.getConfiguration(SkillingNotificationsConfig.CONFIG_GROUP, notificationType.name() + "DELAYV2"));
        if (notificationType == NotificationType.COMBAT) return Utils.getAttackSpeed(client, itemManager) * 600 + delay;
        return delay;
    }

    void setSkillInConfig(NotificationType notificationType) {
        configManager.setConfiguration(SkillingNotificationsConfig.CONFIG_GROUP, "selectedSkill", notificationType);
    }

    boolean isInManiacalMonkeysArea() {
        return ArrayUtils.contains(
                client.getTopLevelWorldView().getMapRegions(),
                MANIACAL_MONKEYS_REGION_ID
        );
    }
}