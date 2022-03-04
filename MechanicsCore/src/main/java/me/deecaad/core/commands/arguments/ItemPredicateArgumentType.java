package me.deecaad.core.commands.arguments;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import org.bukkit.inventory.ItemStack;

import java.util.function.Predicate;

public class ItemPredicateArgumentType extends CommandArgumentType<Predicate<ItemStack>> {

    @Override
    public ArgumentType<?> getBrigadierType() {
        return compatibility().itemPredicate();
    }

    @Override
    public Predicate<ItemStack> parse(CommandContext<Object> context, String key) throws CommandSyntaxException {
        return compatibility().getItemStackPredicate(context, key);
    }
}
