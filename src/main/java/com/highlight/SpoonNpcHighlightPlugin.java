package com.highlight;

import com.google.common.collect.ImmutableSet;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.NpcDespawned;

import java.util.*;

import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.NpcSpawned;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.client.util.ColorUtil;
import net.runelite.client.util.Text;
import net.runelite.api.events.MenuEntryAdded;
import net.runelite.api.events.GameStateChanged;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import com.google.inject.Provides;
import net.runelite.client.config.ConfigManager;
import java.time.Instant;
import java.awt.Color;

import net.runelite.client.ui.overlay.OverlayManager;

import javax.inject.Inject;

import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.Plugin;

@PluginDescriptor(name = "[S] Npc Highlight", description = "NPC highlight by Kourend/Boris", tags = { "SpoonNpcHighlight", "spoon" }, conflicts = { "NPC Indicators" })

@Slf4j
public class SpoonNpcHighlightPlugin extends Plugin
{
    @Inject
    private Client client;
    @Inject
    private OverlayManager overlayManager;
    @Inject
    private SpoonNpcHighlightOverlay overlay;
    @Inject
    private SpoonNpcMinimapOverlay minimapOverlay;
    @Inject
    private SpoonNpcHighlightConfig config;
    public ArrayList<String> tileNames = new ArrayList<>();
    public ArrayList<Integer> tileIds = new ArrayList<>();
    public ArrayList<String> trueTileNames = new ArrayList<>();
    public ArrayList<Integer> trueTileIds = new ArrayList<>();
    public ArrayList<String> swTileNames = new ArrayList<>();
    public ArrayList<Integer> swTileIds = new ArrayList<>();
    public ArrayList<String> hullNames = new ArrayList<>();
    public ArrayList<Integer> hullIds = new ArrayList<>();
    public ArrayList<String> areaNames = new ArrayList<>();
    public ArrayList<Integer> areaIds = new ArrayList<>();
    public ArrayList<String> outlineNames = new ArrayList<>();
    public ArrayList<Integer> outlineIds = new ArrayList<>();
    public ArrayList<String> turboNames = new ArrayList<>();
    public ArrayList<Integer> turboIds = new ArrayList<>();
    public ArrayList<Color> turboColors = new ArrayList<>();
    public ArrayList<NpcSpawn> npcSpawns = new ArrayList<>();
    public ArrayList<String> namesToDisplay = new ArrayList<>();
    public ArrayList<String> ignoreDeadExclusionList = new ArrayList<>();
    public Instant lastTickUpdate;
    public SpoonNpcHighlightConfig.tagStyleMode turboModeStyle;
    public int turboTileWidth = 0;
    public int turboOutlineWidth = 0;
    public int turboOutlineFeather = 0;
    private static final Set<MenuAction> NPC_MENU_ACTIONS = ImmutableSet.of(MenuAction.NPC_FIRST_OPTION, MenuAction.NPC_SECOND_OPTION, MenuAction.NPC_THIRD_OPTION, MenuAction.NPC_FOURTH_OPTION, MenuAction.NPC_FIFTH_OPTION, MenuAction.WIDGET_TARGET_ON_NPC, MenuAction.ITEM_USE_ON_NPC);
    private Random random;

    @Provides
    SpoonNpcHighlightConfig providesConfig(final ConfigManager configManager) {
        return configManager.getConfig(SpoonNpcHighlightConfig.class);
    }
    
    protected void startUp() {
        reset();
        overlayManager.add(overlay);
        overlayManager.add(minimapOverlay);
        splitNameList(config.tileNames(), tileNames);
        splitIdList(config.tileIds(), tileIds);
        splitNameList(config.trueTileNames(), trueTileNames);
        splitIdList(config.trueTileIds(), trueTileIds);
        splitNameList(config.swTileNames(), swTileNames);
        splitIdList(config.swTileIds(), swTileIds);
        splitNameList(config.hullNames(), hullNames);
        splitIdList(config.hullIds(), hullIds);
        splitNameList(config.areaNames(), areaNames);
        splitIdList(config.areaIds(), areaIds);
        splitNameList(config.outlineNames(), outlineNames);
        splitIdList(config.outlineIds(), outlineIds);
        splitNameList(config.turboNames(), turboNames);
        splitIdList(config.turboIds(), turboIds);
        splitNameList(config.displayName(), namesToDisplay);
        splitNameList(config.ignoreDeadExclusion(), ignoreDeadExclusionList);

        random = new Random();
    }
    
    protected void shutDown() {
        reset();
        overlayManager.remove(overlay);
        overlayManager.remove(minimapOverlay);
    }
    
    private void reset() {
        tileNames.clear();
        tileIds.clear();
        trueTileNames.clear();
        trueTileIds.clear();
        swTileNames.clear();
        swTileIds.clear();
        hullNames.clear();
        hullIds.clear();
        areaNames.clear();
        areaIds.clear();
        outlineNames.clear();
        outlineIds.clear();
        npcSpawns.clear();
        turboModeStyle = SpoonNpcHighlightConfig.tagStyleMode.TILE;
        turboTileWidth = 0;
        turboOutlineWidth = 0;
        turboOutlineFeather = 0;
    }
    
    private void splitNameList(final String configStr, final ArrayList<String> strList) {
        if (!configStr.equals("")) {
            for (final String str : configStr.split(",")) {
                if (!str.trim().equals("")) {
                    strList.add(str.trim().toLowerCase());
                }
            }
        }
    }
    
    private void splitIdList(final String configStr, final ArrayList<Integer> idList) {
        if (!configStr.equals("")) {
            for (final String str : configStr.split(",")) {
                if (!str.trim().equals("")) {
                    try {
                        idList.add(Integer.parseInt(str.trim()));
                    }
                    catch (Exception ex) {
                        log.error("s npc Highlight: " + ex.getMessage());
                    }
                }
            }
        }
    }
    
    @Subscribe
    public void onConfigChanged(final ConfigChanged event) {
        switch (event.getKey()) {
            case "tileNames":
                tileNames.clear();
                splitNameList(config.tileNames(), tileNames);
                break;
            case "tileIds":
                tileIds.clear();
                splitIdList(config.tileIds(), tileIds);
                break;
            case "trueTileNames":
                trueTileNames.clear();
                splitNameList(config.trueTileNames(), trueTileNames);
                break;
            case "trueTileIds":
                trueTileIds.clear();
                splitIdList(config.trueTileIds(), trueTileIds);
                break;
            case "swTileNames":
                swTileNames.clear();
                splitNameList(config.swTileNames(), swTileNames);
                break;
            case "swTileIds":
                swTileIds.clear();
                splitIdList(config.swTileIds(), swTileIds);
                break;
            case "hullNames":
                hullNames.clear();
                splitNameList(config.hullNames(), hullNames);
                break;
            case "hullIds":
                hullIds.clear();
                splitIdList(config.hullIds(), hullIds);
                break;
            case "areaNames":
                areaNames.clear();
                splitNameList(config.areaNames(), areaNames);
                break;
            case "areaIds":
                areaIds.clear();
                splitIdList(config.areaIds(), areaIds);
                break;
            case "outlineNames":
                outlineNames.clear();
                splitNameList(config.outlineNames(), outlineNames);
                break;
            case "outlineIds":
                outlineIds.clear();
                splitIdList(config.outlineIds(), outlineIds);
                break;
            case "turboNames":
                turboNames.clear();
                splitNameList(config.turboNames(), turboNames);
                break;
            case "turboIds":
                turboIds.clear();
                splitIdList(config.turboIds(), turboIds);
                break;
        }
    }
    
    @Subscribe
    public void onGameStateChanged(final GameStateChanged event) {
        if (event.getGameState() == GameState.LOGIN_SCREEN || event.getGameState() == GameState.HOPPING) {
            npcSpawns.clear();
        }
    }

    @Subscribe
    public void onMenuEntryAdded(final MenuEntryAdded event) {
        int type = event.getType();
        if (type >= 2000) {
            type -= 2000;
        }
        final MenuAction menuAction = MenuAction.of(type);
        if (NPC_MENU_ACTIONS.contains(menuAction)) {  // shift not pressed: not making a tag?
            NPC npc = client.getCachedNPCs()[event.getIdentifier()];
            Color color = null;
            MenuEntry[] menuEntries = client.getMenuEntries();
            MenuEntry menuEntry = menuEntries[menuEntries.length - 1];
            if (npc.isDead()) {
                color = config.deadNpcMenuColor();
                if (config.deprioritiseDead() && npc.getName() != null && !config.ignoreDeadExclusion().contains(npc.getName().toLowerCase())){
                    menuEntry.setDeprioritized(true);
                }
            } else if (config.highlightMenuNames() && npc.getName() != null && checkAllLists(npc)) {
                color = config.tagStyleMode() == SpoonNpcHighlightConfig.tagStyleMode.TURBO ? Color.getHSBColor(random.nextFloat(), 1.0f, 1.0f) : config.highlightColor();
            }
            if (color != null) {
                String target = ColorUtil.prependColorTag(Text.removeTags(event.getTarget()), color);
                menuEntry.setTarget(target);
                client.setMenuEntries(menuEntries);
            }
        }
        else if (menuAction == MenuAction.EXAMINE_NPC && client.isKeyPressed(81)) {
            final int id = event.getIdentifier();
            final NPC npc = client.getCachedNPCs()[id];
            if (npc != null && npc.getName() != null) {  // && !npc.isDead() ?
                createTagMenuEntry(event, config.tagStyleMode(), npc);
            }
        }
    }

    private void createTagMenuEntry(MenuEntryAdded event, SpoonNpcHighlightConfig.tagStyleMode mode, NPC npc){
        if (npc.getName() == null) return;  // TODO: eventually make this search id instead?

        ArrayList<String> names = whichListNames(mode);
        String highlightType = mode.toString();

        String target = event.getTarget();
        if (config.highlightMenuNames()) {
            int colorCode;
            if (config.tagStyleMode() == SpoonNpcHighlightConfig.tagStyleMode.TURBO) {
                if (turboColors.size() == 0) {
                    colorCode = Color.getHSBColor(random.nextFloat(), 1.0f, 1.0f).getRGB();
                }
                else {
                    colorCode = turboColors.get(turboNames.indexOf(npc.getName().toLowerCase())).getRGB();
                }
            }
            else {
                colorCode = (npc.isDead() ? config.deadNpcMenuColor().getRGB() : config.highlightColor().getRGB());
            }
            target = ColorUtil.prependColorTag(Text.removeTags(event.getTarget()), new Color(colorCode));
        }

        client.createMenuEntry(-1)
            .setOption(names.contains(npc.getName().toLowerCase()) ? "Untag All " + highlightType : "Tag All " + highlightType)
            .setTarget(target)
            .setParam0(event.getActionParam0())
            .setParam1(event.getActionParam1())
            .setIdentifier(event.getIdentifier())
            .setType(MenuAction.RUNELITE);
    }

    @Subscribe
    public void onMenuOptionClicked(final MenuOptionClicked event) {
        if (event.getMenuAction() == MenuAction.RUNELITE && (event.getMenuOption().contains("Tag All ") || event.getMenuOption().contains("Untag All ")) &&
                (SpoonNpcHighlightConfig.tagStyleMode.allToStrings().anyMatch(event.getMenuOption()::contains))) {
            final int id = event.getId();
            final NPC npc = client.getCachedNPCs()[id];
            ArrayList<String> listToChange = new ArrayList<>();
            if (npc.getName() != null) {
                if (event.getMenuOption().contains("Untag All")) {
                    whichListNames(config.tagStyleMode()).remove(npc.getName().toLowerCase());
                }
                else {
                    whichListNames(config.tagStyleMode()).add(npc.getName().toLowerCase());
                }
                listToChange = whichListNames(config.tagStyleMode());
            }
            switch (config.tagStyleMode()) {
                case TILE:
                    config.setTileNames(Text.toCSV(listToChange));
                    break;
                case TRUE_TILE:
                    config.setTrueTileNames(Text.toCSV(listToChange));
                    break;
                case SW_TILE:
                    config.setSwTileNames(Text.toCSV(listToChange));
                    break;
                case HULL:
                    config.setHullNames(Text.toCSV(listToChange));
                    break;
                case AREA:
                    config.setAreaNames(Text.toCSV(listToChange));
                    break;
                case OUTLINE:
                    config.setOutlineNames(Text.toCSV(listToChange));
                    break;
                default:
                    config.setTurboNames(Text.toCSV(listToChange));
            }
            event.consume();
        }
    }

    @Subscribe
    public void onNpcSpawned(final NpcSpawned event) {
        for (final NpcSpawn n : npcSpawns) {
            if (event.getNpc().getIndex() == n.index) {
                int respawnTime = client.getTickCount() - n.diedOnTick + 1;
                if (respawnTime > 350) {  // get rid of any timers that are too long. Max known (apart from prif bunny) is 350: ugthanki camels
                    continue;
                }
                if (n.respawnTime != -1 && n.respawnTime > respawnTime){
                    n.respawnTime = respawnTime;
                }
                if (n.spawnPoint == null) {
                    final NPCComposition comp = event.getNpc().getTransformedComposition();
                    if (comp != null) {
                        n.respawnTime = respawnTime;
                        for (WorldPoint wp : n.spawnLocations) {
                            if (wp.getX() != event.getNpc().getWorldLocation().getX() || wp.getY() != event.getNpc().getWorldLocation().getY()) continue;
                            n.spawnPoint = event.getNpc().getWorldLocation();
                            break;
                        }
                    }
                    if (n.spawnPoint == null) {
                        n.spawnLocations.add(event.getNpc().getWorldLocation());
                    }
                }
                n.dead = false;
                break;
            }
        }
    }
    
    @Subscribe
    public void onNpcDespawned(final NpcDespawned event) {
        if (event.getNpc().isDead()) {
            boolean exists = false;
            for (final NpcSpawn n : npcSpawns) {
                if (event.getNpc().getIndex() == n.index) {
                    n.diedOnTick = client.getTickCount();
                    n.dead = true;
                    exists = true;
                    break;
                }
            }
            if (!exists && event.getNpc().getName() != null) {
                if (checkAllLists(event.getNpc())) {
                    npcSpawns.add(new NpcSpawn(event.getNpc()));
                    NpcSpawn n = npcSpawns.get(npcSpawns.size() - 1);
                    n.diedOnTick = client.getTickCount();
                }
            }
        }
    }
    
    @Subscribe
    public void onGameTick(final GameTick event) {
        lastTickUpdate = Instant.now();
        turboColors.clear();
        for (int i = 0; i < turboNames.size() + turboIds.size(); ++i) {
            turboColors.add(Color.getHSBColor(random.nextFloat(), 1.0f, 1.0f));
        }
        turboModeStyle = SpoonNpcHighlightConfig.tagStyleMode.values()[random.nextInt(6)];
        turboTileWidth = random.nextInt(10) + 1;
        turboOutlineWidth = random.nextInt(50) + 1;
        turboOutlineFeather = random.nextInt(4);
    }

    public boolean checkSpecificList(ArrayList<String> strList, ArrayList<Integer> intList, NPC npc) {
    if (intList.contains(npc.getId())) {
        return true;
    }
    if (npc.getName() != null) {
        String name = npc.getName().toLowerCase();
        for (String str : strList) {
            if (!str.equalsIgnoreCase(name) && (!str.contains("*") || !(str.startsWith("*") && str.endsWith("*") && name.contains(str.replace("*", "")) || str.startsWith("*") && name.endsWith(str.replace("*", ""))) && !name.startsWith(str.replace("*", "")))) continue;
            return true;
        }
    }
    return false;
}

    public boolean checkAllLists(NPC npc) {
        int id = npc.getId();
        if (tileIds.contains(id) || trueTileIds.contains(id) || swTileIds.contains(id) || hullIds.contains(id) || this.areaIds.contains(id) || outlineIds.contains(id) || turboIds.contains(id)) {
            return true;
        }
        if (npc.getName() != null) {
            String name = npc.getName().toLowerCase();
            for (ArrayList<String> strList : new ArrayList<>(Arrays.asList(tileNames, trueTileNames, swTileNames, hullNames, areaNames, outlineNames, turboNames))) {
                for (String str : strList) {
                    if (!str.equalsIgnoreCase(name) && (!str.contains("*") || !(str.startsWith("*") && str.endsWith("*") && name.contains(str.replace("*", "")) || str.startsWith("*") && name.endsWith(str.replace("*", ""))) && !name.startsWith(str.replace("*", "")))) continue;
                    return true;
                }
            }
        }
        return false;
    }

    private ArrayList<String> whichListNames(SpoonNpcHighlightConfig.tagStyleMode mode){
        switch(mode){
            case TRUE_TILE: {return trueTileNames;}
            case SW_TILE: {return swTileNames;}
            case HULL: {return hullNames;}
            case AREA: {return areaNames;}
            case OUTLINE: {return outlineNames;}
            case TURBO: {return turboNames;}
            default: {return tileNames;}
        }
    }

    private ArrayList<Integer> whichListIds(SpoonNpcHighlightConfig.tagStyleMode mode){
        switch(mode){
            case TRUE_TILE: {return trueTileIds;}
            case SW_TILE: {return swTileIds;}
            case HULL: {return hullIds;}
            case AREA: {return areaIds;}
            case OUTLINE: {return outlineIds;}
            case TURBO: {return turboIds;}
            default: {return tileIds;}
        }
    }
}
