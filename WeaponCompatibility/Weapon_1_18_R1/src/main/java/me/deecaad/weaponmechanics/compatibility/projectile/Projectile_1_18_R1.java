package me.deecaad.weaponmechanics.compatibility.projectile;

import me.deecaad.core.utils.LogLevel;
import me.deecaad.core.utils.ReflectionUtil;
import me.deecaad.weaponmechanics.WeaponMechanics;

public class Projectile_1_18_R1 implements IProjectileCompatibility {

    static {
        if (ReflectionUtil.getMCVersion() != 18) {
            WeaponMechanics.debug.log(
                    LogLevel.ERROR,
                    "Loaded " + Projectile_1_18_R1.class + " when not using Minecraft 18",
                    new InternalError()
            );
        }
    }
}