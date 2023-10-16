package me.deecaad.core.mechanics.defaultmechanics;

import me.deecaad.core.file.SerializeData;
import me.deecaad.core.file.SerializerException;
import me.deecaad.core.mechanics.CastData;
import me.deecaad.core.placeholder.PlaceholderMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CommandMechanic extends Mechanic {

    private boolean console;
    private PlaceholderMessage command;

    /**
     * Default constructor for serializer.
     */
    public CommandMechanic() {
    }

    public CommandMechanic(boolean console, String command) {
        this.console = console;
        this.command = new PlaceholderMessage(command);
    }

    public boolean isConsole() {
        return console;
    }

    public String getCommand() {
        return command.getTemplate();
    }

    @Override
    public void use0(CastData cast) {
        Player player = cast.getTarget().getType() == EntityType.PLAYER ? (Player) cast.getTarget() : null;

        String command = LegacyComponentSerializer.legacySection().serialize(this.command.replaceAndDeserialize(cast));
        if (console)
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
        else if (player != null)
            player.performCommand(command);
    }

    @Override
    public String getKeyword() {
        return "Command";
    }

    @Override
    public @Nullable String getWikiLink() {
        return "https://cjcrafter.gitbook.io/mechanics/mechanics/command";
    }

    @NotNull
    @Override
    public Mechanic serialize(@NotNull SerializeData data) throws SerializerException {
        boolean console = data.of("Console").getBool(false);
        String command = data.of("Command").assertType(String.class).assertExists().get();
        return applyParentArgs(data, new CommandMechanic(console, command));
    }
}