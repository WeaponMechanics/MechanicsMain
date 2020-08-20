package me.deecaad.weaponcompatibility;

import me.deecaad.weaponcompatibility.projectile.IProjectileCompatibility;
import me.deecaad.weaponcompatibility.projectile.Projectile_Reflection;
import me.deecaad.weaponcompatibility.scope.IScopeCompatibility;
import me.deecaad.weaponcompatibility.scope.Scope_Reflection;
import me.deecaad.weaponcompatibility.shoot.IShootCompatibility;
import me.deecaad.weaponcompatibility.shoot.Shoot_Reflection;

import javax.annotation.Nonnull;

public class Reflection implements IWeaponCompatibility {

    private IScopeCompatibility scopeCompatibility;
    private IProjectileCompatibility projectileCompatibility;
    private IShootCompatibility shootCompatibility;

    public Reflection() {
        this.scopeCompatibility = new Scope_Reflection();
        this.projectileCompatibility = new Projectile_Reflection();
        this.shootCompatibility = new Shoot_Reflection();
    }

    @Nonnull
    @Override
    public IScopeCompatibility getScopeCompatibility() {
        return scopeCompatibility;
    }

    @Nonnull
    @Override
    public IProjectileCompatibility getProjectileCompatibility() {
        return projectileCompatibility;
    }

    @Nonnull
    @Override
    public IShootCompatibility getShootCompatibility() {
        return shootCompatibility;
    }
}
