package me.deecaad.compatibility;

import me.deecaad.compatibility.nbt.INBTCompatibility;
import me.deecaad.compatibility.projectile.IProjectileCompatibility;
import me.deecaad.compatibility.projectile.Projectile_1_15_R1;
import me.deecaad.compatibility.scope.IScopeCompatibility;
import me.deecaad.compatibility.scope.Scope_1_15_R1;
import me.deecaad.compatibility.shoot.IShootCompatibility;
import me.deecaad.compatibility.shoot.Shoot_1_15_R1;
import me.deecaad.core.utils.DebugUtil;
import me.deecaad.core.utils.LogLevel;
import net.minecraft.server.v1_15_R1.EntityPlayer;
import net.minecraft.server.v1_15_R1.Packet;
import net.minecraft.server.v1_15_R1.PlayerConnection;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_15_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_15_R1.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;

public class v1_15_R1 implements ICompatibility {

    private IScopeCompatibility scopeCompatibility;
    private IProjectileCompatibility projectileCompatibility;
    private IShootCompatibility shootCompatibility;

    @Override
    public boolean isNotFullySupported() {
        return false;
    }

    @Override
    public int getPing(Player player) {
        return getEntityPlayer(player).ping;
    }

    @Override
    public Entity getEntityById(World world, int entityId) {
        return ((CraftWorld) world).getHandle().getEntity(entityId).getBukkitEntity();
    }

    @Override
    public void sendPackets(Player player, Object... packets) {
        PlayerConnection playerConnection = getEntityPlayer(player).playerConnection;
        for (Object packet : packets) {
            playerConnection.sendPacket((Packet<?>) packet);
        }
    }

    @Override
    public IScopeCompatibility getScopeCompatibility() {
        return scopeCompatibility == null ? scopeCompatibility = new Scope_1_15_R1() : scopeCompatibility;
    }

    @Override
    public INBTCompatibility getNBTCompatibility() {
        DebugUtil.log(LogLevel.WARN, "NBT compatibility interface should not be used in this server version.",
                "Only version 1.13 R1 and versions before that should access this method.");
        return null;
    }

    @Override
    public IProjectileCompatibility getProjectileCompatibility() {
        return projectileCompatibility == null ? projectileCompatibility = new Projectile_1_15_R1() : projectileCompatibility;
    }

    @Nonnull
    @Override
    public IShootCompatibility getShootCompatibility() {
        return shootCompatibility == null ? shootCompatibility = new Shoot_1_15_R1() : shootCompatibility;
    }

    public EntityPlayer getEntityPlayer(Player player) {
        return ((CraftPlayer) player).getHandle();
    }
}