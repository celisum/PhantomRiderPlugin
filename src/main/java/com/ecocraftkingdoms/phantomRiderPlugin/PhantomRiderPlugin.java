package com.example.phantomrider;

import org.bukkit.plugin.java.JavaPlugin;

public class PhantomRiderPlugin extends JavaPlugin {
    @Override
    public void onEnable() {
        PlayerEventListener listener = new PlayerEventListener(this);
        getServer().getPluginManager().registerEvents(listener, this);
        getCommand("summonphantom").setExecutor(listener);
        getLogger().info("PhantomRiderPlugin enabled.");
    }

    @Override
    public void onDisable() {
        getLogger().info("PhantomRiderPlugin disabled.");
    }
}
