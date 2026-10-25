package com.example.survival.rank;

import com.example.survival.mobs.MobService;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import io.papermc.paper.event.player.AsyncChatEvent;

public final class RankListener implements Listener {

    private final RankService rankService;
    private final MobService mobService;

    public RankListener(
            RankService rankService,
            MobService mobService
    ) {
        this.rankService = rankService;
        this.mobService = mobService;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Material material = event.getBlock().getType();

        String path = switch (material) {
            case OAK_LOG, BIRCH_LOG, SPRUCE_LOG,
                 JUNGLE_LOG, ACACIA_LOG, DARK_OAK_LOG,
                 MANGROVE_LOG, CHERRY_LOG, PALE_OAK_LOG
                    -> "log";

            case COAL_ORE, DEEPSLATE_COAL_ORE
                    -> "coal-ore";

            case IRON_ORE, DEEPSLATE_IRON_ORE
                    -> "iron-ore";

            case GOLD_ORE, DEEPSLATE_GOLD_ORE, NETHER_GOLD_ORE
                    -> "gold-ore";

            case DIAMOND_ORE, DEEPSLATE_DIAMOND_ORE
                    -> "diamond-ore";

            default -> null;
        };

        if (path == null) {
            return;
        }

        int xp = rankService.getXpReward(path, 0);
        rankService.addXp(player, xp);
    }

    @EventHandler
    public void onMobDeath(EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();
        Player killer = entity.getKiller();

        if (killer == null) {
            return;
        }

        boolean customMob = mobService.isCustomMob(entity);

        int xp = customMob
                ? rankService.getXpReward("custom-mob-kill", 15)
                : rankService.getXpReward("vanilla-mob-kill", 5);

        rankService.addXp(killer, xp);
    }

    @EventHandler
    public void onChat(AsyncChatEvent event) {
        Player player = event.getPlayer();
        PlayerProgress progress = rankService.getPlayerProgress(player);
        Rank rank = progress.getRank();

        Component prefix = Component.text(
                "[" + rank.getDisplayName() + "] ",
                rank.getColor()
        );

        event.renderer((source, displayName, message, viewer) ->
                prefix.append(displayName.color(NamedTextColor.WHITE))
                        .append(Component.text(": ", NamedTextColor.GRAY))
                        .append(message.color(NamedTextColor.WHITE))
        );
    }
}