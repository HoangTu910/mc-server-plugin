package com.example.survival.customitem.ability;

import com.example.survival.customitem.CustomItemService;
import com.example.survival.hud.HudService;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.RayTraceResult;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Storm Caller: a giant spectral hammer slams down where the player aims and
 * calls lightning bolts that set the area on fire; the first bolt jumps to nearby mobs.
 */
public final class LightningStrikeAbility implements ItemAbility {

    private static final String ITEM_ID = "storm_caller";
    /** How close a mob must be to the impact point to take the main hit. */
    private static final double IMPACT_RADIUS = 2.5;
    private static final long CHAIN_DELAY_TICKS = 3;

    private final JavaPlugin plugin;
    private final CustomItemService customItemService;
    private final HudService hudService;
    private final StormHammerAnimation animation;

    public LightningStrikeAbility(JavaPlugin plugin, CustomItemService customItemService, HudService hudService) {
        this.plugin = plugin;
        this.customItemService = customItemService;
        this.hudService = hudService;
        this.animation = new StormHammerAnimation(plugin);
    }

    @Override
    public String itemId() {
        return ITEM_ID;
    }

    @Override
    public String displayName() {
        return "Lightning Strike";
    }

    @Override
    public Optional<Runnable> prepare(Player player, ConfigurationSection settings) {
        StrikeTarget target = findTarget(player, settings.getDouble("range", 20.0));

        if (target == null) {
            hudService.flash(player, Component.text("No target in range", NamedTextColor.GRAY), AbilityService.FLASH_MILLIS);
            return Optional.empty();
        }

        return Optional.of(() -> cast(player, target, settings));
    }

    @Override
    public void shutdown() {
        animation.removeAll();
    }

    private void cast(Player player, StrikeTarget target, ConfigurationSection settings) {
        double damage = settings.getDouble("damage", 15.0);
        int strikes = Math.max(1, settings.getInt("lightning-strikes", 2));
        long strikeInterval = Math.max(1, settings.getLong("strike-interval-ticks", 6));
        int chainCount = settings.getInt("chain-count", 3);
        double chainRadius = settings.getDouble("chain-radius", 6.0);
        double chainMultiplier = settings.getDouble("chain-damage-multiplier", 0.6);
        Fire fire = new Fire(
                settings.getInt("burn-seconds", 4) * 20,
                settings.getBoolean("place-fire", true),
                settings.getDouble("fire-radius", 2.0)
        );

        hudService.flash(player, Component.text("Lightning Strike!", NamedTextColor.AQUA), AbilityService.FLASH_MILLIS);

        ItemStack hammer = customItemService.createItem(ITEM_ID);

        animation.play(player, hammer, target::location, impact -> {
            // First bolt lands on the hammer and starts the chain.
            LivingEntity primary = bolt(player, impact, target.entity(), damage, fire);

            Set<UUID> hit = new HashSet<>();
            Location chainStart = impact.clone().add(0, 1, 0);
            if (primary != null) {
                hit.add(primary.getUniqueId());
                chainStart = primary.getLocation().add(0, primary.getHeight() / 2, 0);
            }
            chain(player, chainStart, hit, damage * chainMultiplier, chainCount, chainRadius, chainMultiplier);

            // Follow-up bolts land a moment later, slightly scattered around the impact.
            for (int i = 1; i < strikes; i++) {
                plugin.getServer().getScheduler().runTaskLater(
                        plugin,
                        () -> bolt(player, scatter(impact, 1.5), primary, damage, fire),
                        i * strikeInterval
                );
            }
        });
    }

    /**
     * One lightning bolt: hits {@code preferred} if it's still close, otherwise the
     * nearest mob, sets it on fire and lights the ground around the bolt.
     *
     * @return the mob that was hit, or null
     */
    private LivingEntity bolt(Player player, Location at, LivingEntity preferred, double damage, Fire fire) {
        if (!player.isOnline()) {
            return null;
        }

        at.getWorld().strikeLightningEffect(at);

        LivingEntity victim = preferred != null
                && preferred.isValid()
                && preferred.getWorld().equals(at.getWorld())
                && preferred.getLocation().distance(at) <= IMPACT_RADIUS
                ? preferred
                : nearestTarget(player, at, IMPACT_RADIUS, Set.of());

        if (victim != null) {
            victim.damage(damage, player);
            victim.setFireTicks(Math.max(victim.getFireTicks(), fire.burnTicks()));
        }

        if (fire.placeFire()) {
            igniteGround(at, fire.radius());
        }
        return victim;
    }

    /** Puts fire on roughly half the open ground blocks within {@code radius} of the bolt. */
    private static void igniteGround(Location center, double radius) {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        int r = (int) Math.ceil(radius);

        for (int dx = -r; dx <= r; dx++) {
            for (int dz = -r; dz <= r; dz++) {
                if (dx * dx + dz * dz > radius * radius || random.nextBoolean()) {
                    continue;
                }
                // Look a little above and below the impact height for the ground surface.
                for (int dy = 2; dy >= -2; dy--) {
                    Block block = center.getBlock().getRelative(dx, dy, dz);
                    if (block.getType().isAir() && block.getRelative(BlockFace.DOWN).getType().isSolid()) {
                        block.setType(Material.FIRE);
                        break;
                    }
                }
            }
        }
    }

    private static Location scatter(Location center, double spread) {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        return center.clone().add(random.nextDouble(-spread, spread), 0, random.nextDouble(-spread, spread));
    }

    /** Lightning jumps from {@code from} to the nearest mob not hit yet, one hop every few ticks. */
    private void chain(
            Player player,
            Location from,
            Set<UUID> hit,
            double damage,
            int remaining,
            double radius,
            double multiplier
    ) {
        if (remaining <= 0 || !player.isOnline()) {
            return;
        }

        LivingEntity next = nearestTarget(player, from, radius, hit);
        if (next == null) {
            return;
        }

        Location to = next.getLocation().add(0, next.getHeight() / 2, 0);
        animation.arc(from, to);
        next.getWorld().playSound(to, Sound.ENTITY_LIGHTNING_BOLT_IMPACT, 0.8f, 1.6f);
        next.damage(damage, player);
        hit.add(next.getUniqueId());

        plugin.getServer().getScheduler().runTaskLater(
                plugin,
                () -> chain(player, to, hit, damage * multiplier, remaining - 1, radius, multiplier),
                CHAIN_DELAY_TICKS
        );
    }

    private static StrikeTarget findTarget(Player player, double range) {
        Location eye = player.getEyeLocation();
        RayTraceResult result = player.getWorld().rayTrace(
                eye,
                eye.getDirection(),
                range,
                FluidCollisionMode.NEVER,
                true,
                0.6,
                entity -> isValidTarget(player, entity)
        );

        if (result == null) {
            return null;
        }
        if (result.getHitEntity() instanceof LivingEntity entity) {
            return new StrikeTarget(entity, entity.getLocation());
        }
        return new StrikeTarget(null, result.getHitPosition().toLocation(player.getWorld()));
    }

    private static LivingEntity nearestTarget(Player player, Location center, double radius, Set<UUID> exclude) {
        return center.getWorld().getNearbyEntities(center, radius, radius, radius).stream()
                .filter(entity -> isValidTarget(player, entity))
                .filter(entity -> !exclude.contains(entity.getUniqueId()))
                .map(LivingEntity.class::cast)
                .filter(entity -> entity.getLocation().distanceSquared(center) <= radius * radius)
                .min(Comparator.comparingDouble(entity -> entity.getLocation().distanceSquared(center)))
                .orElse(null);
    }

    private static boolean isValidTarget(Player caster, Entity entity) {
        return entity instanceof LivingEntity living
                && entity != caster
                && !(entity instanceof ArmorStand)
                && living.isValid()
                && !living.isDead();
    }

    /** Fire settings from config.yml. */
    private record Fire(int burnTicks, boolean placeFire, double radius) {}

    /**
     * Where the lightning lands: follows the aimed mob while the animation plays,
     * or stays on the aimed block if there was no mob or it died.
     */
    private record StrikeTarget(LivingEntity entity, Location fallback) {

        Location location() {
            return entity != null && entity.isValid() ? entity.getLocation() : fallback;
        }
    }
}
