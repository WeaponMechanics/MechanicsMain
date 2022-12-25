package me.deecaad.core.mechanics.defaultmechanics;

import me.deecaad.core.MechanicsCore;
import me.deecaad.core.file.SerializerException;
import me.deecaad.core.file.inline.Argument;
import me.deecaad.core.file.inline.ArgumentMap;
import me.deecaad.core.file.inline.types.IntegerType;
import me.deecaad.core.file.inline.types.StringType;
import me.deecaad.core.mechanics.CastData;
import me.deecaad.core.mechanics.Mechanic;
import me.deecaad.core.placeholder.PlaceholderAPI;
import net.kyori.adventure.Adventure;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.title.Title;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.time.Duration;
import java.util.Map;

public class TitleMechanic extends Mechanic {

    public static final Argument TITLE = new Argument("title", new StringType(true), null);
    public static final Argument SUBTITLE = new Argument("subtitle", new StringType(true), null);
    public static final Argument FADE_IN = new Argument("fadeIn", new IntegerType(0), 5);
    public static final Argument STAY = new Argument("stay", new IntegerType(0), 80);
    public static final Argument FADE_OUT = new Argument("fadeOut", new IntegerType(0), 5);

    private final String title;
    private final String subtitle;
    private final Title.Times times;

    public TitleMechanic(Map<Argument, Object> args) throws SerializerException {
        super(args);

        title = (String) args.get(TITLE);
        subtitle = (String) args.get(SUBTITLE);

        // Construct times object
        Duration fadeIn = Duration.ofMillis((int) args.get(FADE_IN) * 50);
        Duration stay = Duration.ofMillis((int) args.get(STAY) * 50);
        Duration fadeOut = Duration.ofMillis((int) args.get(FADE_OUT) * 50);
        times = Title.Times.times(fadeIn, stay, fadeOut);

        // Should use at least one
        if (title == null && subtitle == null)
            throw new SerializerException("", new String[] {"You must use one of 'title' or 'subtitle', they can't both be blank!"}, "");
    }

    @Override
    public ArgumentMap args() {
        return super.args().addAll(TITLE, SUBTITLE, FADE_IN, STAY, FADE_OUT);
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
        Component titleComponent = title == null ? Component.empty() : PARSER.deserialize(PlaceholderAPI.applyPlaceholders(title, player, itemStack, itemTitle, null, tempPlaceholders));
        Component subtitleComponent = subtitle == null ? Component.empty() : PARSER.deserialize(PlaceholderAPI.applyPlaceholders(subtitle, player, itemStack, itemTitle, null, tempPlaceholders));
        Title title = Title.title(titleComponent, subtitleComponent, times);
        Audience audience = MechanicsCore.getPlugin().adventure.player(player);
        audience.showTitle(title);
    }

    @Override
    public String getKeyword() {
        return "Title";
    }
}