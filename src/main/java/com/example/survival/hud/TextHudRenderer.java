package com.example.survival.hud;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;

import java.util.function.IntSupplier;

/**
 * Fallback HUD that works without the resource pack: plain text bars,
 * replaced entirely by flash messages.
 */
final class TextHudRenderer implements HudRenderer {

    private final IntSupplier barLength;

    TextHudRenderer(IntSupplier barLength) {
        this.barLength = barLength;
    }

    @Override
    public Component render(HudSnapshot snapshot) {
        if (snapshot.flash() != null) {
            return snapshot.flash();
        }

        return Component.text()
                .append(bar("❤", snapshot.health(), snapshot.maxHealth(), snapshot.healthRatio(), NamedTextColor.RED))
                .append(Component.text("    "))
                .append(bar("✦", snapshot.mana(), snapshot.maxMana(), snapshot.manaRatio(), NamedTextColor.AQUA))
                .build();
    }

    /** Builds a bar like: ❤ ||||||||-- 16/20 */
    private Component bar(String icon, double value, double max, double ratio, TextColor color) {
        int length = barLength.getAsInt();
        int filled = (int) Math.round(ratio * length);

        return Component.text()
                .append(Component.text(icon + " ", color))
                .append(Component.text("|".repeat(filled), color))
                .append(Component.text("|".repeat(length - filled), NamedTextColor.DARK_GRAY))
                .append(Component.text(
                        " " + (int) Math.ceil(value) + "/" + (int) Math.round(max),
                        NamedTextColor.WHITE))
                .build();
    }
}
