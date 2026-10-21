package com.example.survival.mobs;
import com.example.survival.SurvivalPlugin;

import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Zombie;
import org.bukkit.persistence.PersistentDataType;

/**
 * A service for managing custom mobs.
 */
public final class MobService {

    private final SurvivalPlugin plugin;
    private final NamespacedKey mobTypeKey;

    public MobService(SurvivalPlugin plugin) {
        this.plugin = plugin;
        this.mobTypeKey = new NamespacedKey(plugin, "custom_mob_type");
    }

    public Zombie spawn(MobType type, Location location) {
        if (type != MobType.SHADOW_ZOMBIE) {
            throw new IllegalArgumentException(
                    "Unsupported mob type: " + type
            );
        }

        String path = "mobs." + type.getId() + ".";

        double maxHealth = plugin.getConfig()
                .getDouble(path + "max-health", 40.0);

        double movementSpeed = plugin.getConfig()
                .getDouble(path + "movement-speed", 0.30);

        Zombie zombie = location.getWorld().spawn(
                location,
                Zombie.class
        );

        zombie.getPersistentDataContainer().set(
                mobTypeKey,
                PersistentDataType.STRING,
                type.getId()
        );

        zombie.setCustomName(type.getDisplayName());
        zombie.setCustomNameVisible(true);
        zombie.setPersistent(true);

        var healthAttribute = zombie.getAttribute(Attribute.MAX_HEALTH);
        if (healthAttribute != null) {
            healthAttribute.setBaseValue(maxHealth);
            zombie.setHealth(maxHealth);
        }

        var speedAttribute = zombie.getAttribute(Attribute.MOVEMENT_SPEED);
        if (speedAttribute != null) {
            speedAttribute.setBaseValue(movementSpeed);
        }

        return zombie;
    }

    public boolean isCustomMob(Entity entity) {
        return entity.getPersistentDataContainer().has(
                mobTypeKey,
                PersistentDataType.STRING
        );
    }

    public MobType getMobType(Entity entity) {
        String id = entity.getPersistentDataContainer().get(
                mobTypeKey,
                PersistentDataType.STRING
        );

        return id == null ? null : MobType.fromId(id);
    }

    public double getAttackDamage(MobType type) {
        return plugin.getConfig().getDouble(
                "mobs." + type.getId() + ".attack-damage",
                6.0
        );
    }

    public org.bukkit.configuration.file.FileConfiguration getPluginConfig() {
        return plugin.getConfig();
    }
}