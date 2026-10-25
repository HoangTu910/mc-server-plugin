package com.example.survival;

import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import com.example.survival.config.ConfigService;
import com.example.survival.customitem.CustomItemCommand;
import com.example.survival.customitem.CustomItemDropListener;
import com.example.survival.customitem.CustomItemService;
import com.example.survival.customitem.ability.AbilityListener;
import com.example.survival.customitem.ability.AbilityService;
import com.example.survival.customitem.ability.LightningStrikeAbility;
import com.example.survival.customitem.ability.VoidStrikeAbility;
import com.example.survival.hud.HudListener;
import com.example.survival.hud.HudService;
import com.example.survival.mana.ManaService;
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

    private AbilityService abilityService;

    @Override
    public void onEnable() {
        getLogger().info("CustomSurvival enabled.");

        ConfigService     configService     = new ConfigService(this);
        MobService        mobService        = new MobService(this);
        RankService       rankService       = new RankService(this);
        CustomItemService customItemService = new CustomItemService(this);
        TraderService     traderService     = new TraderService(this, customItemService);
        ManaService       manaService       = new ManaService(configService);
        HudService        hudService        = new HudService(this, configService, manaService);

        abilityService = new AbilityService(this, manaService, hudService);
        abilityService.register(new VoidStrikeAbility(hudService));
        abilityService.register(new LightningStrikeAbility(this, customItemService, hudService));

        registerListeners(
                new MobCombatListener(this, mobService),
                new RankListener(rankService, mobService),
                new AbilityListener(customItemService, abilityService),
                new CustomItemDropListener(this, customItemService, mobService),
                new HudListener(manaService, hudService)
        );

        Runnable reload = () -> {
            configService.reload();
            hudService.restart();
        };

        registerCommand("mob", new MobCommand(mobService));
        registerCommand("rank", new RankCommand(rankService));
        registerCommand("set-rank", new SetRankCommand(rankService));
        registerCommand("customitem", new CustomItemCommand(customItemService, reload));
        registerCommand("trader", new TraderCommand(traderService));

        hudService.start();
    }

    @Override
    public void onDisable() {
        if (abilityService != null) {
            abilityService.shutdown();
        }
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
