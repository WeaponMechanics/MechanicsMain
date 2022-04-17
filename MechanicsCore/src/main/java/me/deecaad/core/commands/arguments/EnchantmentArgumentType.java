package me.deecaad.core.commands.arguments;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import org.bukkit.enchantments.Enchantment;

public class EnchantmentArgumentType extends CommandArgumentType<Enchantment> {

    @Override
    public ArgumentType<?> getBrigadierType() {
        return compatibility().enchantment();
    }

    @Override
    public Enchantment parse(CommandContext<Object> context, String key) throws CommandSyntaxException {
        return compatibility().getEnchantment(context, key);
    }
}
