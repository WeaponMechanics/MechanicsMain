package me.deecaad.core.utils;

import me.deecaad.compatibility.CompatibilityAPI;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;
import java.util.Collection;

public class DistanceUtils {

    /**
     * Don't let anyone instantiate this class
     */
    private DistanceUtils() {
    }

    /**
     * Gets the default viewing distance, in blocks, of the server (Defined
     * in server.properties)
     *
     * @return The player viewing distance
     */
    public static int getRange() {
        int distance = Bukkit.getServer().getViewDistance();
        return distance << 4;
    }

    /**
     * Gets the viewing distance, in blocks, for the given <code>world</code>
     * (Defined in spigot.yml)
     *
     * @param world The world to pull the viewing distance from
     * @return The player viewing distance
     */
    public static int getRange(World world) {
        int distance = world.getViewDistance();
        return distance << 4;
    }

    /**
     * Sends the given <code>packet</code> to all players in viewdistance of the
     * given location. If there is a loaded paper.yml file, this can use
     * paper's no-tick-view-distance.
     *
     * Note: This while using this method won't cause any notable server side
     * performance issues, this can cause client sided issues. If a player
     * is >100 blocks away, they likely don't need to see the packet (Are they
     * really going to notice that particle? Is it worth the frame drop?)
     *
     * @see DistanceUtils#sendPacket(Location, Object, double)
     *
     * @param origin The location of the packet
     * @param packet The packet to send
     */
    public static void sendPacket(@Nonnull Location origin, Object packet) {
        sendPacket(origin, packet, getRange(origin.getWorld()));
    }

    /**
     * Sends the given <code>packet</code> to all players in range <code>distance</code>
     * with respect to the <code>origin</code>. All players within a bounding box with
     * radius <code>distance</code> will be sent the packet.
     *
     * Note that sending packets with too long of a range can cause client sided performance
     * issues. Spigot's entity tracking range exists mostly to eliminate client side lag from
     * rendering entities from too far away.
     *
     * @param origin The location of the packet
     * @param packet The packet to send
     * @param distance How far away to find players
     */
    @SuppressWarnings("unchecked")
    public static void sendPacket(@Nonnull Location origin, Object packet, double distance) {
        World world = origin.getWorld();
        Collection<Player> players = (Collection<Player>) (Collection<?>)
                world.getNearbyEntities(origin, distance, distance, distance, e -> e.getType() == EntityType.PLAYER);

        for (Player player : players) {
            CompatibilityAPI.getCompatibility().sendPackets(player, packet);
        }
    }
}
