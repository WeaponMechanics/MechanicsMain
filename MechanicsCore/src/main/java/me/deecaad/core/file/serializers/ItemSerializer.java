package me.deecaad.core.file.serializers;

import com.cjcrafter.foliascheduler.util.FieldAccessor;
import com.cjcrafter.foliascheduler.util.MinecraftVersions;
import com.cjcrafter.foliascheduler.util.ReflectionUtil;
import com.cryptomorin.xseries.profiles.builder.XSkull;
import com.cryptomorin.xseries.profiles.objects.ProfileInputType;
import com.cryptomorin.xseries.profiles.objects.Profileable;
import me.deecaad.core.MechanicsCore;
import me.deecaad.core.compatibility.CompatibilityAPI;
import me.deecaad.core.compatibility.nbt.NBTCompatibility;
import me.deecaad.core.file.SerializeData;
import me.deecaad.core.file.Serializer;
import me.deecaad.core.file.SerializerException;
import me.deecaad.core.file.simple.BooleanSerializer;
import me.deecaad.core.file.simple.ByNameSerializer;
import me.deecaad.core.file.simple.CsvSerializer;
import me.deecaad.core.file.simple.DoubleSerializer;
import me.deecaad.core.file.simple.EnumValueSerializer;
import me.deecaad.core.file.simple.IntSerializer;
import me.deecaad.core.file.simple.RegistryValueSerializer;
import me.deecaad.core.file.simple.StringSerializer;
import me.deecaad.core.utils.AdventureUtil;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.Tag;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.block.data.Levelled;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.damage.DamageType;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ArmorMeta;
import org.bukkit.inventory.meta.BlockDataMeta;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.inventory.meta.trim.ArmorTrim;
import org.bukkit.inventory.meta.trim.TrimMaterial;
import org.bukkit.inventory.meta.trim.TrimPattern;
import org.bukkit.tag.DamageTypeTags;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;

public class ItemSerializer implements Serializer<ItemStack> {

    public static final Map<String, Supplier<ItemStack>> ITEM_REGISTRY = new HashMap<>();

    private static final FieldAccessor ingredientsField;

    static {
        ingredientsField = ReflectionUtil.getField(ShapedRecipe.class, "ingredients");
    }

    /**
     * Empty constructor to be used as serializer
     */
    public ItemSerializer() {
    }

    @Override
    @NotNull public ItemStack serialize(@NotNull SerializeData data) throws SerializerException {
        return serializeWithTags(data, Collections.emptyMap());
    }

    public ItemStack serializeWithTags(@NotNull SerializeData data, @NotNull Map<String, Object> tags) throws SerializerException {

        // When the key is null, that probably means we are currently in an
        // inline serializer. Skip the fancy shit.
        if (data.getKey() == null) {
            ItemStack itemStack = serializeWithoutRecipe(data);
            applyTags(itemStack, tags);
            itemStack = serializeRecipe(data, itemStack);
            return itemStack;
        }

        // One liner items make life easier with less indentation
        ItemStack inline = attemptInline(data);
        if (inline != null) {
            applyTags(inline, tags);
            return inline;
        }

        ItemStack itemStack = serializeWithoutRecipe(data);
        applyTags(itemStack, tags);
        itemStack = serializeRecipe(data, itemStack);
        return itemStack;
    }

    public void applyTags(@NotNull ItemStack item, @NotNull Map<String, Object> tags) {
        NBTCompatibility nbt = CompatibilityAPI.getNBTCompatibility();

        for (Map.Entry<String, Object> entry : tags.entrySet()) {
            String[] split = entry.getKey().split(":");
            String plugin = split[0];
            String tag = split[1];

            if (entry.getValue() instanceof String string)
                nbt.setString(item, plugin, tag, string);
            else if (entry.getValue() instanceof Double num)
                nbt.setDouble(item, plugin, tag, num);
            else if (entry.getValue() instanceof Integer num)
                nbt.setInt(item, plugin, tag, num);
            else if (entry.getValue() instanceof int[] arr)
                nbt.setArray(item, plugin, tag, arr);
            else if (entry.getValue() instanceof String[] arr)
                nbt.setStringArray(item, plugin, tag, arr);
            else
                throw new IllegalArgumentException("Unrecognized type " + entry.getValue() + " when setting custom tags");
        }
    }

    public @Nullable ItemStack attemptInline(@NotNull SerializeData data) throws SerializerException {
        // If the data here is a String (not a ConfigurationSection), then we
        // can assume that the user is trying to inline an item.
        if (data.of().is(String.class)) {
            String inlineString = data.of().assertExists().get(String.class).get();
            if (ITEM_REGISTRY.containsKey(inlineString))
                return ITEM_REGISTRY.get(inlineString).get();

            return data.of().assertExists().getMaterialAsItem().get();
        }

        return null;
    }

    public ItemStack serializeWithoutRecipe(@NotNull SerializeData data) throws SerializerException {
        ItemStack itemStack = data.of("Type").assertExists().getMaterialAsItem().get();
        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta == null) {
            throw data.exception("Type", "Did you use air as a material? This is not allowed!",
                "Found item: " + itemStack);
        }

        String name = data.of("Name").getAdventure().orElse(null);
        if (name != null) {
            AdventureUtil.setNameUnparsed(itemMeta, name);
        }

        List<?> lore = data.of("Lore").get(List.class).orElse(null);
        if (lore != null) {
            AdventureUtil.setLoreUnparsed(itemMeta, lore);
        }

        OptionalInt durability = data.of("Durability").assertRange(0, null).getInt();
        if (durability.isPresent()) {
            if (!(itemMeta instanceof org.bukkit.inventory.meta.Damageable damageable)) {
                throw data.exception("Durability", "Tried to set durability on a non-damageable item!",
                    "Your item: " + itemStack);
            }

            damageable.setDamage(durability.getAsInt());
        }

        boolean unbreakable = data.of("Unbreakable").getBool().orElse(false);
        itemMeta.setUnbreakable(unbreakable);

        OptionalInt customModelData = data.of("Custom_Model_Data").getInt();
        if (customModelData.isPresent()) {
            itemMeta.setCustomModelData(customModelData.getAsInt());
        }

        OptionalInt maxStackSize = data.of("Max_Stack_Size").assertRange(1, 99).getInt();
        if (maxStackSize.isPresent()) {
            if (!MinecraftVersions.TRAILS_AND_TAILS.get(5).isAtLeast()) {
                throw data.exception("Max_Stack_Size", "Tried to use max stack size before MC 1.20.5!",
                    "The max stack size was added in Minecraft version 1.20.5!",
                    "Your version: " + MinecraftVersions.getCurrent());
            }

            itemMeta.setMaxStackSize(maxStackSize.getAsInt());
        }

        Optional<Boolean> enchantmentGlintOverride = data.of("Enchantment_Glint_Override").getBool();
        if (enchantmentGlintOverride.isPresent()) {
            if (!MinecraftVersions.TRAILS_AND_TAILS.get(5).isAtLeast()) {
                throw data.exception("Enchantment_Glint_Override", "Tried to use enchantment glint override before MC 1.20.5!",
                    "The enchantment glint override component was added in Minecraft version 1.20.5!",
                    "Your version: " + MinecraftVersions.getCurrent());
            }

            itemMeta.setEnchantmentGlintOverride(enchantmentGlintOverride.get());
        }

        Optional<String> damageResistantInput = data.of("Damage_Resistant").get(String.class);
        if (damageResistantInput.isPresent()) {
            if (!MinecraftVersions.TRICKY_TRIALS.get(2).isAtLeast()) {
                throw data.exception("Damage_Resistant", "Tried to use damage resistance before MC 1.21.2!",
                    "The damage resistance option was added in Minecraft version 1.21.2!",
                    "Your version: " + MinecraftVersions.getCurrent());
            }

            NamespacedKey damageKey = NamespacedKey.fromString(damageResistantInput.get());
            Tag<DamageType> damageTag = Bukkit.getTag(DamageTypeTags.REGISTRY_DAMAGE_TYPES, damageKey, DamageType.class);
            itemMeta.setDamageResistant(damageTag);
        }

        List<List<Optional<Object>>> enchantments = data.ofList("Enchantments")
            .addArgument(new RegistryValueSerializer<>(Registry.ENCHANTMENT, true))
            .requireAllPreviousArgs()
            .addArgument(new IntSerializer(1))
            .assertList();

        for (List<Optional<Object>> split : enchantments) {
            List<Enchantment> enchants = (List<Enchantment>) split.get(0).get();
            int enchantmentLevel = (int) split.get(1).orElse(1);

            for (Enchantment enchantment : enchants) {
                itemMeta.addEnchant(enchantment, enchantmentLevel - 1, true);
            }
        }

        itemStack.setItemMeta(itemMeta);

        // #198
        data.ofList("Tags")
            .addArgument(new StringSerializer())
            .requireAllPreviousArgs()
            .addArgument(new IntSerializer())
            .assertList().forEach(split -> {
                String tag = (String) split.get(0).get();
                int value = (int) split.get(1).orElse(1);
                CompatibilityAPI.getNBTCompatibility().setInt(itemStack, "Custom", tag, value);
            });

        // TODO: Spigot will surely improve this
        Map<String, EquipmentSlotGroup> slotGroupsByName = new HashMap<>();
        slotGroupsByName.put("any", EquipmentSlotGroup.ANY);
        slotGroupsByName.put("mainhand", EquipmentSlotGroup.MAINHAND);
        slotGroupsByName.put("offhand", EquipmentSlotGroup.OFFHAND);
        slotGroupsByName.put("hand", EquipmentSlotGroup.HAND);
        slotGroupsByName.put("head", EquipmentSlotGroup.HEAD);
        slotGroupsByName.put("chest", EquipmentSlotGroup.CHEST);
        slotGroupsByName.put("legs", EquipmentSlotGroup.LEGS);
        slotGroupsByName.put("feet", EquipmentSlotGroup.FEET);
        slotGroupsByName.put("armor", EquipmentSlotGroup.ARMOR);

        List<List<Optional<Object>>> attributesData = data.ofList("Attributes")
            .addArgument(new RegistryValueSerializer<>(Registry.ATTRIBUTE, true))
            .addArgument(new DoubleSerializer())
            .requireAllPreviousArgs()
            .addArgument(new ByNameSerializer<>(EquipmentSlotGroup.class, slotGroupsByName))
            .addArgument(new EnumValueSerializer<>(AttributeModifier.Operation.class, false))
            .assertList();

        for (List<Optional<Object>> split : attributesData) {
            List<Attribute> attributes = (List<Attribute>) split.get(0).get();
            double amount = (double) split.get(1).get();
            EquipmentSlotGroup slot = (EquipmentSlotGroup) split.get(2).orElse(EquipmentSlotGroup.ANY);
            AttributeModifier.Operation operation = ((List<AttributeModifier.Operation>) split.get(3).orElse(AttributeModifier.Operation.ADD_NUMBER)).getFirst();

            for (Attribute attribute : attributes) {

                // Attributes need a completely unique key to avoid attributes
                // overridding each other.
                NamespacedKey key = new NamespacedKey(MechanicsCore.getPlugin(), attribute.getKey().getKey() + "-" + slot);
                AttributeModifier modifier = new AttributeModifier(key, amount, operation, slot);
                itemMeta.addAttributeModifier(attribute, modifier);
            }
        }

        // Add flags after attributes due to a bug introduced in Spigot 1.20.5, see
        // https://github.com/PaperMC/Paper/issues/10693
        boolean hideFlags = data.of("Hide_Flags").getBool().orElse(false);
        if (hideFlags) {
            ItemMeta temp = itemStack.getItemMeta();
            temp.addItemFlags(ItemFlag.values());
            itemStack.setItemMeta(temp);
        }

        String owningPlayer = data.of("Skull_Owning_Player").get(String.class).orElse(null);
        if (owningPlayer != null) {
            try {
                int splitIndex = owningPlayer.indexOf(" ");
                String id = splitIndex == -1 ? owningPlayer : owningPlayer.substring(0, splitIndex);
                String url = splitIndex == -1 ? null : owningPlayer.substring(splitIndex + 1);

                SkullMeta skullMeta = (SkullMeta) itemStack.getItemMeta();

                UUID uuid;
                try {
                    uuid = UUID.fromString(id);
                } catch (IllegalArgumentException e) {
                    uuid = null;
                }

                // Custom skull format... "UUID URL"
                // "970e0a59-b95d-45a9-9039-b43ac4fbfc7c
                // https://textures.minecraft.net/texture/a0564817fcc8dd51bc1957c0b7ea142db687dd6f1caafd35bb4dcfee592421c"
                // https://www.spigotmc.org/threads/create-a-skull-item-stack-with-a-custom-texture-base64.82416/
                if (uuid != null && url != null) {
                    // XSkull.of(itemMeta).profile(XSkull.SkullInputType.UUID, id).apply();
                    XSkull.of(skullMeta).profile(Profileable.of(ProfileInputType.TEXTURE_URL, url)).apply();
                }

                // Standard player name SkullMeta... "CJCrafter", "DeeCaaD", "Darkman_Bree"
                else if (uuid != null) {
                    skullMeta.setOwningPlayer(Bukkit.getServer().getOfflinePlayer(uuid));
                } else {
                    skullMeta.setOwner(owningPlayer);
                }
                itemStack.setItemMeta(skullMeta);
            } catch (ClassCastException e) {
                throw data.exception("Skull_Owning_Player", "Tried to use Skulls when the item wasn't a player head!",
                    "Found item: " + itemStack);
            }
        }

        if (data.has("Potion_Color")) {
            try {
                Color color = data.of("Potion_Color").assertExists().serialize(ColorSerializer.class).get();
                PotionMeta potionMeta = (PotionMeta) itemStack.getItemMeta();
                potionMeta.setColor(color);
                itemStack.setItemMeta(potionMeta);
            } catch (ClassCastException e) {
                throw data.exception("Potion_Color", "Tried to use Potion Color when the item wasn't a potion!",
                    "Found item: " + itemStack);
            }
        }

        if (data.has("Leather_Color")) {
            try {
                Color color = data.of("Leather_Color").assertExists().serialize(ColorSerializer.class).get();
                LeatherArmorMeta meta = (LeatherArmorMeta) itemStack.getItemMeta();
                meta.setColor(color);
                itemStack.setItemMeta(meta);
            } catch (ClassCastException e) {
                throw data.exception("Leather_Color", "Tried to use Leather Color when the item wasn't leather armor!",
                    "Found item: " + itemStack);
            }
        }

        boolean hasOneOfPatternOrMaterial = data.has("Trim_Pattern") || data.has("Trim_Material");
        if (hasOneOfPatternOrMaterial && itemStack.getItemMeta() instanceof ArmorMeta armor) {
            if (MinecraftVersions.TRAILS_AND_TAILS.isBelow()) {
                throw data.exception("Trim_Pattern", "Tried to use trim pattern before MC 1.20!",
                    "The trim pattern was added in Minecraft version 1.20!");
            }

            TrimPattern pattern = data.of("Trim_Pattern").assertExists().getBukkitRegistry(Registry.TRIM_PATTERN).get();
            TrimMaterial material = data.of("Trim_Material").assertExists().getBukkitRegistry(Registry.TRIM_MATERIAL).get();
            armor.setTrim(new ArmorTrim(material, pattern));
            itemStack.setItemMeta(armor);
        }

        if (data.has("Firework")) {
            try {
                // <FireworkEffect.Type>-<Color>-<Boolean=Trail>-<Boolean=Flicker>-<Color=Fade>
                List<List<Optional<Object>>> list = data.ofList("Firework.Effects")
                    .addArgument(new EnumValueSerializer<>(FireworkEffect.Type.class, false))
                    .addArgument(new CsvSerializer<>(new ColorSerializer())) // colors
                    .requireAllPreviousArgs()
                    .addArgument(new BooleanSerializer()) // isTrail
                    .addArgument(new BooleanSerializer()) // isFlicker
                    .addArgument(new CsvSerializer<>(new ColorSerializer())) // fadeColors
                    .assertExists().assertList();

                FireworkMeta meta = (FireworkMeta) itemMeta;
                meta.setPower(data.of("Firework.Power").assertRange(0, 255).getInt().orElse(1));
                for (List<Optional<Object>> split : list) {

                    FireworkEffect.Builder builder = FireworkEffect.builder();
                    builder.with((FireworkEffect.Type) split.get(0).get());

                    // Handle initial colors
                    List<Color> colors = (List<Color>) split.get(1).get();
                    builder.withColor(colors);

                    builder.trail((boolean) split.get(2).orElse(true));
                    builder.flicker((boolean) split.get(3).orElse(true));

                    // Handle the fade colors
                    List<Color> fadeColors = (List<Color>) split.get(4).orElse(List.of());
                    builder.withFade(fadeColors);

                    // Add the newly constructed firework effect to the list.
                    meta.addEffect(builder.build());
                }
                itemStack.setItemMeta(meta);
            } catch (ClassCastException ex) {
                throw data.exception("Firework", "Tried to use Firework when the item wasn't a firework rocket!",
                    "Found item: " + itemStack);
            }
        }

        if (data.has("Light_Level")) {
            if (!MinecraftVersions.CAVES_AND_CLIFFS_1.isAtLeast()) {
                throw data.exception("Light_Level", "Tried to use light level before MC 1.17!",
                    "The light block was added in Minecraft version 1.17!");
            }

            try {
                BlockDataMeta meta = (BlockDataMeta) itemStack.getItemMeta();
                Levelled levelled = (Levelled) meta.getBlockData(Material.LIGHT);
                int level = data.of("Light_Level").assertRange(0, levelled.getMaximumLevel()).getInt().orElse(0);
                levelled.setLevel(level);
                meta.setBlockData(levelled);
                itemStack.setItemMeta(meta);
            } catch (ClassCastException ex) {
                throw data.exception("Light_Level", "Tried to use the Light_Level option on a non 'LIGHT' block");
            }
        }

        if (data.of("Deny_Use_In_Crafting").getBool().orElse(false)) {
            CompatibilityAPI.getNBTCompatibility().setInt(itemStack, "MechanicsCore", "deny-crafting", 1);
        }

        return itemStack;
    }

    public ItemStack serializeRecipe(SerializeData data, ItemStack itemStack) throws SerializerException {
        if (!data.has("Recipe"))
            return itemStack;

        // Create a copy of the item with a set amount to be the result of the
        // crafting recipe.
        int resultAmount = data.of("Recipe.Output_Amount").assertRange(1, 64).getInt().orElse(1);
        ItemStack result = itemStack.clone();
        result.setAmount(resultAmount);

        // Namespaced keys for recipes were added in MC 1.12
        ShapedRecipe recipe = new ShapedRecipe(new NamespacedKey(MechanicsCore.getPlugin(), data.getKey()), result);

        // Bukkit.getRecipe was added in 1.16. We have a try-catch block
        // below in this method to handle 1.12 through 1.15
        if (MinecraftVersions.NETHER_UPDATE.isAtLeast() && Bukkit.getRecipe(recipe.getKey()) != null) {
            return itemStack;
        }

        // The Recipe.Shape should be a list looking similar to:
        // - ABC
        // - DEF
        // - GHI
        List<Object> shape = data.of("Recipe.Shape").assertExists().get(List.class).get();
        String[] shapeArr = shape.stream().map(Object::toString).toArray(String[]::new);
        if (shape.size() < 1 || shape.size() > 3) {
            throw SerializerException.builder()
                .locationRaw(data.of("Recipe.Shape").getLocation())
                .addMessage("Expected a list of either 1, 2, or 3 strings to make the recipe")
                .buildInvalidRange(shape.size(), 1, 3);
        }

        Set<Character> ingredientChars = new HashSet<>();
        for (int i = 0; i < shapeArr.length; i++) {
            String str = shapeArr[i];

            // Before we get mad at the user, let's try removing trailing whitespace
            if (str.length() > 3)
                shapeArr[i] = str = str.trim();

            if (str.length() < 1 || str.length() > 3) {
                throw SerializerException.builder()
                    .locationRaw(data.of("Recipe.Shape").getLocation())
                    .addMessage("Each string in the shape must be between 1 and 3 characters long")
                    .buildInvalidRange(str.length(), 1, 3);
            }

            for (char c : str.toCharArray())
                ingredientChars.add(c);
        }

        // Set shape AFTER our checks above, so we can be more verbose to
        // the user and try to correct their mistakes automatically.
        try {
            recipe.shape(shapeArr);
        } catch (Throwable ex) {
            throw data.exception("Recipe.Shape", "Recipe Shape was formatted incorrectly",
                ex.getMessage(), "Found shape: " + shape);
        }

        // Shaped recipes in 1.12 and lower just use a map of
        // characters and ItemStacks. In 1.13 and higher, recipes
        // use Characters and RecipeChoices.
        final Map<Character, Object> ingredients = new HashMap<>();
        data.of("Recipe.Ingredients").assertExists().assertType(ConfigurationSection.class);
        for (char c : ingredientChars) {

            // Spaces (' ') in spigot are ignored and treated as air for recipes
            if (c == ' ')
                continue;

            ItemStack item = data.of("Recipe.Ingredients." + c).assertExists().serialize(ItemSerializer.class).get();
            ingredients.put(c, new RecipeChoice.ExactChoice(item));
        }

        // Finalize and register the new recipe.
        ingredientsField.set(recipe, ingredients);
        try {
            Bukkit.addRecipe(recipe);
        } catch (IllegalStateException ex) {
            // rethrow if we don't know where this error came from
            if (!ex.getMessage().startsWith("Duplicate recipe ignored with ID mechanicscore:"))
                throw ex;
        }
        return itemStack;
    }
}
