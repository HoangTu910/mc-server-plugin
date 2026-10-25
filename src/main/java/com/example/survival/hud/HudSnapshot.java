package com.example.survival.hud;

import net.kyori.adventure.text.Component;
import org.jspecify.annotations.Nullable;

/**
 * Everything a {@link HudRenderer} needs to draw one frame of a player's HUD.
 * Built by {@link HudService} so renderers never touch Bukkit state directly.
 *
 * @param flash a temporary message to show, or null
 * @param frame increases by one on every HUD update; used for blinking effects
 */
record HudSnapshot(
        double health,
        double maxHealth,
        HealthEffect healthEffect,
        double mana,
        double maxMana,
        @Nullable Component flash,
        long frame
) {

    enum HealthEffect { NORMAL, POISONED, WITHERED, FROZEN }

    double healthRatio() {
        return ratio(health, maxHealth);
    }

    double manaRatio() {
        return ratio(mana, maxMana);
    }

    private static double ratio(double value, double max) {
        return max <= 0 ? 0 : Math.clamp(value / max, 0.0, 1.0);
    }
}
