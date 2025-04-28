package com.panduuuh.hardcoreRuns.commands;

import com.panduuuh.hardcoreRuns.core.BossBarManager;
import com.panduuuh.hardcoreRuns.core.PlayerManager;
import com.panduuuh.hardcoreRuns.core.WorldManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ResetCommand implements CommandExecutor {
    private final WorldManager worldManager;
    private final BossBarManager bossBar;

    public ResetCommand(WorldManager worldManager, BossBarManager bossBar) {
        this.worldManager = worldManager;
        this.bossBar = bossBar;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can execute this command!");
            return true;
        }

        if (cmd.getName().equalsIgnoreCase("reset")) {
            if (bossBar.isTimerRunning() && !player.isOp()) {
                player.sendMessage(ChatColor.RED + "You can only reset during an active run if you're an operator!");
                return true;
            }

            player.sendMessage(ChatColor.YELLOW + "A new world is being generated. Please wait...");
            worldManager.resetWorld(player);
            return true;
        }
        return false;
    }
}