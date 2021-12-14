package me.deecaad.weaponmechanics.compatibility;

import me.deecaad.weaponmechanics.compatibility.projectile.IProjectileCompatibility;
import me.deecaad.weaponmechanics.compatibility.scope.IScopeCompatibility;
import me.deecaad.weaponmechanics.compatibility.shoot.IShootCompatibility;

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