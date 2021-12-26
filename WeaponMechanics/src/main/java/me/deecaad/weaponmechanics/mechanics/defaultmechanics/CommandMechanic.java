package me.deecaad.weaponmechanics.mechanics.defaultmechanics;

import me.deecaad.core.placeholder.PlaceholderAPI;
import me.deecaad.core.utils.StringUtil;
import me.deecaad.weaponmechanics.WeaponMechanics;
import me.deecaad.weaponmechanics.mechanics.CastData;
import me.deecaad.weaponmechanics.mechanics.IMechanic;
import me.deecaad.weaponmechanics.mechanics.Mechanics;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class CommandMechanic implements IMechanic<CommandMechanic> {

    private List<CommandData> commandList;

    /**
     * Empty constructor to be used as serializer
     */
    public CommandMechanic() {
        if (Mechanics.hasMechanic(getKeyword())) return;
        Mechanics.registerMechanic(WeaponMechanics.getPlugin(), this);
    }

    public CommandMechanic(List<CommandData> commandList) {
        this.commandList = commandList;
    }

    @Override
    public void use(CastData castData) {
        Player player = castData.getCaster() instanceof Player ? (Player) castData.getCaster() : null;
        for (CommandData commandData : commandList) {
            String command = PlaceholderAPI.applyPlaceholders(commandData.getCommand(), player, castData.getWeaponStack(), castData.getWeaponTitle());
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
    public CommandMechanic serialize(File file, ConfigurationSection configurationSection, String path) {
        List<String> stringCommandList = configurationSection.getStringList(path);
        if (stringCommandList == null || stringCommandList.isEmpty()) return null;

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