package com.example.survival.rank;

import java.util.Locale;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class SetRankCommand implements CommandExecutor {
    private final RankService rankService;

    public SetRankCommand(RankService rankService) {
        this.rankService = rankService;
    }

    @Override
    public boolean onCommand(
            CommandSender sender,
            Command command,
            String label,
            String[] args
    ) {
        // only admin
        if (!sender.hasPermission("admin")) {
            sender.sendMessage("You don't have permission to use this command.");
            return true;
        }

        if (args.length != 2) {
            sender.sendMessage("Usage: /set-rank <player> <rank>");
            return true;
        }

        Player target = Bukkit.getPlayerExact(args[0]);

        if (target == null) {
            sender.sendMessage("Player is not online.");
            return true;
        }

        Rank rank;

        try {
            rank = Rank.valueOf(args[1].toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            sender.sendMessage("Invalid rank. Available ranks: NEWBIE, ROOKIE, SPECIALIST, EXPERT, MASTER, GRAND_MASTER.");
            return true;
        }

        rankService.setRank(target, rank);

        sender.sendMessage(
                "Set " + target.getName() + "'s rank to " + rank.getDisplayName() + "."
        );

        target.sendMessage(
                "Your rank has been set to " + rank.getDisplayName() + "."
        );

        return true;
    }
}