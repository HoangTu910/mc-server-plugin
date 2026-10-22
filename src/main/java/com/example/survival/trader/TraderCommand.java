package com.example.survival.trader;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandExecutor;
import org.bukkit.entity.Player;

public final class TraderCommand implements CommandExecutor {
    private final TraderService traderService;

    public TraderCommand(TraderService traderService) {
        this.traderService = traderService;
    }

    @Override 
    public boolean onCommand(org.bukkit.command.CommandSender sender, org.bukkit.command.Command command, String label, String[] args) {
        if (!sender.hasPermission("admin")) {
            sender.sendMessage(
                    ChatColor.RED + "You do not have permission."
            );
            return true;
        }

        if (!(sender instanceof Player player)) {
            sender.sendMessage(
                    ChatColor.RED + "This command can only be used in-game."
            );
            return true;
        }

        if (args.length != 2
                || !args[0].equalsIgnoreCase("spawn")) {
            player.sendMessage(
                    ChatColor.YELLOW
                            + "Usage: /trader spawn <trader_id>"
            );
            return true;
        }

        String traderId = args[1];

        try {
            var villager = traderService.spawnTrader(
                    player.getLocation(), traderId
            );

            if (villager == null) {
                player.sendMessage(
                        ChatColor.RED
                                + "Unknown trader: " + traderId
                );
                return true;
            }

            player.sendMessage(
                    ChatColor.GREEN
                            + "Spawned trader: " + traderId
            );
        } catch (RuntimeException exception) {
            player.sendMessage(
                    ChatColor.RED
                            + "Failed to spawn trader. Check server logs."
            );

            exception.printStackTrace();
        }

        return true;
    }
}
