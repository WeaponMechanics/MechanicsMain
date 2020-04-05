package me.deecaad.weaponmechanics.commands.testcommands;

import me.deecaad.core.commands.SubCommand;
import me.deecaad.core.utils.StringUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class WorldGuardCommand extends SubCommand {

    public WorldGuardCommand() {
        super("wm test", "region", "Allows testing for stateflags in regions", "<stateflag>");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            boolean isAllowed = true; // todo
            sender.sendMessage(StringUtils.color("&7isAllowed: &6" + isAllowed));
        }
    }

    @Override
    public List<String> handleCustomTag(String[] args, String tag) {
        switch (tag) {
            case "<stateflag>":
                return new ArrayList<>(); // todo
            default:
                return new ArrayList<>();
        }
    }
}
