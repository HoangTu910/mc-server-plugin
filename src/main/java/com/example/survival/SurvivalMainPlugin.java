package com.example.survival;

import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import com.example.survival.customitem.CustomItemAbilityListener;
import com.example.survival.customitem.CustomItemCommand;
import com.example.survival.customitem.CustomItemDropListener;
import com.example.survival.customitem.CustomItemService;
import com.example.survival.mobs.MobCombatListener;
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

        registerListeners(
                new MobCombatListener(this, mobService),
                new RankListener(rankService, mobService),
                new CustomItemAbilityListener(this, customItemService),
                new CustomItemDropListener(this, customItemService, mobService)
        );

        registerCommand("mob", new MobCommand(mobService));
        registerCommand("rank", new RankCommand(rankService));
        registerCommand("set-rank", new SetRankCommand(rankService));
        registerCommand("customitem", new CustomItemCommand(customItemService, this::reloadConfig));
        registerCommand("trader", new TraderCommand(traderService));
    }

    @Override
    public void onDisable() {
        getLogger().info("CustomSurvival disabled.");
    }

    private void registerListeners(Listener... listeners) {
        for (Listener listener : listeners) {
            getServer().getPluginManager().registerEvents(listener, this);
        }
    }

    /**
     * Binds an executor to a command declared in plugin.yml.
     * If the executor also implements TabCompleter, Bukkit uses it for tab completion.
     */
    private void registerCommand(String name, CommandExecutor executor) {
        PluginCommand command = Objects.requireNonNull(
                getCommand(name),
                "Command '" + name + "' is missing from plugin.yml"
        );
        command.setExecutor(executor);
    }
}
