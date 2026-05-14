package me.deecaad.weaponmechanics.commands;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.jorel.commandapi.arguments.Argument;
import dev.jorel.commandapi.arguments.CommandAPIArgumentType;
import dev.jorel.commandapi.executors.CommandArguments;
import io.papermc.paper.command.brigadier.argument.CustomArgumentType;

/**
 * A string argument that reads a single whitespace-delimited token, allowing any character.
 *
 * <p>CommandAPI's {@link dev.jorel.commandapi.arguments.StringArgument} is backed by Brigadier's
 * {@code StringArgumentType.word()}, which only accepts {@code [a-zA-Z0-9_.+-]}. That makes it
 * impossible to type the special weapon selectors {@code *}, {@code **}, and {@code *r}. This
 * argument reads everything up to the next space instead, so those tokens parse correctly while
 * normal weapon titles still work.
 *
 * <p>The underlying Brigadier type implements Paper's {@link CustomArgumentType} so Paper's command
 * system accepts it (a raw {@code ArgumentType} is rejected during node conversion).
 */
public class WeaponTitleArgument extends Argument<String> {

    public WeaponTitleArgument(String nodeName) {
        super(nodeName, new TokenArgumentType());
    }

    @Override
    public Class<String> getPrimitiveType() {
        return String.class;
    }

    @Override
    public CommandAPIArgumentType getArgumentType() {
        return CommandAPIArgumentType.PRIMITIVE_STRING;
    }

    @Override
    public <Source> String parseArgument(CommandContext<Source> context, String key, CommandArguments previousArgs)
        throws CommandSyntaxException {
        return context.getArgument(key, String.class);
    }

    private static class TokenArgumentType implements CustomArgumentType<String, String> {
        @Override
        public String parse(StringReader reader) {
            int start = reader.getCursor();
            while (reader.canRead() && reader.peek() != ' ') {
                reader.skip();
            }
            return reader.getString().substring(start, reader.getCursor());
        }

        @Override
        public ArgumentType<String> getNativeType() {
            return StringArgumentType.word();
        }
    }
}
