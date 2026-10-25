package com.example.survival.customitem.ability;

import com.example.survival.hud.HudService;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.RayTraceResult;

import java.util.Optional;

/**
 * Void Slayer: hits the first living entity in a straight line in front of the player.
 */
public final class VoidStrikeAbility implements ItemAbility {

    private final HudService hudService;

    public VoidStrikeAbility(HudService hudService) {
        this.hudService = hudService;
    }

    @Override
    public String itemId() {
        return "void_slayer";
    }

    @Override
    public String displayName() {
        return "Void Strike";
    }

    @Override
    public Optional<Runnable> prepare(Player player, ConfigurationSection settings) {
        // A miss still uses the ability, so it can always fire.
        return Optional.of(() -> strike(player, settings));
    }

    private void strike(Player player, ConfigurationSection settings) {
        double range = settings.getDouble("range", 12.0);
        double damage = settings.getDouble("damage", 8.0);

        RayTraceResult rayTrace = player.getWorld().rayTraceEntities(
                player.getEyeLocation(),
                player.getEyeLocation().getDirection(),
                range,
                0.2,
                entity -> entity instanceof LivingEntity
                        && entity != player
        );

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
            hudService.flash(player, Component.text("Void Strike missed!"), AbilityService.FLASH_MILLIS);
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
        hudService.flash(
                player,
                Component.text("Void Strike!", NamedTextColor.LIGHT_PURPLE),
                AbilityService.FLASH_MILLIS
        );
    }
}
