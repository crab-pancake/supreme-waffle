/*
 * Decompiled with CFR 0.150.
 * 
 * Could not load the following classes:
 *  net.runelite.client.config.Config
 *  net.runelite.client.config.ConfigGroup
 *  net.runelite.client.config.ConfigItem
 */
package com.socket.plugins.specialcounterextended;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup(value="Socket Special Counter Config")
public interface SpecialCounterExtendedConfig
extends Config {
    @ConfigItem(position=0, keyName="showHitOverlay", name="Hit Overlay", description="Show the special attack overlay.")
    default boolean showHitOverlay() {
        return true;
    }

    @ConfigItem(position=1, keyName="getFadeDelay", name="Fade Delay", description="Delay, in milliseconds, until the icon disappears.")
    default int getFadeDelay() {
        return 5000;
    }

    @ConfigItem(position=2, keyName="getMaxHeight", name="Travel Height", description="Maximum height, in pixels, for the icon to travel.")
    default int getMaxHeight() {
        return 200;
    }

    @ConfigItem(position=3, keyName="guessDawnbringer", name="Guess Dawnbringer Hit", description="Guess Dawnbringer based on XP drop. Provides faster results.")
    default boolean guessDawnbringer() {
        return true;
    }
}

