package com.panduuuh.hardcoreRuns.core;

import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

public class BossBarManager {
    private final Plugin plugin;
    private final ConfigurationManager config;
    private BossBar bossBar;
    private BukkitTask updateTask;
    private long startTime;

    public BossBarManager(Plugin plugin, ConfigurationManager config) {
        this.plugin = plugin;
        this.config = config;
    }

    public void initialize() {
        createBossBar();
        startUpdateTask();
    }

    private void createBossBar() {
        bossBar = Bukkit.createBossBar(
                "Attempt: " + config.getAttempts() + " | Time: 00:00",
                BarColor.RED,
                BarStyle.SOLID
        );
        bossBar.setVisible(true);
        Bukkit.getOnlinePlayers().forEach(bossBar::addPlayer);
        startTime = System.currentTimeMillis();
    }

    private void startUpdateTask() {
        updateTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            String time = formatTime(System.currentTimeMillis() - startTime);
            bossBar.setTitle(String.format("Attempt: %d | Time: %s",
                    config.getAttempts(), time));
        }, 0L, 20L);
    }

    public void addPlayer(Player player) {
        bossBar.addPlayer(player);
    }

    public void updateAttemptCounter() {
        bossBar.setTitle(String.format("Attempt: %d | Time: 00:00", config.getAttempts()));
        startTime = System.currentTimeMillis();
    }

    public void cleanup() {
        if (updateTask != null) {
            updateTask.cancel();
        }
        if (bossBar != null) {
            bossBar.removeAll();
        }
    }

    private String formatTime(long milliseconds) {
        long seconds = (milliseconds / 1000) % 60;
        long minutes = (milliseconds / (1000 * 60)) % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }
}