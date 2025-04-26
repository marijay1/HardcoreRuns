package com.panduuuh.hardcoreRuns.core;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class NotificationService {

    public void promptWorldReset(Player player) {
        TextComponent message = new TextComponent(
                ChatColor.RED + "All players are dead. Click " +
                        ChatColor.DARK_AQUA + ChatColor.UNDERLINE + "here" + ChatColor.RESET +
                        " or use " + ChatColor.GOLD + "/reset " + ChatColor.RESET + "to start a new attempt."
        );
        message.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/reset"));
        player.spigot().sendMessage(message);
    }
}