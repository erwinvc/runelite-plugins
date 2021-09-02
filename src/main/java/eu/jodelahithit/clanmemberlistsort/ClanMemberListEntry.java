package eu.jodelahithit.clanmemberlistsort;

import net.runelite.api.widgets.Widget;

public class ClanMemberListEntry {
    Widget icon;
    Widget name;
    Widget world;

    public ClanMemberListEntry(Widget name, Widget world, Widget icon) {
        this.name = name;
        this.world = world;
        this.icon = icon;
    }

    public void setOriginalYAndRevalidate(int y) {
        name.setOriginalY(y);
        name.revalidate();
        world.setOriginalY(y);
        world.revalidate();
        icon.setOriginalY(y);
        icon.revalidate();
    }

    public int getIconSpriteID(){
        return icon.getSpriteId();
    }

    public String getPlayerName(){
        return name.getText().toLowerCase();
    }

    public String getWorld(){
        return world.getText();
    }
}