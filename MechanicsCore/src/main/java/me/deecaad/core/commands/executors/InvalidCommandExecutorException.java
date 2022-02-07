package me.deecaad.core.commands.executors;

/**
 * An exception that occurs when an unknown class implements {@link CommandExecutor}.
 */
public class InvalidCommandExecutorException extends RuntimeException {

    public InvalidCommandExecutorException(CommandExecutor<?> unknown) {
        super("Unknown sub-class of CommandExecutor: " + unknown);
    }
}
