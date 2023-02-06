package me.deecaad.core.mechanics.defaultmechanics;

import me.deecaad.core.MechanicsCore;
import me.deecaad.core.file.SerializeData;
import me.deecaad.core.file.SerializerException;
import me.deecaad.core.file.serializers.ChanceSerializer;
import me.deecaad.core.mechanics.CastData;
import me.deecaad.core.placeholder.PlaceholderAPI;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class BossBarMechanic extends Mechanic {

    private String title;
    private BossBar.Color color;
    private BossBar.Overlay style;
    private float progress;
    private int time;

    public BossBarMechanic() {
    }

    public BossBarMechanic(String title, BossBar.Color color, BossBar.Overlay style, float progress, int time) {
        this.title = title;
        this.color = color;
        this.style = style;
        this.progress = progress;
        this.time = time;
    }

    public String getTitle() {
        return title;
    }

    public BossBar.Color getColor() {
        return color;
    }

    public BossBar.Overlay getStyle() {
        return style;
    }

    public float getProgress() {
        return progress;
    }

    public int getTime() {
        return time;
    }

    @Override
    public void use0(CastData cast) {
        if (!(cast.getTarget() instanceof Player player))
            return;

        String itemTitle = cast.getItemTitle();
        ItemStack itemStack = cast.getItemStack();
        Map<String, String> tempPlaceholders = cast.getTempPlaceholders();

        // Parse and send the message to the 1 player
        // TODO this method would benefit from having access to the target list
        MiniMessage PARSER = MechanicsCore.getPlugin().message;
        Component chat = PARSER.deserialize(PlaceholderAPI.applyPlaceholders(title, player, itemStack, itemTitle, null, tempPlaceholders));
        Audience audience = MechanicsCore.getPlugin().adventure.player(player);
        BossBar bossBar = BossBar.bossBar(chat, progress, color, style);

        audience.showBossBar(bossBar);
        new BukkitRunnable() {
            @Override
            public void run() {
                audience.hideBossBar(bossBar);
            }
        }.runTaskLater(MechanicsCore.getPlugin(), time);
    }

    @Override
    public String getKeyword() {
        return "Boss_Bar";
    }

    @Override
    public @Nullable String getWikiLink() {
        return "https://github.com/WeaponMechanics/MechanicsMain/wiki/BossBarMechanic";
    }

    @NotNull
    @Override
    public Mechanic serialize(SerializeData data) throws SerializerException {
        String title = data.of("Title").assertExists().getAdventure();
        BossBar.Color color = data.of("Color").getEnum(BossBar.Color.class, BossBar.Color.RED);
        BossBar.Overlay style = data.of("Style").getEnum(BossBar.Overlay.class, BossBar.Overlay.PROGRESS);
        Double progressTemp = data.of("Progress").serialize(new ChanceSerializer());
        float progress = progressTemp == null ? 1.0f : progressTemp.floatValue();
        int time = data.of("Time").assertPositive().getInt(100);

        return applyParentArgs(data, new BossBarMechanic(title, color, style, progress, time));
    }
}