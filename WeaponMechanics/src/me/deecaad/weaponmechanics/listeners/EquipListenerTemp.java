package me.deecaad.weaponmechanics.listeners;

import me.deecaad.core.events.EntityEquipmentEvent;
import me.deecaad.core.utils.StringUtil;
import org.bukkit.ChatColor;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class EquipListenerTemp implements Listener {

    @EventHandler
    public void onEquip(EntityEquipmentEvent e) {
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
