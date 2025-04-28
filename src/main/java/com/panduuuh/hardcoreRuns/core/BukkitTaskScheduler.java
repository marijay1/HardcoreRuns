package com.panduuuh.hardcoreRuns.core;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

public class BukkitTaskScheduler implements TaskScheduler {
    private final Plugin plugin;

    public BukkitTaskScheduler(Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public BukkitTask runTask(Runnable task) {
        return Bukkit.getScheduler().runTask(plugin, task);
    }

    @Override
    public BukkitTask runTaskLater(Runnable task, long delay) {
        return Bukkit.getScheduler().runTaskLater(plugin, task, delay);
    }

    @Override
    public BukkitTask runTaskAsync(Runnable task) {
        return Bukkit.getScheduler().runTaskAsynchronously(plugin, task);
    }

    @Override
    public BukkitTask runTaskTimer(Runnable task, long delay, long period) {
        return Bukkit.getScheduler().runTaskTimer(plugin, task, delay, period);
    }

    @Override
    public BukkitTask runTaskTimerAsync(Runnable task, long delay, long period) {
        return Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, task, delay, period);
    }
}