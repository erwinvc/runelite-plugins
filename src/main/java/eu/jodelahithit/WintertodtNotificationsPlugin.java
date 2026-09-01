package eu.jodelahithit;

import com.google.inject.Provides;

import javax.inject.Inject;

import net.runelite.api.*;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.events.*;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.Notifier;


import static eu.jodelahithit.WintertodtInterruptType.*;

import net.runelite.api.gameval.AnimationID;
import net.runelite.api.gameval.InventoryID;
import net.runelite.api.gameval.ItemID;
import net.runelite.api.gameval.VarbitID;

@PluginDescriptor(
        name = "Wintertodt Notifications",
        description = "Visual notifications for Wintertodt",
        tags = {"notifications", "wintertodt"}
)
public class WintertodtNotificationsPlugin extends Plugin {
    private static final int MOVEMENT_COOLDOWN_TICKS = 2;
    private static final int ACTION_TIMEOUT_TICKS = 5;
    private static final int IDLE_ANIMATION = -1;

    private static final int WINTERTODT_REGION = 6462;
    private boolean isInWintertodt;
    private WintertodtActivity currentActivity = WintertodtActivity.IDLE;
    private int timerValue;
    private LocalPoint lastPlayerLocation;

    private int ticksSinceMovement = MOVEMENT_COOLDOWN_TICKS;
    private int lastActionTick = -1;

    private int previousRootCount = -1;
    private int previousKindlingCount = -1;

    @Inject
    Client client;

    @Inject
    private WintertodtNotificationsConfig config;

    @Inject
    private WintertodtNotificationsOverlay overlay;

    @Inject
    private OverlayManager overlayManager;

    @Inject
    private Notifier nativeNotifier;

    @Provides
    WintertodtNotificationsConfig getConfig(ConfigManager configManager) {
        return configManager.getConfig(WintertodtNotificationsConfig.class);
    }

    private void resetState() {
        overlay.reset();
        isInWintertodt = false;
        currentActivity = WintertodtActivity.IDLE;
        timerValue = -1;
        lastPlayerLocation = null;
        lastActionTick = -1;
        ticksSinceMovement = MOVEMENT_COOLDOWN_TICKS;
        previousRootCount = -1;
        previousKindlingCount = -1;
    }


    @Override
    protected void startUp() {
        resetState();
        overlayManager.add(overlay);
    }

    @Override
    protected void shutDown() {
        overlayManager.remove(overlay);
        resetState();
    }

    private boolean isInWintertodtRegion() {
        if (client.getLocalPlayer() != null) {
            return client.getLocalPlayer().getWorldLocation().getRegionID() == WINTERTODT_REGION;
        }

        return false;
    }

    private void initializeInventoryCounts() {
        ItemContainer inventory = client.getItemContainer(InventoryID.INV);

        if (inventory == null) {
            previousRootCount = 0;
            previousKindlingCount = 0;
            return;
        }

        previousRootCount = inventory.count(ItemID.WINT_BRUMA_ROOT);

        previousKindlingCount = inventory.count(ItemID.WINT_BRUMA_KINDLING);
    }


    @Subscribe
    public void onGameTick(GameTick gameTick) {
        if (!isInWintertodtRegion()) {
            if (isInWintertodt) {
                resetState();
            }

            return;
        }

        Player player = client.getLocalPlayer();

        if (!isInWintertodt) {
            resetState();
            isInWintertodt = true;

            timerValue = client.getVarbitValue(VarbitID.WINT_TRANSMIT_RESPAWNDELAY);

            initializeInventoryCounts();
        }

        updateMovementState(player);
        checkActionTimeout(player);
    }

    @Subscribe
    public void onVarbitChanged(VarbitChanged event) {
        if (isInWintertodt && event.getVarbitId() == VarbitID.WINT_TRANSMIT_RESPAWNDELAY) {
            timerValue = event.getValue();
        }
    }

    private void updateMovementState(Player player) {
        LocalPoint playerLocation = player.getLocalLocation();

        if (!playerLocation.equals(lastPlayerLocation)) {
            ticksSinceMovement = 0;
        } else {
            ticksSinceMovement = Math.min(
                    ticksSinceMovement + 1,
                    MOVEMENT_COOLDOWN_TICKS);
        }

        lastPlayerLocation = playerLocation;
    }

    private void checkActionTimeout(Player player) {
        if (currentActivity == WintertodtActivity.IDLE || player.getAnimation() != IDLE_ANIMATION || lastActionTick < 0) {
            return;
        }

        int elapsedTicks = client.getTickCount() - lastActionTick;

        if (elapsedTicks >= ACTION_TIMEOUT_TICKS) {
            currentActivity = WintertodtActivity.IDLE;
        }
    }

    @Subscribe
    public void onChatMessage(ChatMessage chatMessage) {
        if (!isInWintertodt) {
            return;
        }

        ChatMessageType chatMessageType = chatMessage.getType();

        if (chatMessageType != ChatMessageType.GAMEMESSAGE && chatMessageType != ChatMessageType.SPAM) {
            return;
        }

        String message = chatMessage.getMessageNode().getValue();

        if (message.startsWith("You carefully fletch the root")) {
            ItemContainer inventory = client.getItemContainer(InventoryID.INV);

            if (inventory != null && inventory.count(ItemID.WINT_BRUMA_ROOT) > 0) {
                setActivity(WintertodtActivity.FLETCHING);
            }

            return;
        }

        WintertodtInterruptType interruptType = WintertodtInterruptType.fromMessage(message);

        if (interruptType == null) {
            return;
        }

        boolean hadActiveActivity = currentActivity != WintertodtActivity.IDLE;
        boolean isDamageInterruption = interruptType == COLD || interruptType == SNOWFALL || interruptType == BRAZIER;
        boolean wasInterrupted = hadActiveActivity && (!isDamageInterruption || currentActivity != WintertodtActivity.WOODCUTTING);

        if (wasInterrupted) {
            interruptCurrentActivity(interruptType);
        }
    }

    private void interruptCurrentActivity(WintertodtInterruptType interruptType) {
        if (currentActivity == WintertodtActivity.IDLE) {
            return;
        }

        WintertodtActivity interruptedActivity = currentActivity;

        if (interruptType.hasNotificationText()) {
            String message = interruptType.getNotificationText() + " stopped " + interruptedActivity.getDisplayName();

            if (config.showInterruptionText()) {
                overlay.showInterruption(message);
            }

            if (config.nativeNotification()) {
                nativeNotifier.notify("Wintertodt: " + message);
            }
        }

        currentActivity = WintertodtActivity.IDLE;
    }

    @Subscribe
    public void onAnimationChanged(final AnimationChanged event) {
        if (!isInWintertodt) {
            return;
        }

        final Player local = client.getLocalPlayer();

        if (event.getActor() != local) {
            return;
        }

        final int animId = local.getAnimation();
        switch (animId) {
            case AnimationID.HUMAN_WOODCUTTING_BRONZE_AXE:
            case AnimationID.HUMAN_WOODCUTTING_IRON_AXE:
            case AnimationID.HUMAN_WOODCUTTING_STEEL_AXE:
            case AnimationID.HUMAN_WOODCUTTING_BLACK_AXE:
            case AnimationID.HUMAN_WOODCUTTING_MITHRIL_AXE:
            case AnimationID.HUMAN_WOODCUTTING_ADAMANT_AXE:
            case AnimationID.HUMAN_WOODCUTTING_RUNE_AXE:
            case AnimationID.HUMAN_WOODCUTTING_GILDED_AXE:
            case AnimationID.HUMAN_WOODCUTTING_DRAGON_AXE:
            case AnimationID.HUMAN_WOODCUTTING_TRAILBLAZER_AXE_NO_INFERNAL:
            case AnimationID.HUMAN_WOODCUTTING_INFERNAL_AXE:
            case AnimationID.HUMAN_WOODCUTTING_3A_AXE:
            case AnimationID.HUMAN_WOODCUTTING_CRYSTAL_AXE:
            case AnimationID.HUMAN_OPENHEAVYCHEST:
            case AnimationID.HUMAN_WOODCUTTING_TRAILBLAZER_RELOADED_AXE_NO_INFERNAL:
            case AnimationID.HUMAN_WOODCUTTING_TRAILBLAZER_AXE:
            case AnimationID.HUMAN_WOODCUTTING_TRAILBLAZER_RELOADED_AXE:
            case AnimationID.FORESTRY_2H_AXE_CHOPPING_BRONZE:
            case AnimationID.FORESTRY_2H_AXE_CHOPPING_IRON:
            case AnimationID.FORESTRY_2H_AXE_CHOPPING_STEEL:
            case AnimationID.FORESTRY_2H_AXE_CHOPPING_BLACK:
            case AnimationID.FORESTRY_2H_AXE_CHOPPING_MITHRIL:
            case AnimationID.FORESTRY_2H_AXE_CHOPPING_ADAMANT:
            case AnimationID.FORESTRY_2H_AXE_CHOPPING_RUNE:
            case AnimationID.FORESTRY_2H_AXE_CHOPPING_DRAGON:
            case AnimationID.FORESTRY_2H_AXE_CHOPPING_CRYSTAL:
            case AnimationID.FORESTRY_2H_AXE_CHOPPING_CRYSTAL_INACTIVE:
            case AnimationID.FORESTRY_2H_AXE_CHOPPING_3A:
                setActivity(WintertodtActivity.WOODCUTTING);
                break;

            case AnimationID.HUMAN_FLETCHING:
                setActivity(WintertodtActivity.FLETCHING);
                break;

            case AnimationID.HUMAN_PICKUPTABLE:
                setActivity(WintertodtActivity.FEEDING_BRAZIER);
                break;

            case AnimationID.HUMAN_CREATEFIRE:
                setActivity(WintertodtActivity.LIGHTING_BRAZIER);
                break;

            case AnimationID.HUMAN_POH_BUILD:
            case AnimationID.HUMAN_POH_BUILD_IMCANDO_HAMMER:
                setActivity(WintertodtActivity.FIXING_BRAZIER);
                break;
        }
    }

    @Subscribe
    public void onItemContainerChanged(ItemContainerChanged event) {
        final ItemContainer container = event.getItemContainer();
        if (!isInWintertodt || container != client.getItemContainer(InventoryID.INV)) {
            return;
        }

        int numRoots = container.count(ItemID.WINT_BRUMA_ROOT);
        int numKindling = container.count(ItemID.WINT_BRUMA_KINDLING);

        WintertodtInterruptType depletionType = null;

        if (currentActivity == WintertodtActivity.FLETCHING && numRoots == 0) {
            depletionType = WintertodtInterruptType.OUT_OF_ROOTS;
        } else if (currentActivity == WintertodtActivity.FEEDING_BRAZIER) {
            boolean rootsRanOut = previousRootCount > 0 && numRoots == 0 && numRoots < previousRootCount;

            boolean kindlingRanOut = previousKindlingCount > 0 && numKindling == 0 && numKindling < previousKindlingCount;

            if (kindlingRanOut) {
                depletionType = WintertodtInterruptType.OUT_OF_KINDLING;
            } else if (rootsRanOut) {
                depletionType = WintertodtInterruptType.OUT_OF_ROOTS;
            }
        }

        previousRootCount = numRoots;
        previousKindlingCount = numKindling;

        if (depletionType != null) {
            interruptCurrentActivity(depletionType);
        }
    }

    private void setActivity(WintertodtActivity activity) {
        currentActivity = activity;
        lastActionTick = client.getTickCount();
        ticksSinceMovement = MOVEMENT_COOLDOWN_TICKS;
    }

    public boolean shouldRenderOverlay() {
        return isInWintertodt && currentActivity == WintertodtActivity.IDLE && timerValue == 0 && ticksSinceMovement >= MOVEMENT_COOLDOWN_TICKS;
    }
}
