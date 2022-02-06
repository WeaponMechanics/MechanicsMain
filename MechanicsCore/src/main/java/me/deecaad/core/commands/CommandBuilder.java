package me.deecaad.core.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.deecaad.core.commands.arguments.Argument;
import me.deecaad.core.compatibility.CompatibilityAPI;
import me.deecaad.core.utils.ReflectionUtil;
import org.bukkit.command.CommandSender;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CommandBuilder {

    private boolean lock;

    private final String label;
    private Permission permission;
    private List<String> aliases;
    private List<Argument<?>> args;
    private List<CommandBuilder> subcommands;
    private CommandExecutor executor;
    private String description;

    public CommandBuilder(String label) {
        if (label == null || label.trim().isEmpty())
            throw new IllegalArgumentException("null or empty string: " + label);

        this.label = label;
        this.permission = null;
        this.aliases = new ArrayList<>();
        this.args = new ArrayList<>();
        this.subcommands = new ArrayList<>();
        this.executor = null;
    }

    public CommandBuilder withPermission(String permission) {
        this.permission = new Permission(permission);
        return this;
    }

    public CommandBuilder withPermission(String permission, PermissionDefault def) {
        this.permission = new Permission(permission, def);
        return this;
    }

    public CommandBuilder withPermission(Permission permission) {
        this.permission = permission;
        return this;
    }

    public CommandBuilder withAliases(String... aliases) {
        this.aliases.addAll(Arrays.asList(aliases));
        return this;
    }

    public CommandBuilder withArgument(Argument<?> argument) {
        this.args.add(argument);
        return this;
    }

    public CommandBuilder withArguments(Argument<?>... arguments) {
        this.args.addAll(Arrays.asList(arguments));
        return this;
    }

    public CommandBuilder withSubCommand(CommandBuilder builder) {
        this.subcommands.add(builder);
        return this;
    }

    public CommandBuilder withDescription(String description) {
        this.description = description;
        return this;
    }

    public void register() {
        if (ReflectionUtil.getMCVersion() >= 13)
            registerBrigadier();
        else
            registerLegacy();
    }

    private void registerLegacy() {

    }

    private void registerBrigadier() {
        Command<?> command = new Command<Object>() {
            @Override
            public int run(CommandContext<Object> context) throws CommandSyntaxException {
                CommandSender sender = CompatibilityAPI.getCommandCompatibility().getCommandSender(context);

                return 0;
            }
        };
    }

}
