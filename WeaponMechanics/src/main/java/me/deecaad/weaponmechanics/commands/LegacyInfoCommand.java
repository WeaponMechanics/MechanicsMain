package me.deecaad.weaponmechanics.commands;

import me.deecaad.core.commands.CommandPermission;
import me.deecaad.core.commands.SubCommand;
import org.bukkit.command.CommandSender;

import java.util.Collection;

@CommandPermission(permission = "weaponmechanics.commands.info")
@Deprecated
public class LegacyInfoCommand extends SubCommand {

    public static char SYM = '\u27A2';

    public LegacyInfoCommand() {
        super("wm", "info", "General plugin information");
    }
    
    @Override
    public void execute(CommandSender sender, String[] args) {
        WeaponMechanicsCommand.info(sender);
    }

    public static String toString(Collection<?> list) {
        StringBuilder builder = new StringBuilder();
        for (Object obj : list) {
            builder.append(obj).append(", ");
        }
        builder.setLength(builder.length() - 2);
        return builder.toString();
    }
}
