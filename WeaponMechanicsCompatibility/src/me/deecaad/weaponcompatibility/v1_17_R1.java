package me.deecaad.weaponcompatibility;

import me.deecaad.weaponcompatibility.projectile.IProjectileCompatibility;
import me.deecaad.weaponcompatibility.projectile.Projectile_1_17_R1;
import me.deecaad.weaponcompatibility.scope.IScopeCompatibility;
import me.deecaad.weaponcompatibility.scope.Scope_1_17_R1;
import me.deecaad.weaponcompatibility.shoot.IShootCompatibility;
import me.deecaad.weaponcompatibility.shoot.Shoot_1_17_R1;

import javax.annotation.Nonnull;

public class v1_17_R1 implements IWeaponCompatibility {

    private final IScopeCompatibility scopeCompatibility;
    private final IProjectileCompatibility projectileCompatibility;
    private final IShootCompatibility shootCompatibility;

    public v1_17_R1() {
        this.scopeCompatibility = new Scope_1_17_R1();
        this.projectileCompatibility = new Projectile_1_17_R1();
        this.shootCompatibility = new Shoot_1_17_R1();
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