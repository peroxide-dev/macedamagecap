package com.salatube.macedamagecap.command;

import com.salatube.macedamagecap.MaceDamageCap;
import com.salatube.macedamagecap.manager.BypassManager;
import com.salatube.macedamagecap.manager.ConfigManager;
import com.salatube.macedamagecap.util.MessageUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

public final class MaceDamageCapCommand implements CommandExecutor {

    private final MaceDamageCap plugin;
    private final ConfigManager configManager;
    private final BypassManager bypassManager;

    public MaceDamageCapCommand(MaceDamageCap plugin) {
        this.plugin = plugin;
        this.configManager = plugin.getConfigManager();
        this.bypassManager = plugin.getBypassManager();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("macedamagecap.admin")) {
            MessageUtil.send(sender, "&cYou do not have permission to use this command.");
            return true;
        }

        if (args.length == 0) {
            sendInfo(sender);
            return true;
        }

        String sub = args[0].toLowerCase(Locale.ROOT);
        switch (sub) {
            case "set" -> handleSet(sender, args);
            case "get" -> handleGet(sender);
            case "reload" -> handleReload(sender);
            case "bypass" -> handleBypass(sender, args);
            case "help" -> sendInfo(sender);
            case "version", "about" -> sendInfo(sender);
            default -> MessageUtil.send(sender,
                    "&cUnknown subcommand: &f" + args[0]
                            + " &7- try &f/mdc help");
        }
        return true;
    }

    private void sendInfo(CommandSender sender) {
        double cap = configManager.getDamageCap();
        Component[] lines = {
                MessageUtil.colorize(MessageUtil.prefix(plugin) + "&6&lMaceDamageCap &7v"
                        + plugin.getDescription().getVersion()),
                MessageUtil.colorize("&7Author: &fsalatube"),
                MessageUtil.colorize("&7Made with love by &fsalatube &7<3"),
                Component.empty(),
                MessageUtil.colorize("&7Current cap: &f" + cap
                        + " &7(" + (cap / 2.0) + " hearts)"),
                MessageUtil.colorize("&7Min/Max cap: &f"
                        + configManager.getMinCap() + " &7/ &f"
                        + configManager.getMaxCap()),
                MessageUtil.colorize("&7Bypass players: &f" + bypassManager.size()),
                Component.empty(),
                MessageUtil.colorize("&e&lCommands:"),
                MessageUtil.colorize("&f /mdc help &8- &7Show this help menu"),
                MessageUtil.colorize("&f /mdc get &8- &7Show current damage cap"),
                MessageUtil.colorize("&f /mdc set <damage> &8- &7Change the damage cap"),
                MessageUtil.colorize("&f /mdc reload &8- &7Reload configuration"),
                MessageUtil.colorize("&f /mdc bypass add <player> &8- &7Grant bypass"),
                MessageUtil.colorize("&f /mdc bypass remove <player> &8- &7Revoke bypass"),
                MessageUtil.colorize("&f /mdc bypass list &8- &7List bypass players"),
                MessageUtil.colorize("&f /mdc bypass check <player> &8- &7Check a player"),
        };
        for (Component c : lines) sender.sendMessage(c);
    }

    private void handleSet(CommandSender sender, String[] args) {
        if (args.length < 2) {
            MessageUtil.send(sender, "&cUsage: &f/mdc set <damage>");
            return;
        }
        double newCap;
        try {
            newCap = Double.parseDouble(args[1]);
        } catch (NumberFormatException e) {
            MessageUtil.send(sender, "&cInvalid number: &f" + args[1]);
            return;
        }
        if (Double.isNaN(newCap) || Double.isInfinite(newCap)) {
            MessageUtil.send(sender, "&cDamage cap must be a finite number.");
            return;
        }
        if (newCap < configManager.getMinCap() || newCap > configManager.getMaxCap()) {
            MessageUtil.send(sender, "&cDamage cap must be between &f"
                    + configManager.getMinCap() + " &cand &f"
                    + configManager.getMaxCap() + "&c.");
            return;
        }
        double oldCap = configManager.getDamageCap();
        configManager.setDamageCap(newCap);
        if (oldCap == newCap) {
            MessageUtil.send(sender, "&eDamage cap is already &f" + newCap
                    + " &e(" + (newCap / 2.0) + " hearts).");
        } else {
            MessageUtil.send(sender, "&aDamage cap updated: &f" + oldCap
                    + " &a\u2192 &f" + newCap
                    + " &7(" + (newCap / 2.0) + " hearts)");
        }
    }

    private void handleGet(CommandSender sender) {
        double cap = configManager.getDamageCap();
        MessageUtil.send(sender, "&7Current damage cap: &f" + cap
                + " &7(" + (cap / 2.0) + " hearts)");
    }

    private void handleReload(CommandSender sender) {
        configManager.reload();
        bypassManager.load();
        MessageUtil.send(sender, "&aConfiguration reloaded from disk.");
        MessageUtil.send(sender, "&7  Damage cap: &f" + configManager.getDamageCap()
                + " &7(" + (configManager.getDamageCap() / 2.0) + " hearts)");
        MessageUtil.send(sender, "&7  Bypass players: &f" + bypassManager.size());
        MessageUtil.send(sender, "&7Made with love by salatube <3");
    }

    private void handleBypass(CommandSender sender, String[] args) {
        if (args.length < 2) {
            MessageUtil.send(sender,
                    "&cUsage: &f/mdc bypass <add|remove|list|check> [player]");
            return;
        }
        String action = args[1].toLowerCase(Locale.ROOT);
        switch (action) {
            case "add" -> handleBypassAdd(sender, args);
            case "remove" -> handleBypassRemove(sender, args);
            case "list" -> handleBypassList(sender);
            case "check" -> handleBypassCheck(sender, args);
            default -> MessageUtil.send(sender,
                    "&cUsage: &f/mdc bypass <add|remove|list|check> [player]");
        }
    }

    private void handleBypassAdd(CommandSender sender, String[] args) {
        if (args.length < 3) {
            MessageUtil.send(sender, "&cUsage: &f/mdc bypass add <player>");
            return;
        }
        OfflinePlayer target = resolvePlayer(args[2]);
        if (target == null) {
            MessageUtil.send(sender, "&cPlayer not found: &f" + args[2]);
            return;
        }
        if (bypassManager.add(target.getUniqueId())) {
            String name = target.getName() != null ? target.getName()
                    : target.getUniqueId().toString();
            MessageUtil.send(sender, "&aAdded &f" + name + " &ato Mace damage bypass.");
            Player online = target.getPlayer();
            if (online != null) {
                MessageUtil.sendRaw(online, MessageUtil.prefix(plugin)
                        + "&aYou can now bypass the Mace damage cap. Made with love by salatube <3");
            }
        } else {
            String name = target.getName() != null ? target.getName()
                    : target.getUniqueId().toString();
            MessageUtil.send(sender, "&e&f" + name + " &eis already in the bypass list.");
        }
    }

    private void handleBypassRemove(CommandSender sender, String[] args) {
        if (args.length < 3) {
            MessageUtil.send(sender, "&cUsage: &f/mdc bypass remove <player>");
            return;
        }
        OfflinePlayer target = resolvePlayer(args[2]);
        if (target == null) {
            MessageUtil.send(sender, "&cPlayer not found: &f" + args[2]);
            return;
        }
        if (bypassManager.remove(target.getUniqueId())) {
            String name = target.getName() != null ? target.getName()
                    : target.getUniqueId().toString();
            MessageUtil.send(sender, "&aRemoved &f" + name + " &afrom Mace damage bypass.");
            Player online = target.getPlayer();
            if (online != null) {
                MessageUtil.sendRaw(online, MessageUtil.prefix(plugin)
                        + "&cYou no longer bypass the Mace damage cap.");
            }
        } else {
            String name = target.getName() != null ? target.getName()
                    : target.getUniqueId().toString();
            MessageUtil.send(sender, "&e&f" + name + " &ewas not in the bypass list.");
        }
    }

    private void handleBypassList(CommandSender sender) {
        Set<UUID> set = bypassManager.getBypassPlayers();
        if (set.isEmpty()) {
            MessageUtil.send(sender, "&7No players currently have Mace bypass enabled.");
            return;
        }
        MessageUtil.send(sender, "&7Bypass players (" + set.size() + "):");
        List<String> names = new ArrayList<>();
        for (UUID id : set) {
            OfflinePlayer op = Bukkit.getOfflinePlayer(id);
            String name = op.getName() != null ? op.getName() : id.toString();
            boolean online = op.isOnline();
            names.add((online ? "&a" : "&7") + name);
        }
        names.sort(Comparator.naturalOrder());
        for (String n : names) {
            MessageUtil.sendRaw(sender, MessageUtil.prefix(plugin) + "  &8\u2022 &r" + n);
        }
    }

    private void handleBypassCheck(CommandSender sender, String[] args) {
        if (args.length < 3) {
            MessageUtil.send(sender, "&cUsage: &f/mdc bypass check <player>");
            return;
        }
        OfflinePlayer target = resolvePlayer(args[2]);
        if (target == null) {
            MessageUtil.send(sender, "&cPlayer not found: &f" + args[2]);
            return;
        }
        String name = target.getName() != null ? target.getName()
                : target.getUniqueId().toString();
        boolean has = bypassManager.isBypassing(target.getUniqueId());
        MessageUtil.send(sender, "&7Bypass for &f" + name + "&7: "
                + (has ? "&a&lenabled" : "&c&ldisabled"));
    }

    private OfflinePlayer resolvePlayer(String arg) {
        if (arg == null || arg.isEmpty()) return null;

        Player online = Bukkit.getPlayerExact(arg);
        if (online != null) return online;

        try {
            UUID id = UUID.fromString(arg);
            return Bukkit.getOfflinePlayer(id);
        } catch (IllegalArgumentException ignored) {
        }

        for (OfflinePlayer op : Bukkit.getOfflinePlayers()) {
            String n = op.getName();
            if (n != null && n.equalsIgnoreCase(arg)) {
                return op;
            }
        }
        return null;
    }
}
