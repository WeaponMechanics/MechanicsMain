package me.deecaad.weaponcompatibility;

import me.deecaad.weaponcompatibility.projectile.IProjectileCompatibility;
import me.deecaad.weaponcompatibility.projectile.Projectile_1_15_R1;
import me.deecaad.weaponcompatibility.scope.IScopeCompatibility;
import me.deecaad.weaponcompatibility.scope.Scope_1_15_R1;
import me.deecaad.weaponcompatibility.shoot.IShootCompatibility;
import me.deecaad.weaponcompatibility.shoot.Shoot_1_15_R1;

public class v1_15_R1 implements IWeaponCompatibility {

    private IScopeCompatibility scopeCompatibility;
    private IProjectileCompatibility projectileCompatibility;
    private IShootCompatibility shootCompatibility;

    @Override
    public IScopeCompatibility getScopeCompatibility() {
        return scopeCompatibility == null ? scopeCompatibility = new Scope_1_15_R1() : scopeCompatibility;
    }

    @Override
    public IProjectileCompatibility getProjectileCompatibility() {
        return projectileCompatibility == null ? projectileCompatibility = new Projectile_1_15_R1() : projectileCompatibility;
    }

    @Override
    public IShootCompatibility getShootCompatibility() {
        return shootCompatibility == null ? shootCompatibility = new Shoot_1_15_R1() : shootCompatibility;
    }
}