package me.deecaad.core.commands.arguments;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import org.bukkit.Particle;

public class ParticleArgumentType extends CommandArgumentType<Particle> {

    @Override
    public ArgumentType<?> getBrigadierType() {
        return compatibility().particle();
    }

    @Override
    public Particle parse(CommandContext<Object> context, String key) throws CommandSyntaxException {
        return compatibility().getParticle(context, key);
    }
}
