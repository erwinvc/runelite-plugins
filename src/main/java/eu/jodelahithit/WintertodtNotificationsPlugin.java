package eu.jodelahithit;

import com.google.inject.Provides;

import javax.inject.Inject;

import net.runelite.api.*;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.ClientTick;
import net.runelite.api.events.VarbitChanged;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

import java.time.Instant;

import static eu.jodelahithit.Constants.*;

@PluginDescriptor(
        name = "Wintertodt Notifications",
        description = "Visual notifications for Wintertodt",
        tags = {"notifications", "wintertodt"}
)
public class WintertodtNotificationsPlugin extends Plugin {
    private static final int WINTERTODT_REGION = 6462;
    private LocalPoint lastPlayerLocation;
    private boolean isInWintertodt;
    private Session session;
    private int wintertodtTimer;
    private int previousAnim;
    private int currentSkipAnim = -1;
    private Instant currentSkipTimer = Instant.now();

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

    @Override
    public void startUp() {
        session = new Session(this);
        overlayManager.add(overlay);
    }

    @Override
    public void shutDown() {
        overlayManager.remove(overlay);
    }

    @Subscribe
    public void onClientTick(ClientTick clientTick) {
        isInWintertodt = isInWintertodtRegion();
        if (!isInWintertodt) return;

        Player player = client.getLocalPlayer();
        if (player == null) return;

        LocalPoint playerLocation = player.getLocalLocation();
        if (!playerLocation.equals(lastPlayerLocation)) {
            session.updateInstant(WintertodtActivity.WALKING);
        }
        lastPlayerLocation = playerLocation;

        if (wintertodtTimer != 0) session.updateInstant(WintertodtActivity.WAITING);

        int anim = player.getAnimation();
        if (anim != -1 && anim != currentSkipAnim) {
            if (WOODCUTTING_ANIMATIONS.contains(anim)) session.updateInstant(WintertodtActivity.WOODCUTTING);
            if (anim == AnimationID.FLETCHING_BOW_CUTTING) session.updateInstant(WintertodtActivity.FLETCHING);
            if (anim == AnimationID.CONSTRUCTION) session.updateInstant(WintertodtActivity.CONSTRUCTION);
            if (anim == AnimationID.LOOKING_INTO) session.updateInstant(WintertodtActivity.BURNING);
            if (anim == AnimationID.FIREMAKING) session.updateInstant(WintertodtActivity.LIGHTING);
            if (anim == AnimationID.CONSUMING) session.updateInstant(WintertodtActivity.EATING);
        }
        if (Utils.checkInstant(currentSkipTimer, 1.0f)){
            currentSkipAnim = -1;
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

        if (messageNode.getValue().startsWith("The cold of")) {
            session.invalidateInstant(WintertodtActivity.FLETCHING);
            Player localPlayer = client.getLocalPlayer();
            if (localPlayer != null) {
                checkAnimationSkip(localPlayer, AnimationID.FLETCHING_BOW_CUTTING);
                checkAnimationSkip(localPlayer, AnimationID.LOOKING_INTO);
            }

        } else if (messageNode.getValue().startsWith("The brazier has gone out.")) {
            session.invalidateInstant(WintertodtActivity.LIGHTING);
        } else if (messageNode.getValue().startsWith("The brazier is broken and shrapnel")) {
            session.invalidateInstant(WintertodtActivity.CONSTRUCTION);
        } else if (messageNode.getValue().startsWith("You have run out of bruma roots")) {
            session.invalidateInstant(WintertodtActivity.LIGHTING);
        } else if (messageNode.getValue().startsWith("You fix the brazier")) {
            session.updateInstant(WintertodtActivity.CONSTRUCTION);
        } else if (messageNode.getValue().startsWith("You light the brazier")) {
            session.updateInstant(WintertodtActivity.LIGHTING);
        }
    }

    private void checkAnimationSkip(Player localPlayer, int animation) {
        if (previousAnim == animation || localPlayer.getAnimation() == animation) {
            currentSkipAnim = animation;
            currentSkipTimer = Instant.now();
            localPlayer.setAnimation(0);
        }
    }

    @Subscribe
    public void onVarbitChanged(VarbitChanged varbitChanged) {
        wintertodtTimer = client.getVar(Varbits.WINTERTODT_TIMER);
    }

    private boolean isInWintertodtRegion() {
        if (client.getLocalPlayer() != null) {
            return client.getLocalPlayer().getWorldLocation().getRegionID() == WINTERTODT_REGION;
        }
        return false;
    }

    public boolean shouldRenderOverlay() {
        return isInWintertodt && !session.isInAnyActivity();
    }
}
