package com.salatube.macedamagecap.listener;

import com.salatube.macedamagecap.MaceDamageCap;
import com.salatube.macedamagecap.manager.BypassManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageModifier;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

public final class MaceDamageListener implements Listener {

    private final MaceDamageCap plugin;
    private final BypassManager bypassManager;

    public MaceDamageListener(MaceDamageCap plugin) {
        this.plugin = plugin;
        this.bypassManager = plugin.getBypassManager();
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player damager)) {
            return;
        }

        ItemStack mainHand = damager.getInventory().getItemInMainHand();
        if (mainHand.getType() != Material.MACE) {
            return;
        }

        if (bypassManager.isBypassing(damager.getUniqueId())) {
            return;
        }

        double cap = plugin.getConfigManager().getDamageCap();
        double rawDamage = event.getDamage(DamageModifier.BASE);

        if (rawDamage > cap) {
            event.setDamage(DamageModifier.BASE, cap);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        bypassManager.unregisterAttachment(event.getPlayer());
    }
}
