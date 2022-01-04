package me.deecaad.weaponmechanics.compatibility.projectile;

import me.deecaad.core.utils.LogLevel;
import me.deecaad.core.utils.ReflectionUtil;
import me.deecaad.weaponmechanics.WeaponMechanics;

public class Projectile_1_16_R3 implements IProjectileCompatibility {

    static {
        if (ReflectionUtil.getMCVersion() != 16) {
            WeaponMechanics.debug.log(
                    LogLevel.ERROR,
                    "Loaded " + Projectile_1_16_R3.class + " when not using Minecraft 16",
                    new InternalError()
            );
        }
    }
}