package me.deecaad.core.commands;

import org.bukkit.command.CommandSender;

public record CommandData(CommandSender sender, Object[] previousArguments, String input, String current) {
}
