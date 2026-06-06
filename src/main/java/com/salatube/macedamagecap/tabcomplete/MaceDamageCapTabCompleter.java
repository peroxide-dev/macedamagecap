package com.salatube.macedamagecap.tabcomplete;

import com.salatube.macedamagecap.MaceDamageCap;
import com.salatube.macedamagecap.manager.ConfigManager;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

public final class MaceDamageCapTabCompleter implements TabCompleter {

    private static final List<String> ROOT_SUBS = List.of("set", "get", "reload", "bypass", "help");
    private static final List<String> BYPASS_SUBS = List.of("add", "remove", "list", "check");
    private static final List<String> PLAYER_SUBS = List.of("add", "remove", "check");

    private final MaceDamageCap plugin;

    public MaceDamageCapTabCompleter(MaceDamageCap plugin) {
        this.plugin = plugin;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                                @NotNull String alias, @NotNull String[] args) {
        List<String> out = new ArrayList<>();
        if (!sender.hasPermission("macedamagecap.admin")) {
            return out;
        }

        if (args.length == 1) {
            filter(out, args[0], ROOT_SUBS);
            return out;
        }

        if (args.length == 2) {
            String sub = args[0].toLowerCase(Locale.ROOT);
            if (sub.equals("bypass")) {
                filter(out, args[1], BYPASS_SUBS);
            } else if (sub.equals("set")) {
                ConfigManager cfg = plugin.getConfigManager();
                if (cfg != null) {
                    suggestDamageValues(out, args[1], cfg.getDamageCap(),
                            cfg.getMinCap(), cfg.getMaxCap());
                }
            } else if (sub.equals("get") || sub.equals("reload") || sub.equals("help")) {
                if (args[1].isEmpty()) {
                    out.add(" ");
                }
            }
            return out;
        }

        if (args.length == 3) {
            String sub = args[0].toLowerCase(Locale.ROOT);
            String action = args[1].toLowerCase(Locale.ROOT);
            if (sub.equals("bypass") && PLAYER_SUBS.contains(action)) {
                suggestPlayers(out, args[2]);
            }
            return out;
        }

        if (args.length == 4) {
            String sub = args[0].toLowerCase(Locale.ROOT);
            String action = args[1].toLowerCase(Locale.ROOT);
            if (sub.equals("bypass") && action.equals("list") || sub.equals("bypass") && action.equals("check")) {
                if (args[3].isEmpty()) {
                    out.add(" ");
                }
            }
        }

        return out;
    }

    private void filter(List<String> out, String arg, List<String> options) {
        String lower = arg.toLowerCase(Locale.ROOT);
        for (String o : options) {
            if (o.toLowerCase(Locale.ROOT).startsWith(lower)) {
                out.add(o);
            }
        }
    }

    private void suggestDamageValues(List<String> out, String arg, double current,
                                     double min, double max) {
        String lower = arg.toLowerCase(Locale.ROOT);
        double[] candidates = {0, 4, 6, 8, 10, 12, 14, 16, 18, 20, 24, 30, 40, current};
        for (double v : candidates) {
            if (v < min || v > max) continue;
            String s = trimDouble(v);
            if (s.toLowerCase(Locale.ROOT).startsWith(lower)) {
                out.add(s);
            }
        }
    }

    private void suggestPlayers(List<String> out, String arg) {
        String lower = arg.toLowerCase(Locale.ROOT);
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.getName().toLowerCase(Locale.ROOT).startsWith(lower)) {
                out.add(p.getName());
            }
        }
        Set<UUID> bypass = plugin.getBypassManager().getBypassPlayers();
        for (UUID id : bypass) {
            OfflinePlayer op = Bukkit.getOfflinePlayer(id);
            String name = op.getName();
            if (name == null) continue;
            if (name.toLowerCase(Locale.ROOT).startsWith(lower)
                    && !out.contains(name)) {
                out.add(name);
            }
        }
    }

    private String trimDouble(double v) {
        if (v == (long) v) return Long.toString((long) v);
        return Double.toString(v);
    }
}
