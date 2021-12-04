package me.deecaad.weaponcompatibility;

import me.deecaad.weaponcompatibility.projectile.IProjectileCompatibility;
import me.deecaad.weaponcompatibility.projectile.Projectile_1_8_R3;
import me.deecaad.weaponcompatibility.scope.IScopeCompatibility;
import me.deecaad.weaponcompatibility.scope.Scope_1_8_R3;
import me.deecaad.weaponcompatibility.shoot.IShootCompatibility;
import me.deecaad.weaponcompatibility.shoot.Shoot_1_8_R3;

import javax.annotation.Nonnull;

public class v1_8_R3 implements IWeaponCompatibility {

    private final IScopeCompatibility scopeCompatibility;
    private final IProjectileCompatibility projectileCompatibility;
    private final IShootCompatibility shootCompatibility;

    public v1_8_R3() {
        this.scopeCompatibility = new Scope_1_8_R3();
        this.projectileCompatibility = new Projectile_1_8_R3();
        this.shootCompatibility = new Shoot_1_8_R3();
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
