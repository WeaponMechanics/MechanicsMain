package me.deecaad.core.compatibility;

import me.deecaad.core.compatibility.block.BlockCompatibility;
import me.deecaad.core.compatibility.block.Block_1_21_R3;
import me.deecaad.core.compatibility.entity.EntityCompatibility;
import me.deecaad.core.compatibility.entity.Entity_1_21_R3;
import me.deecaad.core.compatibility.nbt.NBTCompatibility;
import me.deecaad.core.compatibility.nbt.NBT_1_21_R3;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class v1_21_R3 implements ICompatibility {

    private final EntityCompatibility entityCompatibility;
    private final BlockCompatibility blockCompatibility;
    private final NBTCompatibility nbtCompatibility;

    public v1_21_R3() {
        entityCompatibility = new Entity_1_21_R3();
        blockCompatibility = new Block_1_21_R3();
        nbtCompatibility = new NBT_1_21_R3();
    }

    @Override
    public void sendPackets(Player player, Object packet) {
        getEntityPlayer(player).connection.send((Packet<?>) packet);
    }

    @Override
    public void sendPackets(Player player, Object... packets) {
        ServerGamePacketListenerImpl playerConnection = getEntityPlayer(player).connection;
        for (Object packet : packets) {
            playerConnection.send((Packet<?>) packet);
        }
    }

    @Override
    public @NotNull NBTCompatibility getNBTCompatibility() {
        return nbtCompatibility;
    }

    @Override
    public @NotNull EntityCompatibility getEntityCompatibility() {
        return entityCompatibility;
    }

    @Override
    public @NotNull BlockCompatibility getBlockCompatibility() {
        return blockCompatibility;
    }

    @Override
    public @NotNull ServerPlayer getEntityPlayer(@NotNull Player player) {
        return ((CraftPlayer) player).getHandle();
    }
}