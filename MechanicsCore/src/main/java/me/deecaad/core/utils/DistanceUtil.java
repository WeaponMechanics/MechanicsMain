package me.deecaad.core.utils;

import me.deecaad.core.compatibility.CompatibilityAPI;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;
import java.util.LinkedList;
import java.util.List;

/**
 * This final utility class outlines static methods involving the visible
 * distance around a player, as well as methods to send packets to players
 * so long as the packet's location is within the player's viewing distance.
 */
public final class DistanceUtil {

    // Don't let anyone instantiate this class
    private DistanceUtil() { }

    /**
     * Returns the default viewing distance of worlds, defined by the vanilla
     * minecraft server's <code>server.properties</code> file. Generally, this
     * method should not be used. Use instead: {@link #getRange(World)}
     *
     * @return The non-negative viewing distance, in blocks.
     */
    public static int getRange() {
        int distance = Bukkit.getServer().getViewDistance();
        return distance << 4;
    }

    /**
     * Returns the viewing distance of the given world.
     *
     * @param world Which world to pull the viewing distance from.
     * @return The non-negative viewing distance, in blocks.
     */
    public static int getRange(World world) {

        // world.getRange() only exists in 1.14+
        if (ReflectionUtil.getMCVersion() < 14) return getRange();

        int distance = world.getViewDistance();
        return distance << 4;
    }

    /**
     * Returns the entities withing range
     *
     * @param origin The coordinates that from where entities are taken
     * @return The entities withing range of view distance from origin
     */
    public static List<Player> getPlayersInRange(@Nonnull Location origin) {
        World world = origin.getWorld();
        if (world == null)
            throw new IllegalArgumentException();

        double distance = getRange(world);

        // AABB
        double x1 = origin.getX() - distance;
        double y1 = origin.getY() - distance;
        double z1 = origin.getZ() - distance;
        double x2 = origin.getX() + distance;
        double y2 = origin.getY() + distance;
        double z2 = origin.getZ() + distance;

        // Collect all players in box
        List<Player> players = new LinkedList<>();
        for (Player player : world.getPlayers()) {
            Location pos = player.getLocation();

            if (pos.getX() > x1 && pos.getX() < x2
                    && pos.getY() > y1 && pos.getY() < y2
                    && pos.getZ() > z1 && pos.getZ() < z2) {

                players.add(player);
            }
        }

        return players;
    }

    /**
     * Sends the given packet to all players who can see the given
     * {@link Location}. The distance that a player can see is defined by
     * {@link #getRange(World)}.
     *
     * <p>Note that sending packets with too long of a range can cause client
     * sided performance issues. This has been a problem for a long time in the
     * notchian server, so <a href="https://spigotmc.org/">Spigot</a> added
     * entity tracking range, limiting how far the server would send clients
     * packets. It is a good practice to limit how far away you send your
     * packet.
     * <blockquote><pre><code>
     *     Location origin = /* not shown *&#47;;
     *     Object packet = /* not shown *&#47;;
     *     int distance = Math.min(DistanceUtils.getRange(), 50);
     *     DistanceUtils.sendPacket(origin, packet, distance)
     * </code></pre></blockquote>
     *
     * @param origin The coordinates that the packet is being spawned at.
     * @param packets The packets to send to players in view.
     */
    public static void sendPacket(@Nonnull Location origin, Object... packets) {
        if (origin.getWorld() == null)
            throw new IllegalArgumentException("Cannot have null world");

        for (Player entity : getPlayersInRange(origin)) {
            if (entity.getType() != EntityType.PLAYER) {
                continue;
            }
            CompatibilityAPI.getCompatibility().sendPackets(entity, packets);
        }
    }
}
