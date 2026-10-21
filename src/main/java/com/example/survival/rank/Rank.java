package com.example.survival.rank;

import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;

public enum Rank {
    NEWBIE("Newbie", 0, NamedTextColor.GRAY),
    ROOKIE("Rookie", 100, NamedTextColor.GREEN),
    SPECIALIST("Specialist", 500, NamedTextColor.AQUA),
    EXPERT("Expert", 1000, NamedTextColor.DARK_BLUE),
    MASTER("Master", 2500, NamedTextColor.GOLD),
    GRAND_MASTER("Grand Master", 5000, NamedTextColor.RED);

    private final String displayName;
    private final int    requiredExp;
    private final NamedTextColor color;

    Rank(String displayName, int requiredExp, NamedTextColor color) {
        this.displayName = displayName;
        this.requiredExp = requiredExp;
        this.color = color;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getRequiredExp() {
        return requiredExp;
    }

    public static Rank getRankByExp(int exp) {
        Rank currentRank = NEWBIE;
        for (Rank rank : values()) {
            if (exp >= rank.getRequiredExp()) {
                currentRank = rank;
            } else {
                break;
            }
        }
        return currentRank;
    }

    public NamedTextColor getColor() {
        return color;
    }
}
