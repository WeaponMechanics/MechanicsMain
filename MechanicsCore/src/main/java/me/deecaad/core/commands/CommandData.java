package me.deecaad.core.commands;

import org.bukkit.command.CommandSender;

public class CommandData {

    public final CommandSender sender;
    public final Object[] previousArguments;
    public final String input;
    public final String current;

    public CommandData(CommandSender sender, Object[] previousArguments, String input, String current) {
        this.sender = sender;
        this.previousArguments = previousArguments;
        this.input = input;
        this.current = current;
    }
}
