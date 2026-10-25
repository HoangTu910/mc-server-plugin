package com.example.survival;

import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

import com.example.survival.customitem.CustomItemAbilityListener;
import com.example.survival.customitem.CustomItemCommand;
import com.example.survival.customitem.CustomItemService;
import com.example.survival.mobs.CombatListener;
import com.example.survival.mobs.CustomItemDropListener;
import com.example.survival.mobs.MobCommand;
import com.example.survival.mobs.MobService;
import com.example.survival.rank.RankCommand;
import com.example.survival.rank.RankListener;
import com.example.survival.rank.RankService;
import com.example.survival.rank.SetRankCommand;
import com.example.survival.trader.TraderCommand;
import com.example.survival.trader.TraderService;

import java.util.Objects;

public final class SurvivalMainPlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        saveDefaultConfig();

        getLogger().info("CustomSurvival enabled.");

        MobService        mobService        = new MobService(this);
        RankService       rankService       = new RankService(this);
        CustomItemService customItemService = new CustomItemService(this);
        TraderService     traderService     = new TraderService(this, customItemService);
        
        /* Register the combat listener*/
        getServer().getPluginManager().registerEvents(
                new CombatListener(this, mobService),
                this
        );

        getServer().getPluginManager().registerEvents(
                new RankListener(rankService, mobService),
                this
        );

        getServer().getPluginManager().registerEvents(
                new CustomItemAbilityListener(this, customItemService),
                this
        );

        getServer().getPluginManager().registerEvents(
                new CustomItemDropListener(this, customItemService, mobService),
                this
        );

        /* Register the mob command */
        PluginCommand mobCommand = Objects.requireNonNull(
                getCommand("mob"),
                "Command 'mob' is missing from plugin.yml"
        );

        PluginCommand setRankCommand = Objects.requireNonNull(
                getCommand("set-rank"),
                "Command 'set-rank' is missing from plugin.yml"
        );

        PluginCommand rankCommand = Objects.requireNonNull(
                getCommand("rank"),
                "Command 'rank' is missing from plugin.yml"
        );

        PluginCommand customItemCommand = Objects.requireNonNull(
                getCommand("customitem"),
                "Command 'customitem' is missing from plugin.yml"
        );

        PluginCommand traderCommand = Objects.requireNonNull(
                getCommand("trader"),
                "Command 'trader' is missing from plugin.yml"
        );

        // Set the executor for the rank command
        rankCommand.setExecutor(new RankCommand(rankService));
        setRankCommand.setExecutor(new SetRankCommand(rankService));
        mobCommand.setExecutor(new MobCommand(mobService));
        customItemCommand.setExecutor(
                new CustomItemCommand(
                        customItemService,
                        this::reloadConfig
                )
        );
        traderCommand.setExecutor(new TraderCommand(traderService));
    }

    @Override
    public void onDisable() {
        getLogger().info("CustomSurvival disabled.");
    }
}