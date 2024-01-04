package me.deecaad.core.compatibility.command;

import com.google.common.io.Files;
import com.google.gson.GsonBuilder;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import me.deecaad.core.commands.wrappers.Rotation;
import me.deecaad.core.commands.wrappers.*;
import me.deecaad.core.utils.EnumUtil;
import me.deecaad.core.utils.ReflectionUtil;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.*;
import net.minecraft.commands.arguments.blocks.BlockPredicateArgument;
import net.minecraft.commands.arguments.blocks.BlockStateArgument;
import net.minecraft.commands.arguments.coordinates.*;
import net.minecraft.commands.arguments.item.FunctionArgument;
import net.minecraft.commands.arguments.item.ItemArgument;
import net.minecraft.commands.arguments.item.ItemPredicateArgument;
import net.minecraft.commands.arguments.selector.EntitySelector;
import net.minecraft.commands.synchronization.ArgumentTypes;
import net.minecraft.commands.synchronization.SuggestionProviders;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.*;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ColumnPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.bukkit.*;
import org.bukkit.advancement.Advancement;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.command.CommandSender;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.craftbukkit.v1_18_R2.CraftLootTable;
import org.bukkit.craftbukkit.v1_18_R2.CraftParticle;
import org.bukkit.craftbukkit.v1_18_R2.CraftServer;
import org.bukkit.craftbukkit.v1_18_R2.CraftSound;
import org.bukkit.craftbukkit.v1_18_R2.block.data.CraftBlockData;
import org.bukkit.craftbukkit.v1_18_R2.enchantments.CraftEnchantment;
import org.bukkit.craftbukkit.v1_18_R2.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_18_R2.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_18_R2.potion.CraftPotionEffectType;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.loot.LootTable;
import org.bukkit.potion.PotionEffectType;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@SuppressWarnings({"rawtypes", "unchecked"})
public class Command_1_18_R2 implements CommandCompatibility {

    public static final MinecraftServer SERVER = ((CraftServer) Bukkit.getServer()).getServer();
    private static final DynamicCommandExceptionType ERROR_BIOME_INVALID = new DynamicCommandExceptionType(arg -> new TranslatableComponent("commands.locate.biome.invalid", arg));

    @Override
    public SimpleCommandMap getCommandMap() {
        return SERVER.server.getCommandMap();
    }

    @Override
    public void resendCommandRegistry(Player player) {
        ServerPlayer p = ((CraftPlayer) player).getHandle();
        SERVER.vanillaCommandDispatcher.sendCommands(p);
    }

    @Override
    public void generateFile(File file) {
        try {
            Files.asCharSink(file, StandardCharsets.UTF_8).write(new GsonBuilder().setPrettyPrinting().create()
                    .toJson(ArgumentTypes.serializeNodeToJson(getCommandDispatcher(), getCommandDispatcher().getRoot())));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public CommandSender getCommandSender(CommandContext<Object> context) {
        CommandSourceStack source = (CommandSourceStack) context.getSource();
        return source.getBukkitSender();
    }

    @Override
    public CommandSender getCommandSenderRaw(Object nms) {
        return ((CommandSourceStack) nms).getBukkitSender();
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public CommandDispatcher<Object> getCommandDispatcher() {
        return (CommandDispatcher) SERVER.vanillaCommandDispatcher.getDispatcher();
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
    public ArgumentType<?> entity() {
        return EntityArgument.entity();
    }

    @Override
    public ArgumentType<?> entities() {
        return EntityArgument.entities();
    }

    @Override
    public ArgumentType<?> player() {
        return EntityArgument.player();
    }

    @Override
    public ArgumentType<?> players() {
        return EntityArgument.players();
    }

    @Override
    public ArgumentType<?> entitySummon() {
        return EntitySummonArgument.id();
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
    public ArgumentType<?> location() {
        return Vec3Argument.vec3();
    }

    @Override
    public ArgumentType<?> location2() {
        return Vec2Argument.vec2();
    }

    @Override
    public ArgumentType<?> block() {
        return BlockPosArgument.blockPos();
    }

    @Override
    public ArgumentType<?> block2() {
        return ColumnPosArgument.columnPos();
    }

    @Override
    public ArgumentType<?> biome() {
        return ResourceOrTagLocationArgument.resourceOrTag(Registry.BIOME_REGISTRY); // this is actually only changed 1.18R2 and up
    }

    @Override
    public ArgumentType<?> key() {
        return ResourceLocationArgument.id();
    }

    @Override
    public SuggestionProvider<Object> biomeKey() {
        return biome()::listSuggestions;
    }

    @Override
    public SuggestionProvider<Object> recipeKey() {
        return (SuggestionProvider) SuggestionProviders.ALL_RECIPES;
    }

    @Override
    public SuggestionProvider<Object> soundKey() {
        return (SuggestionProvider) SuggestionProviders.AVAILABLE_SOUNDS;
    }

    @Override
    public SuggestionProvider<Object> entityKey() {
        return (SuggestionProvider) SuggestionProviders.SUMMONABLE_ENTITIES;
    }

    @Override
    public SuggestionProvider<Object> advancementKey() {
        return (context, builder) -> SharedSuggestionProvider.suggestResource(SERVER.getAdvancements().getAllAdvancements().stream().map(net.minecraft.advancements.Advancement::getId), builder);
    }

    @Override
    public SuggestionProvider<Object> lootKey() {
        return (context, builder) -> SharedSuggestionProvider.suggestResource(SERVER.getLootTables().getIds(), builder);
    }

    private static NamespacedKey fromResourceLocation(ResourceLocation key) {
        return NamespacedKey.fromString(key.getNamespace() + ":" + key.getPath());
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private CommandContext<CommandSourceStack> cast(CommandContext<Object> context) {
        return (CommandContext) context;
    }

    @Override
    public Advancement getAdvancement(CommandContext<Object> context, String key) throws CommandSyntaxException {
        return ResourceLocationArgument.getAdvancement(cast(context), key).bukkit;
    }

    @Override
    public float getAngle(CommandContext<Object> context, String key) {
        return AngleArgument.getAngle(cast(context), key);
    }

    @Override
    public EnumSet<Axis> getAxis(CommandContext<Object> context, String key) {
        EnumSet<Axis> bukkitAxis = EnumSet.noneOf(Axis.class);
        EnumSet<Direction.Axis> nmsAxis = SwizzleArgument.getSwizzle(cast(context), key);

        for (Direction.Axis axis : nmsAxis) {
            bukkitAxis.add(switch (axis) {
                case X -> Axis.X;
                case Y -> Axis.Y;
                case Z -> Axis.Z;
            });
        }

        return bukkitAxis;
    }

    @Override
    public BiomeHolder getBiome(CommandContext<Object> context, String key) throws CommandSyntaxException {
        ResourceOrTagLocationArgument.Result<net.minecraft.world.level.biome.Biome> biomeResult = ResourceOrTagLocationArgument.getBiome(cast(context), key);

        String name = biomeResult.asPrintable();

        if (biomeResult.unwrap().left().isPresent()) {
            ResourceKey<net.minecraft.world.level.biome.Biome> resource = biomeResult.unwrap().left().get();
            Biome biome = EnumUtil.getIfPresent(Biome.class, resource.location().getPath()).orElse(Biome.CUSTOM);
            NamespacedKey namespaced = new NamespacedKey(resource.location().getNamespace(), resource.location().getPath());
            return new BiomeHolder(biome, namespaced);
        } else {
            throw ERROR_BIOME_INVALID.create(name);
        }
    }

    @Override
    public Predicate<Block> getBlockPredicate(CommandContext<Object> context, String key) throws CommandSyntaxException {
        Predicate<BlockInWorld> predicate = BlockPredicateArgument.getBlockPredicate(cast(context), key);
        return block -> predicate.test(new BlockInWorld(cast(context).getSource().getLevel(),
                new BlockPos(block.getX(), block.getY(), block.getZ()), true));
    }

    @Override
    public BlockData getBlockState(CommandContext<Object> context, String key) {
        return CraftBlockData.fromData(BlockStateArgument.getBlock(cast(context), key).getState());
    }

    @Override
    public World.Environment getDimension(CommandContext<Object> context, String key) throws CommandSyntaxException {
        return DimensionArgument.getDimension(cast(context), key).getWorld().getEnvironment();
    }

    @Override
    public Enchantment getEnchantment(CommandContext<Object> context, String key) {
        return new CraftEnchantment(ItemEnchantmentArgument.getEnchantment(cast(context), key));
    }

    @Override
    public org.bukkit.entity.Entity getEntitySelector(CommandContext<Object> context, String key) throws CommandSyntaxException {
        EntitySelector selector = cast(context).getArgument(key, EntitySelector.class);

        // Setting this field allows non-op users to use entity selectors.
        // We let command permissions handle the permission system. We may have
        // to check if a vanished player can be seen in this list. TODO.
        ReflectionUtil.setField(ReflectionUtil.getField(EntitySelector.class, boolean.class, 3), selector, false);

        CommandSourceStack source = (CommandSourceStack) context.getSource();
        return selector.findSingleEntity(source).getBukkitEntity();
    }

    @Override
    public List<org.bukkit.entity.Entity> getEntitiesSelector(CommandContext<Object> context, String key) throws CommandSyntaxException {
        EntitySelector selector = cast(context).getArgument(key, EntitySelector.class);

        // Setting this field allows non-op users to use entity selectors.
        // We let command permissions handle the permission system. We may have
        // to check if a vanished player can be seen in this list. TODO.
        ReflectionUtil.setField(ReflectionUtil.getField(EntitySelector.class, boolean.class, 3), selector, false);

        CommandSourceStack source = (CommandSourceStack) context.getSource();
        return selector.findEntities(source).stream()
                .map(Entity::getBukkitEntity)
                .collect(Collectors.toList());
    }

    @Override
    public Player getPlayerSelector(CommandContext<Object> context, String key) throws CommandSyntaxException {
        EntitySelector selector = cast(context).getArgument(key, EntitySelector.class);

        // Setting this field allows non-op users to use entity selectors.
        // We let command permissions handle the permission system. We may have
        // to check if a vanished player can be seen in this list. TODO.
        ReflectionUtil.setField(ReflectionUtil.getField(EntitySelector.class, boolean.class, 3), selector, false);

        CommandSourceStack source = (CommandSourceStack) context.getSource();
        return selector.findSinglePlayer(source).getBukkitEntity();
    }

    @Override
    public List<Player> getPlayersSelector(CommandContext<Object> context, String key) throws CommandSyntaxException {
        EntitySelector selector = cast(context).getArgument(key, EntitySelector.class);

        // Setting this field allows non-op users to use entity selectors.
        // We let command permissions handle the permission system. We may have
        // to check if a vanished player can be seen in this list. TODO.
        ReflectionUtil.setField(ReflectionUtil.getField(EntitySelector.class, boolean.class, 3), selector, false);

        CommandSourceStack source = (CommandSourceStack) context.getSource();
        return selector.findPlayers(source).stream()
                .map(ServerPlayer::getBukkitEntity)
                .collect(Collectors.toList());
    }

    @Override
    public EntityType getEntityType(CommandContext<Object> context, String key) throws CommandSyntaxException {
        net.minecraft.world.entity.EntityType<?> type = Registry.ENTITY_TYPE.get(EntitySummonArgument.getSummonableEntity(cast(context), key));
        return EntityType.fromName(net.minecraft.world.entity.EntityType.getKey(type).getPath());
    }

    @Override
    public DoubleRange getDoubleRange(CommandContext<Object> context, String key) {
        MinMaxBounds.Doubles range = RangeArgument.Floats.getRange(cast(context), key);
        double min = range.getMin() == null ? Double.MIN_VALUE : range.getMin();
        double max = range.getMax() == null ? Double.MAX_VALUE : range.getMax();
        return new DoubleRange(min, max);
    }

    @Override
    public IntRange getIntRange(CommandContext<Object> context, String key) {
        MinMaxBounds.Ints range = RangeArgument.Ints.getRange(cast(context), key);
        int min = range.getMin() == null ? Integer.MIN_VALUE : range.getMin();
        int max = range.getMax() == null ? Integer.MAX_VALUE : range.getMax();
        return new IntRange(min, max);
    }

    @Override
    public ItemStack getItemStack(CommandContext<Object> context, String key) throws CommandSyntaxException {
        return CraftItemStack.asBukkitCopy(ItemArgument.getItem(cast(context), key).createItemStack(1, false));
    }

    @Override
    public Predicate<ItemStack> getItemStackPredicate(CommandContext<Object> context, String key) throws CommandSyntaxException {
        Predicate<net.minecraft.world.item.ItemStack> predicate = ItemPredicateArgument.getItemPredicate(cast(context), key);
        return (item) -> predicate.test(CraftItemStack.asNMSCopy(item));
    }

    @Override
    public String getKeyedAsString(CommandContext<Object> context, String key) throws CommandSyntaxException {
        return ResourceLocationArgument.getId(cast(context), key).toString();
    }

    @Override
    public Column getLocation2DBlock(CommandContext<Object> context, String key) throws CommandSyntaxException {
        ColumnPos column = ColumnPosArgument.getColumnPos(cast(context), key);
        World world = cast(context).getSource().getLevel().getWorld();
        return new Column(world, column.x, column.z);
    }

    @Override
    public Location2d getLocation2DPrecise(CommandContext<Object> context, String key) throws CommandSyntaxException {
        Vec2 vector = Vec2Argument.getVec2(cast(context), key);
        World world = cast(context).getSource().getLevel().getWorld();
        return new Location2d(world, vector.x, vector.y);
    }

    @Override
    public Block getLocationBlock(CommandContext<Object> context, String key) throws CommandSyntaxException {
        BlockPos block = BlockPosArgument.getLoadedBlockPos(cast(context), key);
        World world = cast(context).getSource().getLevel().getWorld();
        return world.getBlockAt(block.getX(), block.getY(), block.getZ());
    }

    @Override
    public Location getLocationPrecise(CommandContext<Object> context, String key) throws CommandSyntaxException {
        Vec3 vector = Vec3Argument.getVec3(cast(context), key);
        World world = cast(context).getSource().getLevel().getWorld();
        return new Location(world, vector.x, vector.y, vector.z);
    }

    @Override
    public LootTable getLootTable(CommandContext<Object> context, String key) {
        ResourceLocation resourceLocation = ResourceLocationArgument.getId(cast(context), key);
        return new CraftLootTable(fromResourceLocation(resourceLocation), SERVER.getLootTables().get(resourceLocation));
    }

    @Override
    public String getObjective(CommandContext<Object> context, String key) throws IllegalArgumentException, CommandSyntaxException {
        return ObjectiveArgument.getObjective(cast(context), key).getName();
    }

    @Override
    public String getObjectiveCriteria(CommandContext<Object> context, String key) {
        return ObjectiveCriteriaArgument.getCriteria(cast(context), key).getName();
    }

    @Override
    public ParticleHolder getParticle(CommandContext<Object> context, String key) {
        ParticleOptions particle = ParticleArgument.getParticle(cast(context), key);
        return new ParticleHolder(CraftParticle.toBukkit(particle), particle, particle.writeToString());
    }

    @Override
    public Player getPlayer(CommandContext<Object> context, String key) throws CommandSyntaxException {
        Player target = Bukkit.getPlayer(GameProfileArgument.getGameProfiles(cast(context), key).iterator().next().getId());
        if (target == null)
            throw GameProfileArgument.ERROR_UNKNOWN_PLAYER.create();

        return target;
    }

    @Override
    public OfflinePlayer getOfflinePlayer(CommandContext<Object> context, String key) throws CommandSyntaxException {
        return Bukkit.getOfflinePlayer(GameProfileArgument.getGameProfiles(cast(context), key).iterator().next().getId());
    }

    @Override
    public PotionEffectType getPotionEffect(CommandContext<Object> context, String key) throws CommandSyntaxException {
        return new CraftPotionEffectType(MobEffectArgument.getEffect(cast(context), key));
    }

    @Override
    public Recipe getRecipe(CommandContext<Object> context, String key) throws CommandSyntaxException {
        net.minecraft.world.item.crafting.Recipe<?> recipe = ResourceLocationArgument.getRecipe(cast(context), key);
        return recipe.toBukkitRecipe();
    }

    @Override
    public Rotation getRotation(CommandContext<Object> context, String key) {
        Vec2 rotation = RotationArgument.getRotation(cast(context), key).getRotation(cast(context).getSource());
        return new Rotation(rotation.x, rotation.y);
    }

    @Override
    public SoundHolder getSound(CommandContext<Object> context, String key) {
        ResourceLocation location = ResourceLocationArgument.getId(cast(context), key);
        SoundEvent sound = Registry.SOUND_EVENT.get(location);
        Sound bukkit = sound == null ? null : CraftSound.getBukkit(sound);
        return new SoundHolder(bukkit, new NamespacedKey(location.getNamespace(), location.getPath()));
    }

    @Override
    public String getTeam(CommandContext<Object> context, String key) throws CommandSyntaxException {
        return TeamArgument.getTeam(cast(context), key).getName();
    }

    @Override
    public int getTime(CommandContext<Object> context, String key) {
        return cast(context).getArgument(key, Integer.class);
    }

    @Override
    public UUID getUUID(CommandContext<Object> context, String key) {
        return UuidArgument.getUuid(cast(context), key);
    }

    @Override
    public Map<String, Object> getCompound(CommandContext<Object> context, String key) {
        CompoundTag nbt = CompoundTagArgument.getCompoundTag(context, key);
        return convertMap(nbt);
    }

    @SuppressWarnings("unchecked")
    private Object convert(Tag tag) {
        return switch (tag.getId()) {
            case Tag.TAG_BYTE, Tag.TAG_SHORT, Tag.TAG_INT, Tag.TAG_LONG -> ((NumericTag) tag).getAsInt();
            case Tag.TAG_FLOAT, Tag.TAG_DOUBLE -> ((NumericTag) tag).getAsDouble();
            case Tag.TAG_STRING -> ((StringTag) tag).getAsString();
            case Tag.TAG_BYTE_ARRAY, Tag.TAG_INT_ARRAY, Tag.TAG_LONG_ARRAY, Tag.TAG_LIST -> convertList((CollectionTag<Tag>) tag);
            case Tag.TAG_COMPOUND -> convertMap((CompoundTag) tag);
            default -> throw new IllegalStateException("Unexpected value: " + tag);
        };
    }

    private Map<String, Object> convertMap(CompoundTag nbt) {
        Map<String, Object> temp = new HashMap<>(nbt.size());

        for (String key : nbt.getAllKeys()) {
            Object value = convert(Objects.requireNonNull(nbt.get(key)));
            temp.put(key, value);
        }

        return temp;
    }

    private List<Object> convertList(CollectionTag<Tag> values) {
        return values.stream().map(this::convert).collect(Collectors.toList());
    }
}