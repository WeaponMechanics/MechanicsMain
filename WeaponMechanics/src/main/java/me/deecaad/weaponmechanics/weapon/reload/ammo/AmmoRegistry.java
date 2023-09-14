package me.deecaad.weaponmechanics.weapon.reload.ammo;

import me.deecaad.core.mechanics.Registry;

public class AmmoRegistry {

    public static final Registry<Ammo> AMMO_REGISTRY = new Registry<>("ammo");

    public static void init() {
        AMMO_REGISTRY.clear(); // For reloads


    }
}
