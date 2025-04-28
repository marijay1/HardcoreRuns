package com.panduuuh.hardcoreRuns.core;

import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

public class BossBarManager {
    private final BukkitTaskScheduler scheduler;
    private final ConfigurationManager config;
    private BossBar bossBar;
    private BukkitTask updateTask;
    private long startTime;
    private boolean timerRunning;

    public BossBarManager(BukkitTaskScheduler scheduler, ConfigurationManager config) {
        this.scheduler = scheduler;
        this.config = config;
        this.timerRunning = false;
    }

    public void initialize() {
        createBossBar();
    }

    private void createBossBar() {
        bossBar = Bukkit.createBossBar(
                "Attempt: " + config.getAttempts() + " | Time: 00:00",
                BarColor.RED,
                BarStyle.SOLID
        );
        bossBar.setVisible(true);
        Bukkit.getOnlinePlayers().forEach(bossBar::addPlayer);
    }

    public void startTimer() {
        if (timerRunning) return;

        startTime = System.currentTimeMillis();
        timerRunning = true;
        startUpdateTask();
    }

    public void stopTimer() {
        if (updateTask != null) {
            updateTask.cancel();
            updateTask = null;
        }
        timerRunning = false;
    }

    private void startUpdateTask() {
        updateTask = scheduler.runTaskTimer(() -> {
            if (!timerRunning) return;

            String time = formatTime(System.currentTimeMillis() - startTime);
            bossBar.setTitle(String.format("Attempt: %d | Time: %s",
                    config.getAttempts(), time));
        }, 0L, 20L);
    }

    public void addPlayer(Player player) {
        bossBar.addPlayer(player);
    }

    private String formatTime(long milliseconds) {
        long totalSeconds = milliseconds / 1000;
        long hours = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;

        if (hours > 0) {
            return String.format("%02d:%02d:%02d", hours, minutes, seconds);
        } else {
            return String.format("%02d:%02d", minutes, seconds);
        }
    }
}