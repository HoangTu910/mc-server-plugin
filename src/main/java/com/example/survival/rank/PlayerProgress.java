package com.example.survival.rank;

public class PlayerProgress {
    private int totalXp;

    public PlayerProgress(int totalXp) {
        this.totalXp = Math.max(0, totalXp);
    }

    public int getTotalXp() {
        return totalXp;
    }

    public void addXp(int xp) {
        if (xp < 0) {
            throw new IllegalArgumentException("XP to add cannot be negative");
        }
        totalXp = (int) Math.min(
            Integer.MAX_VALUE,
            (long) totalXp + xp
        );
    }

    public int getLevel() {
        return Rank.getRankByExp(totalXp).ordinal() + 1;
    }

    public Rank getRank() {
        return Rank.getRankByExp(totalXp);
    }

    public void setTotalXp(int requiredExp) {
        if(requiredExp < 0) {
            throw new IllegalArgumentException("Total XP cannot be negative");
        }
        this.totalXp = Math.min(Integer.MAX_VALUE, requiredExp);
    }
}
