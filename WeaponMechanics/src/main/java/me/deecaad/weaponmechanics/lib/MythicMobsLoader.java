package me.deecaad.weaponmechanics.lib;

import io.lumine.mythic.bukkit.events.MythicMechanicLoadEvent;
import me.deecaad.weaponmechanics.WeaponMechanics;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class MythicMobsLoader implements Listener {

    public MythicMobsLoader() {
        WeaponMechanics.debug.info("Hooking into MythicMobs");
    }

    @EventHandler
    public void onLoad(MythicMechanicLoadEvent event) {
        if (event.getMechanicName().equalsIgnoreCase("weaponMechanicsShoot")) {
            event.register(new MythicMobsWeaponShootSkill(event.getConfig()));
        }
    }
}
