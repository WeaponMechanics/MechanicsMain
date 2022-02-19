package me.deecaad.core.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;
import me.deecaad.core.commands.arguments.Argument;
import me.deecaad.core.commands.executors.CommandExecutor;
import me.deecaad.core.compatibility.CompatibilityAPI;
import me.deecaad.core.utils.ReflectionUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

import static com.mojang.brigadier.builder.LiteralArgumentBuilder.literal;

public class CommandBuilder {

    private boolean lock;

    private final String label;
    private Permission permission;
    private List<String> aliases;
    private List<Argument<Object>> args;
    private List<CommandBuilder> subcommands;
    private CommandExecutor<? extends CommandSender> executor;
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

    public CommandBuilder withArgument(Argument<Object> argument) {
        this.args.add(argument);
        return this;
    }

    public CommandBuilder withArguments(Argument<Object>... arguments) {
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

    public <T extends CommandSender> CommandBuilder executes(CommandExecutor<T> executor) {
        this.executor = executor;
        return this;
    }

    public String getLabel() {
        return label;
    }

    public Permission getPermission() {
        return permission;
    }

    public List<String> getAliases() {
        return aliases;
    }

    public List<Argument<Object>> getArgs() {
        return args;
    }

    public List<CommandBuilder> getSubcommands() {
        return subcommands;
    }

    public CommandExecutor<? extends CommandSender> getExecutor() {
        return executor;
    }

    public String getDescription() {
        return description;
    }

    public void register() {
        if (permission != null)
            Bukkit.getPluginManager().addPermission(permission);
    }





    public Predicate<Object> requirements() {
        if (permission == null) {
            return nms -> true;
        }

        return nms -> {
            CommandSender sender = CompatibilityAPI.getCommandCompatibility().getCommandSenderRaw(nms);
            return sender.hasPermission(permission);
        };
    }

}
