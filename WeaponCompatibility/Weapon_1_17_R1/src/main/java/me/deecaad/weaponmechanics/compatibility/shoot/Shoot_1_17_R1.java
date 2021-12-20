package me.deecaad.weaponmechanics.compatibility.shoot;

import me.deecaad.core.utils.LogLevel;
import me.deecaad.core.utils.ReflectionUtil;
import me.deecaad.weaponmechanics.WeaponMechanics;
import me.deecaad.weaponmechanics.compatibility.scope.Scope_1_17_R1;
import net.minecraft.network.protocol.game.PacketPlayOutPosition;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityLiving;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftPlayer;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class Shoot_1_17_R1 implements IShootCompatibility {

    static {
        if (ReflectionUtil.getMCVersion() != 17) {
            WeaponMechanics.debug.log(
                    LogLevel.ERROR,
                    "Loaded " + Shoot_1_17_R1.class + " when not using Minecraft 17",
                    new InternalError()
            );
        }
    }

    private final Set<PacketPlayOutPosition.EnumPlayerTeleportFlags> RELATIVE_FLAGS = new HashSet<>(Arrays.asList(
            PacketPlayOutPosition.EnumPlayerTeleportFlags.a,
            PacketPlayOutPosition.EnumPlayerTeleportFlags.b,
            PacketPlayOutPosition.EnumPlayerTeleportFlags.c,
            PacketPlayOutPosition.EnumPlayerTeleportFlags.d,
            PacketPlayOutPosition.EnumPlayerTeleportFlags.e));

    private final Set<PacketPlayOutPosition.EnumPlayerTeleportFlags> ABSOLUTE_FLAGS = new HashSet<>(Arrays.asList(PacketPlayOutPosition.EnumPlayerTeleportFlags.a,
            PacketPlayOutPosition.EnumPlayerTeleportFlags.b,
            PacketPlayOutPosition.EnumPlayerTeleportFlags.c));

    @Override
    public void modifyCameraRotation(Player player, float yaw, float pitch, boolean absolute) {
        pitch *= -1;
        ((CraftPlayer) player).getHandle().b.sendPacket(new PacketPlayOutPosition(0, 0, 0, yaw, pitch, absolute ? ABSOLUTE_FLAGS : RELATIVE_FLAGS, 0, true));
    }

    @Override
    public void logDamage(LivingEntity victim, LivingEntity source, double health, double damage, boolean isMelee) {
        DamageSource damageSource;

        if (isMelee) {
            if (source instanceof Player) {
                damageSource = DamageSource.playerAttack(((org.bukkit.craftbukkit.v1_17_R1.entity.CraftPlayer) source).getHandle());
            } else {
                damageSource = DamageSource.mobAttack(((CraftLivingEntity) source).getHandle());
            }
        } else {
            damageSource = DamageSource.projectile(null, ((CraftLivingEntity) source).getHandle());
        }

        EntityLiving nms = ((CraftLivingEntity) victim).getHandle();
        nms.bQ.trackDamage(damageSource, (float) damage, (float) health);
    }

    @Override
    public void setKiller(LivingEntity victim, Player killer) {
        ((CraftLivingEntity) victim).getHandle().bW = ((CraftPlayer) killer).getHandle();
    }
}