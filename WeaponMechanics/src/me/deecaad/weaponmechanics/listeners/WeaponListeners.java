package me.deecaad.weaponmechanics.listeners;

import me.deecaad.weaponmechanics.weapon.WeaponHandler;
import org.bukkit.event.Listener;

public class WeaponListeners implements Listener {

    private WeaponHandler weaponHandler;

    public WeaponListeners(WeaponHandler weaponHandler) {
        this.weaponHandler = weaponHandler;
    }
}
