package me.deecaad.weaponmechanics.listeners;

import me.deecaad.core.events.EquipEvent;
import me.deecaad.core.events.HandDataUpdateEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class EquipEventTemp implements Listener {

    @EventHandler
    public void onUpdate(HandDataUpdateEvent e) {
        System.out.println(e);
    }

    @EventHandler
    public void onEquip(EquipEvent e) {
        System.out.println(e);
    }
}
