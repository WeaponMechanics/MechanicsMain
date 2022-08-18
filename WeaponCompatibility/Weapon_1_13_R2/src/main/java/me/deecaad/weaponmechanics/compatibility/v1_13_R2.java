package me.deecaad.weaponmechanics.compatibility;

import me.deecaad.core.utils.LogLevel;
import me.deecaad.core.utils.ReflectionUtil;
import me.deecaad.weaponmechanics.WeaponMechanics;
import me.deecaad.weaponmechanics.compatibility.scope.IScopeCompatibility;
import me.deecaad.weaponmechanics.compatibility.scope.Scope_1_13_R2;
import me.deecaad.weaponmechanics.weapon.projectile.HitBox;
import net.minecraft.server.v1_13_R2.AxisAlignedBB;
import net.minecraft.server.v1_13_R2.DamageSource;
import net.minecraft.server.v1_13_R2.EntityLiving;
import net.minecraft.server.v1_13_R2.PacketPlayOutPosition;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_13_R2.block.CraftBlock;
import org.bukkit.craftbukkit.v1_13_R2.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_13_R2.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_13_R2.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class v1_13_R2 implements IWeaponCompatibility {

    static {
        if (ReflectionUtil.getMCVersion() != 13) {
            WeaponMechanics.debug.log(
                    LogLevel.ERROR,
                    "Loaded " + v1_13_R2.class + " when not using Minecraft 13",
                    new InternalError()
            );
        }
    }

    private Set<PacketPlayOutPosition.EnumPlayerTeleportFlags> RELATIVE_FLAGS = new HashSet<>(Arrays.asList(PacketPlayOutPosition.EnumPlayerTeleportFlags.X,
            PacketPlayOutPosition.EnumPlayerTeleportFlags.Y,
            PacketPlayOutPosition.EnumPlayerTeleportFlags.Z,
            PacketPlayOutPosition.EnumPlayerTeleportFlags.X_ROT,
            PacketPlayOutPosition.EnumPlayerTeleportFlags.Y_ROT));

    private Set<PacketPlayOutPosition.EnumPlayerTeleportFlags> ABSOLUTE_FLAGS = new HashSet<>(Arrays.asList(PacketPlayOutPosition.EnumPlayerTeleportFlags.X,
            PacketPlayOutPosition.EnumPlayerTeleportFlags.Y,
            PacketPlayOutPosition.EnumPlayerTeleportFlags.Z));

    private final IScopeCompatibility scopeCompatibility;

    public v1_13_R2() {
        this.scopeCompatibility = new Scope_1_13_R2();
    }

    @Nonnull
    @Override
    public IScopeCompatibility getScopeCompatibility() {
        return scopeCompatibility;
    }

    @Override
    public HitBox getHitBox(Block block, boolean allowLiquid) {
        if (block.isEmpty()) return null;

        boolean isLiquid = block.isLiquid();
        if (!allowLiquid) {
            if (block.isPassable() || block.isLiquid()) return null;
        } else if (!isLiquid && block.isPassable()) {
            // Check like this because liquid is also passable...
            return null;
        }

        HitBox hitBox;
        if (isLiquid) {
            hitBox = new HitBox(block.getX(), block.getY(), block.getZ(), block.getX() + 1, block.getY() + 1, block.getZ() + 1);
        } else {
            BoundingBox boundingBox = block.getBoundingBox();
            hitBox = new HitBox(boundingBox.getMinX(), boundingBox.getMinY(), boundingBox.getMinZ(), boundingBox.getMaxX(), boundingBox.getMaxY(), boundingBox.getMaxZ());
        }
        hitBox.setBlockHitBox(block);

        if (WeaponMechanics.getBasicConfigurations().getBool("Check_Accurate_Hitboxes", true)) {
            CraftBlock craftBlock = (CraftBlock) block;
            List<AxisAlignedBB> voxelShape = craftBlock.getNMS().getCollisionShape(craftBlock.getCraftWorld().getHandle(), craftBlock.getPosition()).d();
            if (voxelShape.size() > 1) {
                int x = block.getX();
                int y = block.getY();
                int z = block.getZ();
                for (AxisAlignedBB boxPart : voxelShape) {
                    hitBox.addVoxelShapePart(new HitBox(x + boxPart.minX, y + boxPart.minY, z + boxPart.minZ,
                            x + boxPart.maxX, y + boxPart.maxY, z + boxPart.maxZ));
                }
            }
        }

        return hitBox;
    }

    @Override
    public Vector getLastLocation(Entity entity) {
        net.minecraft.server.v1_13_R2.Entity nms = ((CraftEntity) entity).getHandle();
        return new Vector(nms.lastX, nms.lastY, nms.lastZ);
    }

    @Override
    public void modifyCameraRotation(Player player, float yaw, float pitch, boolean absolute) {
        pitch *= -1;
        ((CraftPlayer) player).getHandle().playerConnection.
                sendPacket(new PacketPlayOutPosition(0, 0, 0, yaw, pitch, absolute ? ABSOLUTE_FLAGS : RELATIVE_FLAGS, 0));
    }

    @Override
    public void logDamage(LivingEntity victim, LivingEntity source, double health, double damage, boolean isMelee) {
        DamageSource damageSource;

        if (isMelee) {
            if (source instanceof Player) {
                damageSource = DamageSource.playerAttack(((org.bukkit.craftbukkit.v1_13_R2.entity.CraftPlayer) source).getHandle());
            } else {
                damageSource = DamageSource.mobAttack(((CraftLivingEntity) source).getHandle());
            }
        } else {
            damageSource = DamageSource.projectile(null, ((CraftLivingEntity) source).getHandle());
        }

        EntityLiving nms = ((CraftLivingEntity) victim).getHandle();
        nms.combatTracker.trackDamage(damageSource, (float) damage, (float) health);
        nms.setLastDamager(((CraftLivingEntity) source).getHandle());
    }

    @Override
    public void setKiller(LivingEntity victim, Player killer) {
        ((CraftLivingEntity) victim).getHandle().killer = ((CraftPlayer) killer).getHandle();
    }
}
