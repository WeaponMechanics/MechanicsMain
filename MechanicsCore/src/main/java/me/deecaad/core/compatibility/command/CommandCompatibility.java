package me.deecaad.core.compatibility.command;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import me.deecaad.core.commands.arguments.EntitySelector;
import org.bukkit.command.CommandSender;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.entity.Player;

public interface CommandCompatibility {

    SimpleCommandMap getCommandMap();

    void resendCommandRegistry(Player player);

    CommandSender getCommandSender(CommandContext<Object> context);

    ArgumentType<?> angle();

    ArgumentType<?> axis();

    ArgumentType<?> blockPredicate();

    ArgumentType<?> blockState();

    ArgumentType<?> chat();

    ArgumentType<?> chatComponent();

    ArgumentType<?> chatFormat();

    ArgumentType<?> dimension();

    ArgumentType<?> enchantment();

    ArgumentType<?> entity(EntitySelector selector);

    ArgumentType<?> entitySummon();

    ArgumentType<?> floatRange();

    ArgumentType<?> intRange();

    ArgumentType<?> itemPredicate();

    ArgumentType<?> itemStack();

    ArgumentType<?> mathOperation();

    ArgumentType<?> minecraftKeyRegistered();

    ArgumentType<?> mobEffect();

    ArgumentType<?> nbtCompound();

    ArgumentType<?> particle();

    ArgumentType<?> position();

    ArgumentType<?> position2D();

    ArgumentType<?> profile();

    ArgumentType<?> rotation();

    ArgumentType<?> scoreboardCriteria();

    ArgumentType<?> scoreboardObjective();

    ArgumentType<?> scoreboardSlot();

    ArgumentType<?> scoreboardTeam();

    ArgumentType<?> scoreholder(boolean single);

    ArgumentType<?> tag();

    ArgumentType<?> time();

    ArgumentType<?> uuid();

    ArgumentType<?> vec2();

    ArgumentType<?> vec3();
}
