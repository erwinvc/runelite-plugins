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

import java.awt.image.BufferedImage;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

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
    private final EnumSet<NotificationType> selectedNotificationTypes = EnumSet.noneOf(NotificationType.class);
    private Tile lastManiacalMonkeyRockTile = null;
    private int[] xpCache;
    private boolean xpCacheInitialized;
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
        session = new Session(this);

        xpCache = new int[Skill.values().length];
        xpCacheInitialized = false;
        lastPlayerLocation = null;
        lastManiacalMonkeyRockTile = null;
        lastBananas = 0;

        updateSelectedSkills();

        panel = new SkillingNotificationsPanel(configManager);

        keyManager.registerKeyListener(inputListener);

        navigationButton = NavigationButton.builder()
                .tooltip("Skilling Notifications")
                .icon(ICON)
                .priority(10)
                .panel(panel)
                .build();

        keyManager.registerKeyListener(inputListener);
        clientToolbar.addNavigation(navigationButton);
        overlayManager.add(overlay);

        xpCache = new int[Skill.values().length];
        if (client.getGameState() == GameState.LOGGED_IN) {
            initializeXpCache();
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
        if (!config.enabled()) return;

        Player player = client.getLocalPlayer();
        if (player == null) {
            lastPlayerLocation = null;
            return;
        }

        int animation = player.getAnimation();

        for (NotificationType notificationType : selectedNotificationTypes) {
            if (notificationType.matchesAnimation(animation)) session.updateInstant(notificationType);
        }

        LocalPoint playerLocation = player.getLocalLocation();
        if (!playerLocation.equals(lastPlayerLocation)) {
            session.updateWalkingInstant();
        }

        lastPlayerLocation = playerLocation;

        if (Constants.SAILING_HELM_ANIMATIONS.contains(animation)) {
            session.updateSailingInstant();
        }

        if (selectedNotificationTypes.contains(NotificationType.MANIACALMONKEYS) && (!isInManiacalMonkeysArea() || lastManiacalMonkeyRockTile != null)) {
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
    public void onGameStateChanged(GameStateChanged event) {
        if (event.getGameState() == GameState.LOGGED_IN) {
            initializeXpCache();
        }
    }

    @Subscribe
    public void onStatChanged(StatChanged event) {
        Skill skill = event.getSkill();
        int skillIndex = skill.ordinal();
        int currentXp = event.getXp();

        if (!xpCacheInitialized) {
            xpCache[skillIndex] = currentXp;
            return;
        }

        int previousXp = xpCache[skillIndex];
        int gainedXp = currentXp - previousXp;

        if (gainedXp >= config.customXPValue()) {
            session.updateInstant(NotificationType.CUSTOMXP);
        }

        xpCache[skillIndex] = currentXp;
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

    private void initializeXpCache() {
        for (Skill skill : Skill.values()) {
            xpCache[skill.ordinal()] = client.getSkillExperience(skill);
        }

        xpCacheInitialized = true;
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

    public Set<NotificationType> getSelectedSkills() {
        return Collections.unmodifiableSet(selectedNotificationTypes);
    }

    void updateSelectedSkills() {
        selectedNotificationTypes.clear();
        for (NotificationType notificationType : NotificationType.values()) {
            if (Boolean.parseBoolean(configManager.getConfiguration(SkillingNotificationsConfig.CONFIG_GROUP, notificationType.name().toUpperCase()))) {
                selectedNotificationTypes.add(notificationType);
            }
        }

    }

    int getExtraSkillDelay(NotificationType type) {
        int delay;

        switch (type) {
            case COOKING:
                delay = config.cookingDelay();
                break;
            case CRAFTING:
                delay = config.craftingDelay();
                break;
            case FISHING:
                delay = config.fishingDelay();
                break;
            case FIREMAKING:
                delay = config.firemakingDelay();
                break;
            case FLETCHING:
                delay = config.fletchingDelay();
                break;
            case HERBLORE:
                delay = config.herbloreDelay();
                break;
            case MINING:
                delay = config.miningDelay();
                break;
            case WOODCUTTING:
                delay = config.woodcuttingDelay();
                break;
            case SMITHING:
                delay = config.smithingDelay();
                break;
            case SAILING:
                delay = config.sailingDelay();
                break;
            case MANIACALMONKEYS:
                delay = config.maniacalMonkeysDelay();
                break;
            case LUNAR:
                delay = config.lunarDelay();
                break;
            case COMBAT:
                delay = config.combatDelay();
                break;
            case CUSTOMXP:
                delay = config.customXPDelay();
                break;
            default:
                return 0;
        }

        if (type == NotificationType.COMBAT) {
            delay += Utils.getAttackSpeed(client, itemManager) * 600;
        }

        return delay;
    }

    void setSkillInConfig(NotificationType notificationType) {
        configManager.setConfiguration(SkillingNotificationsConfig.CONFIG_GROUP, "selectedSkill", notificationType);
    }

    boolean isInManiacalMonkeysArea() {
        Player player = client.getLocalPlayer();
        return player != null && player.getWorldLocation().getRegionID() == MANIACAL_MONKEYS_REGION_ID;
    }
}