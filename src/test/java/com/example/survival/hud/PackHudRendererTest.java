package com.example.survival.hud;

import com.example.survival.hud.HudSnapshot.HealthEffect;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PackHudRendererTest {

    private final PackHudRenderer renderer = new PackHudRenderer();

    @Test
    void spaceGlyphsAddUpToRequestedDistance() {
        for (int pixels = -300; pixels <= 300; pixels++) {
            assertEquals(pixels, spaceWidth(HudGlyphs.space(pixels)), "space(" + pixels + ")");
        }
    }

    @Test
    void lineIsAlwaysHotbarWidth() {
        // The client centers the action bar, so any other width would shift the bars sideways.
        double[][] cases = {{20, 20, 100, 100}, {0, 20, 0, 100}, {7.5, 20, 33, 100}, {137, 140, 999, 1000}};
        for (double[] c : cases) {
            assertEquals(182, width(renderer.render(snapshot(c[0], c[1], c[2], c[3], null))));
        }
    }

    @Test
    void flashMessageKeepsLineWidth() {
        Component flash = Component.text("Void Strike Cooldown 3 giây.");
        assertEquals(182, width(renderer.render(snapshot(10, 20, 50, 100, flash))));
    }

    private static HudSnapshot snapshot(double health, double maxHealth, double mana, double maxMana, Component flash) {
        return new HudSnapshot(health, maxHealth, HealthEffect.NORMAL, mana, maxMana, flash, 0);
    }

    /** Adds up glyph advances the same way the client lays out the line. */
    private static int width(Component component) {
        int width = 0;
        if (component instanceof TextComponent text) {
            String content = text.content();
            if (HudGlyphs.FONT.equals(component.font())) {
                for (char c : content.toCharArray()) {
                    width += hudGlyphAdvance(c);
                }
            } else {
                width += DefaultFontWidths.width(Component.text(content));
            }
        }
        for (Component child : component.children()) {
            width += width(child);
        }
        return width;
    }

    private static int hudGlyphAdvance(char c) {
        if (c == HudGlyphs.HEALTH_FRAME || c == HudGlyphs.MANA_FRAME) {
            return HudGlyphs.FRAME_ADVANCE;
        }
        if (c == HudGlyphs.HEALTH_FILL || c == HudGlyphs.MANA_FILL) {
            return HudGlyphs.FILL_ADVANCE;
        }
        if (c >= HudGlyphs.HEALTH_DIGITS && c <= HudGlyphs.MANA_DIGITS + 10) {
            return HudGlyphs.DIGIT_ADVANCE;
        }
        return spaceWidth(String.valueOf(c));
    }

    private static int spaceWidth(String spaces) {
        int width = 0;
        for (char c : spaces.toCharArray()) {
            if (c >= '' && c <= '') {
                width -= 1 << (c - '');
            } else if (c >= '' && c <= '') {
                width += 1 << (c - '');
            } else {
                throw new IllegalArgumentException("Not a space glyph: " + (int) c);
            }
        }
        return width;
    }
}
