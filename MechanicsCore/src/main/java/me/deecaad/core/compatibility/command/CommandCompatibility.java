package me.deecaad.core.compatibility.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import me.deecaad.core.commands.wrappers.Column;
import me.deecaad.core.commands.wrappers.Location2d;
import me.deecaad.core.commands.wrappers.Rotation;
import org.apache.commons.lang.math.DoubleRange;
import org.apache.commons.lang.math.IntRange;
import org.bukkit.Axis;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.advancement.Advancement;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.command.CommandSender;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.loot.LootTable;
import org.bukkit.potion.PotionEffectType;

import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Predicate;

public interface CommandCompatibility {

    SimpleCommandMap getCommandMap();

    void resendCommandRegistry(Player player);

    CommandSender getCommandSender(CommandContext<Object> context);

    CommandSender getCommandSenderRaw(Object nms);

    CommandDispatcher<Object> getCommandDispatcher();

    // * ----- Argument Types ----- * //

    ArgumentType<?> angle();

    ArgumentType<?> axis();

    ArgumentType<?> blockPredicate();

    ArgumentType<?> blockState();

    ArgumentType<?> chat();

    ArgumentType<?> chatComponent();

    ArgumentType<?> chatFormat();

    ArgumentType<?> dimension();

    ArgumentType<?> enchantment();

    ArgumentType<?> entity();

    ArgumentType<?> entities();

    ArgumentType<?> player();

    ArgumentType<?> players();

    ArgumentType<?> entitySummon();

    ArgumentType<?> itemPredicate();

    ArgumentType<?> itemStack();

    ArgumentType<?> mathOperation();

    ArgumentType<?> mobEffect();

    ArgumentType<?> nbtCompound();

    ArgumentType<?> particle();

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

    ArgumentType<?> location();

    ArgumentType<?> location2();

    ArgumentType<?> block();

    ArgumentType<?> block2();

    ArgumentType<?> biome();

    ArgumentType<?> key();

    // * ----- Suggestion Providers ----- * //

    SuggestionProvider<Object> biomeKey();

    SuggestionProvider<Object> recipeKey();

    SuggestionProvider<Object> soundKey();

    SuggestionProvider<Object> entityKey();

    SuggestionProvider<Object> advancementKey();

    SuggestionProvider<Object> lootKey();

    // * ----- Parsing Argument Types ----- * //

    Advancement getAdvancement(CommandContext<Object> context, String key) throws CommandSyntaxException;

    float getAngle(CommandContext<Object> context, String key);

    EnumSet<Axis> getAxis(CommandContext<Object> context, String key);

    Biome getBiome(CommandContext<Object> context, String key) throws CommandSyntaxException;

    Predicate<Block> getBlockPredicate(CommandContext<Object> context, String key)
            throws CommandSyntaxException;

    BlockData getBlockState(CommandContext<Object> context, String key);

    World.Environment getDimension(CommandContext<Object> context, String key) throws CommandSyntaxException;

    Enchantment getEnchantment(CommandContext<Object> context, String key);

    Entity getEntitySelector(CommandContext<Object> context, String key) throws CommandSyntaxException;

    List<Entity> getEntitiesSelector(CommandContext<Object> context, String key) throws CommandSyntaxException;

    Player getPlayerSelector(CommandContext<Object> context, String key) throws CommandSyntaxException;

    List<Player> getPlayersSelector(CommandContext<Object> context, String key) throws CommandSyntaxException;

    EntityType getEntityType(CommandContext<Object> context, String key) throws CommandSyntaxException;

    DoubleRange getDoubleRange(CommandContext<Object> context, String key);

    IntRange getIntRange(CommandContext<Object> context, String key);

    ItemStack getItemStack(CommandContext<Object> context, String key) throws CommandSyntaxException;

    Predicate<ItemStack> getItemStackPredicate(CommandContext<Object> context, String key)
            throws CommandSyntaxException;

    String getKeyedAsString(CommandContext<Object> context, String key) throws CommandSyntaxException;

    Column getLocation2DBlock(CommandContext<Object> context, String key)
            throws CommandSyntaxException;

    Location2d getLocation2DPrecise(CommandContext<Object> context, String key)
            throws CommandSyntaxException;

    Block getLocationBlock(CommandContext<Object> context, String str) throws CommandSyntaxException;

    Location getLocationPrecise(CommandContext<Object> context, String str)
            throws CommandSyntaxException;

    LootTable getLootTable(CommandContext<Object> context, String key);

    //NBTContainer getNBTCompound(CommandContext<Object> context, String key);

    String getObjective(CommandContext<Object> context, String key)
            throws IllegalArgumentException, CommandSyntaxException;

    String getObjectiveCriteria(CommandContext<Object> context, String key);

    Particle getParticle(CommandContext<Object> context, String key);

    Player getPlayer(CommandContext<Object> context, String key) throws CommandSyntaxException;

    OfflinePlayer getOfflinePlayer(CommandContext<Object> context, String key) throws CommandSyntaxException;

    PotionEffectType getPotionEffect(CommandContext<Object> context, String key)
            throws CommandSyntaxException;

    Recipe getRecipe(CommandContext<Object> context, String key) throws CommandSyntaxException;

    Rotation getRotation(CommandContext<Object> context, String key);

    Sound getSound(CommandContext<Object> context, String key);

    String getTeam(CommandContext<Object> context, String key) throws CommandSyntaxException;

    int getTime(CommandContext<Object> context, String key);

    UUID getUUID(CommandContext<Object> context, String key);

    Map<String, Object> getCompound(CommandContext<Object> context, String key);
}
