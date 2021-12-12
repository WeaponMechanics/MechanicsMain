package me.deecaad.weaponcompatibility;

import me.deecaad.weaponcompatibility.projectile.IProjectileCompatibility;
import me.deecaad.weaponcompatibility.scope.IScopeCompatibility;
import me.deecaad.weaponcompatibility.shoot.IShootCompatibility;

import javax.annotation.Nonnull;

public interface IWeaponCompatibility {

    /**
     * @return the scope compatibility
     */
    @Nonnull
    IScopeCompatibility getScopeCompatibility();

    /**
     * @return the projectile compatibility
     */
    @Nonnull
    IProjectileCompatibility getProjectileCompatibility();

    /**
     * @return the shoot compatibility
     */
    @Nonnull
    IShootCompatibility getShootCompatibility();
}