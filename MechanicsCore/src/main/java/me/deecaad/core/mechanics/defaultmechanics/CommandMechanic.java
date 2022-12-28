package me.deecaad.core.mechanics.defaultmechanics;

import me.deecaad.core.file.inline.Argument;
import me.deecaad.core.file.inline.ArgumentMap;
import me.deecaad.core.file.inline.types.BooleanType;
import me.deecaad.core.file.inline.types.StringType;
import me.deecaad.core.mechanics.CastData;
import me.deecaad.core.mechanics.Mechanic;
import me.deecaad.core.placeholder.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

public class CommandMechanic extends Mechanic {

    public static final Argument CONSOLE = new Argument("console", new BooleanType(), false);
    public static final Argument COMMAND = new Argument("command", new StringType());

    private boolean console;
    private String command;

    /**
     * Default constructor for serializer.
     */
    public CommandMechanic() {
    }

    public CommandMechanic(Map<Argument, Object> args) {
        super(args);

        console = (boolean) args.get(CONSOLE);
        command = (String) args.get(COMMAND);
    }

    @Override
    public ArgumentMap args() {
        return super.args().addAll(CONSOLE, COMMAND);
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
}