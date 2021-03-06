package me.deecaad.core.commands.arguments;

import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import me.deecaad.core.utils.EnumUtil;
import org.bukkit.ChatColor;
import org.bukkit.Color;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class ColorArgumentType extends CommandArgumentType<Color> {

    @Override
    public ArgumentType<?> getBrigadierType() {
        return StringArgumentType.word();
    }

    @Override
    public Color parse(CommandContext<Object> context, String key) throws CommandSyntaxException {
        String input = context.getArgument(key, String.class);

        Optional<ChatColor> optional = EnumUtil.getIfPresent(ChatColor.class, input);
        if (optional.isPresent()) {
            java.awt.Color color = optional.get().asBungee().getColor();
            return Color.fromRGB(0xff & color.getRed(), 0xff & color.getGreen(), 0xff & color.getBlue());
        }

        try {
            int rgb = 0xffffff & Integer.parseInt(input, 16);
            return Color.fromRGB(rgb);
        } catch (NumberFormatException ex) {
            throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherParseException().create("For hex: " + input);
        }
    }

    @Override
    public CompletableFuture<Suggestions> suggestions(CommandContext<Object> context, SuggestionsBuilder builder) {
        builder.suggest("ffffff", new LiteralMessage("white"));
        builder.suggest("ff0000", new LiteralMessage("red"));
        builder.suggest("00ff00", new LiteralMessage("green"));
        builder.suggest("0000ff", new LiteralMessage("blue"));
        builder.suggest("000000", new LiteralMessage("black"));

        for (ChatColor color : EnumUtil.getValues(ChatColor.class))
            if (color.asBungee().getColor() != null)
                builder.suggest(color.name());

        return builder.buildFuture();
    }
}
