package me.deecaad.weaponmechanics.listeners;

import me.deecaad.core.events.EquipEvent;
import me.deecaad.core.events.HandDataUpdateEvent;
import me.deecaad.core.utils.StringUtil;
import me.deecaad.weaponmechanics.WeaponMechanics;
import org.bukkit.ChatColor;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;

public class EquipEventTemp implements Listener {

    @EventHandler (priority = EventPriority.HIGHEST)
    public void onUpdate(HandDataUpdateEvent e) {
        if (e.isCancelled() && e.getEntityType() == EntityType.PLAYER) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    e.getEntity().sendMessage(ChatColor.YELLOW + "Cancelled");
                }
            }.runTask(WeaponMechanics.getPlugin());
        }
    }

    @EventHandler
    public void onEquip(EquipEvent e) {
        if (e.getEntityType() != EntityType.PLAYER)
            return;

        Player player = (Player) e.getEntity();
        if (e.isEquipping()) {
            player.sendMessage(ChatColor.GREEN + "Equipping " + StringUtil.keyToRead(e.getEquipped().getType().name()));
        }
        else if (e.isDequipping()) {
            player.sendMessage(ChatColor.RED + "Removing " + StringUtil.keyToRead(e.getDequipped().getType().name()));
        }
    }
}
