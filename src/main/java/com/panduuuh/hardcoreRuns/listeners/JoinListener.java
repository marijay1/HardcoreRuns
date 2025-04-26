package com.panduuuh.hardcoreRuns.listeners;

import com.panduuuh.hardcoreRuns.core.BossBarManager;
import com.panduuuh.hardcoreRuns.core.PlayerManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class JoinListener implements Listener {
    private final PlayerManager playerManager;
    private final BossBarManager bossBarManager;

    public JoinListener(PlayerManager playerManager, BossBarManager bossBarManager) {
        this.playerManager = playerManager;
        this.bossBarManager = bossBarManager;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player newPlayer = event.getPlayer();
        bossBarManager.addPlayer(newPlayer);
        playerManager.syncNewPlayer(newPlayer);
    }
}