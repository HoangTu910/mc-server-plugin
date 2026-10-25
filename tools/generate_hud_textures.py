"""
Generates the HUD textures used by the resource pack.

Run from the repo root after changing any pixel art below:
    python tools/generate_hud_textures.py

Output: resourcepack/assets/survival/textures/hud/*.png
        resourcepack/assets/minecraft/textures/gui/sprites/hud/heart/*.png (transparent, hides vanilla hearts)
"""

from pathlib import Path

from PIL import Image

ROOT = Path(__file__).resolve().parent.parent
HUD_DIR = ROOT / "resourcepack/assets/survival/textures/hud"
ITEM_DIR = ROOT / "resourcepack/assets/survival/textures/item"
HEART_DIR = ROOT / "resourcepack/assets/minecraft/textures/gui/sprites/hud/heart"

# Vanilla 26.2 heart sprites (all 9x9). Replacing them with transparent images hides the hearts.
VANILLA_HEART_SPRITES = [
    f"{prefix}{kind}{suffix}"
    for prefix in ["", "absorbing_", "frozen_", "poisoned_", "withered_"]
    for kind in ["full", "half", "hardcore_full", "hardcore_half"]
    for suffix in ["", "_blinking"]
] + [
    "container", "container_blinking", "container_hardcore", "container_hardcore_blinking",
    "vehicle_container", "vehicle_full", "vehicle_half",
]


def hex_rgba(value, alpha=255):
    value = value.lstrip("#")
    return tuple(int(value[i:i + 2], 16) for i in (0, 2, 4)) + (alpha,)


def draw_pattern(image, pattern, palette, offset_x=0, offset_y=0):
    for y, row in enumerate(pattern):
        for x, key in enumerate(row):
            if key != ".":
                image.putpixel((offset_x + x, offset_y + y), palette[key])


HEART_ICON = [
    ".oo...oo.",
    "ohro.orro",
    "ohrrorrro",
    "orrrrrrro",
    "orrrrrrdo",
    ".orrrrdo.",
    "..orrdo..",
    "...odo...",
    "....o....",
]
HEART_PALETTE = {
    "o": hex_rgba("3a0a0a"),
    "h": hex_rgba("ffd0d0"),
    "r": hex_rgba("e53935"),
    "d": hex_rgba("8e1b1b"),
}

MANA_ICON = [
    "....o....",
    "...oho...",
    "..ohbbo..",
    ".ohbbbdo.",
    "ohbbbbbdo",
    ".obbbbdo.",
    "..obbdo..",
    "...odo...",
    "....o....",
]
MANA_PALETTE = {
    "o": hex_rgba("0b1a3a"),
    "h": hex_rgba("bfefff"),
    "b": hex_rgba("3b82f6"),
    "d": hex_rgba("1e3a8a"),
}

# 3x5 digits, in glyph order 0-9 then "/".
DIGITS = {
    "0": ["###", "#.#", "#.#", "#.#", "###"],
    "1": [".#.", "##.", ".#.", ".#.", "###"],
    "2": ["###", "..#", "###", "#..", "###"],
    "3": ["###", "..#", ".##", "..#", "###"],
    "4": ["#.#", "#.#", "###", "..#", "..#"],
    "5": ["###", "#..", "###", "..#", "###"],
    "6": ["###", "#..", "###", "#.#", "###"],
    "7": ["###", "..#", "..#", ".#.", ".#."],
    "8": ["###", "#.#", "###", "#.#", "###"],
    "9": ["###", "#.#", "###", "..#", "###"],
    "/": ["..#", "..#", ".#.", "#..", "#.."],
}

# Storm Caller thunder hammer: one 32x32 texture atlas for the 3D model in
# resourcepack/assets/survival/models/item/storm_caller.json.
# Regions are in pixels (x1, y1, x2, y2); the model's UVs are these divided by 2.
HAMMER_REGIONS = {
    "handle": (0, 0, 4, 32),      # uv [0, 0, 2, 16]
    "grip": (4, 0, 8, 16),        # uv [2, 0, 4, 8]
    "gold": (4, 16, 8, 24),       # uv [2, 8, 4, 12]
    "crystal": (4, 24, 8, 32),    # uv [2, 12, 4, 16]
    "head_side": (8, 0, 32, 12),  # uv [4, 0, 16, 6]
    "head_top": (8, 12, 32, 20),  # uv [4, 6, 16, 10]
    "cap_face": (8, 20, 16, 28),  # uv [4, 10, 8, 14]
    "cap_edge": (16, 20, 24, 24), # uv [8, 10, 12, 12]
    "band": (24, 20, 32, 32),     # uv [12, 10, 16, 16]
}
# Lightning bolt drawn on each end of the hammer head ('r' = rune).
CAP_RUNE = [
    "........",
    "....r...",
    "...rr...",
    "..rrrr..",
    "...rr...",
    "...r....",
    "..r.....",
    "........",
]
# Rune color for each animation frame: dim -> bright -> white flash -> bright.
STORM_CALLER_RUNE_FRAMES = ["1e6fd9", "38bdf8", "f0fbff", "38bdf8"]

FRAME_WIDTH = 81   # same width as the 10 vanilla hearts
FRAME_HEIGHT = 9
BAR_LEFT = 10      # bar body starts after the 9px icon + 1px gap
BAR_TOP = 1
BAR_BOTTOM = 7


def make_frame(icon, icon_palette, outline_color):
    image = Image.new("RGBA", (FRAME_WIDTH, FRAME_HEIGHT), (0, 0, 0, 0))
    draw_pattern(image, icon, icon_palette)

    right = FRAME_WIDTH - 1
    corners = {(BAR_LEFT, BAR_TOP), (right, BAR_TOP), (BAR_LEFT, BAR_BOTTOM), (right, BAR_BOTTOM)}

    for x in range(BAR_LEFT, FRAME_WIDTH):
        for y in range(BAR_TOP, BAR_BOTTOM + 1):
            if (x, y) in corners:
                continue
            on_edge = x in (BAR_LEFT, right) or y in (BAR_TOP, BAR_BOTTOM)
            if on_edge:
                image.putpixel((x, y), outline_color)
            else:
                # Inner track: slightly lighter at the top for depth.
                shade = 0x2c - (y - BAR_TOP - 1) * 4
                image.putpixel((x, y), (shade, shade, shade, 235))
    return image


def make_fill_column():
    # White/grey column; the plugin tints it with the text color,
    # so one texture serves every bar and gradient.
    shades = [255, 240, 215, 190, 160]
    image = Image.new("RGBA", (1, len(shades)), (0, 0, 0, 0))
    for y, shade in enumerate(shades):
        image.putpixel((0, y), (shade, shade, shade, 255))
    return image


def make_digits():
    image = Image.new("RGBA", (3 * len(DIGITS), 5), (0, 0, 0, 0))
    white = (255, 255, 255, 255)
    for index, pattern in enumerate(DIGITS.values()):
        draw_pattern(image, pattern, {"#": white}, offset_x=index * 3)
    return image


def fill_region(image, region, color_at):
    """Colors every pixel of a region with color_at(x, y, width, height), x/y local to the region."""
    x1, y1, x2, y2 = region
    width, height = x2 - x1, y2 - y1
    for y in range(height):
        for x in range(width):
            image.putpixel((x1 + x, y1 + y), color_at(x, y, width, height))


def bordered(base, border, light=None, dark=None):
    """Plate with a 1px border and optional top highlight / bottom shadow."""
    def color_at(x, y, width, height):
        if x in (0, width - 1) or y in (0, height - 1):
            return border
        if light and y == 1:
            return light
        if dark and y == height - 2:
            return dark
        return base
    return color_at


def make_hammer_frame(rune):
    image = Image.new("RGBA", (32, 32), (0, 0, 0, 0))
    regions = HAMMER_REGIONS

    def handle(x, y, width, height):
        if x == 0:
            return hex_rgba("8a5a36")
        if x == width - 1:
            return hex_rgba("3d2414")
        return hex_rgba("4e2f1b") if (y * 7 + x * 3) % 9 == 0 else hex_rgba("6b4226")
    fill_region(image, regions["handle"], handle)

    fill_region(image, regions["grip"], lambda x, y, w, h:
                hex_rgba("5c4033") if (x + y) % 4 == 0 else hex_rgba("2e211b"))

    fill_region(image, regions["gold"], bordered(hex_rgba("facc15"), hex_rgba("a16207"),
                                                 light=hex_rgba("fde68a")))

    def crystal(x, y, width, height):
        if 1 <= x <= 2 and 2 <= y <= 5:
            return hex_rgba("ffffff")
        return rune
    fill_region(image, regions["crystal"], crystal)

    steel_side = bordered(hex_rgba("94a3b8"), hex_rgba("334155"),
                          light=hex_rgba("cbd5e1"), dark=hex_rgba("64748b"))

    def head_side(x, y, width, height):
        # Rivets in the corners and a zigzag lightning rune across the middle.
        if (x, y) in {(2, 2), (width - 3, 2), (2, height - 3), (width - 3, height - 3)}:
            return hex_rgba("e2e8f0")
        if 4 <= x <= width - 5 and y == (5 if (x // 2) % 2 == 0 else 6):
            return rune
        return steel_side(x, y, width, height)
    fill_region(image, regions["head_side"], head_side)

    fill_region(image, regions["head_top"], bordered(hex_rgba("7f8ea3"), hex_rgba("334155")))

    cap = bordered(hex_rgba("1e293b"), hex_rgba("0f172a"))
    fill_region(image, regions["cap_face"], lambda x, y, w, h:
                rune if CAP_RUNE[y][x] == "r" else cap(x, y, w, h))

    fill_region(image, regions["cap_edge"], bordered(hex_rgba("1e293b"), hex_rgba("334155")))

    band = bordered(hex_rgba("eab308"), hex_rgba("854d0e"))
    fill_region(image, regions["band"], lambda x, y, w, h:
                rune if x in (3, 4) and y % 3 == 1 else band(x, y, w, h))

    return image


def make_storm_caller():
    """Animated texture: frames stacked vertically, played by the .png.mcmeta file."""
    frames = len(STORM_CALLER_RUNE_FRAMES)
    image = Image.new("RGBA", (32, 32 * frames), (0, 0, 0, 0))
    for index, rune_color in enumerate(STORM_CALLER_RUNE_FRAMES):
        image.paste(make_hammer_frame(hex_rgba(rune_color)), (0, index * 32))
    return image


def main():
    ITEM_DIR.mkdir(parents=True, exist_ok=True)
    make_storm_caller().save(ITEM_DIR / "storm_caller.png")

    HUD_DIR.mkdir(parents=True, exist_ok=True)
    HEART_DIR.mkdir(parents=True, exist_ok=True)

    make_frame(HEART_ICON, HEART_PALETTE, hex_rgba("140404")).save(HUD_DIR / "health_frame.png")
    make_frame(MANA_ICON, MANA_PALETTE, hex_rgba("050a1a")).save(HUD_DIR / "mana_frame.png")
    make_fill_column().save(HUD_DIR / "fill.png")
    make_digits().save(HUD_DIR / "digits.png")

    transparent = Image.new("RGBA", (9, 9), (0, 0, 0, 0))
    for name in VANILLA_HEART_SPRITES:
        transparent.save(HEART_DIR / f"{name}.png")

    print(f"Wrote HUD textures to {HUD_DIR} and {len(VANILLA_HEART_SPRITES)} hidden heart sprites.")


if __name__ == "__main__":
    main()
