package com.example.survival.mana;

import com.example.survival.config.ConfigService;

import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Tracks each player's mana. Kept in memory only; players start full on join.
 */
public final class ManaService {

    private final ConfigService config;
    // Only touched from the main server thread, so a plain HashMap is fine.
    private final Map<UUID, Double> mana = new HashMap<>();

    public ManaService(ConfigService config) {
        this.config = config;
    }

    public double getMaxMana() {
        return config.mana().max();
    }

    public double getMana(Player player) {
        // A player with no entry yet has full mana.
        double current = mana.getOrDefault(player.getUniqueId(), getMaxMana());
        return Math.min(current, getMaxMana());
    }

    /**
     * Spends mana if the player has enough.
     *
     * @return true if the mana was spent, false if the player has too little
     */
    public boolean tryConsume(Player player, double amount) {
        double current = getMana(player);
        if (current < amount) {
            return false;
        }
        mana.put(player.getUniqueId(), current - amount);
        return true;
    }

    /** Regenerates mana for a time span given in server ticks (20 ticks = 1 second). */
    public void regenerate(Player player, int ticks) {
        restore(player, config.mana().regenPerSecond() * ticks / 20.0);
    }

    public void restore(Player player, double amount) {
        mana.put(player.getUniqueId(), Math.min(getMaxMana(), getMana(player) + amount));
    }

    public void reset(Player player) {
        mana.remove(player.getUniqueId());
    }
}
