package me.deecaad.core.commands.executors;

import org.bukkit.command.CommandSender;

public abstract class CommandExecutor<T extends CommandSender> {

    private final Class<T> executor;

    public CommandExecutor(Class<T> executor) {
        this.executor = executor;
    }

    public final Class<T> getExecutor() {
        return executor;
    }

    public abstract void execute(T sender, Object[] arguments);
}
