package eu.jodelahithit;

import java.time.Duration;
import java.time.Instant;
import java.util.EnumMap;
import java.util.concurrent.TimeUnit;

public class Session {
    private static final long BASE_SKILL_DELAY_MS = 500L;
    private static final long SAILING_DELAY_MS = 2000L;

    private final long[] skillActiveUntil = new long[NotificationType.values().length];

    private final SkillingNotificationsPlugin plugin;

    private long walkingUpdatedAt = System.nanoTime();
    private long sailingUpdatedAt = System.nanoTime();

    public Session(SkillingNotificationsPlugin plugin) {
        this.plugin = plugin;
    }

    public static boolean checkInstant(Instant instant, long timeout) {
        if (instant == null) return false;
        return Duration.between(instant, Instant.now()).toMillis() < timeout;
    }

    public void updateInstant(NotificationType type) {
        long delayMs = BASE_SKILL_DELAY_MS + Math.max(plugin.getExtraSkillDelay(type), 0);

        skillActiveUntil[type.ordinal()] =
                System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(delayMs);
    }

    public boolean isSkillActive(NotificationType type) {
        return System.nanoTime() < skillActiveUntil[type.ordinal()];
    }

    public void updateWalkingInstant() {
        walkingUpdatedAt = System.nanoTime();
    }

    public void updateSailingInstant() {
        sailingUpdatedAt = System.nanoTime();
    }


    public boolean isWalking(long extraTimeoutMs) {
        long elapsed = System.nanoTime() - walkingUpdatedAt;
        long timeout = TimeUnit.MILLISECONDS.toNanos(
                1L + Math.max(extraTimeoutMs, 0)
        );

        return elapsed < timeout;
    }

    public boolean isSailing() {
        long elapsed = System.nanoTime() - sailingUpdatedAt;
        return elapsed < TimeUnit.MILLISECONDS.toNanos(SAILING_DELAY_MS);
    }
}
