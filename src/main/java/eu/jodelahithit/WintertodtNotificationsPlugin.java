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

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static eu.jodelahithit.WintertodtInterruptType.*;
import static net.runelite.api.AnimationID.IDLE;

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

    @Inject
    Client client;

    @Inject
    private WintertodtNotificationsConfig config;

    @Inject
    private WintertodtNotificationsOverlay overlay;

    @Inject
    private OverlayManager overlayManager;

    @Provides
    WintertodtNotificationsConfig getConfig(ConfigManager configManager) {
        return configManager.getConfig(WintertodtNotificationsConfig.class);
    }

    private void resetState() {
        isInWintertodt = false;
        currentActivity = WintertodtActivity.IDLE;
        timerValue = -1;
        lastPlayerLocation = null;
        lastActionTick = -1;
        ticksSinceMovement = MOVEMENT_COOLDOWN_TICKS;
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

            timerValue = client.getVarbitValue(
                    VarbitID.WINT_TRANSMIT_RESPAWNDELAY);
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

        MessageNode messageNode = chatMessage.getMessageNode();
        final WintertodtInterruptType interruptType;

        if (messageNode.getValue().startsWith("You carefully fletch the root")) {
            setActivity(WintertodtActivity.FLETCHING);
            return;
        }

        if (messageNode.getValue().startsWith("The cold of")) {
            interruptType = COLD;
        } else if (messageNode.getValue().startsWith("The freezing cold attack")) {
            interruptType = SNOWFALL;
        } else if (messageNode.getValue().startsWith("The brazier is broken and shrapnel")) {
            interruptType = BRAZIER;
        } else if (messageNode.getValue().startsWith("You have run out of bruma roots")) {
            interruptType = WintertodtInterruptType.OUT_OF_ROOTS;
        } else if (messageNode.getValue().startsWith("Your inventory is too full")) {
            interruptType = WintertodtInterruptType.INVENTORY_FULL;
        } else if (messageNode.getValue().startsWith("You fix the brazier")) {
            interruptType = WintertodtInterruptType.FIXED_BRAZIER;
        } else if (messageNode.getValue().startsWith("You light the brazier")) {
            interruptType = WintertodtInterruptType.LIT_BRAZIER;
        } else if (messageNode.getValue().startsWith("The brazier has gone out.")) {
            interruptType = WintertodtInterruptType.BRAZIER_WENT_OUT;
        } else if (messageNode.getValue().startsWith("Congratulations, you've just advanced your")) {
            interruptType = WintertodtInterruptType.LEVEL_UP;
        } else {
            return;
        }

        boolean wasInterrupted = false;

        if (interruptType == SNOWFALL || interruptType == COLD || interruptType == BRAZIER) {
            if (currentActivity != WintertodtActivity.WOODCUTTING && currentActivity != WintertodtActivity.IDLE) {
                wasInterrupted = true;
            }
        } else wasInterrupted = true;

        if (wasInterrupted) {
            currentActivity = WintertodtActivity.IDLE;
        }
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

        int numLogs = 0;
        int numKindling = 0;

        for (Item item : container.getItems()) {
            switch (item.getId()) {
                case ItemID.WINT_BRUMA_ROOT:
                    ++numLogs;
                    break;
                case ItemID.WINT_BRUMA_KINDLING:
                    ++numKindling;
                    break;
            }
        }

        if (numLogs == 0 && currentActivity == WintertodtActivity.FLETCHING) {
            currentActivity = WintertodtActivity.IDLE;
        } else if (numLogs == 0 && numKindling == 0 && currentActivity == WintertodtActivity.FEEDING_BRAZIER) {
            currentActivity = WintertodtActivity.IDLE;
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
