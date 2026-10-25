package com.example.survival.customitem.ability;

import com.example.survival.hud.HudService;
import com.example.survival.mana.ManaService;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Runs item abilities with the rules every ability shares:
 * cooldown check, then the ability's own check, then mana.
 */
public final class AbilityService {

    static final long FLASH_MILLIS = 1500;

    private final JavaPlugin plugin;
    private final ManaService manaService;
    private final HudService hudService;
    private final Map<String, ItemAbility> abilities = new HashMap<>();
    // player -> item id -> time the ability is available again (epoch millis)
    private final Map<UUID, Map<String, Long>> cooldowns = new HashMap<>();

    public AbilityService(JavaPlugin plugin, ManaService manaService, HudService hudService) {
        this.plugin = plugin;
        this.manaService = manaService;
        this.hudService = hudService;
    }

    public void register(ItemAbility ability) {
        abilities.put(ability.itemId(), ability);
    }

    public Optional<ItemAbility> find(String itemId) {
        return Optional.ofNullable(abilities.get(itemId));
    }

    public void activate(Player player, ItemAbility ability) {
        ConfigurationSection settings = settings(ability);
        Map<String, Long> playerCooldowns = cooldowns.computeIfAbsent(player.getUniqueId(), id -> new HashMap<>());

        long now = System.currentTimeMillis();
        long availableAt = playerCooldowns.getOrDefault(ability.itemId(), 0L);

        if (now < availableAt) {
            long remainingSeconds = (availableAt - now + 999) / 1000;
            hudService.flash(
                    player,
                    Component.text(ability.displayName() + " Cooldown " + remainingSeconds + " giây."),
                    FLASH_MILLIS
            );
            return;
        }

        Optional<Runnable> cast = ability.prepare(player, settings);
        if (cast.isEmpty()) {
            return;
        }

        // Checked after the cooldown and the ability's own check, so failed clicks don't spend mana.
        if (!manaService.tryConsume(player, settings.getDouble("mana-cost", 0))) {
            hudService.flash(player, Component.text("Not enough mana!", NamedTextColor.BLUE), FLASH_MILLIS);
            return;
        }

        long cooldownMillis = (long) (settings.getDouble("cooldown-seconds", 0) * 1000);
        playerCooldowns.put(ability.itemId(), now + cooldownMillis);

        cast.get().run();
    }

    public void shutdown() {
        abilities.values().forEach(ItemAbility::shutdown);
    }

    private ConfigurationSection settings(ItemAbility ability) {
        ConfigurationSection section = plugin.getConfig()
                .getConfigurationSection("custom-items." + ability.itemId() + ".ability");
        return section != null ? section : new MemoryConfiguration();
    }
}
