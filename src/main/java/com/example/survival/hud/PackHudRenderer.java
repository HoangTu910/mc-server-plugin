package com.example.survival.hud;

import com.example.survival.hud.HudSnapshot.HealthEffect;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;

/**
 * Draws image bars from the resource pack: health where the vanilla hearts
 * were, mana directly above it, and flash messages on the normal
 * action bar line.
 */
final class PackHudRenderer implements HudRenderer {

    /** Same width as the hotbar, so the line lines up with it when centered. */
    private static final int WIDTH = 182;
    private static final int HEALTH_X = 0;
    // Same x as health: the mana row sits one row higher (see hud.json ascents), directly above it.
    private static final int MANA_X = HEALTH_X;

    private static final double LOW_HEALTH = 0.25;

    private record Gradient(TextColor from, TextColor to) {

        TextColor at(float t) {
            return TextColor.lerp(t, from, to);
        }

        Gradient brighter() {
            return new Gradient(TextColor.lerp(0.35f, from, NamedTextColor.WHITE),
                    TextColor.lerp(0.35f, to, NamedTextColor.WHITE));
        }
    }

    private static final Gradient HEALTH = new Gradient(TextColor.color(0x9B1515), TextColor.color(0xFF5A5A));
    private static final Gradient POISONED = new Gradient(TextColor.color(0x2E7D32), TextColor.color(0x9CCC65));
    private static final Gradient WITHERED = new Gradient(TextColor.color(0x262626), TextColor.color(0x6B6B6B));
    private static final Gradient FROZEN = new Gradient(TextColor.color(0x4FA3D9), TextColor.color(0xC6ECFF));
    private static final Gradient MANA = new Gradient(TextColor.color(0x1E40AF), TextColor.color(0x38D5F8));

    @Override
    public Component render(HudSnapshot snapshot) {
        HudCanvas canvas = new HudCanvas();

        if (snapshot.flash() != null) {
            int width = DefaultFontWidths.width(snapshot.flash());
            canvas.text(snapshot.flash(), Math.max(0, (WIDTH - width) / 2), width);
        }

        Gradient health = healthGradient(snapshot.healthEffect());
        // Blink every other update when health is low.
        if (snapshot.healthRatio() < LOW_HEALTH && snapshot.frame() % 2 == 0) {
            health = health.brighter();
        }

        drawBar(canvas, HEALTH_X, HudGlyphs.HEALTH_FRAME, HudGlyphs.HEALTH_FILL, HudGlyphs.HEALTH_DIGITS,
                snapshot.health(), snapshot.maxHealth(), snapshot.healthRatio(), health);
        drawBar(canvas, MANA_X, HudGlyphs.MANA_FRAME, HudGlyphs.MANA_FILL, HudGlyphs.MANA_DIGITS,
                snapshot.mana(), snapshot.maxMana(), snapshot.manaRatio(), MANA);

        return canvas.build(WIDTH);
    }

    private static Gradient healthGradient(HealthEffect effect) {
        return switch (effect) {
            case POISONED -> POISONED;
            case WITHERED -> WITHERED;
            case FROZEN -> FROZEN;
            case NORMAL -> HEALTH;
        };
    }

    private static void drawBar(
            HudCanvas canvas,
            int x,
            char frame,
            char fill,
            char digits,
            double value,
            double max,
            double ratio,
            Gradient gradient
    ) {
        canvas.glyph(frame, HudGlyphs.FRAME_ADVANCE, x);

        int innerX = x + HudGlyphs.INNER_OFFSET;
        int filled = (int) Math.round(ratio * HudGlyphs.INNER_WIDTH);

        // One tinted 1px column per pixel gives a smooth gradient from a single texture.
        for (int i = 0; i < filled; i++) {
            float t = (float) i / (HudGlyphs.INNER_WIDTH - 1);
            canvas.glyph(fill, HudGlyphs.FILL_ADVANCE, innerX + i, gradient.at(t), false);
        }

        String label = (int) Math.ceil(value) + "/" + (int) Math.round(max);
        int labelWidth = label.length() * HudGlyphs.DIGIT_ADVANCE - 1;
        int labelX = innerX + (HudGlyphs.INNER_WIDTH - labelWidth) / 2;

        for (int i = 0; i < label.length(); i++) {
            canvas.glyph(HudGlyphs.digit(digits, label.charAt(i)), HudGlyphs.DIGIT_ADVANCE,
                    labelX + i * HudGlyphs.DIGIT_ADVANCE, NamedTextColor.WHITE, true);
        }
    }
}
