package com.example.survival.hud;

import net.kyori.adventure.key.Key;

/**
 * Characters defined in the resource pack font {@code survival:hud}.
 *
 * Must stay in sync with resourcepack/assets/survival/font/hud.json
 * and tools/generate_hud_textures.py.
 */
final class HudGlyphs {

    static final Key FONT = Key.key("survival", "hud");

    static final char HEALTH_FRAME = '';
    static final char MANA_FRAME = '';
    static final char HEALTH_FILL = '';
    static final char MANA_FILL = '';
    // Digits 0-9 followed by '/', in two vertical rows.
    static final char HEALTH_DIGITS = '';
    static final char MANA_DIGITS = '';

    // Bitmap glyphs advance by their pixel width + 1.
    static final int FRAME_WIDTH = 81;
    static final int FRAME_ADVANCE = FRAME_WIDTH + 1;
    static final int FILL_ADVANCE = 2;
    static final int DIGIT_ADVANCE = 4;

    // Fill area inside a frame, relative to the frame's left edge.
    static final int INNER_OFFSET = 11;
    static final int INNER_WIDTH = 69;

    // Space glyphs: NEGATIVE_SPACE + i moves left 2^i px, POSITIVE_SPACE + i moves right 2^i px.
    private static final char NEGATIVE_SPACE = '';
    private static final char POSITIVE_SPACE = '';
    private static final int SPACE_STEPS = 8;

    private HudGlyphs() {
    }

    static char digit(char base, char c) {
        return c == '/' ? (char) (base + 10) : (char) (base + (c - '0'));
    }

    /** Builds a string of space glyphs that moves the cursor by {@code pixels}. */
    static String space(int pixels) {
        StringBuilder result = new StringBuilder();
        char base = pixels < 0 ? NEGATIVE_SPACE : POSITIVE_SPACE;
        int remaining = Math.abs(pixels);

        for (int step = SPACE_STEPS - 1; step >= 0; step--) {
            int size = 1 << step;
            while (remaining >= size) {
                result.append((char) (base + step));
                remaining -= size;
            }
        }
        return result.toString();
    }
}
