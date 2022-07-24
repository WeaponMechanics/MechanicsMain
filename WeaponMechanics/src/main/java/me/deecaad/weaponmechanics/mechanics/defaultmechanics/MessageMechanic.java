package me.deecaad.weaponmechanics.mechanics.defaultmechanics;

import me.deecaad.core.file.SerializeData;
import me.deecaad.core.file.SerializerException;
import me.deecaad.weaponmechanics.mechanics.CastData;
import me.deecaad.weaponmechanics.mechanics.IMechanic;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.title.Title;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;

public class MessageMechanic implements IMechanic<MessageMechanic> {

    private boolean sendServer;
    private boolean sendWorld;
    private Component chat;
    private Component actionBar;
    private int actionBarTime;
    private Title title;
    private BossBar bossBar;
    private int bossBarTime;

    @NotNull
    @Override
    public MessageMechanic serialize(SerializeData data) throws SerializerException {
        MiniMessage parser = MiniMessage.miniMessage();

        boolean sendServer = data.of("Send_All_Server").getBool(false);
        boolean sendWorld = data.of("Send_All_World").getBool(false);
        String chatStr = data.of("Chat_Message").get(null);
        Component chat = chatStr == null ? null : parser.deserialize(chatStr);

        String actionBarStr = data.of("Action_Bar.Message").get(null);
        Component actionBar = actionBarStr == null ? null : parser.deserialize(actionBarStr);
        int actionBarTime = data.of("Action_Bar.Time").assertPositive().getInt();

        String titleStr = data.of("Title.Title").get(null);
        String subtitleStr = data.of("Title.Subtitle").get(null);
        Duration fadeIn = Duration.ofMillis(data.of("Title.Fade_In").assertPositive().getInt(0) * 50L);
        Duration stay = Duration.ofMillis(data.of("Title.Stay").assertPositive().getInt(20) * 50L);
        Duration fadeOut = Duration.ofMillis(data.of("Title.Fade_Out").assertPositive().getInt(0) * 50L);
        Title.Times times = Title.Times.times(fadeIn, stay, fadeOut);
        Title title = titleStr == null && subtitleStr == null ? null : Title.title(titleStr == null ? Component.empty() : parser.deserialize(titleStr), titleStr == null ? Component.empty() : parser.deserialize(subtitleStr), times);

        String bossBarStr = data.of("Boss_Bar.Title").get(null);
        BossBar bossBar = BossBar.boss

    }

    @Override
    public void use(CastData castData) {

    }
}
