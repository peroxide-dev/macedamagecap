package com.salatube.macedamagecap.manager;

import com.salatube.macedamagecap.MaceDamageCap;
import org.bukkit.configuration.file.FileConfiguration;

public class ConfigManager {

    private final MaceDamageCap plugin;
    private double damageCap;
    private double minCap;
    private double maxCap;
    private String prefix;
    private boolean logChanges;

    public ConfigManager(MaceDamageCap plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        plugin.reloadConfig();
        FileConfiguration cfg = plugin.getConfig();

        this.damageCap = clamp(cfg.getDouble("damage-cap", 14.0), 0.0, 1000.0);
        this.minCap = Math.max(0.0, cfg.getDouble("min-cap", 0.0));
        this.maxCap = Math.max(this.minCap, cfg.getDouble("max-cap", 1000.0));
        this.damageCap = clamp(this.damageCap, this.minCap, this.maxCap);
        this.prefix = cfg.getString("messages-prefix",
                "&6&lMaceDamageCap &8&l\u00BB &r");
        this.logChanges = cfg.getBoolean("log-changes", true);
    }

    public double getDamageCap() {
        return damageCap;
    }

    public double getMinCap() {
        return minCap;
    }

    public double getMaxCap() {
        return maxCap;
    }

    public String getPrefix() {
        return prefix;
    }

    public boolean isLogChanges() {
        return logChanges;
    }

    public void setDamageCap(double newCap) {
        newCap = clamp(newCap, minCap, maxCap);
        double oldCap = this.damageCap;
        this.damageCap = newCap;
        plugin.getConfig().set("damage-cap", newCap);
        plugin.saveConfig();
        if (logChanges && oldCap != newCap) {
            plugin.getLogger().info("Damage cap changed: " + oldCap + " -> " + newCap
                    + " (" + (newCap / 2.0) + " hearts). Made with love by salatube <3");
        }
    }

    private static double clamp(double value, double min, double max) {
        if (value < min) return min;
        if (value > max) return max;
        return value;
    }
}
