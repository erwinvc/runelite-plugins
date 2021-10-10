package eu.jodelahithit;

public enum WintertodtActivity {
    CONSTRUCTION(2.0f),
    WOODCUTTING(0.1f),
    FLETCHING(0.6f),
    LIGHTING(2.0f),
    BURNING(1.5f),
    WALKING(1.0f),
    WAITING(1.0f),
    EATING(1.0f);

    float timeout;

    WintertodtActivity(float delay){
        this.timeout = delay;
    }
}
