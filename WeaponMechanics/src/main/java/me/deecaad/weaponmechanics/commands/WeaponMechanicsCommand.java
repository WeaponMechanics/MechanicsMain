package me.deecaad.weaponmechanics.commands;

import me.deecaad.core.commands.CommandBuilder;
import me.deecaad.core.commands.Tooltip;
import me.deecaad.core.commands.arguments.Argument;
import me.deecaad.core.commands.arguments.EntityArgumentType;
import me.deecaad.core.commands.arguments.IntegerArgumentType;
import me.deecaad.core.commands.arguments.StringArgumentType;
import me.deecaad.core.commands.executors.CommandExecutor;
import me.deecaad.weaponmechanics.WeaponMechanics;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;

public class WeaponMechanicsCommand {

    public static void build() {
        CommandBuilder command = new CommandBuilder("wm")
                .withSubCommand(
                        new CommandBuilder("list")
                                .withArgument(new Argument<>("page", new IntegerArgumentType()))
                                .executes(new CommandExecutor<CommandSender>(CommandSender.class) {
                                    @Override
                                    public void execute(CommandSender sender, Object[] arguments) {
                                        sender.sendMessage("list: " + Arrays.toString(arguments));
                                    }
                                }))
                .withSubCommand(
                        new CommandBuilder("give")
                                .withArgument(new Argument<>("target", new EntityArgumentType()))
                                .withArgument(new Argument<>("weapon", new StringArgumentType()).replace(data -> WeaponMechanics.getWeaponHandler().getInfoHandler().getSortedWeaponList().stream().map(Tooltip::of).toArray(Tooltip[]::new)))
                                .executes(CommandExecutor.player((player, args) -> {
                                    ItemStack weapon = WeaponMechanics.getWeaponHandler().getInfoHandler().generateWeapon(args[1].toString(), 1);
                                    Entity entity = (Entity) args[0];

                                    if (entity instanceof Player) {
                                        ((Player) entity).getInventory().addItem(weapon);
                                    } else if (entity instanceof LivingEntity) {
                                        ((LivingEntity) entity).getEquipment().setItemInMainHand(weapon);
                                    } else {
                                        player.sendMessage("bad entity type " + entity.getType());
                                    }
                                })));

        command.register();

    }
}
