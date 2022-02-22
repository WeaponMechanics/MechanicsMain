package me.deecaad.weaponmechanics.libs;

import io.lumine.xikage.mythicmobs.api.bukkit.events.MythicMechanicLoadEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class MythicMobsLoader implements Listener {

    @EventHandler
    public void onLoad(MythicMechanicLoadEvent event) {
        if (event.getMechanicName().equalsIgnoreCase("weaponMechanicsShoot")) {
            event.register(new MythicMobsWeaponShootSkill(event.getConfig()));
        }
    }
}
