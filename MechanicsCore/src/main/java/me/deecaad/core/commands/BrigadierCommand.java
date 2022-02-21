package me.deecaad.core.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.Message;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import me.deecaad.core.MechanicsCore;
import me.deecaad.core.commands.arguments.Argument;
import me.deecaad.core.commands.arguments.LiteralArgumentType;
import me.deecaad.core.commands.arguments.MultiLiteralArgumentType;
import me.deecaad.core.commands.executors.CommandExecutor;
import me.deecaad.core.compatibility.CompatibilityAPI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static com.mojang.brigadier.builder.LiteralArgumentBuilder.literal;
import static com.mojang.brigadier.builder.RequiredArgumentBuilder.argument;

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

        MechanicsCore.debug.error("Registering Command: /" + builder.getLabel() + " " + builder.getArgs().stream().map(Argument::getName).collect(Collectors.toList()));
    }

    @Override
    public int run(CommandContext<Object> context) throws CommandSyntaxException {
        CommandSender sender = CompatibilityAPI.getCommandCompatibility().getCommandSender(context);

        // Ensure that the CommandSender is the proper type. This is typically
        // used for Player only commands, but it may also be used for console
        // or command-block only commands, for example.
        if (!builder.getExecutor().getExecutor().isInstance(sender)) {
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
            if (argument.isListed())
                temp.add(argument.parse(context, argument.getName()));

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
                RequiredArgumentBuilder<Object, ?> required = argument(argument.getName(), argument.getType().getBrigadierType());

                // When an argument wants to REPLACE suggestions, only include
                // "buildSuggestionProvider" (which builds custom suggestions).
                if (argument.isReplaceSuggestions()) {
                    required.suggests(buildSuggestionProvider(argument.getName()));
                }

                // When an argument wants to ADD suggestions, we should include
                // the argument's default suggestions as well as the custom
                // suggestions.
                else if (argument.getSuggestions() != null) {
                    required.suggests((context, suggestionsBuilder) -> {
                        argument.getType().getBrigadierType().listSuggestions(context, suggestionsBuilder);
                        buildSuggestionProvider(argument.getName()).getSuggestions(context, suggestionsBuilder);
                        return suggestionsBuilder.buildFuture();
                    });
                }

                // When the argument doesn't have any custom suggestions, we
                // don't need to mess with the suggestions. I explicitly set
                // the value to null here to show this.
                required.suggests(null);

                temp = required;
            }

            if (builder == null)
                temp.requires(argument.requirements()).executes(this);
            else
                temp.requires(argument.requirements()).then(builder);

            builder = temp;
        }
        return builder;
    }

    /**
     * This method is used whenever a command has custom arguments.
     *
     * @param nodeName The name of the argument.
     * @return The non-null suggestions provider.
     */
    private SuggestionProvider<Object> buildSuggestionProvider(String nodeName) {
        return (CommandContext<Object> context, SuggestionsBuilder builder) -> {
            CommandSender sender = CompatibilityAPI.getCommandCompatibility().getCommandSender(context);
            List<Object> previousArguments = new ArrayList<>();
            Argument<?> current = null;

            for (Argument<?> argument : this.builder.getArgs()) {
                current = argument;
                if (current.getName().equals(nodeName))
                    break;

                previousArguments.add(current.isListed() ? current.parse(context, argument.getName()) : null);
            }

            CommandData data = new CommandData(sender, previousArguments.toArray(), builder.getInput(), builder.getRemaining());
            return getSuggestionsBuilder(builder, current.getSuggestions() == null ? new Tooltip[0] : current.getSuggestions().apply(data));
        };
    }

    private CompletableFuture<Suggestions> getSuggestionsBuilder(SuggestionsBuilder builder, Tooltip[] suggestions) {
        String remaining = builder.getRemaining().toLowerCase(Locale.ROOT);
        for (Tooltip suggestion : suggestions) {
            if (suggestion.suggestion().toLowerCase(Locale.ROOT).startsWith(remaining)) {
                Message tooltipMsg = suggestion.tip() == null ? null : new LiteralMessage(suggestion.tip());
                builder.suggest(suggestion.suggestion(), tooltipMsg);
            }
        }
        return builder.buildFuture();
    }


}
