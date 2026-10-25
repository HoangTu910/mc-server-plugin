package com.example.survival.customitem;

import com.example.survival.SurvivalMainPlugin;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.util.RayTraceResult;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class CustomItemAbilityListener implements Listener {

    private static final String VOID_SLAYER_ID = "void_slayer";
    private final SurvivalMainPlugin plugin;
    private final CustomItemService customItemService;
    private final Map<UUID, Long> cooldowns = new HashMap<>();

    public CustomItemAbilityListener(
            SurvivalMainPlugin plugin,
            CustomItemService customItemService
    ) {
        this.plugin = plugin;
        this.customItemService = customItemService;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND
                || (event.getAction() != Action.RIGHT_CLICK_AIR
                && event.getAction() != Action.RIGHT_CLICK_BLOCK)) {
            return;
        }

        Player player = event.getPlayer();

        if (!customItemService.isCustomItem(
                player.getInventory().getItemInMainHand(),
                VOID_SLAYER_ID
        )) 
        {
            return;
        }

        event.setCancelled(true);

        long now = System.currentTimeMillis();
        long cooldownMillis = plugin.getConfig().getLong(
                "custom-items." + VOID_SLAYER_ID
                        + ".ability.cooldown-seconds",
                3
        ) * 1000L;
        long availableAt = cooldowns.getOrDefault(
                player.getUniqueId(), 0L
        );

        if (now < availableAt) {
            long remainingSeconds = (availableAt - now + 999) / 1000;
            player.sendActionBar(
                    "Void Strike Cooldown " + remainingSeconds + " giây."
            );
            return;
        }

        double range = plugin.getConfig().getDouble(
                "custom-items." + VOID_SLAYER_ID
                        + ".ability.range",
                12.0
        );
        double damage = plugin.getConfig().getDouble(
                "custom-items." + VOID_SLAYER_ID
                        + ".ability.damage",
                8.0
        );

        RayTraceResult rayTrace = player.getWorld().rayTraceEntities(
                player.getEyeLocation(),
                player.getEyeLocation().getDirection(),
                range,
                0.2,
                entity -> entity instanceof LivingEntity
                        && entity != player
        );

        cooldowns.put(player.getUniqueId(), now + cooldownMillis);
        player.getWorld().playSound(
                player.getLocation(),
                Sound.ENTITY_ENDER_DRAGON_GROWL,
                0.7f,
                1.8f
        );

        if (rayTrace == null || rayTrace.getHitEntity() == null) {
            player.getWorld().spawnParticle(
                    Particle.PORTAL,
                    player.getEyeLocation().add(
                            player.getEyeLocation().getDirection().multiply(range)
                    ),
                    30,
                    0.3,
                    0.3,
                    0.3,
                    0.1
            );
            player.sendActionBar("Void Strike missed!");
            return;
        }

        Entity hitEntity = rayTrace.getHitEntity();
        ((LivingEntity) hitEntity).damage(damage, player);
        hitEntity.getWorld().spawnParticle(
                Particle.PORTAL,
                hitEntity.getLocation().add(0, 1, 0),
                40,
                0.4,
                0.7,
                0.4,
                0.15
        );
        player.sendActionBar("Void Strike!");
    }
}
