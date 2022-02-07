package me.deecaad.core.commands.executors;

import org.bukkit.command.CommandSender;

/**
 * This interface determines which entities can execute a command. For example,
 * if you use a {@link PlayerCommandExecutor} for a command, only players will
 * be able to use the command.
 *
 * <p>Note that it is not supported to implement this class yourself, and
 * undefined behavior will occur. Instead, use one of:
 *
 * <ul>
 *     <li>{@link PlayerCommandExecutor}</li>
 *     <li>{@link EntityCommandExecutor}</li>
 * </ul>
 */
@FunctionalInterface
public interface CommandExecutor<T extends CommandSender> {
    void execute(T sender);
}
