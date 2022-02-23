package me.deecaad.weaponmechanics.commands;

import me.deecaad.core.commands.CommandBuilder;
import me.deecaad.core.commands.Tooltip;
import me.deecaad.core.commands.Argument;
import me.deecaad.core.commands.arguments.EntityArgumentType;
import me.deecaad.core.commands.arguments.EntityListArgumentType;
import me.deecaad.core.commands.arguments.IntegerArgumentType;
import me.deecaad.core.commands.arguments.StringArgumentType;
import me.deecaad.core.commands.CommandExecutor;
import me.deecaad.weaponmechanics.WeaponMechanics;
import me.deecaad.weaponmechanics.weapon.info.InfoHandler;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.List;

public class WeaponMechanicsCommand {

    public static void build() {
        InfoHandler info = WeaponMechanics.getWeaponHandler().getInfoHandler();

        CommandBuilder command = new CommandBuilder("wm")
                .withAliases("weapon", "weaponmechanics")
                .withSubCommand(
                        new CommandBuilder("list")
                                .withArgument(new Argument<>("page", new IntegerArgumentType(1, 5), 1))
                                .executes(new CommandExecutor<CommandSender>(CommandSender.class) {
                                    @Override
                                    public void execute(CommandSender sender, Object[] arguments) {
                                        sender.sendMessage("list: " + Arrays.toString(arguments));
                                    }
                                }))
                .withSubCommand(
                        new CommandBuilder("give")
                                .withArgument(new Argument<>("target", new EntityListArgumentType()))
                                .withArgument(new Argument<>("weapon", new StringArgumentType()).replace(data -> info.getSortedWeaponList().stream().map(Tooltip::of).toArray(Tooltip[]::new)))
                                .withArgument(new Argument<>("amount", new IntegerArgumentType(1), 1))
                                .executes(CommandExecutor.any((sender, args) -> {

                                    String weaponTitle = info.getWeaponTitle(args[1].toString());
                                    ItemStack weapon = info.generateWeapon(weaponTitle, 1);
                                    List<Entity> entities = (List<Entity>) args[0];

                                    if (entities.isEmpty()) {
                                        sender.sendMessage(ChatColor.RED + "No entities were found");
                                        return;
                                    }

                                    for (Entity entity : entities) {
                                        if (entity instanceof Player) {
                                            ((Player) entity).getInventory().addItem(weapon);
                                        } else if (entity instanceof LivingEntity) {
                                            ((LivingEntity) entity).getEquipment().setItemInMainHand(weapon);
                                        } else {
                                            sender.sendMessage("bad entity type " + entity.getType());
                                        }
                                    }
                                })));

        command.register();

    }
}
