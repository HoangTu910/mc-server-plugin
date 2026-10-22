package com.example.survival.trader;


import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Villager;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MerchantRecipe;
import org.bukkit.persistence.PersistentDataType;

import com.example.survival.SurvivalMainPlugin;
import com.example.survival.customitem.CustomItemService;

import net.kyori.adventure.text.minimessage.MiniMessage;

public final class TraderService {
    private final SurvivalMainPlugin plugin;
    private final CustomItemService customItemService;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();
    private final NamespacedKey traderIdKey;

    public TraderService(SurvivalMainPlugin plugin, CustomItemService customItemService) {
        this.plugin = plugin;
        this.customItemService = customItemService;
        this.traderIdKey = new NamespacedKey(plugin, "trader_id");
    }

    public Villager spawnTrader(Location location, String traderId) {
        String path = "traders." + traderId;
        ConfigurationSection section = plugin.getConfig().getConfigurationSection(path);
        if (section == null) {
            throw new IllegalArgumentException("Trader not found: " + traderId);
        }
        Villager villager = (Villager) location.getWorld().spawnEntity(location, EntityType.VILLAGER);

        villager.setCustomNameVisible(true);
        villager.setAI(false);
        villager.setInvulnerable(true);
        villager.setProfession(parseProfession(
                section.getString("profession", "NONE")
        ));
        String name = section.getString("name", "<gold>Trader");
        if (name == null || name.isBlank()) {
            name = "<gold>Trader";
        }
        villager.customName(miniMessage.deserialize(name));
        villager.getPersistentDataContainer().set(
                        traderIdKey,
                        PersistentDataType.STRING,
                        traderId);
        villager.setRecipes(createRecipes(section));
        return villager;
    }

    private Villager.Profession parseProfession(String value) {
        try {
            return Villager.Profession.valueOf(
                    value.toUpperCase(Locale.ROOT)
            );
        } catch (IllegalArgumentException exception) {
            return Villager.Profession.NONE;
        }
    }

    private ItemStack createTradeItem(
        ConfigurationSection section,
        String tradeId,
        String side
    ) {
        if (section == null) {
            return null;
        }
        String customItemId = section.getString("custom-item");
        ItemStack item;

        if (customItemId != null) {
            item = customItemService.createItem(customItemId);

            if (item == null) {
                plugin.getLogger().warning(
                        "Unknown custom item in " + side
                                + " of trade: " + tradeId
                );
                return null;
            }
        } else {
            String materialName = section.getString("material", "STONE");
            Material material = Material.matchMaterial(materialName);

            if (material == null || !material.isItem()) {
                plugin.getLogger().warning(
                        "Invalid material in " + side
                                + " of trade: " + tradeId
                );
                return null;
            }

            item = new ItemStack(material);
        }

        int amount = section.getInt("amount", 1);

        if (amount < 1 || amount > item.getMaxStackSize()) {
            plugin.getLogger().warning(
                    "Invalid amount in " + side
                            + " of trade: " + tradeId
            );
            return null;
        }

        item.setAmount(amount);
        return item;
    }

    private List<MerchantRecipe> createRecipes(
        ConfigurationSection traderSection
    ) {
        List<MerchantRecipe> recipes = new ArrayList<>();

        ConfigurationSection trades =
                traderSection.getConfigurationSection("trades");

        if (trades == null) {
            return recipes;
        }

        for (String tradeId : trades.getKeys(false)) {
            ConfigurationSection trade =
                    trades.getConfigurationSection(tradeId);

            if (trade == null) {
                continue;
            }

            ConfigurationSection input =
                    trade.getConfigurationSection("input");
            ConfigurationSection output =
                    trade.getConfigurationSection("output");

            if (input == null || output == null) {
                plugin.getLogger().warning(
                        "Invalid trade config: " + tradeId
                );
                continue;
            }

            ItemStack inputItem =
                    createTradeItem(input, tradeId, "input");

            ItemStack result =
                    createTradeItem(output, tradeId, "output");

            if (inputItem == null || result == null) {
                continue;
            }

            int maxUses = Math.max(
                    1, trade.getInt("max-uses", 999999)
            );

            MerchantRecipe recipe = new MerchantRecipe(result, maxUses);
            recipe.addIngredient(inputItem);

            recipes.add(recipe);
        }

        return recipes;
    }

    public boolean isCustomTrader(Villager villager) {
        return villager.getPersistentDataContainer().has(
                traderIdKey,
                PersistentDataType.STRING
        );
    }

}
