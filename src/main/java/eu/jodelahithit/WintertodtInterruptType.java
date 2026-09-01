package eu.jodelahithit;

enum WintertodtInterruptType {
    COLD("The cold of", "Cold damage"),
    SNOWFALL("The freezing cold attack", "A snowfall attack"),
    BRAZIER("The brazier is broken and shrapnel", "Brazier shrapnel"),
    INVENTORY_FULL("Your inventory is too full", "A full inventory"),
    OUT_OF_ROOTS(null, "Running out of roots"),
    OUT_OF_KINDLING(null, "Running out of kindling"),
    FIXED_BRAZIER("You fix the brazier", null),
    LIT_BRAZIER("You light the brazier", null),
    BRAZIER_WENT_OUT("The brazier has gone out.", "The brazier going out"),
    LEVEL_UP("Congratulations, you've just advanced your", "Leveling up");

    private final String messagePrefix;
    private final String notificationText;

    WintertodtInterruptType(
            String messagePrefix,
            String notificationText) {
        this.messagePrefix = messagePrefix;
        this.notificationText = notificationText;
    }

    static WintertodtInterruptType fromMessage(String message) {
        for (WintertodtInterruptType type : values()) {
            if (type.messagePrefix != null
                    && message.startsWith(type.messagePrefix)) {
                return type;
            }
        }

        return null;
    }

    String getNotificationText() {
        return notificationText;
    }

    boolean hasNotificationText() {
        return notificationText != null;
    }
}