package com.example.survival.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Loads config.yml once into typed settings objects.
 *
 * Services should hold this service and call e.g. {@code config.mana()} each
 * time they need a value, so they pick up new values after {@link #reload()}.
 */
public final class ConfigService {

    private final JavaPlugin plugin;

    private ManaSettings mana;
    private HudSettings hud;

    public ConfigService(JavaPlugin plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();

        FileConfiguration config = plugin.getConfig();
        config.options().copyDefaults(true);
        plugin.saveConfig();

        mana = ManaSettings.load(config, plugin.getLogger());
        hud = HudSettings.load(config);
    }

    public ManaSettings mana() {
        return mana;
    }

    public HudSettings hud() {
        return hud;
    }
}
