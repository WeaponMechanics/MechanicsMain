package me.deecaad.weaponcompatibility;

import me.deecaad.compatibility.CompatibilitySetup;
import me.deecaad.weaponcompatibility.projectile.IProjectileCompatibility;
import me.deecaad.weaponcompatibility.scope.IScopeCompatibility;
import me.deecaad.weaponcompatibility.shoot.IShootCompatibility;

public class WeaponCompatibilityAPI {

    private static IWeaponCompatibility weaponCompatibility;

    public static IWeaponCompatibility getWeaponCompatibility() {
        if (weaponCompatibility == null) {
            weaponCompatibility = new CompatibilitySetup().getCompatibleVersion(IWeaponCompatibility.class, "me.deecaad.weaponcompatibility");
        }
        return weaponCompatibility;
    }

    public static IScopeCompatibility getScopeCompatibility() {
        return getWeaponCompatibility().getScopeCompatibility();
    }

    public static IProjectileCompatibility getProjectileCompatibility() {
        return getWeaponCompatibility().getProjectileCompatibility();
    }

    public static IShootCompatibility getShootCompatibility() {
        return getWeaponCompatibility().getShootCompatibility();
    }
}