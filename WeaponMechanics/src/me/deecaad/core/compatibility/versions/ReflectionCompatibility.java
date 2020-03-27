package me.deecaad.core.compatibility.versions;

import io.netty.channel.Channel;
import me.deecaad.core.compatibility.CompatibilityAPI;
import me.deecaad.core.utils.ReflectionUtil;
import me.deecaad.weaponmechanics.compatibility.nbt.INBTCompatibility;
import me.deecaad.weaponmechanics.compatibility.nbt.NBT_Reflection;
import me.deecaad.weaponmechanics.compatibility.projectile.IProjectileCompatibility;
import me.deecaad.weaponmechanics.compatibility.projectile.Projectile_Reflection;
import me.deecaad.weaponmechanics.compatibility.scope.IScopeCompatibility;
import me.deecaad.weaponmechanics.compatibility.scope.Scope_Reflection;
import me.deecaad.weaponmechanics.compatibility.shoot.IShootCompatibility;
import me.deecaad.weaponmechanics.compatibility.shoot.Shoot_Reflection;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Reflection compatibility added as backup plan if fully supported NMS version isn't found
 */
public class ReflectionCompatibility implements ICompatibility {

    private IScopeCompatibility scopeCompatibility;
    private INBTCompatibility nbtCompatibility;
    private IProjectileCompatibility projectileCompatibility;
    private IShootCompatibility shootCompatibility;

    private Method playerGetHandle;
    private Field playerConnection;
    private Field playerPing;
    private Field networkManager;
    private Field channelField;

    private Method worldGetHandle;
    private Method getEntityById;
    private Method getAsBukkitEntity;

    private Method sendPacketMethod;

    public ReflectionCompatibility() {
        this.playerGetHandle = ReflectionUtil.getMethod(ReflectionUtil.getCBClass("entity.CraftPlayer"), "getHandle");
        this.playerConnection = ReflectionUtil.getField(ReflectionUtil.getNMSClass("EntityPlayer"), "playerConnection");
        this.playerPing = ReflectionUtil.getField(ReflectionUtil.getNMSClass("EntityPlayer"), "ping");
        this.networkManager = ReflectionUtil.getField(ReflectionUtil.getNMSClass("PlayerConnection"), "networkManager");
        this.channelField = ReflectionUtil.getField(ReflectionUtil.getNMSClass("NetworkManager"), "channel");
        this.worldGetHandle = ReflectionUtil.getMethod(ReflectionUtil.getCBClass("CraftWorld"), "getHandle");
        this.getEntityById = ReflectionUtil.getMethod(ReflectionUtil.getNMSClass("World"), CompatibilityAPI.getVersion() < 1.09 ? "a" : "getEntity", int.class);
        this.getAsBukkitEntity = ReflectionUtil.getMethod(ReflectionUtil.getNMSClass("Entity"), "getBukkitEntity");
        this.sendPacketMethod = ReflectionUtil.getMethod(ReflectionUtil.getNMSClass("PlayerConnection"), "sendPacket", ReflectionUtil.getNMSClass("Packet"));
    }

    @Override
    public boolean isNotFullySupported() {
        // Not fully supported because of reflections... They're here just as "backup plan"
        return true;
    }

    @Override
    public int getPing(Player player) {
        Object entityPlayer = ReflectionUtil.invokeMethod(this.playerGetHandle, player);
        return (int) ReflectionUtil.invokeField(this.playerPing, entityPlayer);
    }

    @Override
    public Channel getChannel(Player player) {
        Object entityPlayer = ReflectionUtil.invokeMethod(this.playerGetHandle, player);
        Object playerConnection = ReflectionUtil.invokeField(this.playerConnection, entityPlayer);
        return (Channel) ReflectionUtil.invokeField(channelField, ReflectionUtil.invokeField(networkManager, playerConnection));
    }

    @Override
    public Entity getEntityById(World world, int entityId) {
        Object worldServer = ReflectionUtil.invokeMethod(this.worldGetHandle, world);
        Object entity = ReflectionUtil.invokeMethod(this.getEntityById, worldServer, entityId);
        return (Entity) ReflectionUtil.invokeMethod(this.getAsBukkitEntity, entity);
    }

    @Override
    public void sendPackets(Player player, Object... packets) {
        Object entityPlayer = ReflectionUtil.invokeMethod(this.playerGetHandle, player);
        Object playerConnection = ReflectionUtil.invokeField(this.playerConnection, entityPlayer);
        for (Object packet : packets) {
            ReflectionUtil.invokeMethod(sendPacketMethod, playerConnection, packet);
        }
    }

    @Override
    public IScopeCompatibility getScopeCompatibility() {
        return scopeCompatibility == null ? scopeCompatibility = new Scope_Reflection() : scopeCompatibility;
    }

    @Override
    public INBTCompatibility getNBTCompatibility() {
        return nbtCompatibility == null ? nbtCompatibility = new NBT_Reflection() : nbtCompatibility;
    }

    @Override
    public IProjectileCompatibility getProjectileCompatibility() {
        return projectileCompatibility == null ? projectileCompatibility = new Projectile_Reflection() : projectileCompatibility;
    }

    @Override
    public IShootCompatibility getShootCompatibility() {
        return shootCompatibility == null ? shootCompatibility = new Shoot_Reflection() : shootCompatibility;
    }
}