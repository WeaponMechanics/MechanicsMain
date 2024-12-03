package me.deecaad.core.commands;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.Argument;
import me.deecaad.core.MechanicsCore;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.Style;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static net.kyori.adventure.text.Component.text;

public class CommandHelpBuilder {

    private final Style primaryStyle;
    private final Style secondaryStyle;

    public CommandHelpBuilder(@NotNull Style primaryStyle, @NotNull Style secondaryStyle) {
        this.primaryStyle = primaryStyle;
        this.secondaryStyle = secondaryStyle;
    }

    public void register(@NotNull CommandAPICommand command) {

    }

    private void handleSubcommands(@NotNull CommandAPICommand help, @NotNull CommandAPICommand parent) {

        // When the parent (or source) command has subcommands, recursively
        // register 'help variations' for each subcommand into the help command.
        for (CommandAPICommand subcommand : parent.getSubcommands()) {
            CommandAPICommand subHelp = new CommandAPICommand(subcommand.getName())
                .withPermission(subcommand.getPermission())
                .withRequirement(subcommand.getRequirements())
                .withShortDescription(help.getShortDescription() + " " + subcommand.getName());
            help.withSubcommand(subHelp);

            handleSubcommands(subHelp, subcommand);
        }

        // When the parent command has no subcommands, we should show all the
        // arguments that the command accepts.
        if (parent.getSubcommands().isEmpty()) {
            help.executes((sender, args) -> {
                TextComponent.Builder helpResponse = showArguments(help, parent);
                MechanicsCore.getPlugin().adventure.sender(sender).sendMessage(helpResponse);
            });
        }

        // When the parent command has subcommands, we can show a list of each
        // subcommand when you run the help command.
        else {

        }

    }

    private @NotNull TextComponent.Builder showMeta(@NotNull CommandAPICommand help, @NotNull CommandAPICommand parent) {
        TextComponent.Builder builder = text();

        // help.description stores the command's "structure" (e.g. /wm give)
        // This is done in the handleSubcommands() method
        builder.append(text().content("/" + help.getShortDescription() + ": ").style(primaryStyle));
        builder.append(text().content((parent.getFullDescription() != null) ? parent.getFullDescription() : parent.getShortDescription()).style(secondaryStyle));
        builder.appendNewline();

        String permission = parent.getPermission() != null ? parent.getPermission().getPermission().orElse(null) : null;
        if (permission != null) {
            builder.append(text().content("Permission: ").style(primaryStyle)
                .clickEvent(ClickEvent.copyToClipboard(permission))
                .hoverEvent(text("Click to copy")));
            builder.append(text().content(permission).style(secondaryStyle)
                .clickEvent(ClickEvent.copyToClipboard(permission))
                .hoverEvent(text("Click to copy")));
            builder.appendNewline();
        }

        if (parent.getAliases().length != 0) {
            builder.append(text().content("Aliases: ").style(primaryStyle));
            builder.append(text().content(String.join(", ", parent.getAliases())).style(secondaryStyle));
            builder.appendNewline();
        }

        return builder;
    }

    private @NotNull TextComponent.Builder showArguments(@NotNull CommandAPICommand help, @NotNull CommandAPICommand parent) {
        TextComponent.Builder builder = showMeta(help, parent);

        // Let admins click to autofill the command in chat
        builder.append(text().content("Usage: ").style(primaryStyle));
        builder.append(text().content("/" + help.getShortDescription()).style(secondaryStyle)
            .clickEvent(ClickEvent.suggestCommand("/" + help.getShortDescription())))
            .hoverEvent(text("Click to run"));

        // Show <arg1> <arg2> [arg3] [arg4] <...> after the usage
        for (Argument<?> arg : parent.getArguments()) {
            builder.append(showArgument(arg));
        }

        // Show the description of each argument
        if (!parent.getArguments().isEmpty()) {
            builder.appendNewline();
            builder.appendNewline();

            List<Argument<?>> arguments = parent.getArguments();
            for (int i = 0; i < arguments.size(); i++) {
                Argument<?> arg = arguments.get(i);
                String description;
                if (arg instanceof Describable)
                    description = ((Describable) arg).getDescription();
                else
                    description = "No description provided.";

                // Each argument is shown as <arg>: <desc>
                builder.append(text().content(arg.getNodeName() + ": ").style(primaryStyle));
                builder.append(text().content(description).style(secondaryStyle));

                // Only add newlines between arguments
                if (i != arguments.size() - 1) {
                    builder.appendNewline();
                }
            }
        }

        return builder;
    }

    private @NotNull TextComponent.Builder showArgument(@NotNull Argument<?> arg) {
        TextComponent.Builder builder = text();

        if (arg.isOptional()) {
            builder.append(text().content(" [").style(secondaryStyle));
            builder.append(text().content(arg.getNodeName()).style(secondaryStyle));
            builder.append(text().content("]").style(secondaryStyle));
        } else {
            builder.append(text().content(" <" + arg.getNodeName() + ">").style(secondaryStyle));
        }

        return builder;
    }
}
