package me.deecaad.compatibility;

import me.deecaad.compatibility.block.BlockCompatibility;
import me.deecaad.compatibility.block.Block_1_15_R1;
import me.deecaad.compatibility.entity.EntityCompatibility;
import me.deecaad.compatibility.entity.Entity_1_15_R1;
import me.deecaad.compatibility.item.nbt.INBTCompatibility;
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

import static me.deecaad.core.MechanicsCore.debug;

public class v1_15_R1 implements ICompatibility {

    private EntityCompatibility entityCompatibility;
    private BlockCompatibility blockCompatibility;

    public v1_15_R1() {
        entityCompatibility = new Entity_1_15_R1();
        blockCompatibility = new Block_1_15_R1();
    }

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
    public INBTCompatibility getNBTCompatibility() {
        debug.log(LogLevel.WARN, "NBT compatibility interface should not be used in this server version.",
                "Only version 1.13 R1 and versions before that should access this method.");
        return null;
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

    public EntityPlayer getEntityPlayer(Player player) {
        return ((CraftPlayer) player).getHandle();
    }
}