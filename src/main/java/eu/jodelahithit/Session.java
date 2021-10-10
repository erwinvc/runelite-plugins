package eu.jodelahithit;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Dictionary;
import java.util.Hashtable;

public class Session {
    private Dictionary<WintertodtActivity, Instant> instants = new Hashtable<>();
    private WintertodtNotificationsPlugin plugin;

    public Session(WintertodtNotificationsPlugin plugin) {
        this.plugin = plugin;
        for(WintertodtActivity activity : WintertodtActivity.values()){
            updateInstant(activity);
        }
    }

    public void updateInstant(WintertodtActivity activity) {
        instants.put(activity, Instant.now());
    }

    public boolean isInActivity(WintertodtActivity activity) {
        Instant instant = instants.get(activity);
        if (instant != null) {
            return Utils.checkInstant(instant, activity.timeout);
        }
        return false;
    }

    public boolean isInAnyActivity() {
        for(WintertodtActivity activity : WintertodtActivity.values()) {
            if (isInActivity(activity)) return true;
        }
        return false;
    }

    public void invalidateInstant(WintertodtActivity activity) {
        instants.put(activity, Instant.now().minus(1, ChronoUnit.MINUTES));
    }
}