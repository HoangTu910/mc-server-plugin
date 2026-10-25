
package com.example.survival.customitem;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class CustomItemService {

    private final JavaPlugin plugin;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();
    private final NamespacedKey customItemIdKey;

    public CustomItemService(JavaPlugin plugin) {
        this.plugin = plugin;
        this.customItemIdKey =
                new NamespacedKey(plugin, "custom_item_id");
    }

    public ItemStack createItem(String itemId) {
        String path = "custom-items." + itemId;

        ConfigurationSection section =
                plugin.getConfig().getConfigurationSection(path);

        if (section == null) {
            return null;
        }

        String materialName = section.getString("material");

        if (materialName == null || materialName.isBlank()) {
            throw new IllegalArgumentException(
                    "Custom item '" + itemId
                            + "' is missing a material."
            );
        }

        Material material = Material.matchMaterial(materialName);

        if (material == null || !material.isItem()) {
            throw new IllegalArgumentException(
                    "Invalid item material: " + materialName
            );
        }

        int amount = section.getInt("amount", 1);

        if (amount < 1 || amount > material.getMaxStackSize()) {
            throw new IllegalArgumentException(
                    "Invalid amount for item: " + itemId
            );
        }

        ItemStack item = new ItemStack(material, amount);
        ItemMeta meta = item.getItemMeta();

        String name = section.getString("name");

        if (name != null) {
            meta.displayName(miniMessage.deserialize(name));
        }

        List<String> loreLines =
                section.getStringList("lore");

        if (!loreLines.isEmpty()) {
            List<Component> lore = new ArrayList<>();

            for (String line : loreLines) {
                lore.add(miniMessage.deserialize(line));
            }

            meta.lore(lore);
        }

        // Custom look from the resource pack, e.g. "survival:storm_caller"
        // -> resourcepack/assets/survival/items/storm_caller.json
        String itemModel = section.getString("item-model");

        if (itemModel != null) {
            NamespacedKey modelKey = NamespacedKey.fromString(itemModel);

            if (modelKey == null) {
                plugin.getLogger().warning(
                        "Invalid item-model for " + itemId + ": " + itemModel
                );
            } else {
                meta.setItemModel(modelKey);
            }
        }

        item.setItemMeta(meta);

        ConfigurationSection enchantments =
                section.getConfigurationSection("enchantments");

        if (enchantments != null) {
            for (String enchantName : enchantments.getKeys(false)) {
                String keyName = enchantName
                        .toLowerCase(Locale.ROOT);

                NamespacedKey key =
                        NamespacedKey.minecraft(keyName);

                Enchantment enchantment =
                        Registry.ENCHANTMENT.get(key);

                if (enchantment == null) {
                    plugin.getLogger().warning(
                            "Unknown enchantment: " + enchantName
                    );
                    continue;
                }

                int level = enchantments.getInt(enchantName);

                if (level < 1) {
                    plugin.getLogger().warning(
                            "Invalid enchantment level for "
                                    + enchantName + ": " + level
                    );
                    continue;
                }

                // Intentionally allows levels beyond normal limits.
                item.addUnsafeEnchantment(enchantment, level);
            }
        }

        item.editPersistentDataContainer(pdc ->
                pdc.set(
                        customItemIdKey,
                        PersistentDataType.STRING,
                        itemId
                )
        );

        return item;
    }

    public boolean isCustomItem(ItemStack item, String itemId) {
        return itemId.equals(getItemId(item));
    }

    /**
     * Returns the custom item id stored on the item, or null for normal items.
     */
    public String getItemId(ItemStack item) {
        if (item == null || item.isEmpty()) {
            return null;
        }

        return item.getPersistentDataContainer()
                .get(customItemIdKey, PersistentDataType.STRING);
    }

    public List<String> getItemIds() {
        ConfigurationSection section =
                plugin.getConfig()
                        .getConfigurationSection("custom-items");

        if (section == null) {
            return List.of();
        }

        return new ArrayList<>(section.getKeys(false));
    }
}
