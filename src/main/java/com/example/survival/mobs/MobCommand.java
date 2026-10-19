package com.example.survival.mobs;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class MobCommand implements CommandExecutor {

    private final MobService mobService;

    public MobCommand(MobService mobService) {
        this.mobService = mobService;
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

        if (args.length != 2
                || !args[0].equalsIgnoreCase("spawn")) {
            player.sendMessage(
                    "Usage: /mob spawn shadow_zombie"
            );
            return true;
        }

        MobType type = MobType.fromId(args[1]);

        if (type == null) {
            player.sendMessage("Unknown mob type: " + args[1]);
            return true;
        }

        Location spawnLocation = player.getLocation()
                .add(player.getLocation().getDirection().multiply(2));

        mobService.spawn(type, spawnLocation);

        player.sendMessage(
                "Spawned " + type.getDisplayName() + "!"
        );

        return true;
    }
}