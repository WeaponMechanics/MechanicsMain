package me.deecaad.core.effects.items;

import me.deecaad.core.effects.AbstractEffect;
import me.deecaad.core.effects.data.EffectData;
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
import java.util.Map;
import java.util.stream.Collectors;

public class ItemEffect extends AbstractEffect {

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
    public void spawnOnce(@Nonnull Plugin source, @Nonnull World world, double x, double y, double z, @Nullable EffectData data) {
        EntityItem drop = new EntityItem(((CraftWorld)world).getHandle(), x, y, z, CraftItemStack.asNMSCopy(toDrop));

        PacketPlayOutSpawnEntity spawnPacket = new PacketPlayOutSpawnEntity(drop);
        PacketPlayOutEntityMetadata metaPacket = new PacketPlayOutEntityMetadata(drop.getId(), drop.getDataWatcher(), true);
        PacketPlayOutEntityDestroy destroyPacket = new PacketPlayOutEntityDestroy(drop.getId());

        Location origin = new Location(world, x, y, z);
        List<Player> players = world.getEntitiesByClass(Player.class).stream()
                .filter(player -> player.getLocation().distance(origin) < VIEW_DISTANCE)
                .collect(Collectors.toList());

        for (Player player : players) {
            PlayerConnection connection = ((CraftPlayer) player).getHandle().playerConnection;
            connection.sendPacket(spawnPacket);
            connection.sendPacket(metaPacket);

            Bukkit.getScheduler().runTaskLater(source, () -> connection.sendPacket(destroyPacket), ticksAlive);
        }
    }

    @Override
    public AbstractEffect serialize(Map<String, SerializerData<?>> args) {
        return null;
    }

    @Override
    public Map<String, SerializerData<?>> getDefaults() {
        return null;
    }
}
