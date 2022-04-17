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
        int rgb;
        if (optional.isPresent())
            rgb = optional.get().asBungee().getColor().getRGB();
        else {
            try {
                rgb = Integer.parseInt(input, 16);
            } catch (NumberFormatException ex) {
                throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherParseException().create("For hex: " + input);
            }
        }

        return Color.fromRGB(rgb);
    }

    @Override
    public CompletableFuture<Suggestions> suggestions(CommandContext<Object> context, SuggestionsBuilder builder) {
        builder.suggest("FFFFFF", new LiteralMessage("white"));
        builder.suggest("FF0000", new LiteralMessage("red"));
        builder.suggest("00FF00", new LiteralMessage("green"));
        builder.suggest("0000ff", new LiteralMessage("blue"));
        builder.suggest("000000", new LiteralMessage("black"));

        for (String str : EnumUtil.getOptions(ChatColor.class))
            builder.suggest(str);

        return builder.buildFuture();
    }
}
