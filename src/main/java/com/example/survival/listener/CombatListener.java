package com.example.survival.listener;

import java.util.concurrent.ThreadLocalRandom;
import com.example.survival.SurvivalMainPlugin;
import com.example.survival.mobs.MobService;
import com.example.survival.mobs.MobType;

import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;

public final class CombatListener implements Listener {

    private final SurvivalMainPlugin plugin;
    private final MobService mobService;

    public CombatListener(
            SurvivalMainPlugin plugin,
            MobService mobService
    ) {
        this.plugin = plugin;
        this.mobService = mobService;
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        Entity damager = event.getDamager();

        if (!mobService.isCustomMob(damager)) {
            return;
        }

        MobType type = mobService.getMobType(damager);

        if (type == null || !(damager instanceof LivingEntity)) {
            return;
        }

        // Chỉ thay đổi damage khi custom mob đánh người chơi.
        if (event.getEntity() instanceof Player) {
            event.setDamage(mobService.getAttackDamage(type));
        }
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();

        if (!mobService.isCustomMob(entity)) {
            return;
        }

        MobType type = mobService.getMobType(entity);

        if (type == null) {
            return;
        }

        String path = "mobs." + type.getId() + ".";

        // Loại bỏ loot mặc định của zombie.
        event.getDrops().clear();

        int fleshMin = plugin.getConfig().getInt(
                path + "loot.rotten-flesh-min", 1);
        int fleshMax = plugin.getConfig().getInt(
                path + "loot.rotten-flesh-max", 2);

        int fleshAmount = randomInclusive(fleshMin, fleshMax);

        if (fleshAmount > 0) {
            event.getDrops().add(
                    new ItemStack(Material.ROTTEN_FLESH, fleshAmount)
            );
        }

        double nuggetChance = plugin.getConfig().getDouble(
                path + "loot.iron-nugget-chance", 0.25);

        if (ThreadLocalRandom.current().nextDouble() < nuggetChance) {
            int nuggetMin = plugin.getConfig().getInt(
                    path + "loot.iron-nugget-min", 1);
            int nuggetMax = plugin.getConfig().getInt(
                    path + "loot.iron-nugget-max", 2);

            int nuggetAmount = randomInclusive(nuggetMin, nuggetMax);

            if (nuggetAmount > 0) {
                event.getDrops().add(
                        new ItemStack(Material.IRON_NUGGET, nuggetAmount)
                );
            }
        }

        event.setDroppedExp(
                plugin.getConfig().getInt(path + "xp", 5)
        );
    }

    private int randomInclusive(int min, int max) {
        if (max < min) {
            return min;
        }

        return ThreadLocalRandom.current().nextInt(min, max + 1);
    }
}