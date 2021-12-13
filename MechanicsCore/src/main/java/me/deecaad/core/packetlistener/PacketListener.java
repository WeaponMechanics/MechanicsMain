package me.deecaad.core.packetlistener;

import com.google.common.collect.MapMaker;
import com.mojang.authlib.GameProfile;
import io.netty.channel.*;
import me.deecaad.core.compatibility.CompatibilityAPI;
import me.deecaad.core.utils.Debugger;
import me.deecaad.core.utils.LogLevel;
import me.deecaad.core.utils.ReflectionUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.plugin.Plugin;

import javax.annotation.Nonnull;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * This abstract class outlines a packet interceptor that listens for packets
 * of information being exchanged between the server and connected players.
 *
 * <p>If a plugin is listening for 5 or more different packets, then it should use
 * an implementation of this class to separate out the work for the packets.
 *
 * @see PacketHandlerListener
 */
public abstract class PacketListener {

    private static final Class<?> LOGIN_PACKET;
    private static final Field GAME_PROFILE;

    // Fields for getting the channel of a player
    private static final Field playerConnectionField;
    private static final Field networkManagerField;
    private static final Field channelField;

    // Reflection for server connection, important
    // for injecting a player's channel before the
    // PlayerLoginEvent (Because the player's channel
    // hasn't been setup yet)
    private static final Class<?> minecraftServerClass;
    private static final Class<?> serverConnectionClass;
    private static final Field minecraftServerField;
    private static final Field serverConnectionField;

    private static final Method networkMarkersMethod;
    private static final Field networkMarkersField;

    static {
        final Class<?> entityPlayerClass = ReflectionUtil.getNMSClass("server.level", "EntityPlayer");
        final Class<?> playerConnectionClass = ReflectionUtil.getNMSClass("server.network", "PlayerConnection");
        final Class<?> networkManagerClass = ReflectionUtil.getNMSClass("network", "NetworkManager");
        final Class<?> craftServerClass = ReflectionUtil.getCBClass("CraftServer");

        LOGIN_PACKET = ReflectionUtil.getNMSClass("network.protocol.login", "PacketLoginInStart");
        GAME_PROFILE = ReflectionUtil.getField(LOGIN_PACKET, GameProfile.class);

        playerConnectionField = ReflectionUtil.getField(entityPlayerClass, playerConnectionClass);
        networkManagerField = ReflectionUtil.getField(playerConnectionClass, networkManagerClass);
        channelField = ReflectionUtil.getField(networkManagerField.getType(), Channel.class);

        minecraftServerClass = ReflectionUtil.getNMSClass("server", "MinecraftServer");
        serverConnectionClass = ReflectionUtil.getNMSClass("server.network", "ServerConnection");
        minecraftServerField = ReflectionUtil.getField(craftServerClass, minecraftServerClass);
        serverConnectionField = ReflectionUtil.getField(minecraftServerClass, serverConnectionClass);

        if (ReflectionUtil.getMCVersion() <= 16) {
            networkMarkersMethod = ReflectionUtil.getMethod(serverConnectionClass, Collection.class, serverConnectionClass);
            networkMarkersField = null;
        } else {
            networkMarkersMethod = null;
            networkMarkersField = ReflectionUtil.getField(serverConnectionClass, Collection.class, 2);
        }
    }

    // Simple way to get unique handlerNames if one plugin
    // instantiates multiple PacketListeners
    private static final AtomicInteger IDS = new AtomicInteger(0);

    // We don't use UUID's here (Like we normally would) because
    // when the PacketLoginInStart is sent, the game profile's UUID
    // is not yet set, but the player's name is.
    private final Map<String, Channel> channelCache = new MapMaker().weakValues().makeMap();

    protected final Plugin plugin;
    protected final Debugger debug;
    protected final String handlerName;

    private boolean isClosed;
    private Listener listener;

    private final Collection<Object> lock;

    // Minecraft Server Channels
    private final List<Channel> serverChannels = new ArrayList<>();
    private final ChannelInboundHandlerAdapter serverChannelHandler;
    private final ChannelInitializer<Channel> beginInitProtocol;
    private final ChannelInitializer<Channel> endInitProtocol;

    /**
     * Sets up the protocols for receiving packets, and injects them into the
     * server's channels. if there is a point when you no longer need to
     * intercept packets, this should be closed to uninject the protocols from
     * the server channels.
     * @see PacketListener#close()
     *
     * <p>This constructor also instantiates a {@link Listener} to get a player's
     * channel when they join, and to automatically close this packet listener
     * when the <code>plugin</code> is disabled.
     *
     * @param plugin The non-null plugin to register this listener to.
     * @param debug  The non-null debugger to relay possible issues to.
     */
    @SuppressWarnings("unchecked")
    public PacketListener(@Nonnull final Plugin plugin, @Nonnull final Debugger debug) {
        this.plugin = plugin;
        this.debug = debug;
        this.handlerName = "MechanicsCore-" + plugin.getName() + "-" + IDS.incrementAndGet();

        // Handle internal minecraft server channel that
        // handles packets before the player's channel
        Object mcServer = ReflectionUtil.invokeField(minecraftServerField, plugin.getServer());
        Object serverConnection = ReflectionUtil.invokeField(serverConnectionField, mcServer);

        if (ReflectionUtil.getMCVersion() <= 16) {
            this.lock = (Collection<Object>) ReflectionUtil.invokeMethod(networkMarkersMethod, null, serverConnection);
        } else {
            this.lock = (Collection<Object>) ReflectionUtil.invokeField(networkMarkersField, serverConnection);
        }

        endInitProtocol = new ChannelInitializer<Channel>() {
            @Override
            protected void initChannel(Channel channel) {

                // No need to keep adding channels if
                // this listener is already closed
                if (isClosed) return;

                try {

                    // We cannot allow new channels to be added to the manager while we
                    // are submitting our own.
                    synchronized (PacketListener.this.lock) {

                        channel.eventLoop().submit(() -> injectChannel(channel));
                    }
                } catch (Exception e) {
                    debug.log(LogLevel.ERROR, "Cannot inject incoming channel " + channel, e);
                }
            }
        };

        beginInitProtocol = new ChannelInitializer<Channel>() {
            @Override
            protected void initChannel(Channel channel) {
                channel.pipeline().addLast(endInitProtocol);
            }
        };

        serverChannelHandler = new ChannelInboundHandlerAdapter() {
            @Override
            public void channelRead(ChannelHandlerContext ctx, Object msg) {
                Channel channel = (Channel) msg;
                channel.pipeline().addFirst(beginInitProtocol);
                ctx.fireChannelRead(msg);
            }
        };

        // The ServerConnection class has multiple lists
        // and we don't know the name of the list. To find
        // the correct list, we just loop through every single
        // list and check if the element is the correct type
        boolean looking = true;
        for (int i = 0; looking; i++) {
            final Field field = ReflectionUtil.getField(serverConnectionClass, List.class, i);
            List<Object> channels = (List<Object>) ReflectionUtil.invokeField(field, serverConnection);

            for (Object obj : channels) {
                if (!(obj instanceof ChannelFuture))
                    continue;

                Channel channel = ((ChannelFuture) obj).channel();

                serverChannels.add(channel);
                channel.pipeline().addFirst(serverChannelHandler);
                looking = false;
            }
        }

        registerInjectionListener();

        // Inject players that are already connected to the server
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            injectPlayer(player);
        }
    }

    private void registerInjectionListener() {

        if (this.listener != null)
            throw new IllegalStateException("Listener already registered");

        this.listener = new Listener() {

            // Make sure to only inject a player
            // if they have not been denied
            @EventHandler(priority = EventPriority.HIGHEST)
            public void onLogin(PlayerLoginEvent e) {
                if (e.getResult() != PlayerLoginEvent.Result.ALLOWED) {
                    debug.debug("Did not inject player " + e.getPlayer().getName() + " because: " + e.getResult());
                    return;
                } else if (PacketListener.this.isClosed) {
                    debug.debug("Did not inject player " + e.getPlayer().getName() + " because interceptor closed");
                }

                injectPlayer(e.getPlayer());
            }

            // We want to close this packet listener to clean up the
            // server channels. Otherwise, artifacts of this listener
            // will remain
            @EventHandler
            public void onDisable(PluginDisableEvent e) {
                if (e.getPlugin().equals(PacketListener.this.plugin)) {
                    close();
                }
            }
        };

        plugin.getServer().getPluginManager().registerEvents(listener, plugin);
    }

    /**
     * Returns the plugin that instantiated this listener.
     *
     * @return The non-null plugin.
     */
    @Nonnull
    public Plugin getPlugin() {
        return plugin;
    }

    /**
     * Gets the name of the handler that this listener uses to inject into
     * {@link Channel}s.
     *
     * @return The unique, non-null handler name. The returned name will follow
     *         a <code>MechanicsCore-&lt;plugin&gt;-&lt;id&gt;</code> format.
     */
    @Nonnull
    public String getHandlerName() {
        return handlerName;
    }

    /**
     * Returns <code>true</code> if this listener has been closed, and is no
     * longer intercepting packets.
     *
     * @return <code>true</code> if the {@link #close()} method has been called.
     * @see PacketListener#close()
     */
    public boolean isClosed() {
        return isClosed;
    }

    /**
     * Returns the {@link Channel} associated with the given
     * <code>player</code>'s name. If no {@link Channel} is cached for that
     * player, then this method will use reflection to grab it.
     *
     * @param player The player to grab the {@link Channel} from.
     * @return The cached (Or newly cached) {@link Channel}.
     */
    public Channel getChannel(Player player) {
        Channel channel = channelCache.get(player.getName());

        // Channel is not yet cached, so make sure to cache it
        // for fast getting
        if (channel == null) {
            CompatibilityAPI.getCompatibility().getEntityPlayer(player);
            Object nmsPlayer = CompatibilityAPI.getEntityCompatibility().getNMSEntity(player);
            Object connection = ReflectionUtil.invokeField(playerConnectionField, nmsPlayer);
            Object manager = ReflectionUtil.invokeField(networkManagerField, connection);

            channel = (Channel) ReflectionUtil.invokeField(channelField, manager);

            channelCache.put(player.getName(), channel);
        }

        return channel;
    }

    /**
     * Adds the given <code>player</code> to this listener by injecting the
     * {@link Channel} associated with it into this listener.
     *
     * @param player The non-null bukkit player to inject.
     * @throws ClassCastException If there is another plugin's packet listener
     *                            with the same {@link #getHandlerName()} to
     *                            this one. If this occurs, your plugin name is
     *                            too generic, and should be changed.
     * @see #injectChannel(Channel)
     */
    public void injectPlayer(@Nonnull Player player) {
        injectChannel(getChannel(player)).player = player;
    }

    /**
     * Adds the given <code>channel</code> to this listener by adding a
     * {@link PacketInterceptor} to it the key {@link #getHandlerName()}.
     *
     * <p>If the {@link Channel} has already been injected, this method will only
     * return the associated {@link PacketInterceptor} (Without any side
     * effects).
     *
     * @param channel The channel to inject.
     * @return The interceptor that was added to the channel.
     */
    public PacketInterceptor injectChannel(@Nonnull Channel channel) {
        PacketInterceptor interceptor = (PacketInterceptor) channel.pipeline().get(handlerName);

        if (interceptor == null) {
            interceptor = new PacketInterceptor();
            channel.pipeline().addBefore("packet_handler", handlerName, interceptor);
        }

        return interceptor;
    }

    /**
     * Uninjects the channel of the bukkit {@link Player}. This prevents the
     * {@link PacketInterceptor} from intercepting outgoing packets to that
     * {@link Player}.
     *
     * @param player The non-null {@link Player} to uninject.
     */
    public void uninjectPlayer(@Nonnull Player player) {
        Channel channel = getChannel(player);

        channel.pipeline().remove(handlerName);
    }

    /**
     * Closes this listener, stopping it from intercepting packets. After
     * calling this method, the result of {@link #isClosed()} will always be
     * <code>true</code>.
     *
     * <p>This method uninjects the {@link PacketInterceptor}s from all online
     * players' channels, as well as the server channels. Note that if a
     * {@link Player} was injected, logs off, this method is called, then that
     * {@link Player} logs back on, they create a new {@link Channel}.
     */
    public final void close() {
        if (!isClosed) {
            HandlerList.unregisterAll(listener);

            for (Player player : plugin.getServer().getOnlinePlayers()) {
                uninjectPlayer(player);
            }

            for (Channel channel : serverChannels) {
                final ChannelPipeline pipeline = channel.pipeline();
                channel.eventLoop().execute(() -> pipeline.remove(serverChannelHandler));
            }

            isClosed = true;
        }
    }

    /**
     * This method is called when any of the injected {@link Channel}s
     * intercepts an incoming (client to server) packet. Note that the result
     * of {@link Packet#getPlayer()} may be <code>null</code>.
     *
     * @param wrapper The non-null wrapper containing the packet.
     */
    protected abstract void onPacketIn(Packet wrapper);

    /**
     * This method is called when any of the injected {@link Channel}s
     * intercepts an outgoing (server to client) packet. Note that the result
     * of {@link Packet#getPlayer()} may be <code>null</code>.
     *
     * @param wrapper The non-null wrapper containing the packet.
     */
    protected abstract void onPacketOut(Packet wrapper);


    private final class PacketInterceptor extends ChannelDuplexHandler {

        // Set by injectPlayer(Player)
        private Player player;

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

            final Channel channel = ctx.channel();
            if (LOGIN_PACKET.isInstance(msg)) {
                GameProfile profile = (GameProfile) GAME_PROFILE.get(msg);
                channelCache.put(profile.getName(), channel);
            }

            Packet packet = new Packet(player, msg);

            try {
                onPacketIn(packet);
            } catch (Exception e) {
                debug.log(LogLevel.ERROR, "Unhandled exception caught onPacketIn(Packet)", e);
                super.channelRead(ctx, msg);
                return;
            }

            // If this packet listener did not cancel the packet,
            // then send the packet
            if (!packet.isCancelled()) {
                super.channelRead(ctx, packet.getPacket());
            }
        }

        @Override
        public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {

            Packet packet = new Packet(player, msg);

            try {
                onPacketOut(packet);
            } catch (Exception e) {
                debug.log(LogLevel.ERROR, "Unhandled exception caught onPacketOut(Packet)", e);
                super.write(ctx, msg, promise);
                return;
            }

            // If this packet listener did not cancel the packet,
            // then send the packet
            if (!packet.isCancelled()) {
                super.write(ctx, msg, promise);
            }
        }
    }
}
