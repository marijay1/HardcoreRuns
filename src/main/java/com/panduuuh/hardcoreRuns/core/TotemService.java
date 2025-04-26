package com.panduuuh.hardcoreRuns.core;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class TotemService {
    public boolean teamHasTotems() {
        return Bukkit.getOnlinePlayers().stream()
                .anyMatch(this::hasTotem);
    }

    public boolean hasTotem(Player player) {
        return player.getInventory().getItemInOffHand().getType() == Material.TOTEM_OF_UNDYING;
    }

    public void consumeTotem(Player player) {
        ItemStack offhand = player.getInventory().getItemInOffHand();
        if (offhand.getType() == Material.TOTEM_OF_UNDYING) {
            offhand.setAmount(offhand.getAmount() - 1);
        }
    }
}