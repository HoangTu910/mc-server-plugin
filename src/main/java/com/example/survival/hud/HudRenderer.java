package com.example.survival.hud;

import net.kyori.adventure.text.Component;

/**
 * Turns a {@link HudSnapshot} into the action bar component sent to the player.
 */
interface HudRenderer {

    Component render(HudSnapshot snapshot);
}
