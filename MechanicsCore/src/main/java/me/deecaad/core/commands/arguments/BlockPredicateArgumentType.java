package me.deecaad.core.commands.arguments;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.deecaad.core.compatibility.CompatibilityAPI;
import org.bukkit.block.Block;

import java.util.function.Predicate;

public class BlockPredicateArgumentType extends CommandArgumentType<Predicate<Block>> {

    @Override
    public ArgumentType<?> getBrigadierType() {
        return CompatibilityAPI.getCommandCompatibility().blockPredicate();
    }

    @Override
    public Predicate<Block> parse(CommandContext<Object> context, String key) throws CommandSyntaxException {
        return CompatibilityAPI.getCommandCompatibility().getBlockPredicate(context, key);
    }

    public static Predicate<Block> TRUE(String name) {
        return new Predicate<Block>() {
            @Override
            public boolean test(Block o) {
                return true;
            }

            @Override
            public String toString() {
                return name;
            }
        };
    }

    public static Predicate<Block> FALSE(String name) {
        return new Predicate<Block>() {
            @Override
            public boolean test(Block o) {
                return false;
            }

            @Override
            public String toString() {
                return name;
            }
        };
    }
}
