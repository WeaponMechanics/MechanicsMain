package me.deecaad.weaponmechanics.commands.testcommands;

import me.deecaad.core.commands.SubCommand;
import org.bukkit.command.CommandSender;

import java.util.Arrays;

public class TestCommand extends SubCommand {

    public TestCommand() {
        super("wm", "test", "Random test functions for devs", SUB_COMMANDS);
        
        commands.register(new HitboxCommand());
        commands.register(new ExplosionCommand());
        commands.register(new WorldGuardCommand());
        commands.register(new RecoilCommand());
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length > 0) {
            commands.execute(args[0], sender, Arrays.copyOfRange(args, 1, args.length));
            return;
        }
        sendHelp(sender, args);
    }
}
