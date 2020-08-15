package me.deecaad.core.file.serializers;

import me.deecaad.compatibility.CompatibilityAPI;
import me.deecaad.core.MechanicsCore;
import me.deecaad.core.file.Serializer;
import me.deecaad.core.utils.ReflectionUtil;
import me.deecaad.core.utils.StringUtils;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Content;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.Bukkit;
import org.bukkit.boss.BossBar;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import javax.annotation.Nullable;
import java.io.File;
import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static me.deecaad.core.MechanicsCore.debug;

public class Message implements Serializer<Message> {

    /**
     * Reflection support for 1.8 action bar
     */
    private static final Constructor<?> packetPlayOutChatConstructor;
    private static final Constructor<?> chatComponentTextConstructor;

    static {
        if (CompatibilityAPI.getVersion() < 1.09) {
            packetPlayOutChatConstructor = ReflectionUtil.getConstructor(ReflectionUtil.getNMSClass("PacketPlayOutChat"), ReflectionUtil.getNMSClass("IChatBaseComponent"), byte.class);
            chatComponentTextConstructor = ReflectionUtil.getConstructor(ReflectionUtil.getNMSClass("ChatComponentText"), String.class);
        } else {
            packetPlayOutChatConstructor = chatComponentTextConstructor = null;
        }
    }

    private BaseComponent chat;
    private BaseComponent actionBar;
    private String[] legacyChat;
    private String legacyActionBar;
    private int actionBarTime;
    private String title;
    private String subtitle;
    private int fadeIn;
    private int stay;
    private int fadeOut;
    private BossBar bossBar;
    private int bossBarTime;

    /**
     * Default constructor for serializer
     */
    public Message() {
    }

    public Message(@Nullable BaseComponent chat,
                   @Nullable BaseComponent actionBar,
                   @Nullable String[] legacyChat,
                   @Nullable String legacyActionBar,
                   int actionBarTime,
                   @Nullable String title,
                   @Nullable String subtitle,
                   int fadeIn,
                   int stay,
                   int fadeOut,
                   @Nullable BossBar bossBar,
                   int bossBarTime) {

        this.chat = chat;
        this.actionBar = actionBar;
        this.legacyChat = legacyChat;
        this.legacyActionBar = legacyActionBar;
        this.actionBarTime = actionBarTime;
        this.title = title;
        this.subtitle = subtitle;
        this.fadeIn = fadeIn;
        this.stay = stay;
        this.fadeOut = fadeOut;
        this.bossBar = bossBar;
        this.bossBarTime = bossBarTime;
    }

    public BaseComponent getChat() {
        return chat;
    }

    public void setChat(BaseComponent chat) {
        this.chat = chat;
    }

    public BaseComponent getActionBar() {
        return actionBar;
    }

    public void setActionBar(BaseComponent actionBar) {
        this.actionBar = actionBar;
    }

    public String[] getLegacyChat() {
        return legacyChat;
    }

    public void setLegacyChat(String[] legacyChat) {
        this.legacyChat = legacyChat;
    }

    public String getLegacyActionBar() {
        return legacyActionBar;
    }

    public void setLegacyActionBar(String legacyActionBar) {
        this.legacyActionBar = legacyActionBar;
    }

    public int getActionBarTime() {
        return actionBarTime;
    }

    public void setActionBarTime(int actionBarTime) {
        this.actionBarTime = actionBarTime;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }

    public int getFadeIn() {
        return fadeIn;
    }

    public void setFadeIn(int fadeIn) {
        this.fadeIn = fadeIn;
    }

    public int getStay() {
        return stay;
    }

    public void setStay(int stay) {
        this.stay = stay;
    }

    public int getFadeOut() {
        return fadeOut;
    }

    public void setFadeOut(int fadeOut) {
        this.fadeOut = fadeOut;
    }

    public BossBar getBossBar() {
        return bossBar;
    }

    public void setBossBar(BossBar bossBar) {
        this.bossBar = bossBar;
    }

    public int getBossBarTime() {
        return bossBarTime;
    }

    public void setBossBarTime(int bossBarTime) {
        this.bossBarTime = bossBarTime;
    }

    public void send(Player player) {

        // Packet used to send action bar
        Object packet = null;

        // Handle the initial chat message and action bar message
        if (CompatibilityAPI.getVersion() < 1.09) {
            if (legacyChat != null) player.sendMessage(legacyChat);
            if (legacyActionBar != null) {
                Object chatComponent = ReflectionUtil.newInstance(chatComponentTextConstructor, actionBar);
                packet = ReflectionUtil.newInstance(packetPlayOutChatConstructor, chatComponent, (byte) 2);
                CompatibilityAPI.getCompatibility().sendPackets(player, packet);
            }
        } else {
            player.spigot().sendMessage(ChatMessageType.CHAT, chat);
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, actionBar);
        }

        // If the action bar needs to stay for an extended period of time, handle it
        if (actionBarTime > 40) {
            final Object finalPacket = packet;
            new BukkitRunnable() {

                // The amount of left
                int time = actionBarTime;

                @Override
                public void run() {
                    if (time > 40) {
                        if (CompatibilityAPI.getVersion() < 1.09) {
                            if (finalPacket == null) {
                                debug.error("Error occurred while sending repeated action bar... packet is null?");
                            } else {
                                CompatibilityAPI.getCompatibility().sendPackets(player, finalPacket);
                            }
                        } else {
                            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, actionBar);
                        }
                        time -= 40;
                    } else {
                        cancel();
                    }
                }
            }.runTaskTimerAsynchronously(MechanicsCore.getPlugin(), 40, 40);
        }

        if (CompatibilityAPI.getVersion() < 1.11) {
            player.sendTitle(title, subtitle);
        } else {
            player.sendTitle(title, subtitle, fadeIn, stay, fadeOut);
        }

        if (bossBar != null) {
            bossBar.addPlayer(player);
            Bukkit.getScheduler().runTaskLater(MechanicsCore.getPlugin(), () -> {
                bossBar.removePlayer(player);
            }, bossBarTime);
        }
    }

    @Override
    public String getKeyword() {
        return "Messages";
    }

    @Override
    public Message serialize(File file, ConfigurationSection configurationSection, String path) {
        ConfigurationSection config = configurationSection.getConfigurationSection(path);
        final double version = CompatibilityAPI.getVersion();

        BaseComponent chat = null;
        BaseComponent actionBar = null;
        String[] legacyChat = null;
        String legacyActionBar = null;
        int actionBarTime = 0;

        // Handles "chat" or "legacyChat"
        if (config.isString("Chat.Messages")) {
            if (version < 1.09) {
                legacyChat = new String[]{StringUtils.color(config.getString("Chat.Messages"))};
            } else {
                chat = new TextComponent(TextComponent.fromLegacyText(StringUtils.color(config.getString("Chat.Messages"))));
            }
        } else {
            List<String> strings = config.getStringList("Chat.Messages");

            if (version < 1.09) {
                legacyChat = strings.stream()
                        .map(StringUtils::color)
                        .toArray(String[]::new);
            } else {

                // Advanced chat features start here:
                ComponentBuilder builder = new ComponentBuilder();
                List<String> hoverActions = config.getStringList("Chat.Hover_Actions");
                List<String> clickActions = config.getStringList("Chat.Click_Actions");

                for (int i = 0; i < strings.size(); i++) {
                    BaseComponent[] components = TextComponent.fromLegacyText(StringUtils.color(strings.get(i)));

                    HoverEvent hoverEvent = null;
                    ClickEvent clickEvent = null;

                    if (i < hoverActions.size()) {

                        // HoverEvents are easy to serialize because we
                        // only need to support the "SHOW_TEXT" action
                        String hover = hoverActions.get(i);
                        Content content = new Text(TextComponent.fromLegacyText(hover));
                        hoverEvent = new HoverEvent(HoverEvent.Action.SHOW_TEXT, content);
                    }
                    if (i < clickActions.size()) {

                        // Click the data up from ACTION~TEXT
                        String clickData = clickActions.get(i);
                        String[] split = clickData.split("~", 2);
                        ClickEvent.Action action;

                        try {
                            // Data validation
                            String data = split[1]; // Just to throw the exception
                            action = ClickEvent.Action.valueOf(split[0]);
                        } catch (IllegalArgumentException e) {
                            List<String> options = Arrays.stream(ClickEvent.Action.values())
                                    .map(ClickEvent.Action::name)
                                    .collect(Collectors.toList());

                            debug.error("Unknown action: " + split[0]
                                            + ", did you mean " + StringUtils.didYouMean(split[0], options),
                                    StringUtils.foundAt(file, path));
                            continue;
                        } catch (ArrayIndexOutOfBoundsException e) {
                            debug.error("Unknown input: " + clickData,
                                    "Follow the format: ClickAction~Your Text Here");
                            continue;
                        }

                        clickEvent = new ClickEvent(action, split[1]);
                    }

                    for (BaseComponent component : components) {
                        component.setClickEvent(clickEvent);
                        component.setHoverEvent(hoverEvent);

                        builder.append(component);
                    }
                }
            }
        }

        // Handles "actionBar", "legacyActionBar", and "actionBarTime"
        actionBarTime = config.getInt("Action_Bar.Time", -1);
        if (version < 1.09) {
            legacyActionBar = StringUtils.color(config.getString("Action_Bar.Message"));
        } else {
            if (config.contains("Action_Bar.Message"))
                actionBar = new TextComponent(TextComponent.fromLegacyText(StringUtils.color(config.getString("Action_Bar.Message"))));
        }

        // Handle title/subtitle stuff
        String title = StringUtils.color(config.getString("Title.Title"));
        String subtitle = StringUtils.color(config.getString("Title.Subtitle"));
        int fadeIn = config.getInt("Title.Fade_In", -1);
        int stay = config.getInt("Title.Stay", -1);
        int fadeOut = config.getInt("Title.Fade_Out");

        BossBar bossBar = null;
        int bossBarTime = config.getInt("Boss_Bar.Active_Time", 200); // 10 seconds
        if (config.contains("Boss_Bar")) {
            bossBar = new BossBarSerializer().serialize(file, configurationSection, path + ".Boss_Bar");
        }

        return new Message(chat, actionBar, legacyChat, legacyActionBar, actionBarTime, title, subtitle, fadeIn, stay, fadeOut, bossBar, bossBarTime);
    }
}
