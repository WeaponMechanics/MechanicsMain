package me.deecaad.weaponmechanics.compatibility;

import me.deecaad.core.utils.LogLevel;
import me.deecaad.core.utils.ReflectionUtil;
import me.deecaad.weaponmechanics.WeaponMechanics;
import me.deecaad.weaponmechanics.compatibility.scope.IScopeCompatibility;
import me.deecaad.weaponmechanics.compatibility.scope.Scope_1_9_R2;
import me.deecaad.weaponmechanics.weapon.projectile.HitBox;
import net.minecraft.server.v1_9_R2.*;
import org.bukkit.craftbukkit.v1_9_R2.CraftWorld;
import org.bukkit.craftbukkit.v1_9_R2.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_9_R2.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_9_R2.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.*;
import org.bukkit.util.Vector;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class v1_9_R2 implements IWeaponCompatibility {

    static {
        if (ReflectionUtil.getMCVersion() != 9) {
            WeaponMechanics.debug.log(
                    LogLevel.ERROR,
                    "Loaded " + v1_9_R2.class + " when not using Minecraft 9",
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

    public v1_9_R2() {
        this.scopeCompatibility = new Scope_1_9_R2();
    }

    @Nonnull
    @Override
    public IScopeCompatibility getScopeCompatibility() {
        return scopeCompatibility;
    }

    @Override
    public HitBox getHitBox(org.bukkit.entity.Entity entity) {
        if (entity.isInvulnerable() || !entity.getType().isAlive() || entity.isDead()) return null;

        HitBox hitBox = new HitBox(entity.getLocation().toVector(), getLastLocation(entity))
                .grow(getWidth(entity), getHeight(entity));
        hitBox.setLivingEntity((LivingEntity) entity);

        if (entity instanceof ComplexLivingEntity && WeaponMechanics.getBasicConfigurations().getBool("Check_Accurate_Hitboxes", true)) {
            for (ComplexEntityPart entityPart : ((ComplexLivingEntity) entity).getParts()) {
                AxisAlignedBB boxPart = ((CraftEntity) entityPart).getHandle().getBoundingBox();
                hitBox.addVoxelShapePart(new HitBox(boxPart.a, boxPart.b, boxPart.c, boxPart.d, boxPart.e, boxPart.f));
            }
        }
        return hitBox;
    }

    @Override
    public HitBox getHitBox(org.bukkit.block.Block block, boolean allowLiquid) {
        if (block.isEmpty()) return null;

        boolean isLiquid = block.isLiquid();
        if (!allowLiquid && isLiquid) return null;

        if (isLiquid) {
            HitBox hitBox = new HitBox(block.getX(), block.getY(), block.getZ(), block.getX() + 1, block.getY() + 1, block.getZ() + 1);
            hitBox.setBlockHitBox(block);
            return hitBox;
        }

        WorldServer worldServer = ((CraftWorld) block.getWorld()).getHandle();
        BlockPosition blockPosition = new BlockPosition(block.getX(), block.getY(), block.getZ());
        IBlockData blockData = worldServer.getType(blockPosition);
        Block nmsBlock = blockData.getBlock();

        // Passable block check -> false means passable (thats why !)
        if (!(blockData.d(worldServer, blockPosition) != Block.k && nmsBlock.a(blockData, false))) return null;

        AxisAlignedBB aabb = blockData.c(worldServer, blockPosition);
        // 1.12 -> e
        // 1.11 -> d
        // 1.9 - 1.10 -> c

        int x = blockPosition.getX(), y = blockPosition.getY(), z = blockPosition.getZ();
        HitBox hitBox = new HitBox(x + aabb.a, y + aabb.b, z + aabb.c, x + aabb.d, y + aabb.e, z + aabb.f);
        hitBox.setBlockHitBox(block);
        return hitBox;
    }

    @Override
    public double getWidth(org.bukkit.entity.Entity entity) {
        return ((CraftEntity) entity).getHandle().width;
    }

    @Override
    public double getHeight(Entity entity) {
        return ((CraftEntity) entity).getHandle().length;
    }

    @Override
    public Vector getLastLocation(Entity entity) {
        net.minecraft.server.v1_9_R2.Entity nms = ((CraftEntity) entity).getHandle();
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
                damageSource = DamageSource.playerAttack(((org.bukkit.craftbukkit.v1_9_R2.entity.CraftPlayer) source).getHandle());
            } else {
                damageSource = DamageSource.mobAttack(((CraftLivingEntity) source).getHandle());
            }
        } else {
            damageSource = DamageSource.projectile(null, ((CraftLivingEntity) source).getHandle());
        }

        EntityLiving nms = ((CraftLivingEntity) victim).getHandle();
        nms.combatTracker.trackDamage(damageSource, (float) damage, (float) health);
        nms.a(((CraftLivingEntity) source).getHandle());
    }

    @Override
    public void setKiller(LivingEntity victim, Player killer) {
        ((CraftLivingEntity) victim).getHandle().killer = ((CraftPlayer) killer).getHandle();
    }
}