
package com.example.survival.customitem;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public final class CustomItemCommand
        implements CommandExecutor, TabCompleter {

    private final CustomItemService customItemService;
    private final Runnable reloadConfig;

    public CustomItemCommand(
            CustomItemService customItemService,
            Runnable reloadConfig
    ) {
        this.customItemService = customItemService;
        this.reloadConfig = reloadConfig;
    }

    @Override
    public boolean onCommand(
            CommandSender sender,
            Command command,
            String label,
            String[] args
    ) {
        if (args.length == 0) {
            sender.sendMessage(
                    ChatColor.YELLOW
                            + "Usage: /customitem <give|list|reload>"
            );
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "give" -> {
                if (!sender.hasPermission("customitem.admin")) {
                    sender.sendMessage(
                            ChatColor.RED + "You do not have permission."
                    );
                    return true;
                }

                if (args.length != 3) {
                    sender.sendMessage(
                            ChatColor.YELLOW
                                    + "Usage: /customitem give "
                                    + "<player> <item_id>"
                    );
                    return true;
                }

                Player target = Bukkit.getPlayerExact(args[1]);

                if (target == null) {
                    sender.sendMessage(
                            ChatColor.RED + "Player is not online."
                    );
                    return true;
                }

                String itemId = args[2];
                ItemStack item;

                try {
                    item = customItemService.createItem(itemId);
                } catch (IllegalArgumentException exception) {
                    sender.sendMessage(
                            ChatColor.RED + exception.getMessage()
                    );
                    return true;
                }

                if (item == null) {
                    sender.sendMessage(
                            ChatColor.RED
                                    + "Unknown custom item: " + itemId
                    );
                    return true;
                }

                var leftovers =
                        target.getInventory().addItem(item);

                // Drop remaining items if the inventory is full.
                leftovers.values().forEach(remaining ->
                        target.getWorld().dropItemNaturally(
                                target.getLocation(), remaining
                        )
                );

                sender.sendMessage(
                        ChatColor.GREEN + "Gave " + itemId
                                + " to " + target.getName() + "."
                );

                return true;
            }

            case "list" -> {
                List<String> ids = customItemService.getItemIds();

                sender.sendMessage(
                        ChatColor.GOLD + "Custom items: "
                                + String.join(", ", ids)
                );

                return true;
            }

            case "reload" -> {
                if (!sender.hasPermission("customitem.admin")) {
                    sender.sendMessage(
                            ChatColor.RED + "You do not have permission."
                    );
                    return true;
                }

                reloadConfig.run();

                sender.sendMessage(
                        ChatColor.GREEN
                                + "Custom item configuration reloaded."
                );

                return true;
            }

            default -> {
                sender.sendMessage(
                        ChatColor.YELLOW
                                + "Usage: /customitem <give|list|reload>"
                );
                return true;
            }
        }
    }

    @Override
    public List<String> onTabComplete(
            CommandSender sender,
            Command command,
            String alias,
            String[] args
    ) {
        if (args.length == 1) {
            return List.of("give", "list", "reload");
        }

        if (args.length == 2
                && args[0].equalsIgnoreCase("give")) {
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> name.regionMatches(
                            true, 0, args[1], 0, args[1].length()
                    ))
                    .toList();
        }

        if (args.length == 3
                && args[0].equalsIgnoreCase("give")) {
            return customItemService.getItemIds().stream()
                    .filter(id -> id.regionMatches(
                            true, 0, args[2], 0, args[2].length()
                    ))
                    .toList();
        }

        return new ArrayList<>();
    }
}
