package eu.jodelahithit.clanmemberlistsort;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.clan.*;
import net.runelite.api.widgets.Widget;

import java.util.Arrays;
import java.util.Random;

@Slf4j
public class ClanMemberListEntry {
    Widget icon;
    Widget name;
    Widget world;
    ClanRank clanRank = randomRank();

    public ClanMemberListEntry(Widget name, Widget world, Widget icon) {
        this.name = name;
        this.world = world;
        this.icon = icon;
    }

    private ClanRank randomRank() {
        int pick = new Random().nextInt(ClanRank.values().length);
        return ClanRank.values()[pick];
    }

    public void setOriginalYAndRevalidate(int y) {
        name.setOriginalY(y);
        name.revalidate();
        world.setOriginalY(y);
        world.revalidate();
        icon.setOriginalY(y);
        icon.revalidate();
    }

    private String removeIconFromName(String name){
        return name.replaceAll("( <img=[0-9]+>)", "");
    }

    //#Todo cache clan rank?
    public void updateClanRank(Client client) {
        ClanChannel clanChannel = client.getClanChannel();
        if (clanChannel == null) {
            debugClanChannel();
            return;
        }

        ClanSettings clanSettings = client.getClanSettings();
        if (clanSettings == null) {
            debugClanSettings(clanChannel);
            return;
        }
        ClanChannelMember member = clanChannel.findMember(removeIconFromName(name.getText()));
        if (member == null) {
            debugClanMember(clanChannel, clanSettings);
            return;
        }
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

    //Debugging
    static int debugClanChannelPrintCount = 0;
    private void debugClanChannel() {
        if (debugClanChannelPrintCount++ > 5) return;
        log.error("Clan channel is null");
    }

    static int debugClanSettingsPrintCount = 0;
    private void debugClanSettings(ClanChannel clanChannel) {
        if (debugClanSettingsPrintCount++ > 5) return;
        log.error("Clan settings is null for clan: {}", clanChannel.getName());
    }

    static int debugClanMemberPrintCount = 0;
    private void debugClanMember(ClanChannel clanChannel, ClanSettings clanSettings) {
        if (debugClanMemberPrintCount++ > 5) return;
        log.error("Clan member is null for clan: " + clanChannel.getName());
        StringBuilder clanMemberNames = new StringBuilder();
        for(ClanChannelMember member : clanChannel.getMembers()){
            String memberName = member.getName();
            clanMemberNames.append(memberName).append(Arrays.toString(memberName.getBytes())).append("\n");
        }
        log.error("Player name: {}|{}\n{}", name.getText(), Arrays.toString(name.getText().getBytes()), clanMemberNames);
    }
}