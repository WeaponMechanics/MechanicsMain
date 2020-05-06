package me.deecaad.core.packetlistener;

import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import me.deecaad.core.utils.LogLevel;
import me.deecaad.core.utils.ReflectionUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import static me.deecaad.weaponmechanics.WeaponMechanics.debug;

public class PacketListenerAPI implements Listener {

    private static Map<String, PacketHandlerHolder> packetHandlerHolders = new HashMap<>();
    private static final String PACKET_HANDLER_NAME = "packet_handler";
    private static Method playerGetHandle;
    private static Field playerConnection;
    private static Field channelField;
    private static Field networkManager;

    /**
     * This automatically initializes PacketListenerAPI
     *
     * @param plugin the plugin initializing
     */
    public PacketListenerAPI(Plugin plugin) {
        playerGetHandle = ReflectionUtil.getMethod(ReflectionUtil.getCBClass("entity.CraftPlayer"), "getHandle");
        playerConnection = ReflectionUtil.getField(ReflectionUtil.getNMSClass("EntityPlayer"), "playerConnection");
        channelField = ReflectionUtil.getField(ReflectionUtil.getNMSClass("NetworkManager"), "channel");
        networkManager = ReflectionUtil.getField(ReflectionUtil.getNMSClass("PlayerConnection"), "networkManager");

        Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
        for (Player p : Bukkit.getOnlinePlayers()) {
            injectPlayer(p);
        }
    }

    /**
     * Same plugin can't add two packet handlers which are both listening same packet name.
     *
     * @param pluginAdding the plugin which adds this new packet handler
     * @param packetHandler the new packet handler
     */
    public static void addPacketHandler(Plugin pluginAdding, PacketHandler packetHandler) {
        boolean wasEmpty = packetHandlerHolders.isEmpty();
        PacketHandlerHolder packetHandlerHolder = packetHandlerHolders.get(pluginAdding.getDescription().getName());
        if (packetHandlerHolder == null) {
            packetHandlerHolders.put(pluginAdding.getDescription().getName(), new PacketHandlerHolder());
            packetHandlerHolder = packetHandlerHolders.get(pluginAdding.getDescription().getName());
        }
        if (packetHandlerHolder.getPacketHandler(packetHandler.getPacketName()) != null) {
            debug.log(LogLevel.ERROR,
                    "Plugin " + pluginAdding.getDescription().getName() + " tried to add packet handler with same name twice (" + packetHandler.getPacketName() + ").");
            return;
        }
        packetHandlerHolder.addPacketHandler(packetHandler);
        if (!wasEmpty) {
            return;
        }
        for (Player p : Bukkit.getOnlinePlayers()) {
            injectPlayer(p);
        }
    }

    private static Channel getChannel(Player player) {
        Object entityPlayer = ReflectionUtil.invokeMethod(playerGetHandle, player);
        Object nmsPlayerConnection = ReflectionUtil.invokeField(playerConnection, entityPlayer);
        return (Channel) ReflectionUtil.invokeField(channelField, ReflectionUtil.invokeField(networkManager, nmsPlayerConnection));
    }

    /**
     * Used to inject player to packet listening.
     *
     * @param player the player to inject
     */
    public static void injectPlayer(Player player) {
        if (packetHandlerHolders.isEmpty()) {
            return;
        }
        ChannelDuplexHandler channelDuplexHandler = new ChannelDuplexHandler() {
            @Override
            public void write(ChannelHandlerContext channelHandlerContext, Object object, ChannelPromise channelPromise) throws Exception {
                if (onPacket(new Packet(player, object))) {
                    return;
                }
                super.write(channelHandlerContext, object, channelPromise);
            }

            @Override
            public void channelRead(ChannelHandlerContext channelHandlerContext, Object object) throws Exception {
                if (onPacket(new Packet(player, object))) {
                    return;
                }
                super.channelRead(channelHandlerContext, object);
            }
        };
        getChannel(player).pipeline().addBefore(PACKET_HANDLER_NAME, player.getName(), channelDuplexHandler);
    }

    /**
     * Used to uninject player from packet listening.
     *
     * @param player the player to uninject
     */
    public static void uninjectPlayer(Player player) {
        if (packetHandlerHolders.isEmpty()) {
            return;
        }
        Channel channel = getChannel(player);
        if (channel.pipeline().get(player.getName()) != null) {
            channel.pipeline().remove(player.getName());
        }
    }

    /**
     * Called to modify packet based on its packet handlers
     *
     * @param packet the packet instance
     * @return the cancellation state of the packet
     */
    public static boolean onPacket(Packet packet) {
        for (Map.Entry<String, PacketHandlerHolder> packetHandlerHolder : packetHandlerHolders.entrySet()) {
            runPacketHandler(packet, packetHandlerHolder, "PacketDebugger");
            runPacketHandler(packet, packetHandlerHolder, packet.getPacket().getClass().getSimpleName());
        }
        return packet.isCancelled();
    }

    /**
     * This is just used to shorten onPacket(packet) method because PacketDebugger has to be also checked
     *
     * @param packet the packet instance
     * @param packetHandlerHolder the packet handler holder entry
     * @param packetName the packet name to check
     */
    private static void runPacketHandler(Packet packet, Map.Entry<String, PacketHandlerHolder> packetHandlerHolder, String packetName) {
        PacketHandler packetHandler = packetHandlerHolder.getValue().getPacketHandler(packetName);
        if (packetHandler != null) {
            try {
                packetHandler.onPacket(packet);
            } catch (Exception e) {
                debug.log(LogLevel.WARN,
                        "Catched exception from plugin " + packetHandlerHolder.getKey() + "'s packet listener " + packetHandler.getPacketName() + ".",
                        e);
            }
        }
    }

    /**
     * This should be called when reloading or shutting down server!
     */
    public static void onDisable() {
        for (Player p : Bukkit.getOnlinePlayers()) {
            uninjectPlayer(p);
        }
        packetHandlerHolders.clear();
    }

    @EventHandler
    public void join(PlayerJoinEvent e) {
        PacketListenerAPI.injectPlayer(e.getPlayer());
    }

    @EventHandler
    public void quit(PlayerQuitEvent e) {
        PacketListenerAPI.uninjectPlayer(e.getPlayer());
    }
}
