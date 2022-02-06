package me.deecaad.core.compatibility.command;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import me.deecaad.core.commands.arguments.EntitySelector;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.*;
import net.minecraft.commands.arguments.blocks.BlockPredicateArgument;
import net.minecraft.commands.arguments.blocks.BlockStateArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.commands.arguments.coordinates.ColumnPosArgument;
import net.minecraft.commands.arguments.coordinates.RotationArgument;
import net.minecraft.commands.arguments.coordinates.SwizzleArgument;
import net.minecraft.commands.arguments.coordinates.Vec2Argument;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.commands.arguments.item.FunctionArgument;
import net.minecraft.commands.arguments.item.ItemArgument;
import net.minecraft.commands.arguments.item.ItemPredicateArgument;
import org.bukkit.command.CommandSender;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.entity.Player;

public class Command_1_18_R1 implements CommandCompatibility {

    @Override
    public SimpleCommandMap getCommandMap() {
        return null;
    }

    @Override
    public void resendCommandRegistry(Player player) {

    }

    @Override
    public CommandSender getCommandSender(CommandContext<Object> context) {
        CommandSourceStack source = (CommandSourceStack) context.getSource();

        CommandSender sender = source.getBukkitSender();
        return null;
    }

    @Override
    public ArgumentType<?> angle() {
        return AngleArgument.angle();
    }

    @Override
    public ArgumentType<?> axis() {
        return SwizzleArgument.swizzle();
    }

    @Override
    public ArgumentType<?> blockPredicate() {
        return BlockPredicateArgument.blockPredicate();
    }

    @Override
    public ArgumentType<?> blockState() {
        return BlockStateArgument.block();
    }

    @Override
    public ArgumentType<?> chat() {
        return MessageArgument.message();
    }

    @Override
    public ArgumentType<?> chatComponent() {
        return ComponentArgument.textComponent();
    }

    @Override
    public ArgumentType<?> chatFormat() {
        return ColorArgument.color();
    }

    @Override
    public ArgumentType<?> dimension() {
        return DimensionArgument.dimension();
    }

    @Override
    public ArgumentType<?> enchantment() {
        return ItemEnchantmentArgument.enchantment();
    }

    @Override
    public ArgumentType<?> entity(EntitySelector selector) {
        return switch (selector) {
            case ENTITY -> EntityArgument.entity();
            case ENTITIES -> EntityArgument.entities();
            case PLAYER -> EntityArgument.player();
            case PLAYERS -> EntityArgument.players();
        };
    }

    @Override
    public ArgumentType<?> entitySummon() {
        return EntitySummonArgument.id();
    }

    @Override
    public ArgumentType<?> floatRange() {
        return RangeArgument.floatRange();
    }

    @Override
    public ArgumentType<?> intRange() {
        return RangeArgument.intRange();
    }

    @Override
    public ArgumentType<?> itemPredicate() {
        return ItemPredicateArgument.itemPredicate();
    }

    @Override
    public ArgumentType<?> itemStack() {
        return ItemArgument.item();
    }

    @Override
    public ArgumentType<?> mathOperation() {
        return OperationArgument.operation();
    }

    @Override
    public ArgumentType<?> minecraftKeyRegistered() {
        return ResourceLocationArgument.id();
    }

    @Override
    public ArgumentType<?> mobEffect() {
        return MobEffectArgument.effect();
    }

    @Override
    public ArgumentType<?> nbtCompound() {
        return CompoundTagArgument.compoundTag();
    }

    @Override
    public ArgumentType<?> particle() {
        return ParticleArgument.particle();
    }

    @Override
    public ArgumentType<?> position() {
        return BlockPosArgument.blockPos();
    }

    @Override
    public ArgumentType<?> position2D() {
        return ColumnPosArgument.columnPos();
    }

    @Override
    public ArgumentType<?> profile() {
        return GameProfileArgument.gameProfile();
    }

    @Override
    public ArgumentType<?> rotation() {
        return RotationArgument.rotation();
    }

    @Override
    public ArgumentType<?> scoreboardCriteria() {
        return ObjectiveCriteriaArgument.criteria();
    }

    @Override
    public ArgumentType<?> scoreboardObjective() {
        return ObjectiveArgument.objective();
    }

    @Override
    public ArgumentType<?> scoreboardSlot() {
        return ScoreboardSlotArgument.displaySlot();
    }

    @Override
    public ArgumentType<?> scoreboardTeam() {
        return TeamArgument.team();
    }

    @Override
    public ArgumentType<?> scoreholder(boolean single) {
        return single ? ScoreHolderArgument.scoreHolder() : ScoreHolderArgument.scoreHolders();
    }

    @Override
    public ArgumentType<?> tag() {
        return FunctionArgument.functions();
    }

    @Override
    public ArgumentType<?> time() {
        return TimeArgument.time();
    }

    @Override
    public ArgumentType<?> uuid() {
        return UuidArgument.uuid();
    }

    @Override
    public ArgumentType<?> vec2() {
        return Vec2Argument.vec2();
    }

    @Override
    public ArgumentType<?> vec3() {
        return Vec3Argument.vec3();
    }
}
