package com.panduuuh.hardcoreRuns.listeners;

import com.panduuuh.hardcoreRuns.core.SharedInventoryManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerItemBreakEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.plugin.Plugin;

public class InventoryChangeListener implements Listener {
    private final SharedInventoryManager inventoryManager;
    private final Plugin plugin;

    public InventoryChangeListener(SharedInventoryManager inventoryManager) {
        this.inventoryManager = inventoryManager;
        this.plugin = inventoryManager.getPlugin();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getPlayer() instanceof Player) {
            inventoryManager.updatePlayerInventory((Player) event.getPlayer());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onItemDrop(PlayerDropItemEvent event) {
        if (!event.isCancelled()) {
            event.getPlayer().getServer().getScheduler().runTaskLater(
                    plugin,
                    () -> inventoryManager.updatePlayerInventory(event.getPlayer()),
                    1L
            );
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onItemPickup(EntityPickupItemEvent event) {
        if (!event.isCancelled() && event.getEntity() instanceof Player) {
            event.getEntity().getServer().getScheduler().runTaskLater(
                    plugin,
                    () -> inventoryManager.updatePlayerInventory((Player) event.getEntity()),
                    1L
            );
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!event.isCancelled() && event.getWhoClicked() instanceof Player) {
            inventoryManager.updatePlayerInventory((Player) event.getWhoClicked());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onItemConsume(PlayerItemConsumeEvent event) {
        if (!event.isCancelled()) {
            event.getPlayer().getServer().getScheduler().runTaskLater(
                    plugin,
                    () -> inventoryManager.updatePlayerInventory(event.getPlayer()),
                    1L
            );
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onItemBreak(PlayerItemBreakEvent event) {
        event.getPlayer().getServer().getScheduler().runTaskLater(
                plugin,
                () -> inventoryManager.updatePlayerInventory(event.getPlayer()),
                1L
        );
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (event.getKeepInventory()) {
            event.getEntity().getServer().getScheduler().runTaskLater(
                    plugin,
                    () -> inventoryManager.updatePlayerInventory(event.getEntity()),
                    1L
            );
        }
    }
}