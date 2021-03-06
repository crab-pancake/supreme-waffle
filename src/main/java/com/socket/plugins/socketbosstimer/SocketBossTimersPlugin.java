/*
 * Decompiled with CFR 0.150.
 * 
 * Could not load the following classes:
 *  com.google.inject.Provides
 *  javax.inject.Inject
 *  net.runelite.api.Client
 *  net.runelite.api.NPC
 *  net.runelite.api.events.GameTick
 *  net.runelite.api.events.NpcDespawned
 *  net.runelite.client.Notifier
 *  net.runelite.client.config.ConfigManager
 *  net.runelite.client.eventbus.EventBus
 *  net.runelite.client.eventbus.Subscribe
 *  net.runelite.client.game.ItemManager
 *  net.runelite.client.plugins.Plugin
 *  net.runelite.client.plugins.PluginDescriptor
 *  net.runelite.client.ui.overlay.infobox.InfoBox
 *  net.runelite.client.ui.overlay.infobox.InfoBoxManager
 *  net.runelite.client.util.ColorUtil
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package com.socket.plugins.socketbosstimer;

import com.google.inject.Provides;
import java.awt.Color;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import javax.inject.Inject;

import com.socket.org.json.JSONObject;
import com.socket.packet.SSend;
import com.socket.packet.SReceive;
import net.runelite.api.Client;
import net.runelite.api.NPC;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.NpcDespawned;
import net.runelite.client.Notifier;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.infobox.InfoBox;
import net.runelite.client.ui.overlay.infobox.InfoBoxManager;
import net.runelite.client.util.ColorUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@PluginDescriptor(name="Socket - Boss Timers", description="Show boss spawn timer overlays. Zhuri made the multiple worlds possible. I just made it socket.", tags={"combat", "pve", "overlay", "spawn"})
public class SocketBossTimersPlugin
extends Plugin {
    private static final Logger log = LoggerFactory.getLogger(SocketBossTimersPlugin.class);
    @Inject
    private Client client;
    @Inject
    private InfoBoxManager infoBoxManager;
    @Inject
    private ItemManager itemManager;
    @Inject
    private Notifier notifier;
    @Inject
    private SocketBossTimersConfig config;
    @Inject
    private EventBus eventBus;
    public ArrayList<Integer> worldList = new ArrayList();

    @Provides
    SocketBossTimersConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(SocketBossTimersConfig.class);
    }

    protected void shutDown() throws Exception {
        this.infoBoxManager.removeIf(t -> t instanceof SocketRespawnTimer);
    }

    @Subscribe
    private void onGameTick(GameTick e) {
        if (this.config.notifyOnTime()) {
            for (InfoBox infoBox : this.infoBoxManager.getInfoBoxes()) {
                if (!(infoBox instanceof SocketRespawnTimer)) continue;
                Instant endTime = ((SocketRespawnTimer)infoBox).getEndTime();
                Instant now = Instant.now();
                long delta = now.until(endTime, ChronoUnit.SECONDS);
                if (delta > (long)this.config.notifyTime() || delta <= 0L) continue;
                this.notifier.notify("Boss Spawning");
            }
        }
    }

    @Subscribe
    public void onNpcDespawned(NpcDespawned npcDespawned) {
        NPC npc = npcDespawned.getNpc();
        int world = this.client.getWorld();
        if (!npc.isDead()) {
            return;
        }
        int npcId = npc.getId();
        Boss boss = Boss.find(npcId);
        if (boss == null) {
            return;
        }
        if (this.config.socketBossKill()) {
            JSONObject data = new JSONObject();
            data.put("bossId", npc.getId());
            data.put("bossName", npc.getName());
            data.put("world", this.client.getWorld());
            data.put("name", this.client.getLocalPlayer().getName());
            JSONObject payload = new JSONObject();
            payload.put("socketrespawn", data);
            this.eventBus.post(new SSend(payload));
        }
        if (!this.config.multiWorldTimers()) {
            this.infoBoxManager.removeIf(t -> t instanceof SocketRespawnTimer && ((SocketRespawnTimer) t).getBoss() == boss);
        }
        SocketRespawnTimer timer = new SocketRespawnTimer(boss, this.itemManager.getImage(boss.getItemSpriteId()), this, world);
        timer.setTooltip(ColorUtil.wrapWithColorTag(npc.getName(), Color.YELLOW) + "</br>" + ColorUtil.wrapWithColorTag("World: " + world, Color.YELLOW));
        this.infoBoxManager.addInfoBox(timer);
    }

    @Subscribe
    public void onSReceive(SReceive event) {
        try {
            JSONObject payload = event.getPayload();
            if (payload.has("socketrespawn")) {
                JSONObject data = payload.getJSONObject("socketrespawn");
                String name = data.getString("name");
                int bossId = data.getInt("bossId");
                String bossName = data.getString("bossName");
                int world = data.getInt("world");
                Boss boss = Boss.find(bossId);
                if (!name.equals(this.client.getLocalPlayer().getName())) {
                    if (!this.config.multiWorldTimers()) {
                        this.infoBoxManager.removeIf(t -> t instanceof SocketRespawnTimer && ((SocketRespawnTimer) t).getBoss() == boss);
                    }
                    boolean alreadyExists = false;
                    for (InfoBox rTimer : this.infoBoxManager.getInfoBoxes()) {
                        if (!rTimer.getTooltip().contains("World: " + world)) continue;
                        alreadyExists = true;
                        break;
                    }
                    if (!alreadyExists) {
                        SocketRespawnTimer timer = new SocketRespawnTimer(boss, this.itemManager.getImage(boss.getItemSpriteId()), this, world);
                        timer.setTooltip(ColorUtil.wrapWithColorTag(bossName, Color.YELLOW) + "</br>" + ColorUtil.wrapWithColorTag("World: " + world, Color.YELLOW));
                        this.infoBoxManager.addInfoBox(timer);
                    }
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}

