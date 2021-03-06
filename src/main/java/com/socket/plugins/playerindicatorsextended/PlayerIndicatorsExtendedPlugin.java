/*
 * Decompiled with CFR 0.150.
 * 
 * Could not load the following classes:
 *  com.google.inject.Provides
 *  javax.inject.Inject
 *  net.runelite.api.Actor
 *  net.runelite.api.Client
 *  net.runelite.api.Player
 *  net.runelite.api.events.GameTick
 *  net.runelite.client.config.ConfigManager
 *  net.runelite.client.eventbus.Subscribe
 *  net.runelite.client.game.ChatIconManager
 *  net.runelite.client.plugins.Plugin
 *  net.runelite.client.plugins.PluginDescriptor
 *  net.runelite.client.plugins.PluginManager
 *  net.runelite.client.ui.overlay.Overlay
 *  net.runelite.client.ui.overlay.OverlayManager
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package com.socket.plugins.playerindicatorsextended;

import com.google.inject.Provides;
import java.util.ArrayList;
import java.util.Objects;
import javax.inject.Inject;
import net.runelite.api.Actor;
import net.runelite.api.Client;
import net.runelite.api.Player;
import net.runelite.api.events.GameTick;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.ChatIconManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.PluginManager;
import com.socket.packet.SocketMembersUpdate;
import com.socket.packet.SJoin;
import com.socket.packet.SShutdown;
import net.runelite.client.ui.overlay.OverlayManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@PluginDescriptor(name="Socket - Player Indicator", description="Shows you players who are in your socket", tags={"indicator, socket, player, highlight"})
public class PlayerIndicatorsExtendedPlugin
extends Plugin {
    private static final Logger log = LoggerFactory.getLogger(PlayerIndicatorsExtendedPlugin.class);
    @Inject
    private PlayerIndicatorsExtendedConfig config;
    @Inject
    private Client client;
    @Inject
    private OverlayManager overlayManager;
    @Inject
    private PluginManager pluginManager;
    @Inject
    private PlayerIndicatorsExtendedOverlay overlay;
    @Inject
    private PlayerIndicatorsExtendedMinimapOverlay overlayMinimap;
    @Inject
    private ChatIconManager chatIconManager;
    private ArrayList<Actor> players;
    private ArrayList<String> names;
    int activeTick = 0;
    boolean cleared = false;

    @Provides
    PlayerIndicatorsExtendedConfig getConfig(ConfigManager configManager) {
        return configManager.getConfig(PlayerIndicatorsExtendedConfig.class);
    }

    public ArrayList<Actor> getPlayers() {
        return this.players;
    }

    protected void startUp() {
        this.overlayManager.add(this.overlay);
        this.overlayManager.add(this.overlayMinimap);
        this.players = new ArrayList();
        this.names = new ArrayList();
    }

    protected void shutDown() {
        this.overlayManager.remove(this.overlay);
        this.overlayManager.remove(this.overlayMinimap);
    }

    @Subscribe
    public void onSJoin(SJoin event) {
        this.names.add(event.getPlayerName());
        if (event.getPlayerName().equals(Objects.requireNonNull(this.client.getLocalPlayer()).getName())) {
            this.names.clear();
        }
    }

    @Subscribe
    public void onSocketMembersUpdate(SocketMembersUpdate event) {
        this.names.clear();
        for (String s : event.getMembers()) {
            if (s.equals(this.client.getLocalPlayer().getName())) continue;
            this.names.add(s);
        }
    }

    @Subscribe
    public void onSShutdown(SShutdown event) {
        this.names.clear();
    }

    @Subscribe
    public void onGameTick(GameTick event) {
        this.players.clear();
        block0: for (Player p : this.client.getPlayers()) {
            for (String name : this.names) {
                if (!name.equals(p.getName())) continue;
                this.players.add(p);
                continue block0;
            }
        }
    }
}

