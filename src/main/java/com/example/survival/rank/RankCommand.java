package com.example.survival.rank;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;


public final class RankCommand implements CommandExecutor{
    private final RankService rankService;

    public RankCommand(RankService rankService) {
        this.rankService = rankService;
    }

    @Override
    public boolean onCommand(
            CommandSender sender,
            Command command,
            String label,
            String[] args
    ) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("This command must be run by a player.");
            return true;
        }

        if(args.length != 0) {
            player.sendMessage("Usage: /rank");
            return true;
        }

        player.sendMessage(
                "Rank: " + rankService.getRank(player).getDisplayName()
              + "\nTotal XP: " + rankService.getPlayerProgress(player).getTotalXp()
              + "\nLevel: " + rankService.getPlayerProgress(player).getLevel()
              + "\nNext Rank: " + rankService.getNextRank(player).getDisplayName()
              + "\n"
        );

        return true;
    }
}
