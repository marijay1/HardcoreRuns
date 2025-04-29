package com.panduuuh.hardcoreRuns;

import com.panduuuh.hardcoreRuns.core.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class HardcoreRuns extends JavaPlugin {

    private PluginManager pluginManager;

    @Override
    public void onEnable() {
        pluginManager = new PluginManager(this);
        pluginManager.initialize();
    }

    @Override
    public void onDisable() {
        if (pluginManager != null) {
            pluginManager.shutdown();
        }
    }
}
