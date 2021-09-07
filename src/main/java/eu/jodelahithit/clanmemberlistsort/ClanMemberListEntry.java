package eu.jodelahithit.clanmemberlistsort;

import net.runelite.api.Client;
import net.runelite.api.clan.ClanChannel;
import net.runelite.api.clan.ClanChannelMember;
import net.runelite.api.clan.ClanRank;
import net.runelite.api.clan.ClanSettings;
import net.runelite.api.widgets.Widget;

public class ClanMemberListEntry {
    Widget icon;
    Widget name;
    Widget world;
    ClanRank clanRank = ClanRank.GUEST;

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

    //#Todo cache clan rank?
    public void updateClanRank(Client client) {
        ClanChannel clanChannel = client.getClanChannel();
        ClanSettings clanSettings = client.getClanSettings();
        if (clanChannel == null || clanSettings == null) return;
        ClanChannelMember member = clanChannel.findMember(name.getText());
        if (member == null) return;
        clanRank = member.getRank();
    }

    public int getIconSpriteID() {
        return icon.getSpriteId();
    }

    public String getPlayerName() {
        return name.getText().toLowerCase();
    }

    public String getWorld() {
        return world.getText();
    }

    public ClanRank getClanRank() {
        return clanRank;
    }
}