package com.example.survival;

import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

import com.example.survival.listener.CombatListener;
import com.example.survival.mobs.MobCommand;
import com.example.survival.mobs.MobService;

import java.util.Objects;

public final class SurvivalPlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        saveDefaultConfig();

        getLogger().info("CustomSurvival enabled.");

        MobService mobService = new MobService(this);

        /* Register the combat listener*/
        getServer().getPluginManager().registerEvents(
                new CombatListener(this, mobService),
                this
        );

        /* Register the mob command */
        PluginCommand mobCommand = Objects.requireNonNull(
                getCommand("mob"),
                "Command 'mob' is missing from plugin.yml"
        );

        /* Set the executor for the mob command */
        mobCommand.setExecutor(new MobCommand(mobService));

        getLogger().info("CustomMobs enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().info("CustomSurvival disabled.");
    }
}