package com.example.survival.config;

import org.bukkit.configuration.ConfigurationSection;

import java.util.logging.Logger;

/**
 * Settings under {@code mana:} in config.yml.
 */
public record ManaSettings(double max, double regenPerSecond) {

    static ManaSettings load(ConfigurationSection config, Logger logger) {
        double max = config.getDouble("mana.max");
        if (max <= 0) {
            logger.warning("mana.max must be greater than 0, using 100");
            max = 100;
        }

        double regenPerSecond = config.getDouble("mana.regen-per-second");
        if (regenPerSecond < 0) {
            logger.warning("mana.regen-per-second cannot be negative, using 0");
            regenPerSecond = 0;
        }

        return new ManaSettings(max, regenPerSecond);
    }
}
