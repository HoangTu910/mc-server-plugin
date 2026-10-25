package com.example.survival.hud;

import com.example.survival.mana.ManaService;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public final class HudListener implements Listener {

    private final ManaService manaService;
    private final HudService hudService;

    public HudListener(ManaService manaService, HudService hudService) {
        this.manaService = manaService;
        this.hudService = hudService;
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        // Without this, both maps keep an entry for every player who ever joined.
        manaService.reset(event.getPlayer());
        hudService.clear(event.getPlayer());
    }
}
