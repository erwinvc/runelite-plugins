package eu.jodelahithit.clanmemberlistsort;

import com.google.inject.Provides;

import javax.inject.Inject;

import net.runelite.api.*;
import net.runelite.api.events.ScriptPostFired;
import net.runelite.api.events.WidgetLoaded;
import net.runelite.api.widgets.*;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

import java.util.*;
import java.util.List;

@PluginDescriptor(
        name = "Clan Member List Sorting",
        description = "Adds a sort button to the clan member list",
        tags = {"clan", "members", "list", "sort", "sorting", "alphabetically"}
)
public class ClanMemberListSortPlugin extends Plugin {
    final String SORT_BY_NONE = "Sort by none";
    final String SORT_BY_NAME = "Sort by name";
    final String SORT_BY_RANK = "Sort by rank";

    final int WIDGET_HEIGHT = 15;
    final int UNK_CLAN_TAB_SCRIPT = 2859;

    @Inject
    private Client client;
    @Inject
    private ClanMemberListSortConfig config;
    @Inject
    private ClientThread clientThread;

    @Provides
    ClanMemberListSortConfig getConfig(ConfigManager configManager) {
        return configManager.getConfig(ClanMemberListSortConfig.class);
    }

    @Subscribe
    public void onScriptPostFired(ScriptPostFired event) {
        if (event.getScriptId() != UNK_CLAN_TAB_SCRIPT) return;
        Widget itemContainer = client.getWidget(WidgetInfo.CLAN_MEMBER_LIST);

        if (itemContainer == null) return;
        Widget[] containerChildren = itemContainer.getDynamicChildren();
        if (containerChildren.length % 3 != 0) return;

        List<ClanMemberListEntry> widgets = new ArrayList<>();

        for (int i = 0; i < containerChildren.length; i += 3) {
            widgets.add(new ClanMemberListEntry(containerChildren[i], containerChildren[i + 1], containerChildren[i + 2]));
        }
        widgets.sort(Comparator.comparing(ClanMemberListEntry::getIconSpriteID));

        for (int i = 0; i < widgets.size(); i++) {
            widgets.get(i).setOriginalY(WIDGET_HEIGHT * i);
            widgets.get(i).revalidate();
        }
    }

    @Subscribe
    public void onWidgetLoaded(WidgetLoaded event) {
        if (event.getGroupId() == WidgetInfo.CLAN_MEMBER_LIST.getGroupId()) {
            init();
        }
    }


    private boolean isClanMemberListHidden() {
        Widget widget = client.getWidget(WidgetInfo.CLAN_MEMBER_LIST.getGroupId(), 0);
        return widget == null || widget.isHidden();
    }

    Widget clanMemberListWidget;
    Widget sortButton;
    private void init() {
        if (isClanMemberListHidden()) return;


        clanMemberListWidget = client.getWidget(WidgetInfo.CLAN_MEMBER_LIST.getGroupId(), 0);

        sortButton = clanMemberListWidget.createChild(-1, WidgetType.GRAPHIC);

        sortButton.setAction(0, SORT_BY_NONE);
        sortButton.setAction(1, SORT_BY_NAME);
        sortButton.setAction(2, SORT_BY_RANK);
        sortButton.setOriginalY(2);
        sortButton.setOriginalX(2);
        sortButton.setOriginalHeight(16);
        sortButton.setOriginalWidth(16);
        sortButton.setSpriteId(SpriteID.SCROLLBAR_ARROW_DOWN);
        sortButton.setOnOpListener((JavaScriptCallback) this::handleButton);
        sortButton.setHasListener(true);
        sortButton.revalidate();
    }

    private void handleButton(ScriptEvent event) {
        System.out.println(event.getOp());
        switch (event.getOp()) {
            //case SORT_BY_NONE:
            //    break;
            //case SORT_BY_NAME:
            //    break;
            //case SORT_BY_RANK:
            //    break;
        }
    }

    @Override
    public void startUp() {
        clientThread.invokeLater(this::init);
    }

    @Override
    public void shutDown() {

    }
}
