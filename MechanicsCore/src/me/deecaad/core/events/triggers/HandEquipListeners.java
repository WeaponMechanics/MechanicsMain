package me.deecaad.core.events.triggers;

import me.deecaad.core.events.ArmorEquipEvent;
import me.deecaad.core.events.HandEquipEvent;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public class HandEquipListeners implements Listener {

    @EventHandler (ignoreCancelled = true)
    public void itemHeld(PlayerItemHeldEvent e) {
        ItemStack item = e.getPlayer().getInventory().getItem(e.getNewSlot());
        Bukkit.getPluginManager().callEvent(new HandEquipEvent(e.getPlayer(), item, EquipmentSlot.HAND));
        EquipListener.test.put(EquipmentSlot.HAND, item);
    }

    @EventHandler
    public void handEquip(HandEquipEvent e) {
        Bukkit.broadcastMessage(e.isMainHand() ? EquipmentSlot.HAND.name() : EquipmentSlot.OFF_HAND.name() + " IS EQUIPPING: " + e.isEquipping());
    }

    @EventHandler
    public void armorEquip(ArmorEquipEvent e) {
        Bukkit.broadcastMessage(e.getSlot().name() + " IS EQUIPPING: " + e.isEquipping());
    }
}