package com.example.survival.config;

import org.bukkit.configuration.ConfigurationSection;

/**
 * Settings under {@code hud:} in config.yml.
 *
 * @param resourcePack true to draw image bars from the resource pack,
 *                     false for plain text bars that work without it
 */
public record HudSettings(int updateTicks, int barLength, boolean resourcePack) {

    static HudSettings load(ConfigurationSection config) {
        return new HudSettings(
                Math.max(1, config.getInt("hud.update-ticks")),
                Math.max(1, config.getInt("hud.bar-length")),
                config.getBoolean("hud.resource-pack")
        );
    }
}
