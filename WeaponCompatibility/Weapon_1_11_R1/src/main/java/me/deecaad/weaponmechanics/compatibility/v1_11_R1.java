package me.deecaad.weaponmechanics.compatibility;

import me.deecaad.core.utils.LogLevel;
import me.deecaad.core.utils.ReflectionUtil;
import me.deecaad.weaponmechanics.WeaponMechanics;
import me.deecaad.weaponmechanics.compatibility.projectile.IProjectileCompatibility;
import me.deecaad.weaponmechanics.compatibility.projectile.Projectile_1_11_R1;
import me.deecaad.weaponmechanics.compatibility.scope.IScopeCompatibility;
import me.deecaad.weaponmechanics.compatibility.scope.Scope_1_11_R1;
import me.deecaad.weaponmechanics.compatibility.shoot.IShootCompatibility;
import me.deecaad.weaponmechanics.compatibility.shoot.Shoot_1_11_R1;

import javax.annotation.Nonnull;

public class v1_11_R1 implements IWeaponCompatibility {

    static {
        if (ReflectionUtil.getMCVersion() != 11) {
            WeaponMechanics.debug.log(
                    LogLevel.ERROR,
                    "Loaded " + v1_11_R1.class + " when not using Minecraft 11",
                    new InternalError()
            );
        }
    }

    private final IScopeCompatibility scopeCompatibility;
    private final IProjectileCompatibility projectileCompatibility;
    private final IShootCompatibility shootCompatibility;

    public v1_11_R1() {
        this.scopeCompatibility = new Scope_1_11_R1();
        this.projectileCompatibility = new Projectile_1_11_R1();
        this.shootCompatibility = new Shoot_1_11_R1();
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
