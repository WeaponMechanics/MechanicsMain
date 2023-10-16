package me.deecaad.core.mechanics.defaultmechanics;

import me.deecaad.core.MechanicsCore;
import me.deecaad.core.file.SerializeData;
import me.deecaad.core.file.SerializerException;
import me.deecaad.core.mechanics.CastData;
import me.deecaad.core.placeholder.PlaceholderMessage;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MessageMechanic extends Mechanic {

    private PlaceholderMessage message;

    /**
     * Default constructor for serializer.
     */
    public MessageMechanic() {
    }

    public MessageMechanic(String message) {
        this.message = new PlaceholderMessage(message);
    }

    public String getMessage() {
        return message.getTemplate();
    }

    @Override
    public void use0(CastData cast) {
        if (!(cast.getTarget() instanceof Player player))
            return;

        // Parse and send the message to the 1 player
        // TODO this method would benefit from having access to the target list
        Component chat = message.replaceAndDeserialize(cast);
        Audience audience = MechanicsCore.getPlugin().adventure.player(player);
        audience.sendMessage(chat);
    }

    @Override
    public String getKeyword() {
        return "Message";
    }

    @Override
    public @Nullable String getWikiLink() {
        return "https://cjcrafter.gitbook.io/mechanics/mechanics/message";
    }

    @NotNull
    @Override
    public Mechanic serialize(@NotNull SerializeData data) throws SerializerException {
        String message = data.of("Message").assertExists().getAdventure();
        return applyParentArgs(data, new MessageMechanic(message));
    }
}