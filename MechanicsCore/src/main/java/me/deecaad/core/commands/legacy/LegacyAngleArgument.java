package me.deecaad.core.commands.legacy;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import me.deecaad.core.utils.VectorUtil;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;

import java.util.Arrays;
import java.util.Collection;

public class LegacyAngleArgument implements ArgumentType<LegacyAngleArgument.SingleAngle> {

    private static final Collection<String> EXAMPLES = Arrays.asList("0", "~", "~-5");
    public static final SimpleCommandExceptionType ERROR_NOT_COMPLETE = new SimpleCommandExceptionType(Component.translatable("argument.angle.incomplete"));
    public static final SimpleCommandExceptionType ERROR_INVALID_ANGLE = new SimpleCommandExceptionType(Component.translatable("argument.angle.invalid"));

    public static float getAngle(CommandContext<CommandSender> context, String name) {
        return context.getArgument(name, SingleAngle.class).getAngle(context.getSource());
    }

    public SingleAngle parse(StringReader stringReader) throws CommandSyntaxException {
        if (!stringReader.canRead()) {
            throw ERROR_NOT_COMPLETE.createWithContext(stringReader);
        } else {
            boolean bl = WorldCoordinate.isRelative(stringReader);
            float f = stringReader.canRead() && stringReader.peek() != ' ' ? stringReader.readFloat() : 0.0F;
            if (!Float.isNaN(f) && !Float.isInfinite(f)) {
                return new SingleAngle(f, bl);
            } else {
                throw ERROR_INVALID_ANGLE.createWithContext(stringReader);
            }
        }
    }

    public Collection<String> getExamples() {
        return EXAMPLES;
    }

    public record SingleAngle(float angle, boolean isRelative) {

        public float getAngle(CommandSender source) {
            float relative = isRelative && source instanceof Entity entity ? entity.getLocation().getPitch() : 0f;
            float adjusted = (float) VectorUtil.normalize(angle + relative);

            if (adjusted >= 180f)
                adjusted -= 360f;
            if (adjusted < -180f)
                adjusted += 360f;

            return adjusted;
        }
    }
}
