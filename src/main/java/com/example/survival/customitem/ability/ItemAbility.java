package com.example.survival.customitem.ability;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.Optional;

/**
 * A right-click ability bound to one custom item.
 *
 * Cooldown and mana are handled by {@link AbilityService} using
 * {@code cooldown-seconds} and {@code mana-cost} from the item's
 * {@code ability:} section in config.yml.
 */
public interface ItemAbility {

    /** Custom item id (key under {@code custom-items} in config.yml) that triggers this ability. */
    String itemId();

    /** Name shown in messages, e.g. "Void Strike". */
    String displayName();

    /**
     * Checks whether the ability can be used right now and returns the action that performs it.
     * Runs before mana is spent, so returning empty (e.g. no target in range) costs nothing.
     *
     * @param settings the item's {@code ability:} section from config.yml
     */
    Optional<Runnable> prepare(Player player, ConfigurationSection settings);

    /** Removes anything the ability left in the world, called when the plugin disables. */
    default void shutdown() {
    }
}
