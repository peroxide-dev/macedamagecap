package com.salatube.macedamagecap.manager;

import com.salatube.macedamagecap.MaceDamageCap;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionAttachmentInfo;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

public class BypassManager {

    private static final String BYPASS_PERMISSION = "macedamagecap.bypass";

    private final MaceDamageCap plugin;
    private final File file;
    private final Set<UUID> bypassPlayers = new HashSet<>();
    private final Map<UUID, PermissionAttachment> attachments = new HashMap<>();

    public BypassManager(MaceDamageCap plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "bypass.yml");
    }

    public void load() {
        for (Player p : Bukkit.getOnlinePlayers()) {
            unregisterAttachment(p);
        }
        attachments.clear();
        bypassPlayers.clear();

        if (!file.exists()) {
            try {
                file.getParentFile().mkdirs();
                file.createNewFile();
                FileConfiguration cfg = YamlConfiguration.loadConfiguration(file);
                cfg.set("players", Collections.emptyList());
                cfg.set("# Made with love by salatube <3", null);
                cfg.save(file);
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "Could not create bypass.yml", e);
                return;
            }
        }

        FileConfiguration cfg = YamlConfiguration.loadConfiguration(file);
        List<String> list = cfg.getStringList("players");
        for (String id : list) {
            try {
                bypassPlayers.add(UUID.fromString(id));
            } catch (IllegalArgumentException ex) {
                plugin.getLogger().warning("Skipped invalid UUID in bypass.yml: " + id);
            }
        }

        for (UUID id : bypassPlayers) {
            Player p = Bukkit.getPlayer(id);
            if (p != null) {
                attachPermission(p);
            }
        }

        plugin.getLogger().info("Loaded " + bypassPlayers.size() + " bypass entries from bypass.yml");
    }

    public void save() {
        try {
            if (!file.exists()) {
                file.getParentFile().mkdirs();
                file.createNewFile();
            }
            FileConfiguration cfg = YamlConfiguration.loadConfiguration(file);
            List<String> ids = bypassPlayers.stream()
                    .map(UUID::toString)
                    .sorted()
                    .collect(java.util.stream.Collectors.toList());
            cfg.set("players", ids);
            cfg.save(file);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not save bypass.yml", e);
        }
    }

    public boolean isBypassing(UUID uuid) {
        if (uuid == null) return false;
        if (bypassPlayers.contains(uuid)) return true;
        Player p = Bukkit.getPlayer(uuid);
        if (p == null) return false;
        if (p.hasPermission(BYPASS_PERMISSION)) return true;
        for (PermissionAttachmentInfo info : p.getEffectivePermissions()) {
            if (BYPASS_PERMISSION.equalsIgnoreCase(info.getPermission()) && info.getValue()) {
                return true;
            }
        }
        return false;
    }

    public boolean add(UUID uuid) {
        if (uuid == null) return false;
        boolean added = bypassPlayers.add(uuid);
        if (added) {
            Player p = Bukkit.getPlayer(uuid);
            if (p != null) attachPermission(p);
            save();
        }
        return added;
    }

    public boolean remove(UUID uuid) {
        if (uuid == null) return false;
        boolean removed = bypassPlayers.remove(uuid);
        if (removed) {
            Player p = Bukkit.getPlayer(uuid);
            if (p != null) unregisterAttachment(p);
            save();
        }
        return removed;
    }

    public Set<UUID> getBypassPlayers() {
        return new HashSet<>(bypassPlayers);
    }

    public int size() {
        return bypassPlayers.size();
    }

    private void attachPermission(Player p) {
        if (attachments.containsKey(p.getUniqueId())) return;
        if (p.hasPermission(BYPASS_PERMISSION)) return;
        try {
            PermissionAttachment att = p.addAttachment(plugin);
            att.setPermission(BYPASS_PERMISSION, true);
            attachments.put(p.getUniqueId(), att);
        } catch (Throwable t) {
            plugin.getLogger().log(Level.WARNING,
                    "Could not attach bypass permission to " + p.getName(), t);
        }
    }

    public void unregisterAttachment(Player p) {
        if (p == null) return;
        PermissionAttachment att = attachments.remove(p.getUniqueId());
        if (att != null) {
            try {
                p.removeAttachment(att);
            } catch (Throwable ignored) {
            }
        }
    }
}
