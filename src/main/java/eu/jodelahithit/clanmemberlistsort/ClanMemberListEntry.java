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

    public void setOriginalY(int y) {
        name.setOriginalY(y);
        world.setOriginalY(y);
        icon.setOriginalY(y);
    }

    public void revalidate() {
        name.revalidate();
        world.revalidate();
        icon.revalidate();
    }
    public int getIconSpriteID(){
        return icon.getSpriteId();
    }
}