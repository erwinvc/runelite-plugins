package eu.jodelahithit;

import java.time.Duration;
import java.time.Instant;
import java.util.Dictionary;
import java.util.Hashtable;

public class Session {
    private Dictionary<Skill, Instant> skillInstants = new Hashtable<>();
    private SkillingNotificationsPlugin plugin;

    public Session(SkillingNotificationsPlugin plugin) {
        this.plugin = plugin;
    }

    private boolean CheckInstant(Instant instant, float timeout) {
        return Duration.between(instant, Instant.now()).toMillis() < (timeout * 1000);
    }

    public void UpdateInstant(Skill skill) {
        skillInstants.put(skill, Instant.now());
    }

    public boolean IsSkillActive(Skill skill) {
        Instant instant = skillInstants.get(skill);
        if (instant != null) {
            return CheckInstant(instant, 0.1f + Math.max(plugin.GetExtraDelay(skill), 0));
        }
        return false;
    }
}
