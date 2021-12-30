package me.deecaad.core.compatibility.entity;

import net.minecraft.server.v1_16_R3.Entity;
import net.minecraft.server.v1_16_R3.EntityFallingBlock;
import net.minecraft.server.v1_16_R3.EntityItem;
import net.minecraft.server.v1_16_R3.PacketPlayOutEntityDestroy;
import net.minecraft.server.v1_16_R3.PacketPlayOutEntityMetadata;
import net.minecraft.server.v1_16_R3.PacketPlayOutEntityVelocity;
import net.minecraft.server.v1_16_R3.PacketPlayOutSpawnEntity;
import net.minecraft.server.v1_16_R3.PlayerConnection;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_16_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_16_R3.block.CraftBlockState;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_16_R3.inventory.CraftItemStack;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FakeEntity_1_16_R3 extends FakeEntity {

    private Entity entity;

    public FakeEntity_1_16_R3(@NotNull Location location, @NotNull EntityType type, @Nullable Object data) {
        super(location, type, data);
    }

    @Override
    protected void init(@NotNull Location location, @NotNull EntityType type, @Nullable Object data) {
        if (location.getWorld() == null)
            throw new IllegalArgumentException();

        CraftWorld world = (CraftWorld) location.getWorld();
        Entity entity;

        if (type == EntityType.DROPPED_ITEM && data != null) {
            entity = new EntityItem(world.getHandle(), location.getX(), location.getY(), location.getZ(), CraftItemStack.asNMSCopy((ItemStack) data));
        } else if (type == EntityType.FALLING_BLOCK && data != null) {
            entity = new EntityFallingBlock(world.getHandle(), location.getX(), location.getY(), location.getZ(), ((CraftBlockState) data).getHandle());
        } else {
            entity = world.createEntity(location, type.getEntityClass());
        }

        this.entity = entity;
        this.cache = entity.getId();
    }

    @Override
    public void setMotion(double dx, double dy, double dz) {

    }

    @Override
    public void setPosition(double x, double y, double z) {

    }

    @Override
    public void setRotation(float yaw, float pitch, boolean absolute) {

    }

    @Override
    public void setPositionRotation(short dx, short dy, short dz, byte yaw, byte pitch) {

    }

    @Override
    public void show(Player player) {
        PlayerConnection connection = ((CraftPlayer) player).getHandle().playerConnection;
        connection.sendPacket(new PacketPlayOutSpawnEntity(entity));

        showMeta(player);
    }

    @Override
    public void showMeta(Player player) {
        PlayerConnection connection = ((CraftPlayer) player).getHandle().playerConnection;


    }

    @Override
    public void remove(Player player) {
        PlayerConnection connection = ((CraftPlayer) player).getHandle().playerConnection;
        connection.sendPacket(new PacketPlayOutEntityDestroy(cache));
    }
}
