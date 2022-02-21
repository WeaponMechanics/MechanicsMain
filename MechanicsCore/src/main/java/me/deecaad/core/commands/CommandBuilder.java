package me.deecaad.core.commands;

import me.deecaad.core.commands.arguments.Argument;
import me.deecaad.core.commands.arguments.LiteralArgumentType;
import me.deecaad.core.commands.arguments.MultiLiteralArgumentType;
import me.deecaad.core.commands.executors.CommandExecutor;
import me.deecaad.core.compatibility.CompatibilityAPI;
import me.deecaad.core.utils.ReflectionUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

public class CommandBuilder implements Cloneable {

    private final String label;
    private Permission permission;
    private Predicate<CommandSender> requirements;
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
        Permission temp = new Permission(permission);
        return withPermission(temp);
    }

    public CommandBuilder withPermission(String permission, PermissionDefault def) {
        Permission temp = new Permission(permission, def);
        return withPermission(temp);
    }

    public CommandBuilder withPermission(Permission permission) {
        this.permission = permission;
        if (permission != null && Bukkit.getPluginManager().getPermission(permission.getName()) == null)
            Bukkit.getPluginManager().addPermission(permission);
        return this;
    }

    public CommandBuilder withRequirements(Predicate<CommandSender> requirements) {
        this.requirements = requirements;
        return this;
    }

    public CommandBuilder withAliases(String... aliases) {
        this.aliases.addAll(Arrays.asList(aliases));
        return this;
    }

    @SuppressWarnings("all")
    public CommandBuilder withArgument(Argument<?> argument) {
        this.args.add((Argument<Object>) argument);
        return this;
    }

    @SuppressWarnings("all")
    public CommandBuilder withArguments(Argument<Object>... arguments) {
        this.args.addAll(Arrays.asList(arguments));
        return this;
    }

    @SuppressWarnings("all")
    public CommandBuilder withArguments(List<Argument<?>> arguments) {
        //noinspection unchecked
        this.args.addAll((Collection) arguments);
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

    public Predicate<CommandSender> getRequirements() {
        return requirements;
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
        if (ReflectionUtil.getMCVersion() >= 13) {
            if (executor != null)
                new BrigadierCommand(this);

            for (CommandBuilder subcommand : subcommands)
                flatten(this.clone(), new ArrayList<>(), subcommand);
        }
        else {
            throw new IllegalStateException("oops forgot to add legacy");
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static void flatten(CommandBuilder root, List<Argument<?>> arguments, CommandBuilder subcommand) {

        LiteralArgumentType literals = new LiteralArgumentType(subcommand.label);
        Argument<?> argument = new Argument<>("sub-command", literals)
                .withPermission(subcommand.getPermission())
                .withRequirements(subcommand.getRequirements())
                .setListed(false);

        arguments.add(argument);

        if(subcommand.getExecutor() != null) {
            root.args = (List) arguments;
            root.withArguments((List) subcommand.getArgs());
            root.executes(subcommand.getExecutor());

            root.subcommands = new ArrayList<>();
            new BrigadierCommand(root);
        }

        // Flatten all subcommands
        for (CommandBuilder subsubcommand : subcommand.getSubcommands())
            flatten(subcommand, new ArrayList<>(arguments), subsubcommand);
    }

    public Predicate<Object> requirements() {
        return nms -> {
            CommandSender sender = CompatibilityAPI.getCommandCompatibility().getCommandSenderRaw(nms);
            if (permission != null && !sender.hasPermission(permission))
                return false;
            else if (requirements != null && !requirements.test(sender))
                return false;
            else
                return true;
        };
    }

    @Override
    public CommandBuilder clone() {
        try {
            CommandBuilder builder = (CommandBuilder) super.clone();
            builder.subcommands = new ArrayList<>(builder.subcommands);
            builder.args = new ArrayList<>(builder.args);
            return builder;
        } catch (CloneNotSupportedException ex) {
            throw new RuntimeException(ex);
        }
    }
}
