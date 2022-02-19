package me.deecaad.core.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;
import me.deecaad.core.commands.arguments.Argument;
import me.deecaad.core.commands.arguments.LiteralArgumentType;
import me.deecaad.core.commands.executors.CommandExecutor;
import me.deecaad.core.compatibility.CompatibilityAPI;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;

import static com.mojang.brigadier.builder.LiteralArgumentBuilder.literal;

public class BrigadierCommand implements Command<Object> {

    private final CommandBuilder builder;

    public BrigadierCommand(CommandBuilder builder) {
        this.builder = builder;

        CommandDispatcher<Object> dispatcher = CompatibilityAPI.getCommandCompatibility().getCommandDispatcher();

        LiteralArgumentBuilder<Object> result = literal(builder.getLabel()).requires(builder.requirements());
        if (builder.getArgs().isEmpty())
            result.executes(this);
        else
            result.then(buildArguments(builder.getArgs()));

        dispatcher.register(result);
    }

    @Override
    public int run(CommandContext<Object> context) throws CommandSyntaxException {
        CommandSender sender = CompatibilityAPI.getCommandCompatibility().getCommandSender(context);

        // Ensure that the CommandSender is the proper type. This is typically
        // used for Player only commands, but it may also be used for console
        // or command-block only commands, for example.
        if (!sender.getClass().isAssignableFrom(builder.getExecutor().getExecutor())) {
            sender.sendMessage(ChatColor.RED + builder.getLabel() + " is a " + builder.getExecutor().getExecutor().getSimpleName() + " only command.");
            return -1;
        }

        try {
            //noinspection unchecked
            ((CommandExecutor<CommandSender>) builder.getExecutor()).execute(sender, parseBrigadierArguments(context));
            return 0;
        } catch (CommandSyntaxException ex) {
            throw ex;
        } catch (Exception ex) {
            sender.sendMessage(ChatColor.RED + "Some error occurred whilst executing command. Check console for error. ");
            ex.printStackTrace();
            return -1;
        }
    }

    private Object[] parseBrigadierArguments(CommandContext<Object> context) throws Exception {
        List<Object> temp = new ArrayList<>(builder.getArgs().size());
        for (Argument<?> argument : builder.getArgs())
            temp.add(argument.parse(context));

        return temp.toArray();
    }

    private ArgumentBuilder<Object, ?> buildArguments(List<Argument<Object>> args) {
        if (args == null || args.isEmpty())
            throw new IllegalArgumentException("empty args");

        ArgumentBuilder<Object, ?> builder = null;
        ArgumentBuilder<Object, ?> temp;
        for (int i = args.size() - 1; i >= 0; i--) {
            Argument<?> argument = args.get(i);

            // LiteralArgumentTypes are just hard-coded string values, which
            // are usually used as sub-commands. These must be registered
            if (argument.getType() instanceof LiteralArgumentType) {
                LiteralArgumentType literal = (LiteralArgumentType) argument.getType();
                temp = literal(literal.getLiteral());
            }

            // All other argument types can be parsed normally
            else {
                temp = RequiredArgumentBuilder.argument(argument.getName(), argument.getType().getBrigadierType());
            }

            if (builder == null)
                temp.requires(this.builder.requirements()).executes(this);
            else
                temp.requires(this.builder.requirements()).then(builder);
            builder = temp;
        }
        return builder;
    }
}
