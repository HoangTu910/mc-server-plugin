package com.example.survival.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;

import java.util.concurrent.ThreadLocalRandom;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;

import com.example.survival.SurvivalMainPlugin;
import com.example.survival.customitem.CustomItemService;
import com.example.survival.mobs.MobService;
import com.example.survival.mobs.MobType;

public final class CustomItemDropListener implements Listener {
    private final CustomItemService customItemService;
    private final MobService mobService;
    private final SurvivalMainPlugin plugin;
    
    public CustomItemDropListener(
            SurvivalMainPlugin plugin,
            CustomItemService customItemService,
            MobService mobService
    ) {
        this.customItemService = customItemService;
        this.plugin = plugin;
        this.mobService = mobService;
    }

    @EventHandler 
    public void onMobDeath(EntityDeathEvent event) {
        Player killer = event.getEntity().getKiller();

        /*only award items when player kill a mob */
        if(killer == null) {
            return;
        }

        MobType customMobType = mobService.getMobType(event.getEntity());
        if (customMobType == null) {
            return;
        }

        ConfigurationSection section = plugin.getConfig()
                                             .getConfigurationSection("mob-drops." + customMobType.getId());
        if (section == null) {
            plugin.getLogger().warning("No drop configuration found for mob: " + customMobType.getId());
            return;
        }

        String itemId = section.getString("item");
        if (itemId == null || itemId.isBlank()) {
            plugin.getLogger().warning("Invalid item ID for mob: " + customMobType.getId());
            return;
        }

        double dropChance = section.getDouble("chance", 0.0);

        /*roll the drop chance */
        ItemStack item = customItemService.createItem(itemId);
        if (item == null) {
            return;
        }

        if (ThreadLocalRandom.current().nextDouble() < dropChance) {
            event.getDrops().add(item);
        }
    }
}
