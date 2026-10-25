package com.example.survival.customitem.ability;

import com.example.survival.customitem.CustomItemService;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

/**
 * Right-click with a custom item in the main hand activates its ability, if it has one.
 */
public final class AbilityListener implements Listener {

    private final CustomItemService customItemService;
    private final AbilityService abilityService;

    public AbilityListener(CustomItemService customItemService, AbilityService abilityService) {
        this.customItemService = customItemService;
        this.abilityService = abilityService;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND
                || (event.getAction() != Action.RIGHT_CLICK_AIR
                && event.getAction() != Action.RIGHT_CLICK_BLOCK)) {
            return;
        }

        String itemId = customItemService.getItemId(event.getPlayer().getInventory().getItemInMainHand());
        if (itemId == null) {
            return;
        }

        abilityService.find(itemId).ifPresent(ability -> {
            event.setCancelled(true);
            abilityService.activate(event.getPlayer(), ability);
        });
    }
}
