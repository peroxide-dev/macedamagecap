package com.salatube.macedamagecap;

import com.salatube.macedamagecap.command.MaceDamageCapCommand;
import com.salatube.macedamagecap.listener.MaceDamageListener;
import com.salatube.macedamagecap.manager.BypassManager;
import com.salatube.macedamagecap.manager.ConfigManager;
import com.salatube.macedamagecap.tabcomplete.MaceDamageCapTabCompleter;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class MaceDamageCap extends JavaPlugin {

    private static MaceDamageCap instance;
    private ConfigManager configManager;
    private BypassManager bypassManager;

    @Override
    public void onEnable() {
        instance = this;

        saveDefaultConfig();

        this.configManager = new ConfigManager(this);
        this.bypassManager = new BypassManager(this);
        this.bypassManager.load();

        PluginCommand command = getCommand("macedamagecap");
        if (command == null) {
            getLogger().severe("Could not register command 'macedamagecap'. Disabling plugin.");
            setEnabled(false);
            return;
        }

        MaceDamageCapCommand executor = new MaceDamageCapCommand(this);
        command.setExecutor(executor);
        command.setTabCompleter(new MaceDamageCapTabCompleter(this));

        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(new MaceDamageListener(this), this);

        getLogger().info("===============================================");
        getLogger().info(" MaceDamageCap v" + getDescription().getVersion());
        getLogger().info(" Made with love by salatube <3");
        getLogger().info(" Author: " + getDescription().getAuthors().get(0));
        getLogger().info(" Current damage cap: " + configManager.getDamageCap()
                + " (" + (configManager.getDamageCap() / 2.0) + " hearts)");
        getLogger().info(" Bypass players loaded: " + bypassManager.getBypassPlayers().size());
        getLogger().info("===============================================");
    }

    @Override
    public void onDisable() {
        if (bypassManager != null) {
            bypassManager.save();
        }
        getLogger().info("MaceDamageCap disabled. Made with love by salatube <3");
    }

    public static MaceDamageCap getInstance() {
        return instance;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public BypassManager getBypassManager() {
        return bypassManager;
    }
}
