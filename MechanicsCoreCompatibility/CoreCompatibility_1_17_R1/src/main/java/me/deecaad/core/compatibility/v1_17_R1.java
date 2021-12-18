package me.deecaad.core.compatibility;

import me.deecaad.core.compatibility.block.BlockCompatibility;
import me.deecaad.core.compatibility.block.Block_1_17_R1;
import me.deecaad.core.compatibility.entity.EntityCompatibility;
import me.deecaad.core.compatibility.entity.Entity_1_17_R1;
import me.deecaad.core.compatibility.nbt.NBTCompatibility;
import me.deecaad.core.compatibility.nbt.NBT_1_17_R1;

import net.minecraft.server.network.PlayerConnection;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.network.protocol.Packet;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_17_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;

public class v1_17_R1 implements ICompatibility {

    private final EntityCompatibility entityCompatibility;
    private final BlockCompatibility blockCompatibility;
    private final NBTCompatibility nbtCompatibility;

    public v1_17_R1() {
        entityCompatibility = new Entity_1_17_R1();
        blockCompatibility = new Block_1_17_R1();
        nbtCompatibility = new NBT_1_17_R1();
    }

    @Override
    public int getPing(Player player) {
        return player.getPing();
    }

    @Override
    public Entity getEntityById(World world, int entityId) {
        net.minecraft.world.entity.Entity e = ((CraftWorld) world).getHandle().getEntity(entityId);
        return e == null ? null : e.getBukkitEntity();
    }

    @Override
    public void sendPackets(Player player, Object packet) {
        getEntityPlayer(player).b.sendPacket((Packet<?>) packet);
    }

    @Override
    public void sendPackets(Player player, Object... packets) {
        PlayerConnection playerConnection = getEntityPlayer(player).b;
        for (Object packet : packets) {
            playerConnection.sendPacket((Packet<?>) packet);
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

    @Override
    public EntityPlayer getEntityPlayer(Player player) {
        return ((CraftPlayer) player).getHandle();
    }
}