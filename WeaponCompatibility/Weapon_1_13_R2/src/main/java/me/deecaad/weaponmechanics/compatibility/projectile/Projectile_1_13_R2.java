package me.deecaad.weaponmechanics.compatibility.projectile;

import me.deecaad.core.utils.LogLevel;
import me.deecaad.core.utils.ReflectionUtil;
import me.deecaad.weaponmechanics.WeaponMechanics;

public class Projectile_1_13_R2 implements IProjectileCompatibility {

    static {
        if (ReflectionUtil.getMCVersion() != 13) {
            WeaponMechanics.debug.log(
                    LogLevel.ERROR,
                    "Loaded " + Projectile_1_13_R2.class + " when not using Minecraft 13",
                    new InternalError()
            );
        }
    }
}