/*
 * Decompiled with CFR 0.150.
 */
package com.socket.packet;

public class SocketPlayerLeave {
    private String playerName;

    public SocketPlayerLeave(String playerName) {
        this.playerName = playerName;
    }

    public String getPlayerName() {
        return this.playerName;
    }
}
