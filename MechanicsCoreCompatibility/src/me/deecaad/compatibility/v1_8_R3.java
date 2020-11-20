package me.deecaad.compatibility;

import me.deecaad.compatibility.block.BlockCompatibility;
import me.deecaad.compatibility.block.Block_1_8_R3;
import me.deecaad.compatibility.entity.EntityCompatibility;
import me.deecaad.compatibility.entity.Entity_1_8_R3;
import me.deecaad.compatibility.item.nbt.INBTCompatibility;
import net.minecraft.server.v1_8_R3.EntityPlayer;
import net.minecraft.server.v1_8_R3.Packet;
import net.minecraft.server.v1_8_R3.PlayerConnection;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;

public class v1_8_R3 implements ICompatibility {

    private EntityCompatibility entityCompatibility;
    private BlockCompatibility blockCompatibility;

    public v1_8_R3() {
        entityCompatibility = new Entity_1_8_R3();
        blockCompatibility = new Block_1_8_R3();
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
    public Entity getEntityById(World bukkitWorld, int entityId) {
        net.minecraft.server.v1_8_R3.World world = ((CraftWorld) bukkitWorld).getHandle();

        net.minecraft.server.v1_8_R3.Entity entity = world.a(entityId);
        return entity == null ? null : entity.getBukkitEntity();
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
        throw new UnsupportedOperationException("You cannot use NBT in this version");
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