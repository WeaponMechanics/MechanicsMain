package me.deecaad.core.mechanics.defaultmechanics;

import me.deecaad.core.MechanicsCore;
import me.deecaad.core.file.inline.Argument;
import me.deecaad.core.file.inline.ArgumentMap;
import me.deecaad.core.file.inline.types.StringType;
import me.deecaad.core.mechanics.CastData;
import me.deecaad.core.mechanics.Mechanic;
import me.deecaad.core.placeholder.PlaceholderAPI;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

public class MessageMechanic extends Mechanic {

    public static final Argument MESSAGE = new Argument("message", new StringType(true));

    private String message;

    /**
     * Default constructor for serializer.
     */
    public MessageMechanic() {
    }

    public MessageMechanic(Map<Argument, Object> args) {
        super(args);

        message = (String) args.get(MESSAGE);
    }

    @Override
    public ArgumentMap args() {
        return super.args().addAll(MESSAGE);
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
        Component chat = PARSER.deserialize(PlaceholderAPI.applyPlaceholders(message, player, itemStack, itemTitle, null, tempPlaceholders));
        Audience audience = MechanicsCore.getPlugin().adventure.player(player);
        audience.sendMessage(chat);
    }

    @Override
    public String getKeyword() {
        return "Message";
    }
}