package com.panduuuh.hardcoreRuns.listeners;

import com.panduuuh.hardcoreRuns.core.DiscordService;
import com.panduuuh.hardcoreRuns.core.NotificationService;
import com.panduuuh.hardcoreRuns.core.PlayerManager;
import com.panduuuh.hardcoreRuns.core.WorldManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

public class DeathListener implements Listener {
    private final PlayerManager playerManager;
    private final WorldManager worldManager;
    private final DiscordService discordService;
    private final NotificationService notifications;

    public DeathListener(PlayerManager playerManager, WorldManager worldManager,
                         DiscordService discordService, NotificationService notifications) {
        this.playerManager = playerManager;
        this.worldManager = worldManager;
        this.discordService = discordService;
        this.notifications = notifications;

    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        long timeAlive = System.currentTimeMillis() - worldManager.getRunStartTime();

        if (playerManager.handleTeamTotemActivation()) {
            playerManager.reviveAllPlayers();
        } else {
            worldManager.incrementAttempt();
            discordService.sendDeathAlert(player, timeAlive, event.getDeathMessage());
            notifications.promptWorldReset(player);
        }
    }
}