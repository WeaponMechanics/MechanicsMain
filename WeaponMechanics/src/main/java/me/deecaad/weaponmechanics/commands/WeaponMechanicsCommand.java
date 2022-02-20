package me.deecaad.weaponmechanics.commands;

import me.deecaad.core.commands.CommandBuilder;
import me.deecaad.core.commands.Tooltip;
import me.deecaad.core.commands.arguments.Argument;
import me.deecaad.core.commands.arguments.EntityArgumentType;
import me.deecaad.core.commands.arguments.IntegerArgumentType;
import me.deecaad.core.commands.arguments.StringArgumentType;
import me.deecaad.weaponmechanics.WeaponMechanics;

public class WeaponMechanicsCommand {

    public static void build() {
        CommandBuilder command = new CommandBuilder("wm")
                .withSubCommand(
                        new CommandBuilder("list")
                                .withArgument(new Argument<>("page", new IntegerArgumentType()))
                ).withSubCommand(
                        new CommandBuilder("give")
                                .withArgument(new Argument<>("target", new EntityArgumentType()).append(data -> new Tooltip[]{ Tooltip.of("me", data.sender.getName()) }))
                                .withArgument(new Argument<>("weapon", new StringArgumentType()).replace(data -> WeaponMechanics.getWeaponHandler().getInfoHandler().getSortedWeaponList().stream().map(Tooltip::of).toArray(Tooltip[]::new)))
                );

        command.register();

    }
}
