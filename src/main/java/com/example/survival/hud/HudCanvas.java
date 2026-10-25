package com.example.survival.hud;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.ShadowColor;
import net.kyori.adventure.text.format.TextColor;

/**
 * Places glyphs at exact pixel positions on one line of text.
 *
 * Text is normally laid out left to right; this tracks the cursor and inserts
 * space glyphs (including negative ones) to jump to each requested x position.
 */
final class HudCanvas {

    private final TextComponent.Builder builder = Component.text();
    private int cursor;

    /** Draws a resource pack glyph with its top-left at x. */
    void glyph(char glyph, int advance, int x, TextColor color, boolean shadow) {
        moveTo(x);
        Component component = Component.text(glyph).font(HudGlyphs.FONT).color(color);
        builder.append(shadow ? component : component.shadowColor(ShadowColor.none()));
        cursor += advance;
    }

    void glyph(char glyph, int advance, int x) {
        glyph(glyph, advance, x, NamedTextColor.WHITE, false);
    }

    /** Draws normal text whose rendered width is already known. */
    void text(Component text, int x, int width) {
        moveTo(x);
        builder.append(text);
        cursor += width;
    }

    /**
     * Finishes the line so its total width is exactly {@code width}.
     * The client centers the action bar, so a fixed width keeps positions fixed.
     */
    Component build(int width) {
        moveTo(width);
        return builder.build();
    }

    private void moveTo(int x) {
        int distance = x - cursor;
        if (distance != 0) {
            builder.append(Component.text(HudGlyphs.space(distance)).font(HudGlyphs.FONT));
            cursor = x;
        }
    }
}
