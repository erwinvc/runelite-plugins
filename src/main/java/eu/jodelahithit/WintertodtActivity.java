package eu.jodelahithit;

public enum WintertodtActivity {
    IDLE("what you were doing"),
    WOODCUTTING("woodcutting"),
    FLETCHING("fletching"),
    FEEDING_BRAZIER("feeding the brazier"),
    FIXING_BRAZIER("repairing the brazier"),
    LIGHTING_BRAZIER("lighting the brazier");

    private final String displayName;

    WintertodtActivity(String displayName)
    {
        this.displayName = displayName;
    }

    String getDisplayName()
    {
        return displayName;
    }
}