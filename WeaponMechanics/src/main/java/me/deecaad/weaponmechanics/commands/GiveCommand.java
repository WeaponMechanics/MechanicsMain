package me.deecaad.weaponmechanics.commands;

import me.deecaad.core.commands.CommandPermission;
import me.deecaad.core.commands.SubCommand;
import me.deecaad.core.utils.StringUtil;
import me.deecaad.weaponmechanics.weapon.info.InfoHandler;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

import static me.deecaad.weaponmechanics.WeaponMechanics.getWeaponHandler;

@CommandPermission(permission = "weaponmechanics.commands.give")
public class GiveCommand extends SubCommand {
    
    // wm give <Player> <Weapon> <Amount>
    
    public GiveCommand() {
        super("wm", "give", "Gives a given number of weapons to a given player", "<weapon> <amount> <player>");

        setAliases(Collections.singletonList("get"));
    }
    
    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length == 0 || args.length > 3) {
            sender.sendMessage(StringUtil.color(toString()));
            return;
        }

        InfoHandler info = getWeaponHandler().getInfoHandler();
        String weaponTitle = info.getWeaponTitle(args[0]);
        if (!getWeaponHandler().getInfoHandler().hasWeapon(weaponTitle)) {
            sender.sendMessage(ChatColor.RED + "Could not find weapon " + args[0]);
            return;
        }

        int amount = 1;
        if (args.length > 1) {
            try {
                amount = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                sender.sendMessage(ChatColor.RED + "Tried to use amount which wasn't number " + args[1]);
                return;
            }
            if (amount < 0 || amount > 64) {
                sender.sendMessage(ChatColor.RED + "Tried to use amount which was less than 1 or more than 64");
                return;
            }
        }

        Player player;
        if (args.length > 2) {
            player = Bukkit.getPlayer(args[2]);
            if (player == null) {
                sender.sendMessage(ChatColor.RED + "Could not find player " + args[2]);
                return;
            }
        } else if (sender instanceof Player) {
            player = (Player) sender;
        } else {
            // Not player
            sender.sendMessage(ChatColor.RED + "You can't give weapons for console, sorry. :(");
            return;
        }

        info.giveOrDropWeapon(info.getWeaponTitle(weaponTitle), player, amount);
    }

    @Override
    public List<String> handleCustomTag(String[] args, String current) {
        switch (current) {
            case "<weapon>":
                return getWeaponHandler().getInfoHandler().getSortedWeaponList();
            default:
                return super.handleCustomTag(args, current);
        }
    }
}