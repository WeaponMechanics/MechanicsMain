package me.deecaad.weaponmechanics.commands;

import me.deecaad.core.commands.SubCommand;
import me.deecaad.core.utils.StringUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class GetCommand extends SubCommand {
    
    public GetCommand() {
        super("wm", "get", "Get a given number of weapons", "<weapon> <amount>");
    }
    
    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            // Tell console this is a Player only command
            return;
        }
        Player player = (Player) sender;
        
        if (args.length == 0) {
            // open gui with all weapons
            return;
        }
        // get gun item from args[1]
        // Use args[2] for amount
        // give to player
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
