package me.deecaad.core.mechanics.defaultmechanics;

import me.deecaad.core.file.SerializeData;
import me.deecaad.core.file.SerializerException;
import me.deecaad.core.mechanics.CastData;
import me.deecaad.core.placeholder.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class CommandMechanic extends Mechanic {

    private boolean console;
    private String command;

    /**
     * Default constructor for serializer.
     */
    public CommandMechanic() {
    }

    public CommandMechanic(boolean console, String command) {
        this.console = console;
        this.command = command;
    }

    public boolean isConsole() {
        return console;
    }

    public String getCommand() {
        return command;
    }

    @Override
    public void use0(CastData cast) {
        Player player = cast.getTarget().getType() == EntityType.PLAYER ? (Player) cast.getTarget() : null;
        String itemTitle = cast.getItemTitle();
        ItemStack itemStack = cast.getItemStack();
        Map<String, String> tempPlaceholders = cast.getTempPlaceholders();

        String command = PlaceholderAPI.applyPlaceholders(this.command, player, itemStack, itemTitle, null, tempPlaceholders);
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
        return "https://github.com/WeaponMechanics/MechanicsMain/wiki/CommandMechanic";
    }

    @NotNull
    @Override
    public Mechanic serialize(SerializeData data) throws SerializerException {
        boolean console = data.of("Console").getBool(false);
        String command = data.of("Command").assertType(String.class).assertExists().get();
        return applyParentArgs(data, new CommandMechanic(console, command));
    }
}