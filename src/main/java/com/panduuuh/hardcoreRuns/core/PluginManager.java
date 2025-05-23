package com.panduuuh.hardcoreRuns.core;

import com.panduuuh.hardcoreRuns.HardcoreRuns;
import com.panduuuh.hardcoreRuns.commands.ResetCommand;
import com.panduuuh.hardcoreRuns.listeners.*;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;

import java.util.Arrays;
import java.util.List;

public class PluginManager {
    private final HardcoreRuns plugin;
    private final ConfigurationManager config;
    private final TaskScheduler scheduler;
    private final Logger logger;
    private final WorldManager world;
    private final PlayerManager player;
    private final DiscordService discord;
    private final BossBarManager bossBar;
    private final SharedInventoryManager sharedInventory;
    private final List<Listener> listeners;
    private final ResetCommand resetCommand;
    private final NotificationService notificationService;

    public PluginManager(HardcoreRuns plugin) {
        this.plugin = plugin;
        this.config = new ConfigurationManager(plugin);
        this.scheduler = new BukkitTaskScheduler(plugin);
        this.logger = new BukkitLogger(plugin);
        this.sharedInventory = new SharedInventoryManager(plugin, config, logger);
        this.player = new PlayerManager(plugin, config, scheduler, logger, sharedInventory);
        this.bossBar = new BossBarManager(scheduler, config);
        this.world = new WorldManager(plugin, config, player, scheduler, bossBar, logger, sharedInventory);
        this.discord = new DiscordService(config, scheduler, logger);
        this.resetCommand = new ResetCommand(world, bossBar);
        this.notificationService = new NotificationService();

        this.listeners = Arrays.asList(
                new DamageListener(player),
                new DeathListener(player, world, discord, notificationService, bossBar),
                new JoinListener(player, bossBar),
                new FoodListener(player),
                new ExperienceListener(player),
                new QuitListener(config, sharedInventory),
                new HealthRegainListener(player),
                new InventoryChangeListener(sharedInventory)
        );

        this.player.setWorldManager(world);
    }

    public void initialize() {
        config.load();
        sharedInventory.initialize();
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
        plugin.getCommand("reset").setExecutor(resetCommand);
    }

    public void shutdown() {
        config.save();
        bossBar.stopTimer();
        sharedInventory.shutdown();
        world.cleanupOldRuns();
    }
}