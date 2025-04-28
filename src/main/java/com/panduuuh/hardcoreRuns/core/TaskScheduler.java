package com.panduuuh.hardcoreRuns.core;

import org.bukkit.scheduler.BukkitTask;

public interface TaskScheduler {
    BukkitTask runTask(Runnable task);
    BukkitTask runTaskLater(Runnable task, long delay);
    BukkitTask runTaskAsync(Runnable task);
    BukkitTask runTaskTimer(Runnable task, long delay, long period);
    BukkitTask runTaskTimerAsync(Runnable task, long delay, long period);
}