package me.deecaad.core.commands.arguments;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.deecaad.core.commands.wrappers.ParticleHolder;

public class ParticleArgumentType extends CommandArgumentType<ParticleHolder> {

    @Override
    public ArgumentType<?> getBrigadierType() {
        return compatibility().particle();
    }

    @Override
    public ParticleHolder parse(CommandContext<Object> context, String key) throws CommandSyntaxException {
        return compatibility().getParticle(context, key);
    }
}
