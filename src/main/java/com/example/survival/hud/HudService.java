package com.example.survival.hud;

import com.example.survival.config.ConfigService;
import com.example.survival.hud.HudSnapshot.HealthEffect;
import com.example.survival.mana.ManaService;

import net.kyori.adventure.text.Component;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Shows each player's health and mana in the action bar.
 *
 * Other features should call {@link #flash} instead of sendActionBar,
 * so their messages don't fight with the HUD.
 */
public final class HudService {

    private record FlashMessage(Component message, long expiresAt) {}

    private final JavaPlugin plugin;
    private final ConfigService config;
    private final ManaService manaService;
    private final HudRenderer packRenderer = new PackHudRenderer();
    private final HudRenderer textRenderer;
    private final Map<UUID, FlashMessage> flashMessages = new HashMap<>();
    private BukkitTask task;
    private long frame;

    public HudService(JavaPlugin plugin, ConfigService config, ManaService manaService) {
        this.plugin = plugin;
        this.config = config;
        this.manaService = manaService;
        this.textRenderer = new TextHudRenderer(() -> config.hud().barLength());
    }

    /** Starts the repeating task that regenerates mana and redraws the HUD. */
    public void start() {
        // Read once: the timer period is fixed when the task is scheduled.
        int periodTicks = config.hud().updateTicks();

        task = plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            frame++;
            for (Player player : plugin.getServer().getOnlinePlayers()) {
                manaService.regenerate(player, periodTicks);
                player.sendActionBar(render(player));
            }
        }, 0L, periodTicks);
    }

    public void stop() {
        if (task != null) {
            task.cancel();
            task = null;
        }
    }

    /** Restarts the task so a changed {@code hud.update-ticks} takes effect. */
    public void restart() {
        stop();
        start();
    }

    /** Shows a message on the HUD for a short time. */
    public void flash(Player player, Component message, long durationMillis) {
        long expiresAt = System.currentTimeMillis() + durationMillis;
        flashMessages.put(player.getUniqueId(), new FlashMessage(message, expiresAt));
        player.sendActionBar(render(player));
    }

    public void clear(Player player) {
        flashMessages.remove(player.getUniqueId());
    }

    private Component render(Player player) {
        HudRenderer renderer = config.hud().resourcePack() ? packRenderer : textRenderer;
        return renderer.render(snapshot(player));
    }

    private HudSnapshot snapshot(Player player) {
        AttributeInstance maxHealthAttribute = player.getAttribute(Attribute.MAX_HEALTH);
        double maxHealth = maxHealthAttribute != null ? maxHealthAttribute.getValue() : 20.0;

        return new HudSnapshot(
                player.getHealth(),
                maxHealth,
                healthEffect(player),
                manaService.getMana(player),
                manaService.getMaxMana(),
                activeFlash(player),
                frame
        );
    }

    private Component activeFlash(Player player) {
        FlashMessage flash = flashMessages.get(player.getUniqueId());
        if (flash == null) {
            return null;
        }
        if (System.currentTimeMillis() >= flash.expiresAt()) {
            flashMessages.remove(player.getUniqueId());
            return null;
        }
        return flash.message();
    }

    private static HealthEffect healthEffect(Player player) {
        if (player.hasPotionEffect(PotionEffectType.WITHER)) {
            return HealthEffect.WITHERED;
        }
        if (player.hasPotionEffect(PotionEffectType.POISON)) {
            return HealthEffect.POISONED;
        }
        if (player.getFreezeTicks() > 0) {
            return HealthEffect.FROZEN;
        }
        return HealthEffect.NORMAL;
    }
}
