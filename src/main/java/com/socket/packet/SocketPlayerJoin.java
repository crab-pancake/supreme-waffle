/*
 * Decompiled with CFR 0.150.
 */
package com.socket.packet;

public class SocketPlayerJoin {
    private String playerName;

    public SocketPlayerJoin(String playerName) {
        this.playerName = playerName;
    }

    public String getPlayerName() {
        return this.playerName;
    }
}
