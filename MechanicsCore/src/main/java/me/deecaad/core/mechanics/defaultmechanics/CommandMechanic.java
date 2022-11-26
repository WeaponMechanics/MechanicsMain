package me.deecaad.core.mechanics.defaultmechanics;

import me.deecaad.core.MechanicsCore;
import me.deecaad.core.file.SerializeData;
import me.deecaad.core.file.SerializerException;
import me.deecaad.core.mechanics.CastData;
import me.deecaad.core.mechanics.IMechanic;
import me.deecaad.core.mechanics.Mechanics;
import me.deecaad.core.placeholder.PlaceholderAPI;
import me.deecaad.core.utils.StringUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CommandMechanic implements IMechanic<CommandMechanic> {

    private List<CommandData> commandList;

    /**
     * Empty constructor to be used as serializer
     */
    public CommandMechanic() {
        if (Mechanics.hasMechanic(getKeyword())) return;
        Mechanics.registerMechanic(MechanicsCore.getPlugin(), this);
    }

    public CommandMechanic(List<CommandData> commandList) {
        this.commandList = commandList;
    }

    @Override
    public void use(CastData castData) {
        Player player = castData.getCaster().getType() == EntityType.PLAYER ? (Player) castData.getCaster() : null;
        String itemTitle = castData.getItemTitle();
        ItemStack itemStack = castData.getItemStack();
        Map<String, String> tempPlaceholders = castData.getTempPlaceholders();

        for (CommandData commandData : commandList) {
            String command = PlaceholderAPI.applyPlaceholders(commandData.getCommand(), player, itemStack, itemTitle, null, tempPlaceholders);
            if (commandData.isConsole()) {
                Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), command);
            } else if (player != null) {
                player.performCommand(command);
            }
        }
    }

    @Override
    public String getKeyword() {
        return "Commands";
    }

    @Override
    public boolean shouldSerialize(SerializeData data) {

        // Let Mechanics handle all automatic serializer stuff
        return false;
    }

    @Override
    @Nonnull
    public CommandMechanic serialize(SerializeData data) throws SerializerException {
        List<String> stringCommandList = data.config.getStringList(data.key);

        List<CommandData> commandList = new ArrayList<>();
        for (String commandInList : stringCommandList) {
            String command = StringUtil.color(commandInList);
            if (command.toLowerCase().startsWith("console:")) {
                command = command.substring("console:".length());
                commandList.add(new CommandData(true, command));
            } else {
                commandList.add(new CommandData(false, command));
            }
        }

        return new CommandMechanic(commandList);
    }

    private static class CommandData {

        private final boolean console;
        private final String command;

        public CommandData(boolean console, String command) {
            this.console = console;
            this.command = command;
        }

        public boolean isConsole() {
            return console;
        }

        public String getCommand() {
            return command;
        }
    }
}