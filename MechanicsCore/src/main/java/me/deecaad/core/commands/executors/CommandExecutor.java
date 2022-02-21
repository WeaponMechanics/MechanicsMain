package me.deecaad.core.commands.executors;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.function.BiConsumer;

public abstract class CommandExecutor<T extends CommandSender> {

    private final Class<T> executor;

    public CommandExecutor(Class<T> executor) {
        this.executor = executor;
    }

    public final Class<T> getExecutor() {
        return executor;
    }

    public abstract void execute(T sender, Object[] arguments);


    public static CommandExecutor<Player> player(BiConsumer<Player, Object[]> command) {
        return new CommandExecutor<Player>(Player.class) {
            @Override
            public void execute(Player sender, Object[] arguments) {
                command.accept(sender, arguments);
            }
        };
    }
}
