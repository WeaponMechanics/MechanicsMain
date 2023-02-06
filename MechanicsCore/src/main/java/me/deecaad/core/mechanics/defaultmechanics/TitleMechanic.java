package me.deecaad.core.mechanics.defaultmechanics;

import me.deecaad.core.MechanicsCore;
import me.deecaad.core.file.SerializeData;
import me.deecaad.core.file.SerializerException;
import me.deecaad.core.mechanics.CastData;
import me.deecaad.core.placeholder.PlaceholderAPI;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.title.Title;
import net.kyori.adventure.util.Ticks;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class TitleMechanic extends Mechanic {

    private String title;
    private String subtitle;
    private Title.Times times;

    /**
     * Default constructor for serializer.
     */
    public TitleMechanic() {
    }

    public TitleMechanic(String title, String subtitle, Title.Times times) {
        this.title = title;
        this.subtitle = subtitle;
        this.times = times;
    }

    public String getTitle() {
        return title;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public Title.Times getTimes() {
        return times;
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

    @Override
    public @Nullable String getWikiLink() {
        return "https://github.com/WeaponMechanics/MechanicsMain/wiki/TitleMechanic";
    }

    @NotNull
    @Override
    public Mechanic serialize(SerializeData data) throws SerializerException {
        String title = data.of("Title").getAdventure(null);
        String subtitle = data.of("Subtitle").getAdventure(null);
        int fadeIn = data.of("Fade_In").assertPositive().getInt(10);
        int stay = data.of("Stay").assertPositive().getInt(70);
        int fadeOut = data.of("Fade_Out").assertPositive().getInt(20);

        // User should define at least one of these...
        if (title == null && subtitle == null)
            throw data.exception(null, "Missing both 'title' and 'subtitle' options");

        Title.Times times = Title.Times.times(Ticks.duration(fadeIn), Ticks.duration(stay), Ticks.duration(fadeOut));
        return applyParentArgs(data, new TitleMechanic(title, subtitle, times));
    }
}