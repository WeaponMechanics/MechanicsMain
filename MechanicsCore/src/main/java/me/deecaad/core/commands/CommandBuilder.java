package me.deecaad.core.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;
import me.deecaad.core.commands.arguments.Argument;
import me.deecaad.core.commands.arguments.LiteralArgumentType;
import me.deecaad.core.commands.executors.CommandExecutor;
import me.deecaad.core.compatibility.CompatibilityAPI;
import me.deecaad.core.utils.ReflectionUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.function.Predicate;

import static com.mojang.brigadier.builder.LiteralArgumentBuilder.literal;

public class CommandBuilder {

    private boolean lock;

    private final String label;
    private Permission permission;
    private List<String> aliases;
    private List<Argument<?>> args;
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

    public <T extends CommandSender> CommandBuilder executes(CommandExecutor<T> executor) {
        this.executor = executor;
        return this;
    }

    public void register() {
        if (permission != null)
            Bukkit.getPluginManager().addPermission(permission);

        if (ReflectionUtil.getMCVersion() >= 13)
            registerBrigadier();
        else
            registerLegacy();
    }

    private void registerLegacy() {
    }

    @SuppressWarnings("unchecked")
    private void registerBrigadier() {
        Command<?> command = new Command<Object>() {
            @Override
            public int run(CommandContext<Object> context) throws CommandSyntaxException {
                CommandSender sender = CompatibilityAPI.getCommandCompatibility().getCommandSender(context);

                if (!sender.getClass().isAssignableFrom(executor.getExecutor())) {
                    sender.sendMessage(ChatColor.RED + label + " is a " + executor.getExecutor().getSimpleName() + " only command.");
                    return -1;
                }

                try {
                    ((CommandExecutor<CommandSender>) executor).execute(sender, parseBrigadierArguments(context));
                    return 0;
                } catch (CommandSyntaxException ex) {
                    throw ex;
                } catch (Exception ex) {
                    sender.sendMessage(ChatColor.RED + "Some error occurred whilst executing command. Check console for error. ");
                    ex.printStackTrace();
                    return -1;
                }
            }
        };

        LiteralCommandNode<Object> result;
        if (args.size() == 0) {

        } else {
            ArgumentBuilder<Object, ?> commandArguments = null;

            CommandDispatcher<Object> dispatcher = CompatibilityAPI.getCommandCompatibility().getCommandDispatcher();
            result = dispatcher.register(literal(label).requires(handlePermissions()));
        }
    }

    private Object[] parseBrigadierArguments(CommandContext<Object> context) throws Exception {
        List<Object> temp = new ArrayList<>(args.size());
        for (Argument<?> argument : args)
            temp.add(argument.parse(context));

        return temp.toArray();
    }

    private Predicate<Object> handlePermissions() {
        if (permission == null) {
            return nms -> true;
        }

        return nms -> {
            CommandSender sender = CompatibilityAPI.getCommandCompatibility().getCommandSenderRaw(nms);
            return sender.hasPermission(permission);
        };
    }

    private ArgumentBuilder<Object, ?> createBuilder(Argument<?> argument) {
        if (argument.getType() instanceof LiteralArgumentType) {

        }
    }

}
