package com.panduuuh.hardcoreRuns.core;

import com.panduuuh.hardcoreRuns.HardcoreRuns;
import com.panduuuh.hardcoreRuns.commands.ResetCommand;
import com.panduuuh.hardcoreRuns.listeners.*;
import org.bukkit.event.Listener;

import java.util.Arrays;
import java.util.List;

public class PluginManager {
    private final HardcoreRuns plugin;
    private final ConfigurationManager config;
    private final WorldManager world;
    private final PlayerManager player;
    private final DiscordService discord;
    private final BossBarManager bossBar;
    private final List<Listener> listeners;
    private final ResetCommand resetCommand;
    private final NotificationService notificationService;

    public PluginManager(HardcoreRuns plugin) {
        this.plugin = plugin;
        this.config = new ConfigurationManager(plugin);
        this.player = new PlayerManager(plugin, config);
        this.world = new WorldManager(plugin, config, player);
        this.discord = new DiscordService(config, plugin);
        this.bossBar = new BossBarManager(plugin, config);
        this.resetCommand = new ResetCommand(world, player);
        this.notificationService = new NotificationService();

        this.listeners = Arrays.asList(
                new DamageListener(player),
                new DeathListener(player, world, discord, notificationService),
                new JoinListener(player, bossBar),
                new FoodListener(player),
                new ExperienceListener(player)
        );
    }

    public void initialize() {
        config.load();
        world.initializeWorlds();
        bossBar.initialize();
        registerListeners();
        registerCommands();
    }

    private void registerListeners() {
        listeners.forEach(listener ->
                plugin.getServer().getPluginManager().registerEvents(listener, plugin));
    }

    private void registerCommands() {
        plugin.getCommand("reset").setExecutor(resetCommand); // Direct command registration
    }

    public void shutdown() {
        config.save();
        bossBar.cleanup();
        world.cleanupOldRuns();
    }
}