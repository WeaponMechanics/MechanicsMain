package me.deecaad.compatibility;

import me.deecaad.compatibility.block.BlockCompatibility;
import me.deecaad.compatibility.block.BlockReflection;
import me.deecaad.compatibility.entity.EntityCompatibility;
import me.deecaad.compatibility.entity.EntityReflection;
import me.deecaad.compatibility.nbt.NBTCompatibility;
import me.deecaad.compatibility.nbt.NBT_Reflection;
import me.deecaad.core.utils.ReflectionUtil;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Reflection compatibility added as backup plan if fully supported NMS version isn't found
 */
public class Reflection implements ICompatibility {

    private final NBTCompatibility nbtCompatibility;
    private final EntityCompatibility entityCompatibility;
    private final BlockCompatibility blockCompatibility;

    private final Method playerGetHandle;
    private final Field playerConnection;
    private final Field playerPing;

    private final Method worldGetHandle;
    private final Method getEntityById;
    private final Method getAsBukkitEntity;

    private final Method sendPacketMethod;

    public Reflection() {
        nbtCompatibility = new NBT_Reflection();
        entityCompatibility = new EntityReflection();
        blockCompatibility = new BlockReflection();

        this.playerGetHandle = ReflectionUtil.getMethod(ReflectionUtil.getCBClass("entity.CraftPlayer"), "getHandle");
        this.playerConnection = ReflectionUtil.getField(ReflectionUtil.getNMSClass("EntityPlayer"), "playerConnection");
        this.playerPing = ReflectionUtil.getField(ReflectionUtil.getNMSClass("EntityPlayer"), "ping");
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
    public Entity getEntityById(World world, int entityId) {
        Object worldServer = ReflectionUtil.invokeMethod(this.worldGetHandle, world);
        Object entity = ReflectionUtil.invokeMethod(this.getEntityById, worldServer, entityId);
        return (Entity) ReflectionUtil.invokeMethod(this.getAsBukkitEntity, entity);
    }

    @Override
    public void sendPackets(Player player, Object packet) {
        Object entityPlayer = ReflectionUtil.invokeMethod(this.playerGetHandle, player);
        Object playerConnection = ReflectionUtil.invokeField(this.playerConnection, entityPlayer);
        ReflectionUtil.invokeMethod(sendPacketMethod, playerConnection, packet);
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
    public NBTCompatibility getNBTCompatibility() {
        return nbtCompatibility;
    }

    @Nonnull
    @Override
    public EntityCompatibility getEntityCompatibility() {
        return entityCompatibility;
    }

    @Nonnull
    @Override
    public BlockCompatibility getBlockCompatibility() {
        return blockCompatibility;
    }
}