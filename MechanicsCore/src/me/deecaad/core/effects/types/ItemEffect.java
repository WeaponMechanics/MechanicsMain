package me.deecaad.core.effects.types;

import me.deecaad.compatibility.CompatibilityAPI;
import me.deecaad.compatibility.entity.EntityCompatibility;
import me.deecaad.core.effects.Effect;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
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
    protected void spawnOnce(@Nonnull Plugin source, @Nonnull World world, double x, double y, double z, @Nullable Object data) {

        EntityCompatibility compatibility = CompatibilityAPI.getCompatibility().getEntityCompatibility();

        Object entity = CompatibilityAPI.getCompatibility().getEntityCompatibility().toNMSItemEntity(toDrop, world, x, y, z);
        Object spawn = compatibility.getSpawnPacket(entity);
        Object metadata = compatibility.getMetadataPacket(entity);
        Object destroy = compatibility.getDestroyPacket(entity);

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

            CompatibilityAPI.getCompatibility().sendPackets(player, spawn, metadata);
            Bukkit.getScheduler().runTaskLater(source,
                    () -> CompatibilityAPI.getCompatibility().sendPackets(player, destroy),
                    ticksAlive);
        }
    }

    @Override
    protected void spawnOnceFor(@Nonnull Plugin source, @Nonnull Player player, @Nonnull World world, double x, double y, double z, @Nullable Object data) {

        EntityCompatibility compatibility = CompatibilityAPI.getCompatibility().getEntityCompatibility();

        Object entity = CompatibilityAPI.getCompatibility().getEntityCompatibility().toNMSItemEntity(toDrop, world, x, y, z);
        Object spawn = compatibility.getSpawnPacket(entity);
        Object metadata = compatibility.getMetadataPacket(entity);
        Object destroy = compatibility.getDestroyPacket(entity);

        CompatibilityAPI.getCompatibility().sendPackets(player, spawn, metadata);

        Bukkit.getScheduler().runTaskLater(source,
                () -> CompatibilityAPI.getCompatibility().sendPackets(player, destroy),
                ticksAlive);
    }
}
