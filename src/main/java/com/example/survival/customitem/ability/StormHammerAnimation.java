package com.example.survival.customitem.ability;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Display;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Transformation;
import org.bukkit.util.Vector;
import org.joml.AxisAngle4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Visuals for the lightning ability. Timeline, in ticks (20 ticks = 1 second):
 *
 * <pre>
 *  0-9   charge: sparks spiral up around the caster, a shrinking ring marks the target,
 *        a giant hammer grows and spins high above the target
 *  10    slam: the hammer drops onto the target (client-side smooth movement)
 *  13    impact: lightning, shockwave ring, damage (via the onImpact callback)
 *  13-20 shockwave ring expands
 *  20    hammer fades out, removed at 26
 * </pre>
 *
 * The hammer is an {@link ItemDisplay}: an entity that only renders an item, which the
 * client can scale, rotate and move smoothly between states we send.
 */
final class StormHammerAnimation {

    private static final int SLAM_TICK = 10;
    private static final int SLAM_DURATION = 3;
    private static final int IMPACT_TICK = SLAM_TICK + SLAM_DURATION;
    private static final int FADE_TICK = 20;
    private static final int FADE_DURATION = 6;
    private static final int END_TICK = FADE_TICK + FADE_DURATION;

    private static final float HAMMER_SCALE = 4f;
    private static final double HOVER_HEIGHT = 12;
    /**
     * Hammer center height above the impact point once it has landed. At scale 4 the
     * upside-down head face sits ~2.6 blocks below the center, and the crystal ~3.3,
     * so the crystal sinks into the ground and the head rests just above it.
     */
    private static final double LANDED_HEIGHT = 2.7;

    private static final Color STORM_CYAN = Color.fromRGB(0x7DD3FC);
    private static final Color STORM_WHITE = Color.fromRGB(0xF0FBFF);

    private final JavaPlugin plugin;
    private final Set<ItemDisplay> activeHammers = new HashSet<>();

    StormHammerAnimation(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    void play(Player caster, ItemStack hammerItem, Supplier<Location> target, Consumer<Location> onImpact) {
        Location start = target.get();
        World world = start.getWorld();
        ItemDisplay hammer = spawnHammer(start.clone().add(0, HOVER_HEIGHT, 0), hammerItem);

        world.playSound(caster.getLocation(), Sound.BLOCK_BEACON_POWER_SELECT, 1f, 1.6f);
        world.playSound(start, Sound.ITEM_TRIDENT_THUNDER, 1.2f, 1.2f);

        // Mutable state shared with the task below; arrays because lambdas need effectively-final variables.
        int[] tick = {0};
        Location[] impact = {null};

        plugin.getServer().getScheduler().runTaskTimer(plugin, (BukkitTask task) -> {
            int t = tick[0]++;

            if (!hammer.isValid() || t > END_TICK) {
                remove(hammer);
                task.cancel();
                return;
            }

            if (t == 1) {
                // Grow and spin into view. Must be sent a tick after spawning so the client interpolates.
                animate(hammer, hammerTransform(HAMMER_SCALE, 120f), SLAM_TICK - 1);
            }

            if (t < SLAM_TICK) {
                Location aim = target.get();
                chargeSparks(caster, t);
                targetRing(aim, 2.5 - t * 0.2);
                hammer.teleport(aim.clone().add(0, HOVER_HEIGHT, 0));
            }

            if (t == SLAM_TICK) {
                impact[0] = target.get().clone();
                hammer.teleport(impact[0].clone().add(0, LANDED_HEIGHT, 0));
                world.playSound(impact[0], Sound.ITEM_TRIDENT_RIPTIDE_3, 1f, 0.8f);
            }

            if (t > SLAM_TICK && t < IMPACT_TICK) {
                trail(hammer.getLocation());
            }

            if (t == IMPACT_TICK) {
                world.playSound(impact[0], Sound.ITEM_MACE_SMASH_GROUND_HEAVY, 1.5f, 0.7f);
                world.spawnParticle(Particle.EXPLOSION, impact[0], 3, 0.5, 0.2, 0.5, 0);
                onImpact.accept(impact[0]);
            }

            if (t >= IMPACT_TICK && t <= IMPACT_TICK + 7) {
                shockwave(impact[0], 0.6 + (t - IMPACT_TICK) * 0.55);
            }

            if (t == FADE_TICK) {
                animate(hammer, hammerTransform(0f, 120f), FADE_DURATION);
            }
        }, 0L, 1L);
    }

    /** Jagged particle bolt between two points, used for chain lightning. */
    void arc(Location from, Location to) {
        World world = from.getWorld();
        ThreadLocalRandom random = ThreadLocalRandom.current();
        int segments = 6;
        Vector previous = from.toVector();

        for (int i = 1; i <= segments; i++) {
            Vector point = from.toVector().add(to.toVector().subtract(from.toVector()).multiply((double) i / segments));
            if (i < segments) {
                point.add(new Vector(random.nextGaussian(), random.nextGaussian(), random.nextGaussian()).multiply(0.25));
            }

            Vector step = point.clone().subtract(previous);
            int dots = Math.max(1, (int) (step.length() / 0.15));
            for (int d = 0; d < dots; d++) {
                Vector at = previous.clone().add(step.clone().multiply((double) d / dots));
                world.spawnParticle(Particle.ELECTRIC_SPARK, at.getX(), at.getY(), at.getZ(), 1, 0, 0, 0, 0);
            }
            previous = point;
        }
        world.spawnParticle(Particle.END_ROD, to, 6, 0.2, 0.3, 0.2, 0.02);
    }

    void removeAll() {
        activeHammers.forEach(Display::remove);
        activeHammers.clear();
    }

    private ItemDisplay spawnHammer(Location location, ItemStack item) {
        ItemDisplay hammer = location.getWorld().spawn(location, ItemDisplay.class, display -> {
            display.setItemStack(item);
            display.setTransformation(hammerTransform(0.01f, 0f));
            display.setBrightness(new Display.Brightness(15, 15));
            display.setGlowing(true);
            display.setGlowColorOverride(STORM_CYAN);
            display.setTeleportDuration(SLAM_DURATION);
            display.setViewRange(2f);
            // Never saved to the world file, in case the server stops mid-animation.
            display.setPersistent(false);
        });
        activeHammers.add(hammer);
        return hammer;
    }

    private void remove(ItemDisplay hammer) {
        activeHammers.remove(hammer);
        hammer.remove();
    }

    /**
     * The hammer model stands upright with the head on top. Rotating 180° around Z turns it
     * head-down for the slam; the Y rotation spins it around its vertical axis.
     */
    private static Transformation hammerTransform(float scale, float spinDegrees) {
        Quaternionf rotation = new Quaternionf()
                .rotateY((float) Math.toRadians(spinDegrees))
                .rotateZ((float) Math.toRadians(180));
        return new Transformation(
                new Vector3f(),
                rotation,
                new Vector3f(scale, scale, scale),
                new Quaternionf(new AxisAngle4f())
        );
    }

    private static void animate(ItemDisplay display, Transformation transformation, int durationTicks) {
        display.setInterpolationDelay(0);
        display.setInterpolationDuration(durationTicks);
        display.setTransformation(transformation);
    }

    private static void chargeSparks(Player caster, int tick) {
        Location base = caster.getLocation();
        for (int strand = 0; strand < 2; strand++) {
            double angle = tick * 0.6 + strand * Math.PI;
            double x = Math.cos(angle) * 0.8;
            double z = Math.sin(angle) * 0.8;
            caster.getWorld().spawnParticle(Particle.ELECTRIC_SPARK,
                    base.getX() + x, base.getY() + tick * 0.2, base.getZ() + z, 2, 0.02, 0.02, 0.02, 0);
        }
    }

    private static void targetRing(Location center, double radius) {
        Particle.DustOptions dust = new Particle.DustOptions(STORM_CYAN, 1.2f);
        ring(center, Math.max(0.3, radius), 24, Particle.DUST, dust);
    }

    private static void shockwave(Location center, double radius) {
        Particle.DustTransition dust = new Particle.DustTransition(STORM_WHITE, STORM_CYAN, 1.5f);
        ring(center, radius, (int) (radius * 14), Particle.DUST_COLOR_TRANSITION, dust);
        ring(center, radius, (int) (radius * 4), Particle.ELECTRIC_SPARK, null);
    }

    private static void trail(Location hammerCenter) {
        hammerCenter.getWorld().spawnParticle(Particle.END_ROD, hammerCenter, 8, 0.15, 1.5, 0.15, 0.01);
    }

    private static <T> void ring(Location center, double radius, int points, Particle particle, T data) {
        World world = center.getWorld();
        for (int i = 0; i < points; i++) {
            double angle = 2 * Math.PI * i / points;
            world.spawnParticle(particle,
                    center.getX() + Math.cos(angle) * radius,
                    center.getY() + 0.15,
                    center.getZ() + Math.sin(angle) * radius,
                    1, 0, 0, 0, 0, data);
        }
    }
}
