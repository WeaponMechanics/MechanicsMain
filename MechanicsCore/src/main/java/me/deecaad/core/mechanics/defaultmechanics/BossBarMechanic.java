package me.deecaad.core.mechanics.defaultmechanics;

import me.deecaad.core.MechanicsCore;
import me.deecaad.core.file.inline.Argument;
import me.deecaad.core.file.inline.ArgumentMap;
import me.deecaad.core.file.inline.types.DoubleType;
import me.deecaad.core.file.inline.types.EnumType;
import me.deecaad.core.file.inline.types.IntegerType;
import me.deecaad.core.file.inline.types.StringType;
import me.deecaad.core.mechanics.CastData;
import me.deecaad.core.mechanics.Mechanic;
import me.deecaad.core.placeholder.PlaceholderAPI;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Map;

public class BossBarMechanic extends Mechanic {

    public static final Argument TITLE = new Argument("title", new StringType(true));
    public static final Argument COLOR = new Argument("color", new EnumType<>(BossBar.Color.class), BossBar.Color.WHITE);
    public static final Argument STYLE = new Argument("style", new EnumType<>(BossBar.Overlay.class), BossBar.Overlay.PROGRESS);
    public static final Argument PROGRESS = new Argument("progress", new DoubleType(0.0, 1.0), 1.0);
    public static final Argument TIME = new Argument("time", new IntegerType(0), 100);

    private final String title;
    private final BossBar.Color color;
    private final BossBar.Overlay style;
    private final float progress;
    private final int time;

    public BossBarMechanic(Map<Argument, Object> args) {
        super(args);

        title = (String) args.get(TITLE);
        color = (BossBar.Color) args.get(COLOR);
        style = (BossBar.Overlay) args.get(STYLE);
        progress = ((Number) args.get(PROGRESS)).floatValue();
        time = (int) args.get(TIME);
    }

    @Override
    public ArgumentMap args() {
        return super.args().addAll(TITLE, COLOR, STYLE, PROGRESS, TIME);
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
        return "Message";
    }
}