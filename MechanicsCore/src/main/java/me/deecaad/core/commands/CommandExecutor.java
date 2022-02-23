package me.deecaad.core.commands;

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

    /**
     * Shorthand to create a command using lambda expressions.
     *
     * @param command The non-null command to execute.
     * @return The non-null constructed CommandExecutor.
     */
    public static CommandExecutor<CommandSender> any(BiConsumer<CommandSender, Object[]> command) {
        return new CommandExecutor<CommandSender>(CommandSender.class) {
            @Override
            public void execute(CommandSender sender, Object[] arguments) {
                command.accept(sender, arguments);
            }
        };
    }

    /**
     * Shorthand to create a player only command using lambda expressions.
     *
     * @param command The non-null command to execute (which takes the player
     *                who sent the command and the Object[] arguments).
     * @return The non-null constructed CommandExecutor.
     */
    public static CommandExecutor<Player> player(BiConsumer<Player, Object[]> command) {
        return new CommandExecutor<Player>(Player.class) {
            @Override
            public void execute(Player sender, Object[] arguments) {
                command.accept(sender, arguments);
            }
        };
    }
}
