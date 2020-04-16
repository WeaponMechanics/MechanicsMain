package me.deecaad.weaponmechanics.general;

import me.deecaad.compatibility.CompatibilityAPI;
import me.deecaad.core.file.Serializer;
import me.deecaad.core.placeholder.PlaceholderAPI;
import me.deecaad.core.utils.ReflectionUtil;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nullable;
import java.io.File;
import java.lang.reflect.Constructor;

public class SendMessage implements Serializer<SendMessage> {

    private boolean perWorld;
    private String chat;
    private String actionBar;
    private String title;
    private String subtitle;

    /**
     * Reflection support for 1.8 action bar
     */
    private static Constructor<?> packetPlayOutChatConstructor;
    private static Constructor<?> chatComponentTextConstructor;

    /**
     * Empty constructor to be used as serializer
     */
    public SendMessage() {
        if (CompatibilityAPI.getVersion() < 1.09) {
            if (packetPlayOutChatConstructor == null) {
                packetPlayOutChatConstructor = ReflectionUtil.getConstructor(ReflectionUtil.getNMSClass("PacketPlayOutChat"), ReflectionUtil.getNMSClass("IChatBaseComponent"), byte.class);
            }
            if (chatComponentTextConstructor == null) {
                chatComponentTextConstructor = ReflectionUtil.getConstructor(ReflectionUtil.getNMSClass("ChatComponentText"), String.class);
            }
        }
    }

    public SendMessage(boolean perWorld, String chat, String actionBar, String title, String subtitle) {
        this.perWorld = perWorld;
        this.chat = chat;
        this.actionBar = actionBar;
        this.title = title;
        this.subtitle = subtitle;
    }

    /**
     * Sends all given message types for player.
     * All messages are updated using PlaceholderAPI before sent for the player.
     *
     * @param forAll whether or not to send this message for all players in one world
     * @param player the receiving player
     * @param weaponStack if required for PlaceholderAPI checking
     * @param weaponTitle if required for PlaceholderAPI checking
     */
    public void send(boolean forAll, Player player, @Nullable ItemStack weaponStack, @Nullable String weaponTitle) {

        String chat = PlaceholderAPI.applyPlaceholders(this.chat, player, weaponStack, weaponTitle);
        String actionBar = PlaceholderAPI.applyPlaceholders(this.actionBar, player, weaponStack, weaponTitle);
        String title = PlaceholderAPI.applyPlaceholders(this.title, player, weaponStack, weaponTitle);
        String subtitle = PlaceholderAPI.applyPlaceholders(this.subtitle, player, weaponStack, weaponTitle);

        if (forAll) {
            if (this.perWorld) {
                player.getWorld().getPlayers().forEach(worldPlayer -> sendFor(worldPlayer, chat, actionBar, title, subtitle));
            } else {
                Bukkit.getOnlinePlayers().forEach(globalPlayer -> sendFor(globalPlayer, chat, actionBar, title, subtitle));
            }
        } else {
            sendFor(player, chat, actionBar, title, subtitle);
        }

    }

    private void sendFor(Player player, String chat, String actionBar, String title, String subtitle) {
        if (chat != null) {
            player.sendMessage(chat);
        }
        if (actionBar != null) {
            if (CompatibilityAPI.getVersion() < 1.09) {
                CompatibilityAPI.getCompatibility().sendPackets(player, ReflectionUtil.newInstance(packetPlayOutChatConstructor, ReflectionUtil.newInstance(chatComponentTextConstructor, actionBar), (byte) 2));
            } else {
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(actionBar));
            }
        }
        if (title != null || subtitle != null) {
            if (CompatibilityAPI.getVersion() < 1.11) {
                player.sendTitle(title, subtitle);
            } else {
                player.sendTitle(title, subtitle, -1, -1, -1);
            }
        }
    }

    @Override
    public String getKeyword() {
        return "Message";
    }

    @Override
    public SendMessage serialize(File file, ConfigurationSection configurationSection, String path) {
        String chat = configurationSection.getString(path + ".Chat");
        String actionBar = configurationSection.getString(path + ".Action_Bar");
        String title = configurationSection.getString(path + ".Title");
        String subtitle = configurationSection.getString(path + ".Subtitle");
        if (chat == null && actionBar == null && title == null && subtitle == null) {
            return null;
        }
        chat = ChatColor.translateAlternateColorCodes('&', chat);
        actionBar = ChatColor.translateAlternateColorCodes('&', actionBar);
        title = ChatColor.translateAlternateColorCodes('&', title);
        subtitle = ChatColor.translateAlternateColorCodes('&', subtitle);
        boolean perWorld = configurationSection.getBoolean(path + ".Per_World");
        return new SendMessage(perWorld, chat, actionBar, title, subtitle);
    }
}
