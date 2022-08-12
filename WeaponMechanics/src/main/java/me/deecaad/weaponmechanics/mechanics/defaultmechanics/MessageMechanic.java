package me.deecaad.weaponmechanics.mechanics.defaultmechanics;

import me.deecaad.core.MechanicsCore;
import me.deecaad.core.file.SerializeData;
import me.deecaad.core.file.SerializerException;
import me.deecaad.core.placeholder.PlaceholderAPI;
import me.deecaad.weaponmechanics.WeaponMechanics;
import me.deecaad.weaponmechanics.mechanics.CastData;
import me.deecaad.weaponmechanics.mechanics.IMechanic;
import me.deecaad.weaponmechanics.mechanics.Mechanics;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

public class MessageMechanic implements IMechanic<MessageMechanic> {

    private boolean sendServer;
    private boolean sendWorld;

    private String chatStr;

    private String actionBarStr;
    private int actionBarTime;

    private String titleStr;
    private String subtitleStr;
    private Title.Times times;

    private String bossBarStr;
    private BossBar.Color color;
    private BossBar.Overlay overlay;
    private float progress;
    private int bossBarTime;

    /**
     * Default constructor for serializer
     */
    public MessageMechanic() {
        if (Mechanics.hasMechanic(getKeyword())) return;
        Mechanics.registerMechanic(WeaponMechanics.getPlugin(), this);
    }

    public MessageMechanic(boolean sendServer, boolean sendWorld, String chatStr, String actionBarStr, int actionBarTime,
                           String titleStr, String subtitleStr, Title.Times times, String bossBarStr, BossBar.Color color,
                           BossBar.Overlay overlay, float progress, int bossBarTime) {
        this.sendServer = sendServer;
        this.sendWorld = sendWorld;
        this.chatStr = chatStr;
        this.actionBarStr = actionBarStr;
        this.actionBarTime = actionBarTime;
        this.titleStr = titleStr;
        this.subtitleStr = subtitleStr;
        this.times = times;
        this.bossBarStr = bossBarStr;
        this.color = color;
        this.overlay = overlay;
        this.progress = progress;
        this.bossBarTime = bossBarTime;
    }

    @Override
    public String getKeyword() {
        return "Message";
    }

    @Override
    public boolean shouldSerialize(SerializeData data) {
        // Let Mechanics handle auto serializer stuff
        return false;
    }

    @Override
    public boolean requirePlayer() {
        return !sendWorld && !sendServer;
    }

    @Override
    public void use(CastData cast) {
        if (sendServer) {
            for (Player player : Bukkit.getOnlinePlayers())
                send(player, cast);
        } else if (sendWorld) {
            for (Player player : cast.getCastWorld().getPlayers())
                send(player, cast);
        } else if (cast.getCaster() != null && cast.getCaster().getType() == EntityType.PLAYER) {
            send((Player) cast.getCaster(), cast);
        }
    }

    private void send(Player player, CastData cast) {
        MiniMessage PARSER = MechanicsCore.getPlugin().message;
        Audience audience = MechanicsCore.getPlugin().adventure.player(player);

        Component chat = chatStr == null ? null : PARSER.deserialize(placeholders(chatStr, player, cast));
        Component actionBar = actionBarStr == null ? null : PARSER.deserialize(placeholders(actionBarStr, player, cast));

        Component titleComponent = titleStr == null ? Component.empty() : PARSER.deserialize(placeholders(titleStr, player, cast));
        Component subtitleComponent = subtitleStr == null ? Component.empty() : PARSER.deserialize(placeholders(subtitleStr, player, cast));
        Title title = titleStr == null && subtitleStr == null ? null : Title.title(titleComponent, subtitleComponent, times);

        BossBar bossBar = bossBarStr == null ? null : BossBar.bossBar(PARSER.deserialize(placeholders(bossBarStr, player, cast)), progress, color, overlay);

        if (chat != null) audience.sendMessage(chat);
        if (title != null) audience.showTitle(title);

        // Action Bars are *NOT* timed in vanilla Minecraft. To get around this,
        // we resend the action bar on a timer. This timer system can be improved
        // to support smaller timer amounts, but I am lazy and increments of 40 is
        // good enough for now.
        if (actionBar != null) {
            audience.sendActionBar(actionBar);
            new BukkitRunnable() {
                int ticker = 0;

                @Override
                public void run() {
                    ticker += 40;
                    if (ticker >= actionBarTime) {
                        cancel();
                        return;
                    }

                    audience.sendActionBar(actionBar);
                }
            }.runTaskTimer(WeaponMechanics.getPlugin(), 40, 40);
        }

        if (bossBar != null) {
            audience.showBossBar(bossBar);
            new BukkitRunnable() {
                @Override
                public void run() {
                    audience.hideBossBar(bossBar);
                }
            }.runTaskLater(WeaponMechanics.getPlugin(), bossBarTime);
        }
    }

    private String placeholders(String text, Player player, CastData data) {

        Map<String, String> tempPlaceholders = null;
        String shooterName = data.getData(CommonDataTags.SHOOTER_NAME.name(), String.class);
        String victimName = data.getData(CommonDataTags.VICTIM_NAME.name(), String.class);
        if (shooterName != null || victimName != null) {
            tempPlaceholders = new HashMap<>();
            tempPlaceholders.put("%shooter%", shooterName != null ? shooterName : player.getName());
            tempPlaceholders.put("%victim%", victimName);
        }

        return PlaceholderAPI.applyPlaceholders(text, player, data.getWeaponStack(), data.getWeaponTitle(), null, tempPlaceholders);
    }

    @NotNull
    @Override
    public MessageMechanic serialize(SerializeData data) throws SerializerException {

        boolean sendServer = data.of("Send_All_Server").getBool(false);
        boolean sendWorld = data.of("Send_All_World").getBool(false);
        String chatStr = data.of("Chat_Message").get(null);

        String actionBarStr = data.of("Action_Bar.Message").getAdventure(null);
        int actionBarTime = data.of("Action_Bar.Time").assertPositive().getInt(40);

        String titleStr = data.of("Title.Title").getAdventure(null);
        String subtitleStr = data.of("Title.Subtitle").getAdventure(null);
        Duration fadeIn = Duration.ofMillis(data.of("Title.Fade_In").assertPositive().getInt(0) * 50L);
        Duration stay = Duration.ofMillis(data.of("Title.Stay").assertPositive().getInt(20) * 50L);
        Duration fadeOut = Duration.ofMillis(data.of("Title.Fade_Out").assertPositive().getInt(0) * 50L);
        Title.Times times = Title.Times.times(fadeIn, stay, fadeOut);

        String bossBarStr = data.of("Boss_Bar.Title").getAdventure(null);
        BossBar.Color color = data.of("Boss_Bar.Color").getEnum(BossBar.Color.class, BossBar.Color.WHITE);
        BossBar.Overlay style = data.of("Boss_Bar.Style").getEnum(BossBar.Overlay.class, BossBar.Overlay.PROGRESS);
        float progress = (float) data.of("Boss_Bar.Progress").assertRange(0.0, 1.0).getDouble(1.0);
        int bossBarTime = data.of("Boss_Bar.Time").assertPositive().getInt(100);

        return new MessageMechanic(sendServer, sendWorld, chatStr, actionBarStr, actionBarTime, titleStr, subtitleStr, times, bossBarStr, color, style, progress, bossBarTime);
    }
}
