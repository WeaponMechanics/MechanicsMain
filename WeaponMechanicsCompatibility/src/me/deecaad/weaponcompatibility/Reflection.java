package me.deecaad.weaponcompatibility;

import me.deecaad.weaponcompatibility.projectile.IProjectileCompatibility;
import me.deecaad.weaponcompatibility.projectile.Projectile_Reflection;
import me.deecaad.weaponcompatibility.scope.IScopeCompatibility;
import me.deecaad.weaponcompatibility.scope.Scope_Reflection;
import me.deecaad.weaponcompatibility.shoot.IShootCompatibility;
import me.deecaad.weaponcompatibility.shoot.Shoot_Reflection;

public class Reflection implements IWeaponCompatibility {

    private IScopeCompatibility scopeCompatibility;
    private IProjectileCompatibility projectileCompatibility;
    private IShootCompatibility shootCompatibility;

    @Override
    public IScopeCompatibility getScopeCompatibility() {
        return scopeCompatibility == null ? scopeCompatibility = new Scope_Reflection() : scopeCompatibility;
    }

    @Override
    public IProjectileCompatibility getProjectileCompatibility() {
        return projectileCompatibility == null ? projectileCompatibility = new Projectile_Reflection() : projectileCompatibility;
    }

    @Override
    public IShootCompatibility getShootCompatibility() {
        return shootCompatibility == null ? shootCompatibility = new Shoot_Reflection() : shootCompatibility;
    }
}
