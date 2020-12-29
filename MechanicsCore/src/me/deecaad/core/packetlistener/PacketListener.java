package me.deecaad.core.packetlistener;

import com.google.common.collect.MapMaker;
import com.mojang.authlib.GameProfile;
import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.ChannelPromise;
import me.deecaad.compatibility.CompatibilityAPI;
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
    private static final Method networkMarketsMethod;

    static {
        final Class<?> entityPlayerClass = ReflectionUtil.getNMSClass("EntityPlayer");
        final Class<?> craftServerClass = ReflectionUtil.getCBClass("CraftServer");

        LOGIN_PACKET = ReflectionUtil.getNMSClass("PacketLoginInStart");
        GAME_PROFILE = ReflectionUtil.getField(LOGIN_PACKET, null, GameProfile.class);

        playerConnectionField = ReflectionUtil.getField(entityPlayerClass, "playerConnection");
        networkManagerField = ReflectionUtil.getField(playerConnectionField.getType(), "networkManager");
        channelField = ReflectionUtil.getField(networkManagerField.getType(), null, Channel.class);

        minecraftServerClass = ReflectionUtil.getNMSClass("MinecraftServer");
        serverConnectionClass = ReflectionUtil.getNMSClass("ServerConnection");
        minecraftServerField = ReflectionUtil.getField(craftServerClass, null, minecraftServerClass);
        serverConnectionField = ReflectionUtil.getField(minecraftServerClass, null, serverConnectionClass);
        networkMarketsMethod = ReflectionUtil.getMethod(serverConnectionClass, Collection.class, serverConnectionClass);
    }

    // Simple way to get unique handlerNames if one plugin
    // instantiates multiple PacketListeners
    private static final AtomicInteger IDS = new AtomicInteger(0);

    // We don't use UUID's here (Like we normally would) because
    // when the PacketLoginInStart is sent, the game profile's UUID
    // is not yet set, but the player's name is.
    private Map<String, Channel> channelCache = new MapMaker().weakValues().makeMap();

    protected final Plugin plugin;
    protected final Debugger debug;
    protected final String handlerName;

    protected boolean isClosed;
    private Listener listener;

    // Lock from that stops the main thread from changing
    // the List (or Queue in Paper) in the ServerConnection
    // while we are injecting server channels
    private final Collection<Object> lock;

    // Minecraft Server Channels
    private List<Channel> serverChannels = new ArrayList<>();
    private ChannelInboundHandlerAdapter serverChannelHandler;
    private ChannelInitializer<Channel> beginInitProtocol;
    private ChannelInitializer<Channel> endInitProtocol;

    @SuppressWarnings("unchecked")
    public PacketListener(@Nonnull final Plugin plugin, @Nonnull final Debugger debug) {
        this.plugin = plugin;
        this.debug = debug;
        this.handlerName = "MechanicsCore-" + plugin.getName() + "-" + IDS.incrementAndGet();

        registerInjectionListener();

        // Handle internal minecraft server channel that
        // handles packets before the player's channel
        Object mcServer = ReflectionUtil.invokeField(minecraftServerField, plugin.getServer());
        Object serverConnection = ReflectionUtil.invokeField(serverConnectionField, mcServer);

        this.lock = (Collection<Object>) ReflectionUtil.invokeMethod(networkMarketsMethod, null, serverConnection);

        endInitProtocol = new ChannelInitializer<Channel>() {
            @Override
            protected void initChannel(Channel channel) {
                try {

                    // We need to make sure the main thread doesn't
                    // interfere with the lock while we are injecting
                    // incoming channels
                    synchronized (PacketListener.this.lock) {

                        // No need to keep adding channels if
                        // this listener is already closed
                        if (isClosed) return;

                        channel.eventLoop().submit(() -> injectChannel(channel));
                    }
                } catch (Exception e) {
                    debug.log(LogLevel.ERROR, "Cannot inject incoming channel " + channel, e);
                }
            }
        };

        beginInitProtocol = new ChannelInitializer<Channel>() {
            @Override
            protected void initChannel(Channel channel)  {
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
            final Field field = ReflectionUtil.getField(serverConnectionClass, null, List.class, i);
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

        for (Player player : plugin.getServer().getOnlinePlayers())
            injectPlayer(player);
    }

    /**
     * Gets the plugin associated with this <code>PacketListener</code>
     *
     * @return bukkit plugin
     */
    @Nonnull
    public Plugin getPlugin() {
        return plugin;
    }

    /**
     * Gets the unique handlerName of this <code>PacketListener</code>.
     * Every <code>PacketListener</code> has a unique name
     *
     * @return handlerName
     */
    @Nonnull
    public String getHandlerName() {
        return handlerName;
    }

    /**
     * @return true if this packet listener has been closed
     * @see PacketListener#close()
     */
    public boolean isClosed() {
        return isClosed;
    }

    /**
     * Instantiates this <code>PacketListener</code>'s
     * <code>listener</code> and listens for the
     * <code>PlayerLoginEvent</code> and the
     * <code>PluginDisableEvent</code> to inject/uninject
     * channels. The listener is cached for later for the
     * close() method
     *
     * @see PacketListener#close()
     */
    private void registerInjectionListener() {

        if (this.listener != null)
            throw new IllegalStateException("Listener already registered");

        this.listener = new Listener() {

            // Make sure to only inject a player
            // if they have not been denied
            @EventHandler(priority = EventPriority.LOWEST)
            public void onLogin(PlayerLoginEvent e) {
                if (e.getResult() != PlayerLoginEvent.Result.ALLOWED) {
                    debug.debug("Did not inject player " + e.getPlayer().getName() + " because: " + e.getResult());
                    return;
                } else if (PacketListener.this.isClosed) {
                    debug.debug("Did not inject player " + e.getPlayer().getName() + " because interceptor closed");
                }

                injectPlayer(e.getPlayer());
            }

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
     * Gets the <code>Channel</code> associated with the given
     * <code>Player</code>. This will almost always pull the channel
     * from cache because players are injected from the initial
     * <code>PacketLoginInStart</code> sent to the server.
     *
     * @param player The player to grab the channel from
     * @return The channel associated with the given player
     */
    public Channel getChannel(Player player) {
        Channel channel = channelCache.get(player.getName());

        // Channel is not yet cached, so make sure to cache it
        // for fast getting
        if (channel == null) {
            Object nmsPlayer = CompatibilityAPI.getEntityCompatibility().getNMSEntity(player);
            Object connection = ReflectionUtil.invokeField(playerConnectionField, nmsPlayer);
            Object manager = ReflectionUtil.invokeField(networkManagerField, connection);
            channel = (Channel) ReflectionUtil.invokeField(channelField, manager);

            channelCache.put(player.getName(), channel);
        }

        return channel;
    }

    /**
     * Injects/adds the given <code>player</code>'s <code>Channel</code>
     * into this <code>PacketListener</code>. If the player's channel
     * is already registered, this will update the
     * <code>PacketInterceptor</code>'s <code>player</code> instance to
     * the given <code>player</code>
     *
     * @param player The player to inject
     * @throws ClassCastException If there is a different packet interceptor with the same handlerName
     */
    public void injectPlayer(@Nonnull Player player) {
        injectChannel(getChannel(player)).player = player;
    }

    /**
     * Injects/adds the given <code>channel</code> into this
     * <code>PacketListener</code>. If the <code>channel</code> has
     * already been injected, then this method will return the
     * <code>PacketInterceptor</code> involved (for internal use).
     * Otherwise, a new <code>PacketInterceptor</code> will be
     * instantiated.
     *
     * @param channel The channel to inject
     * @return The PacketInterceptor associated with the channel
     * @throws ClassCastException If there is a different packet interceptor with the same handlerName
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
     * Uninjects/removes the given <code>player</code> from this
     * <code>PacketListener</code>.
     *
     * @param player The player to uninject
     */
    public void uninjectPlayer(@Nonnull Player player) {
        Channel channel = getChannel(player);

        channel.pipeline().remove(handlerName);
    }

    /**
     * Closes this <code>PacketListener</code>. This changes
     * the <code>isClosed()</code> state to <code>true</code>,
     * uninjects all server channels, and uninjects all online
     * players
     *
     * @see PacketListener#uninjectPlayer(Player)
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
     * Gets called when any of the injected <code>Channel</code>'s
     * <code>PacketInterceptor</code> reads an incoming packet
     *
     * Note: This means that any injected server channel may
     * get this packet, meaning the result of
     * <code>wrapper#getPlayer</code> may equal <code>null</code>
     *
     * @param wrapper The nonnull wrapper containing the packet
     */
    protected abstract void onPacketIn(Packet wrapper);

    /**
     * Gets called when any of the injected <code>Channel</code>'s
     * <code>PacketInterceptor</code> writes an outgoing packet.
     *
     * @param wrapper The nonnull wrapper containing the packet
     */
    protected abstract void onPacketOut(Packet wrapper);


    /**
     * Intercepts incoming and outgoing packets
     */
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
