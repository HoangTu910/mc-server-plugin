package com.example.survival.rank;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import com.example.survival.SurvivalMainPlugin;

public class RankService {
    private final SurvivalMainPlugin plugin;
    private final File dataFile;
    private final Map<UUID, PlayerProgress> playerProgressMap = new ConcurrentHashMap<>();

    private YamlConfiguration data;

    public RankService(SurvivalMainPlugin plugin) {
        this.plugin = plugin;
        this.dataFile = new File(plugin.getDataFolder(), "player-data.yml");
        loadData();
    }

    private void loadData() {
        if (!dataFile.exists()) {
            try {
                dataFile.createNewFile();
            } catch (Exception e) {
                plugin.getLogger().severe("Failed to create player-data.yml: " + e.getMessage());
            }
        }
        data = YamlConfiguration.loadConfiguration(dataFile);
        for (String key : data.getKeys(false)) {
            UUID playerId = UUID.fromString(key);
            int totalXp = data.getInt(key + ".totalXp", 0);
            playerProgressMap.put(playerId, new PlayerProgress(totalXp));
        }
    }

    public PlayerProgress getPlayerProgress(UUID playerId) {
        return playerProgressMap.computeIfAbsent(playerId, id -> new PlayerProgress(0));
    }

    public PlayerProgress getPlayerProgress(Player player) {
        return getPlayerProgress(player.getUniqueId());
    }

    public Rank getRank(Player player) {
        return getPlayerProgress(player).getRank();
    }

    public Rank getNextRank(Player player) {
        return Rank.getRankByExp(getPlayerProgress(player).getTotalXp() + 1);
    }

    public void addXp(Player player, int xp) {
        if(xp < 0) {
            throw new IllegalArgumentException("XP to add cannot be negative");
        }
        PlayerProgress progress = getPlayerProgress(player.getUniqueId());
        Rank           oldRank  = progress.getRank();
        progress.addXp(xp);

        Rank newRank = progress.getRank();
        player.sendMessage(
                "§a+" + xp + " XP §7| Total: "
                        + progress.getTotalXp()
                        + " | Level: " + progress.getLevel()
        );
        if(!oldRank.equals(newRank)) {
            player.sendMessage(
                    "§6§lRANK UP! §eYou have reached the rank of §b"
                            + newRank.getDisplayName() + "!"
            );
        }
        saveData();
    }

    public void saveData() {
        for (Map.Entry<UUID, PlayerProgress> entry
                : playerProgressMap.entrySet()) {
            data.set(
                    entry.getKey() + ".totalXp",
                    entry.getValue().getTotalXp()
            );
        }

        try {
            data.save(dataFile);
        } catch (IOException exception) {
            plugin.getLogger().severe(
                    "Could not save player progression: "
                            + exception.getMessage()
            );
        }
    }

    public void setRank(Player player, Rank newRank) {
        PlayerProgress progress = getPlayerProgress(player);
        progress.setTotalXp(newRank.getRequiredExp());

        saveData();
    }
}
