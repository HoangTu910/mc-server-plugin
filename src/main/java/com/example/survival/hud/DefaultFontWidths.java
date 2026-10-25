package com.example.survival.hud;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

/**
 * Pixel widths of the vanilla default font, measured from the 26.2 ascii.png.
 * Used to center normal text between custom HUD glyphs.
 * Characters not listed (including most accented letters) are 6px wide.
 */
final class DefaultFontWidths {

    private DefaultFontWidths() {
    }

    static int width(Component text) {
        String plain = PlainTextComponentSerializer.plainText().serialize(text);
        int width = 0;
        for (int i = 0; i < plain.length(); i++) {
            width += width(plain.charAt(i));
        }
        return width;
    }

    static int width(char c) {
        return switch (c) {
            case '!', '\'', ',', '.', ':', ';', 'i', '|' -> 2;
            case '`', 'l' -> 3;
            case ' ', '"', '(', ')', '*', 'I', '[', ']', 't', '{', '}' -> 4;
            case '<', '>', 'f', 'k' -> 5;
            case '@', '~' -> 7;
            default -> 6;
        };
    }
}
