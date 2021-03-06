/*
 * Decompiled with CFR 0.150.
 * 
 * Could not load the following classes:
 *  javax.inject.Inject
 *  net.runelite.api.Client
 *  net.runelite.api.Player
 *  net.runelite.api.Point
 *  net.runelite.api.coords.LocalPoint
 *  net.runelite.client.ui.FontManager
 *  net.runelite.client.ui.overlay.OverlayLayer
 *  net.runelite.client.ui.overlay.OverlayPanel
 *  net.runelite.client.ui.overlay.OverlayPosition
 *  net.runelite.client.ui.overlay.OverlayPriority
 *  net.runelite.client.ui.overlay.OverlayUtil
 *  net.runelite.client.ui.overlay.outline.ModelOutlineRenderer
 */
package com.socket.plugins.sockethealing;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.util.ArrayList;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.Player;
import net.runelite.api.Point;
import net.runelite.api.coords.LocalPoint;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;
import net.runelite.client.ui.overlay.OverlayUtil;
import net.runelite.client.ui.overlay.outline.ModelOutlineRenderer;

public class SocketHealingOverlay
extends OverlayPanel {
    private final Client client;
    private final SocketHealingPlugin plugin;
    private final SocketHealingConfig config;
    private ModelOutlineRenderer modelOutlineRenderer;

    @Inject
    private SocketHealingOverlay(Client client, SocketHealingPlugin plugin, SocketHealingConfig config, ModelOutlineRenderer modelOutlineRenderer) {
        this.client = client;
        this.plugin = plugin;
        this.config = config;
        this.modelOutlineRenderer = modelOutlineRenderer;
        this.setPosition(OverlayPosition.DYNAMIC);
        this.setPriority(OverlayPriority.HIGH);
        this.setLayer(OverlayLayer.ABOVE_SCENE);
    }

    public Dimension render(Graphics2D graphics) {
        if (this.config.healingFontType() == SocketHealingConfig.SocketFontType.CUSTOM) {
            graphics.setFont(new Font(FontManager.getRunescapeFont().toString(), 1, this.config.fontSize()));
        } else if (this.config.healingFontType() == SocketHealingConfig.SocketFontType.REGULAR) {
            graphics.setFont(FontManager.getRunescapeFont());
        } else if (this.config.healingFontType() == SocketHealingConfig.SocketFontType.BOLD) {
            graphics.setFont(FontManager.getRunescapeBoldFont());
        } else if (this.config.healingFontType() == SocketHealingConfig.SocketFontType.SMALL) {
            graphics.setFont(FontManager.getRunescapeSmallFont());
        }
        ArrayList<LocalPoint> playerPoints = new ArrayList<>();
        for (Player p : this.client.getPlayers()) {
            if (p.getName() == null || !this.plugin.getPartyMembers().containsKey(p.getName())) continue;
            String playerName = p.getName();
            SocketHealingPlayer player = this.plugin.getPartyMembers().get(playerName);
            int health = player.getHealth();
            Color highlightColor = Color.WHITE;
            Color textColor = Color.WHITE;
            if (health > this.config.greenZone()) {
                highlightColor = new Color(this.config.greenZoneColor().getRed(), this.config.greenZoneColor().getGreen(), this.config.greenZoneColor().getBlue(), this.config.opacity());
            }
            textColor = this.config.greenZoneColor();
            if (health <= this.config.greenZone() && health > this.config.orangeZone()) {
                highlightColor = new Color(this.config.orangeZoneColor().getRed(), this.config.orangeZoneColor().getGreen(), this.config.orangeZoneColor().getBlue(), this.config.opacity());
                textColor = this.config.orangeZoneColor();
            } else if (health <= this.config.orangeZone()) {
                highlightColor = new Color(this.config.redZoneColor().getRed(), this.config.redZoneColor().getGreen(), this.config.redZoneColor().getBlue(), this.config.opacity());
                textColor = this.config.redZoneColor();
            }
            if (this.config.displayHealth() || !this.config.hpPlayerNames().equals("") && this.plugin.playerNames.contains(playerName.toLowerCase())) {
                String text = "";
                text = this.config.showName() ? playerName + " - " + health : String.valueOf(health);
                int offsetHp = 0;
                for (LocalPoint lp : playerPoints) {
                    if (lp.getX() != p.getLocalLocation().getX() || lp.getY() != p.getLocalLocation().getY()) continue;
                    ++offsetHp;
                }
                int xOffset = this.config.getIndicatorXOffset();
                int yOffset = this.config.getIndicatorYOffset();
                Point point = p.getCanvasTextLocation(graphics, text, 0);
                if (point != null) {
                    point = new Point(point.getX() + xOffset, point.getY() - yOffset);
                    if (offsetHp != 0) {
                        int x = point.getX();
                        int y = point.getY() - 15 * offsetHp;
                        point = new Point(x, y);
                    }
                    OverlayUtil.renderTextLocation(graphics, point, text, textColor);
                }
                playerPoints.add(p.getLocalLocation());
            }
            if (!this.config.highlightedPlayerNames().toLowerCase().contains(playerName.toLowerCase()) || !this.config.highlightOutline()) continue;
            this.modelOutlineRenderer.drawOutline(p, this.config.hpThiCC(), highlightColor, this.config.glow());
        }
        return null;
    }
}

