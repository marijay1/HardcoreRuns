package com.panduuuh.hardcoreRuns.commands;

import com.panduuuh.hardcoreRuns.core.PlayerManager;
import com.panduuuh.hardcoreRuns.core.WorldManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ResetCommand implements CommandExecutor {
    private final WorldManager worldManager;
    private final PlayerManager playerManager;

    public ResetCommand(WorldManager worldManager, PlayerManager playerManager) {
        this.worldManager = worldManager;
        this.playerManager = playerManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can execute this command!");
            return true;
        }

        Player player = (Player) sender;
        if (cmd.getName().equalsIgnoreCase("reset")) {
            worldManager.resetWorld(player);
            return true;
        }
        return false;
    }
}