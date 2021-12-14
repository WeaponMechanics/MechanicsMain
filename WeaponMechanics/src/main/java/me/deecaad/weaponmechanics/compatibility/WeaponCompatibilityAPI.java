package me.deecaad.weaponmechanics.compatibility;

import me.deecaad.core.compatibility.CompatibilitySetup;
import me.deecaad.weaponmechanics.compatibility.projectile.IProjectileCompatibility;
import me.deecaad.weaponmechanics.compatibility.scope.IScopeCompatibility;
import me.deecaad.weaponmechanics.compatibility.shoot.IShootCompatibility;

public class WeaponCompatibilityAPI {

    private static IWeaponCompatibility weaponCompatibility;

    public static IWeaponCompatibility getWeaponCompatibility() {
        if (weaponCompatibility == null) {
            weaponCompatibility = new CompatibilitySetup().getCompatibleVersion(IWeaponCompatibility.class, "me.deecaad.weaponmechanics.compatibility");
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