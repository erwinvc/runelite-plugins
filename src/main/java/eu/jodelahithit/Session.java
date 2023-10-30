package eu.jodelahithit;

import java.time.Duration;
import java.time.Instant;
import java.util.Dictionary;
import java.util.Hashtable;

public class Session {
    private Dictionary<NotificationType, Instant> skillInstants = new Hashtable<>();
    private SkillingNotificationsPlugin plugin;
    private Instant walkingInstant = Instant.now();

    public Session(SkillingNotificationsPlugin plugin) {
        this.plugin = plugin;
    }

    public static boolean checkInstant(Instant instant, float timeout) {
        return Duration.between(instant, Instant.now()).toMillis() < timeout;
    }

    public void updateInstant(NotificationType notificationType) {
        skillInstants.put(notificationType, Instant.now());
    }

    public boolean isSkillActive(NotificationType notificationType) {
        Instant instant = skillInstants.get(notificationType);
        if (instant != null) {
            return checkInstant(instant, 500 + Math.max(plugin.getExtraSkillDelay(notificationType), 0));
        }
        return false;
    }

    public void updateWalkingInstant(){
        walkingInstant = Instant.now();
    }

    public boolean isWalking(float extraTimeout){
       return checkInstant(walkingInstant, 1.0f + extraTimeout);
    }
}
