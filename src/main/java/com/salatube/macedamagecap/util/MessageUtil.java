package com.salatube.macedamagecap.util;

import com.salatube.macedamagecap.MaceDamageCap;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.command.CommandSender;

public final class MessageUtil {

    private static final LegacyComponentSerializer LEGACY =
            LegacyComponentSerializer.legacyAmpersand();

    private MessageUtil() {
    }

    public static Component colorize(String message) {
        if (message == null) return Component.empty();
        return LEGACY.deserialize(message);
    }

    public static void sendRaw(CommandSender sender, String message) {
        sender.sendMessage(colorize(message));
    }

    public static void send(CommandSender sender, String message) {
        MaceDamageCap plugin = MaceDamageCap.getInstance();
        String prefix = plugin != null && plugin.getConfigManager() != null
                ? plugin.getConfigManager().getPrefix()
                : "&6&lMaceDamageCap &8&l\u00BB &r";
        sender.sendMessage(colorize(prefix + message));
    }

    public static String prefix(MaceDamageCap plugin) {
        return plugin != null && plugin.getConfigManager() != null
                ? plugin.getConfigManager().getPrefix()
                : "&6&lMaceDamageCap &8&l\u00BB &r";
    }
}
