package me.deecaad.core.effects.types;

import me.deecaad.compatibility.CompatibilityAPI;
import me.deecaad.core.effects.Effect;
import net.minecraft.server.v1_15_R1.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_15_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_15_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_15_R1.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.stream.Collectors;

public class ItemEffect extends Effect {

    private ItemStack toDrop;
    private int ticksAlive;

    public ItemEffect(ItemStack toDrop, int ticksAlive) {
        this.toDrop = toDrop;
        this.ticksAlive = ticksAlive;
    }

    public ItemEffect(Material type, int amount, int ticksAlive) {
        this.toDrop = new ItemStack(type, amount);
        this.ticksAlive = ticksAlive;
    }

    @Override
    public void spawnOnce(@Nonnull Plugin source, @Nonnull World world, double x, double y, double z, @Nullable Object data) {

        // todo Reflection/Compatibility api
        EntityItem drop = new EntityItem(((CraftWorld)world).getHandle(), x, y, z, CraftItemStack.asNMSCopy(toDrop));

        PacketPlayOutSpawnEntity spawnPacket = new PacketPlayOutSpawnEntity(drop);
        PacketPlayOutEntityMetadata metaPacket = new PacketPlayOutEntityMetadata(drop.getId(), drop.getDataWatcher(), true);
        PacketPlayOutEntityDestroy destroyPacket = new PacketPlayOutEntityDestroy(drop.getId());

        List<Player> players = world.getEntitiesByClass(Player.class).stream()
                .filter(player -> {

                    Location loc = player.getLocation();
                    double dx = x - loc.getX();
                    double dy = y - loc.getY();
                    double dz = z - loc.getZ();

                    return Math.sqrt(dx * dx + dy * dy + dz * dz) <= VIEW_DISTANCE;
                })
                .collect(Collectors.toList());

        for (Player player : players) {
            PlayerConnection connection = ((CraftPlayer) player).getHandle().playerConnection;
            connection.sendPacket(spawnPacket);
            connection.sendPacket(metaPacket);

            Bukkit.getScheduler().runTaskLater(source, () -> connection.sendPacket(destroyPacket), ticksAlive);
        }
    }

    @Override
    protected void spawnOnceFor(@Nonnull Plugin source, @Nonnull Player player, @Nonnull World world, double x, double y, double z, @Nullable Object data) {
        // todo Reflection/Compatibility api
        EntityItem drop = new EntityItem(((CraftWorld)world).getHandle(), x, y, z, CraftItemStack.asNMSCopy(toDrop));

        PacketPlayOutSpawnEntity spawnPacket = new PacketPlayOutSpawnEntity(drop);
        PacketPlayOutEntityMetadata metaPacket = new PacketPlayOutEntityMetadata(drop.getId(), drop.getDataWatcher(), true);
        PacketPlayOutEntityDestroy destroyPacket = new PacketPlayOutEntityDestroy(drop.getId());

        CompatibilityAPI.getCompatibility().sendPackets(player, spawnPacket, metaPacket);

        Bukkit.getScheduler().runTaskLater(source, () -> CompatibilityAPI.getCompatibility().sendPackets(player, destroyPacket), ticksAlive);
    }
}
