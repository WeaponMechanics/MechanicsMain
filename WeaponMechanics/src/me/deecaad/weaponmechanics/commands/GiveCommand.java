package me.deecaad.weaponmechanics.commands;

import me.deecaad.core.commands.CommandPermission;
import me.deecaad.core.commands.SubCommand;
import me.deecaad.core.utils.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

@CommandPermission(permission = "weaponmechanics.commands.give")
public class GiveCommand extends SubCommand {
    
    // give <Player> <Weapon> <Amount>
    
    public GiveCommand() {
        super("wm", "give", "Gives a given number of weapons to a given player", "<player> <weapon> <amount>");
    }
    
    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(StringUtils.color(toString()));
            return;
        }
        Player player = Bukkit.getPlayer(args[0]);
        if (player == null) {
            player.sendMessage(StringUtils.color("&cCould not find player \"" + args[0] + "\""));
            return;
        }
        
        if (args.length < 2 && sender instanceof Player) {
            // sender opens an inventory to put weapons in player's inv
            return;
        }
        // weapon from args[1]
        // amount from args[2]
        return;
    }

    @Override
    public List<String> handleCustomTag(String[] args, String current) {
        switch (current) {
            case "<weapon>":
                return StringUtils.getList("ak-47", "ar-15", "desert_eagle", "Minigun", "Sniper");
            default:
                return super.handleCustomTag(args, current);
        }
    }
}
