/*
 * Decompiled with CFR 0.150.
 * 
 * Could not load the following classes:
 *  net.runelite.client.plugins.Plugin
 *  net.runelite.client.ui.overlay.infobox.Timer
 */
package com.socket.plugins.socketbosstimer;

import java.awt.image.BufferedImage;
import java.time.temporal.ChronoUnit;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.ui.overlay.infobox.Timer;

class SocketRespawnTimer
extends Timer {
    private final Boss boss;

    public SocketRespawnTimer(Boss boss, BufferedImage bossImage, Plugin plugin, int world) {
        super(boss.getSpawnTime().toMillis(), ChronoUnit.MILLIS, bossImage, plugin);
        this.boss = boss;
    }

    public Boss getBoss() {
        return this.boss;
    }
}

