package eu.jodelahithit.clanmemberlistsort;

import com.google.inject.Provides;

import javax.inject.Inject;

import net.runelite.api.*;
import net.runelite.api.events.ScriptPostFired;
import net.runelite.api.events.WidgetLoaded;
import net.runelite.api.widgets.*;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

import java.util.*;
import java.util.List;

@PluginDescriptor(
        name = "Clan Member List Sorting",
        description = "Adds a sort button to the clan member list",
        tags = {"clan", "members", "list", "sorting", "alphabetically", "world", "rank", "role"}
)
public class ClanMemberListSortPlugin extends Plugin {
    static final String CONFIG_GROUP = "clanmemberlistsorting";

    final int WIDGET_HEIGHT = 15;
    final int UNK_CLAN_TAB_SCRIPT = 2859;

    private Widget clanMemberListHeaderWidget;
    private Widget clanMemberListsWidget;
    private Widget sortButton;
    private SortType activeSortType;

    @Inject Client client;
    @Inject ConfigManager configManager;
    @Inject ClanMemberListSortConfig config;

    @Provides
    ClanMemberListSortConfig getConfig(ConfigManager configManager) {
        return configManager.getConfig(ClanMemberListSortConfig.class);
    }

    @Override
    public void startUp() {
        activeSortType = config.activeSortType();
    }

    @Override
    public void shutDown() {
        if (clanMemberListHeaderWidget != null) clanMemberListHeaderWidget.deleteAllChildren();
    }

    @Subscribe
    public void onScriptPostFired(ScriptPostFired event) {
        if (event.getScriptId() != UNK_CLAN_TAB_SCRIPT) return;
        Widget[] containerChildren = clanMemberListsWidget.getDynamicChildren();

        List<ClanMemberListEntry> widgets = new ArrayList<>();

        for (int i = 0; i < containerChildren.length; i += 3) {
            widgets.add(new ClanMemberListEntry(containerChildren[i], containerChildren[i + 1], containerChildren[i + 2]));
        }

        switch (activeSortType) {
            case SORT_BY_WORLD:
                widgets.sort(Comparator.comparing(ClanMemberListEntry::getWorld));
                break;
            case SORT_BY_NAME:
                widgets.sort(Comparator.comparing(ClanMemberListEntry::getPlayerName));
                break;
            case SORT_BY_RANK:
                widgets.sort(Comparator.comparing(ClanMemberListEntry::getIconSpriteID));
                break;
        }

        for (int i = 0; i < widgets.size(); i++) {
            widgets.get(i).setOriginalYAndRevalidate(WIDGET_HEIGHT * i);
        }
    }

    @Subscribe
    public void onWidgetLoaded(WidgetLoaded event) {
        if (event.getGroupId() == WidgetInfo.CLAN_MEMBER_LIST.getGroupId()) {
            initWidgets();
        }
    }

    private boolean isClanMemberListHidden() {
        Widget widget = client.getWidget(WidgetInfo.CLAN_MEMBER_LIST.getGroupId(), 0);
        return widget == null || widget.isHidden();
    }

    private void initWidgets() {
        if (isClanMemberListHidden()) return;

        clanMemberListsWidget = client.getWidget(WidgetInfo.CLAN_MEMBER_LIST);
        clanMemberListHeaderWidget = client.getWidget(WidgetInfo.CLAN_MEMBER_LIST.getGroupId(), 0);
        clanMemberListHeaderWidget.deleteAllChildren();

        sortButton = clanMemberListHeaderWidget.createChild(-1, WidgetType.GRAPHIC);
        reorderSortButton(activeSortType);
        sortButton.setOriginalY(2);
        sortButton.setOriginalX(2);
        sortButton.setOriginalHeight(16);
        sortButton.setOriginalWidth(16);
        sortButton.setSpriteId(SpriteID.SCROLLBAR_ARROW_DOWN);
        sortButton.setOnOpListener((JavaScriptCallback) this::handleSortButton);
        sortButton.setHasListener(true);
        sortButton.revalidate();
    }

    private void handleSortButton(ScriptEvent event) {
        for (SortType type : SortType.values()) {
            if (type.actionIndex == event.getOp()) {
                activeSortType = type;
                saveActiveSortTypeToConfig();
                reorderSortButton(type);
                return;
            }
        }
    }

    private void reorderSortButton(SortType firstType) {
        int index = 0;
        sortButton.setAction(index, firstType.name);
        firstType.actionIndex = 1;
        for (SortType type : SortType.values()) {
            if (type == firstType) continue;
            sortButton.setAction(++index, type.name);
            type.actionIndex = index + 1;
        }
    }

    private void saveActiveSortTypeToConfig() {
        configManager.setConfiguration(CONFIG_GROUP, "activeSortType", activeSortType);
    }

    @Subscribe
    public void onConfigChanged(ConfigChanged configChanged) {
        if (configChanged.getGroup().equals(CONFIG_GROUP) && configChanged.getKey().equals("activeSortType")) {
            activeSortType = config.activeSortType();
        }
    }
}
