package me.deecaad.weaponmechanics.mechanics.defaultmechanics;

import me.deecaad.compatibility.CompatibilityAPI;
import me.deecaad.core.file.Serializer;
import me.deecaad.core.placeholder.PlaceholderAPI;
import me.deecaad.core.utils.LogLevel;
import me.deecaad.core.utils.ReflectionUtil;
import me.deecaad.core.utils.StringUtils;
import me.deecaad.weaponmechanics.WeaponMechanics;
import me.deecaad.weaponmechanics.mechanics.CastData;
import me.deecaad.weaponmechanics.mechanics.IMechanic;
import me.deecaad.weaponmechanics.mechanics.Mechanics;
import me.deecaad.weaponmechanics.wrappers.MessageHelper;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Content;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.lang.reflect.Constructor;

import static me.deecaad.weaponmechanics.WeaponMechanics.debug;

public class MessageMechanic implements Serializer<MessageMechanic>, IMechanic {

    /**
     * Reflection support for 1.8 action bar
     */
    private static Constructor<?> packetPlayOutChatConstructor;
    private static Constructor<?> chatComponentTextConstructor;

    static {
        if (CompatibilityAPI.getVersion() < 1.09) {
            packetPlayOutChatConstructor = ReflectionUtil.getConstructor(ReflectionUtil.getNMSClass("PacketPlayOutChat"), ReflectionUtil.getNMSClass("IChatBaseComponent"), byte.class);
            chatComponentTextConstructor = ReflectionUtil.getConstructor(ReflectionUtil.getNMSClass("ChatComponentText"), String.class);
        }
    }

    private boolean sendGlobally;
    private boolean sendGloballyForWorld;
    private ChatData chatData;
    private String actionBar;
    private int actionBarTime;
    private TitleData titleData;
    private BossBarData bossBarData;

    /**
     * Empty constructor to be used as serializer
     */
    public MessageMechanic() {
        if (Mechanics.hasMechanic(getKeyword())) return;
        Mechanics.registerMechanic(WeaponMechanics.getPlugin(), getKeyword());
    }

    public MessageMechanic(boolean sendGlobally, boolean sendGloballyForWorld, ChatData chatData, String actionBar, int actionBarTime, TitleData titleData, BossBarData bossBarData) {
        this.sendGlobally = sendGlobally;
        this.sendGloballyForWorld = sendGloballyForWorld;
        this.chatData = chatData;
        this.actionBar = actionBar;
        this.actionBarTime = actionBarTime;
        this.titleData = titleData;
        this.bossBarData = bossBarData;
    }

    @Override
    public void use(CastData castData) {
        if (sendGlobally) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                send(castData, player);
            }
            return;
        } else if (sendGloballyForWorld) {
            for (Player player : castData.getCastLocation().getWorld().getPlayers()) {
                send(castData, player);
            }
            return;
        }
        send(castData, (Player) castData.getCaster());
    }

    private void send(CastData castData, Player player) {
        MessageHelper messageHelper = WeaponMechanics.getPlayerWrapper(player).getMessageHelper();

        // If this would be "boolean" it would throw null pointer
        Boolean fetchWeaponInfoValue = castData.getData(CastData.CommonDataTags.WEAPON_INFO.name(), Boolean.class);
        boolean isWeaponInfoCast = fetchWeaponInfoValue != null && fetchWeaponInfoValue;

        if (chatData != null) {
            String chatMessage = PlaceholderAPI.applyPlaceholders(chatData.message, player, castData.getWeaponStack(), castData.getWeaponTitle());
            if (CompatibilityAPI.getVersion() < 1.09) {
                player.sendMessage(chatMessage);
            } else {
                TextComponent chatMessageComponent = new TextComponent(chatMessage);
                if (chatData.clickEventAction != null) {
                    chatMessageComponent.setClickEvent(new ClickEvent(chatData.clickEventAction, PlaceholderAPI.applyPlaceholders(chatData.clickEventValue, player, castData.getWeaponStack(), castData.getWeaponTitle())));
                }
                if (chatData.hoverEventValue != null) {
                    Content content = new Text(TextComponent.fromLegacyText(PlaceholderAPI.applyPlaceholders(chatData.hoverEventValue, player, castData.getWeaponStack(), castData.getWeaponTitle())));
                    chatMessageComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, content));
                }
                player.spigot().sendMessage(ChatMessageType.CHAT, chatMessageComponent);
            }
        }

        if (actionBar != null && (!isWeaponInfoCast || !messageHelper.denyInfoActionBar())) {

            sendActionBar(player, castData.getWeaponStack(), castData.getWeaponTitle());
            if (!isWeaponInfoCast) {
                if (actionBarTime > 40) {

                    messageHelper.updateActionBarTime(actionBarTime);

                    new BukkitRunnable() {
                        int ticker = 0;

                        @Override
                        public void run() {

                            sendActionBar(player, castData.getWeaponStack(), castData.getWeaponTitle());

                            ticker += 40;
                            if (ticker >= actionBarTime) {
                                cancel();
                            }
                        }
                    }.runTaskTimerAsynchronously(WeaponMechanics.getPlugin(), 40, 40);
                } else {
                    messageHelper.updateActionBarTime(40);
                }
            }
        }

        if (titleData != null && (!isWeaponInfoCast || !messageHelper.denyInfoTitle())) {
            String titleMessage = PlaceholderAPI.applyPlaceholders(titleData.title, player, castData.getWeaponStack(), castData.getWeaponTitle());
            String subtitleMessage = PlaceholderAPI.applyPlaceholders(titleData.subtitle, player, castData.getWeaponStack(), castData.getWeaponTitle());
            if (CompatibilityAPI.getVersion() < 1.11) {

                // By default it fade in, stay and fade out takes 60 ticks
                if (!isWeaponInfoCast) messageHelper.updateTitleTime(60);

                player.sendTitle(titleMessage, subtitleMessage);
            } else {

                if (!isWeaponInfoCast) messageHelper.updateTitleTime(titleData.fadeIn + titleData.stay + titleData.fadeOut);

                player.sendTitle(titleMessage, subtitleMessage, titleData.fadeIn, titleData.stay, titleData.fadeOut);
            }
        }

        if (bossBarData != null) {
            String bossBarMessage = PlaceholderAPI.applyPlaceholders(bossBarData.title, player, castData.getWeaponStack(), castData.getWeaponTitle());

            if (isWeaponInfoCast) {

                // This is here to only use ONE boss bar for weapon info displaying
                BossBar bossBar = messageHelper.getCurrentInfoBossBar();
                if (bossBar == null) {
                    // Not found, create new one
                    bossBar = Bukkit.createBossBar(bossBarMessage, bossBarData.barColor, bossBarData.barStyle);
                    bossBar.addPlayer(player);
                    messageHelper.setCurrentInfoBossBar(bossBar);
                } else {
                    // Found, cancel last one's cancel task and set new title and other things
                    Bukkit.getScheduler().cancelTask(messageHelper.getCurrentInfoBossBarTask());
                    bossBar.setTitle(bossBarMessage);
                    bossBar.setColor(bossBarData.barColor);
                    bossBar.setStyle(bossBarData.barStyle);
                }
                // Update the cancel task id
                messageHelper.setCurrentInfoBossBarTask(new BukkitRunnable() {
                    @Override
                    public void run() {
                        // Remove all players, remove boss bar from MessageHandler and remove task id
                        // If this code is reached, new boss bar will be made next time
                        messageHelper.getCurrentInfoBossBar().removeAll();
                        messageHelper.setCurrentInfoBossBar(null);
                        messageHelper.setCurrentInfoBossBarTask(0);
                    }
                }.runTaskLaterAsynchronously(WeaponMechanics.getPlugin(), bossBarData.time).getTaskId());
            } else {
                BossBar bossBar = Bukkit.createBossBar(bossBarMessage, bossBarData.barColor, bossBarData.barStyle);
                bossBar.addPlayer(player);
                new BukkitRunnable() {
                    public void run() {
                        bossBar.removeAll();
                    }
                }.runTaskLaterAsynchronously(WeaponMechanics.getPlugin(), bossBarData.time);
            }
        }
    }

    private void sendActionBar(Player player, ItemStack weaponStack, String weaponTitle) {
        String actionBarMessage = PlaceholderAPI.applyPlaceholders(actionBar, player, weaponStack, weaponTitle);
        if (CompatibilityAPI.getVersion() < 1.09) {
            CompatibilityAPI.getCompatibility().sendPackets(player,
                    ReflectionUtil.newInstance(packetPlayOutChatConstructor,
                            ReflectionUtil.newInstance(chatComponentTextConstructor, actionBarMessage), (byte) 2));
        } else {
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(actionBarMessage));
        }
    }

    @Override
    public boolean requirePlayer() {
        // Make it require player IF global send isn't enabled
        // Since then this message is always directly for player
        return !sendGlobally && !sendGloballyForWorld;
    }

    @Override
    public String getKeyword() {
        return "Message";
    }

    @Override
    public MessageMechanic serialize(File file, ConfigurationSection configurationSection, String path) {

        // CHAT
        ChatData chatData = null;
        String chatMessage = configurationSection.getString(path + ".Chat.Message");
        if (chatMessage != null) {

            ClickEvent.Action clickEventAction = null;
            String clickEventValue = null;
            String hoverEventValue = configurationSection.getString(path + ".Chat.Hover_Text");

            String clickEventString = configurationSection.getString(path + ".Chat.Click");
            if (clickEventString != null) {
                String[] parsedClickEvent = StringUtils.split(clickEventString);
                if (parsedClickEvent.length != 2) {
                    debug.log(LogLevel.ERROR,
                            StringUtils.foundInvalid("parameter length"),
                            StringUtils.foundAt(file, path + ".Chat.Click"),
                            "Make sure the format is <ClickEvent.Action>-<value>");
                    return null;
                }
                try {
                    clickEventAction = ClickEvent.Action.valueOf(parsedClickEvent[0].toUpperCase());
                    clickEventValue = parsedClickEvent[1];
                } catch (IllegalArgumentException e) {
                    debug.log(LogLevel.ERROR,
                            StringUtils.foundInvalid("ClickEvent.Action"),
                            StringUtils.foundAt(file, path + ".Chat.Click", parsedClickEvent[0].toUpperCase()),
                            StringUtils.debugDidYouMean(parsedClickEvent[0].toUpperCase(), ClickEvent.Action.class));
                    return null;
                }
            }
            chatData = new ChatData(StringUtils.color(chatMessage), clickEventAction, StringUtils.color(clickEventValue), StringUtils.color(hoverEventValue));
        }

        // ACTION BAR
        String actionBarMessage = StringUtils.color(configurationSection.getString(path + ".Action_Bar.Message"));
        int actionBarTime = configurationSection.getInt(path + ".Action_Bar.Time");
        if (actionBarTime != 0 && actionBarTime < 40) {
            debug.log(LogLevel.ERROR,
                    StringUtils.foundInvalid("action bar time"),
                    StringUtils.foundAt(file, path + ".Action_Bar.Time", actionBarTime),
                    "If action bar time is below 40, don't use it at all!");
            return null;
        }

        // TITLE
        TitleData titleData = null;
        String titleMessage = configurationSection.getString(path + ".Title.Title");
        String subtitleMessage = configurationSection.getString(path + ".Title.Subtitle");
        if (titleMessage != null || subtitleMessage != null) {
            int fadeIn = 0, stay = 0, fadeOut = 0;
            String titleTime = configurationSection.getString(path + ".Title.Time");
            if (titleTime != null) {
                String[] splittedTitleTime = StringUtils.split(titleTime);
                if (splittedTitleTime.length != 3) {
                    debug.log(LogLevel.ERROR,
                            StringUtils.foundInvalid("parameter length"),
                            StringUtils.foundAt(file, path + ".Title.Time"),
                            "Make sure the format is <fade in ticks>-<stay ticks>-<fade out ticks>");
                    return null;
                }
                try {
                    fadeIn = Integer.parseInt(splittedTitleTime[0]);
                    stay = Integer.parseInt(splittedTitleTime[1]);
                    fadeOut = Integer.parseInt(splittedTitleTime[2]);
                } catch (NumberFormatException e) {
                    debug.log(LogLevel.ERROR,
                            StringUtils.foundInvalid("number format"),
                            StringUtils.foundAt(file, path + ".Title.Time", titleTime));
                    return null;
                }
            }

            titleData = new TitleData(StringUtils.color(titleMessage), StringUtils.color(subtitleMessage), fadeIn, stay, fadeOut);
        }

        // BOSS BAR
        BossBarData bossBarData = null;
        String bossBarMessage = configurationSection.getString(path + ".Boss_Bar.Title");
        if (bossBarMessage != null) {
            if (CompatibilityAPI.getVersion() < 1.09) {
                debug.log(LogLevel.WARN, "Boss bar isn't available in 1.8 server version.");
                return null;
            }
            BarColor barColor;
            String stringBarColor = configurationSection.getString(path + ".Boss_Bar.Bar_Color").toUpperCase();
            try {
                barColor = BarColor.valueOf(stringBarColor);
            } catch (IllegalArgumentException e) {
                debug.log(LogLevel.ERROR,
                        StringUtils.foundInvalid("bar color"),
                        StringUtils.foundAt(file, path + ".Boss_Bar.Bar_Color", stringBarColor),
                        StringUtils.debugDidYouMean(stringBarColor, BarColor.class));
                return null;
            }
            BarStyle barStyle;
            String stringBarStyle = configurationSection.getString(path + ".Boss_Bar.Bar_Style").toUpperCase();
            try {
                barStyle = BarStyle.valueOf(stringBarStyle);
            } catch (IllegalArgumentException e) {
                debug.log(LogLevel.ERROR,
                        StringUtils.foundInvalid("bar style"),
                        StringUtils.foundAt(file, path + ".Boss_Bar.Bar_Style", stringBarStyle),
                        StringUtils.debugDidYouMean(stringBarStyle, BarStyle.class));
                return null;
            }
            int time = configurationSection.getInt(path + ".Boss_Bar.Time");
            bossBarData = new BossBarData(StringUtils.color(bossBarMessage), barColor, barStyle, time);
        }

        boolean sendGlobally = configurationSection.getBoolean(path + ".Send_Globally");
        boolean sendGloballyForWorld = configurationSection.getBoolean(path + ".Send_Globally_For_World");
        return new MessageMechanic(sendGlobally, sendGloballyForWorld, chatData, actionBarMessage, actionBarTime, titleData, bossBarData);
    }

    private static class ChatData {

        public String message;
        public ClickEvent.Action clickEventAction;
        public String clickEventValue;
        // No need for HoverEvent.Action since only SHOW_TEXT is usable
        public String hoverEventValue;

        public ChatData(String message, ClickEvent.Action clickEventAction, String clickEventValue, String hoverEventValue) {
            this.message = message;
            this.clickEventAction = clickEventAction;
            this.clickEventValue = clickEventValue;
            this.hoverEventValue = hoverEventValue;
        }
    }

    private static class TitleData {

        public String title;
        public String subtitle;
        public int fadeIn;
        public int stay;
        public int fadeOut;

        public TitleData(String title, String subtitle, int fadeIn, int stay, int fadeOut) {
            this.title = title;
            this.subtitle = subtitle;
            if (this.subtitle != null && this.title == null) {
                this.title = "";
            }
            this.fadeIn = fadeIn == 0 ? 10 : fadeIn;
            this.stay = stay == 0 ? 20 : stay;
            this.fadeOut = fadeOut == 0 ? 10 : fadeOut;
        }
    }

    private static class BossBarData {

        public String title;
        public BarColor barColor;
        public BarStyle barStyle;
        public int time;

        public BossBarData(String title, BarColor barColor, BarStyle barStyle, int time) {
            this.title = title;
            this.barColor = barColor == null ? BarColor.WHITE : barColor;
            this.barStyle = barStyle == null ? BarStyle.SOLID : barStyle;
            this.time = time == 0 ? 20 : time;
        }
    }
}