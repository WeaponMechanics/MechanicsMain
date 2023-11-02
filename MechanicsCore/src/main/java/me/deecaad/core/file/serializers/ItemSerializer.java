package me.deecaad.core.file.serializers;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import me.deecaad.core.MechanicsCore;
import me.deecaad.core.compatibility.CompatibilityAPI;
import me.deecaad.core.compatibility.nbt.NBTCompatibility;
import me.deecaad.core.file.*;
import me.deecaad.core.utils.AttributeType;
import me.deecaad.core.utils.EnumUtil;
import me.deecaad.core.utils.ReflectionUtil;
import me.deecaad.core.utils.StringUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.*;
import org.bukkit.block.data.Levelled;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.*;
import org.bukkit.inventory.meta.trim.ArmorTrim;
import org.bukkit.inventory.meta.trim.TrimMaterial;
import org.bukkit.inventory.meta.trim.TrimPattern;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class ItemSerializer implements Serializer<ItemStack> {

    public static final Map<String, Supplier<ItemStack>> ITEM_REGISTRY = new HashMap<>();

    /**
     * Reflection support for versions before 1.11 when setting unbreakable tag
     */
    private static Method spigotMethod;
    private static Method setUnbreakable;

    // 1.16+ use adventure in item lore and display name (hex code support)
    private static Field loreField;
    private static Field displayField;

    private static final Field ingredientsField;

    static {
        ingredientsField = ReflectionUtil.getField(ShapedRecipe.class, "ingredients");

        if (ReflectionUtil.getMCVersion() >= 16) {
            Class<?> c = ReflectionUtil.getCBClass("inventory.CraftMetaItem");
            loreField = ReflectionUtil.getField(c, "lore");
            displayField = ReflectionUtil.getField(c, "displayName");
        }
    }

    /**
     * Empty constructor to be used as serializer
     */
    public ItemSerializer() {
    }

    @Override
    @NotNull
    public ItemStack serialize(@NotNull SerializeData data) throws SerializerException {
        return serializeWithTags(data, Collections.emptyMap());
    }

    public ItemStack serializeWithTags(@NotNull SerializeData data, @NotNull Map<String, Object> tags) throws SerializerException {

        // When the key is null, that probably means we are currently in an
        // inline serializer. Skip the fancy shit.
        if (data.key == null) {
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

    public ItemStack attemptInline(@NotNull SerializeData data) throws SerializerException {
        try {

            // Check the ITEM_REGISTRY to see if they are trying to inline
            // an item... Like pathto in serializers but easier.
            String registry = data.of().assertType(String.class).assertExists().get();
            if (ITEM_REGISTRY.containsKey(registry))
                return ITEM_REGISTRY.get(registry).get();

            // Support for one-liner item serializer
            Material type = data.of().assertType(String.class).assertExists().getEnum(Material.class);
            if (type != null)
                return new ItemStack(type);

        } catch (SerializerTypeException ex) {
            // We only catch **TYPE** exceptions, since when this element is
            // NOT a 1 liner, the type exception will be thrown.
        } catch (SerializerEnumException ex) {
            // We catch the ENUM exception since we want to compare it against
            // the ITEM_REGISTRY as well. For example, steel_sheet from the
            // registry should be included as a valid option.
            Collection<String> options = ex.getOptions();
            options.addAll(ITEM_REGISTRY.keySet());
            throw new SerializerOptionsException(ex.getSerializerName(), "Material", options, ex.getActual(), data.of().getLocation())
                    .addMessage("https://github.com/WeaponMechanics/MechanicsMain/wiki/References#materials");
        }

        return null;
    }

    public ItemStack serializeWithoutRecipe(@NotNull SerializeData data) throws SerializerException {

        // TODO Add byte data support using 'Data:' or 'Extra_Data:' key
        Material type = data.of("Type").assertExists().getEnum(Material.class);
        ItemStack itemStack = new ItemStack(type);

        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta == null) {
            throw data.exception("Type", "Did you use air as a material? This is not allowed!",
                    SerializerException.forValue(type));
        }

        String name = data.of("Name").getAdventure(null);
        if (name != null) {
            Component component = MechanicsCore.getPlugin().message.deserialize("<!italic>" + name);

            if (ReflectionUtil.getMCVersion() < 16) {
                String element = LegacyComponentSerializer.legacySection().serialize(component);
                itemMeta.setDisplayName(element);
            } else {
                String display = GsonComponentSerializer.gson().serialize(component);
                ReflectionUtil.setField(displayField, itemMeta, display);
            }
        }

        List<?> lore = data.of("Lore").assertType(List.class).get(null);
        if (lore != null && !lore.isEmpty()) {
            List<String> temp = new ArrayList<>(lore.size());

            for (Object obj : lore) {
                Component component = MechanicsCore.getPlugin().message.deserialize("<!italic>" + StringUtil.colorAdventure(obj.toString()));
                String element = ReflectionUtil.getMCVersion() < 16 ? LegacyComponentSerializer.legacySection().serialize(component) : GsonComponentSerializer.gson().serialize(component);
                temp.add(element);
            }

            if (ReflectionUtil.getMCVersion() < 16)
                itemMeta.setLore(temp);
            else
                ReflectionUtil.setField(loreField, itemMeta, temp);
            //ReflectionUtil.invokeMethod(safelyAdd, null, ReflectionUtil.invokeField(loreField, itemMeta), temp, true);
        }

        short durability = (short) data.of("Durability").assertPositive().getInt(-99);
        if (durability != -99) {
            if (CompatibilityAPI.getVersion() >= 1.132) {
                ((org.bukkit.inventory.meta.Damageable) itemMeta).setDamage(durability);
            } else {
                itemStack.setDurability(durability);
            }
        }

        boolean unbreakable = data.of("Unbreakable").getBool(false);
        if (CompatibilityAPI.getVersion() >= 1.11) {
            itemMeta.setUnbreakable(unbreakable);
        } else {
            setupUnbreakable();
            ReflectionUtil.invokeMethod(setUnbreakable, ReflectionUtil.invokeMethod(spigotMethod, itemMeta), true);
        }

        if (data.has("Custom_Model_Data") && CompatibilityAPI.getVersion() >= 1.14) {
            itemMeta.setCustomModelData(data.of("Custom_Model_Data").assertExists().getInt());
        }

        boolean hideFlags = data.of("Hide_Flags").getBool(false);
        if (hideFlags) {
            itemMeta.addItemFlags(ItemFlag.values());
        }

        List<String[]> enchantments = data.ofList("Enchantments")
                .addArgument(Enchantment.class, true, true)
                .addArgument(int.class, true)
                .assertList().get();


        if (enchantments != null) {
            for (String[] split : enchantments) {
                Enchantment enchant;
                if (CompatibilityAPI.getVersion() < 1.13) {
                    enchant = Enchantment.getByName(split[0].trim().toLowerCase(Locale.ROOT));
                } else {
                    enchant = Enchantment.getByKey(org.bukkit.NamespacedKey.minecraft(split[0].trim().toLowerCase(Locale.ROOT)));
                }
                if (enchant == null) {
                    throw new SerializerOptionsException("Item", "Enchantment",
                            Arrays.stream(Enchantment.values()).map(ench -> ReflectionUtil.getMCVersion() < 13 ? ench.getName() : ench.getKey().getKey()).collect(Collectors.toList()),
                            split[0], data.of("Enchantments").getLocation());
                }
                int enchantmentLevel = Integer.parseInt(split[1]);
                itemMeta.addEnchant(enchant, enchantmentLevel - 1, true);
            }
        }

        itemStack.setItemMeta(itemMeta);

        // #198
        data.ofList("Tags")
                .addArgument(String.class, true)
                .addArgument(int.class, true)
                .assertList().stream().forEach(split -> {
                    CompatibilityAPI.getNBTCompatibility().setInt(itemStack, "Custom", split[0], Integer.parseInt(split[1]));
                });

        List<String[]> attributes = data.ofList("Attributes")
                .addArgument(AttributeType.class, true)
                .addArgument(double.class, true)
                .addArgument(NBTCompatibility.AttributeSlot.class, false)
                .assertList().get();

        if (attributes != null) {
            for (String[] split : attributes) {

                AttributeType attribute = EnumUtil.parseEnums(AttributeType.class, split[0]).get(0);
                NBTCompatibility.AttributeSlot slot = split.length > 2 ? EnumUtil.parseEnums(NBTCompatibility.AttributeSlot.class, split[2]).get(0) : null;
                double amount = Double.parseDouble(split[1]);

                CompatibilityAPI.getNBTCompatibility().setAttribute(itemStack, attribute, slot, amount);
            }
        }

        String owningPlayer = data.of("Skull_Owning_Player").assertType(String.class).get(null);
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
                // "970e0a59-b95d-45a9-9039-b43ac4fbfc7c https://textures.minecraft.net/texture/a0564817fcc8dd51bc1957c0b7ea142db687dd6f1caafd35bb4dcfee592421c"
                // https://www.spigotmc.org/threads/create-a-skull-item-stack-with-a-custom-texture-base64.82416/
                if (uuid != null && url != null) {
                    GameProfile dummy = new GameProfile(uuid, "ArmorMechanicsSkull");
                    byte[] encoded = Base64.getEncoder().encode(String.format("{textures:{SKIN:{url:\"%s\"}}}", url).getBytes());
                    dummy.getProperties().put("textures", new Property("textures", new String(encoded)));

                    Field field = ReflectionUtil.getField(skullMeta.getClass(), GameProfile.class);
                    ReflectionUtil.setField(field, skullMeta, dummy);
                }

                // Standard player name SkullMeta... "CJCrafter", "DeeCaaD", "Darkman_Bree"
                else if (uuid != null) {
                    if (CompatibilityAPI.getVersion() >= 1.12) {
                        skullMeta.setOwningPlayer(Bukkit.getServer().getOfflinePlayer(uuid));
                    } else {
                        skullMeta.setOwner(Bukkit.getServer().getOfflinePlayer(uuid).getName());
                    }
                } else {
                    skullMeta.setOwner(owningPlayer);
                }
                itemStack.setItemMeta(skullMeta);
            } catch (ClassCastException e) {
                throw data.exception("Skull_Owning_Player", "Tried to use Skulls when the item wasn't a player head!",
                        SerializerException.forValue(type));
            }
        }

        if (ReflectionUtil.getMCVersion() >= 11 && data.has("Potion_Color")) {
            try {
                Color color = data.of("Potion_Color").assertExists().serialize(new ColorSerializer()).getColor();
                PotionMeta potionMeta = (PotionMeta) itemStack.getItemMeta();
                potionMeta.setColor(color);
                itemStack.setItemMeta(potionMeta);
            } catch (ClassCastException e) {
                throw data.exception("Potion_Color", "Tried to use Potion Color when the item wasn't a potion!",
                        SerializerException.forValue(type));
            }
        }

        if (data.has("Leather_Color")) {
            try {
                Color color = data.of("Leather_Color").assertExists().serialize(new ColorSerializer()).getColor();
                LeatherArmorMeta meta = (LeatherArmorMeta) itemStack.getItemMeta();
                meta.setColor(color);
                itemStack.setItemMeta(meta);
            } catch (ClassCastException e) {
                throw data.exception("Leather_Color", "Tried to use Leather Color when the item wasn't leather armor!",
                        SerializerException.forValue(type));
            }
        }

        if (ReflectionUtil.getMCVersion() >= 20 && itemStack.getItemMeta() instanceof ArmorMeta armor) {

            // If you have one, you NEED both
            boolean hasOneOfPatternOrMaterial = data.has("Trim_Pattern") || data.has("Trim_Material");

            TrimPattern pattern = data.of("Trim_Pattern").assertExists(hasOneOfPatternOrMaterial).getKeyed(Registry.TRIM_PATTERN, null);
            TrimMaterial material = data.of("Trim_Material").assertExists(hasOneOfPatternOrMaterial).getKeyed(Registry.TRIM_MATERIAL, null);

            if (pattern != null && material != null) {
                armor.setTrim(new ArmorTrim(material, pattern));
                itemStack.setItemMeta(armor);
            }
        }

        if (data.has("Firework")) {

            try {
                // <FireworkEffect.Type>-<Color>-<Boolean=Trail>-<Boolean=Flicker>-<Color=Fade>
                List<String[]> list = data.ofList("Firework.Effects")
                        .addArgument(FireworkEffect.Type.class, true)
                        .addArgument(ColorSerializer.class, true, true)
                        .addArgument(boolean.class, false)
                        .addArgument(boolean.class, false)
                        .addArgument(ColorSerializer.class, false)
                        .assertExists().assertList().get();

                FireworkMeta meta = (FireworkMeta) itemMeta;
                meta.setPower(data.of("Firework.Power").assertPositive().getInt(1));
                for (String[] split : list) {

                    FireworkEffect.Builder builder = FireworkEffect.builder();
                    builder.with(FireworkEffect.Type.valueOf(split[0]));

                    // Handle initial colors
                    String[] colors = split[1].split(", ?");
                    for (String color : colors)
                        builder.withColor(ColorSerializer.fromString(data.move("Firework.Effects"), color));

                    builder.trail(split.length > 2 && split[2].equalsIgnoreCase("true"));
                    builder.flicker(split.length > 3 && split[3].equalsIgnoreCase("true"));

                    // Handle the fade colors
                    String[] fadeColors = split.length > 4 ? split[4].split(", ?") : new String[0];
                    for (String color : fadeColors)
                        builder.withFade(ColorSerializer.fromString(data.move("Firework.Effects"), color));

                    // Add the newly constructed firework effect to the list.
                    meta.addEffect(builder.build());
                }
                itemStack.setItemMeta(meta);
            } catch (ClassCastException ex) {
                throw data.exception("Firework", "Tried to use Firework when the item wasn't a firework rocket!",
                        SerializerException.forValue(type));
            }
        }

        if (data.has("Light_Level")) {
            if (ReflectionUtil.getMCVersion() < 17) {
                throw data.exception("Light_Level", "Tried to use light level before MC 1.17!",
                        "The light block was added in Minecraft version 1.17!");
            }

            try {
                BlockDataMeta meta = (BlockDataMeta) itemStack.getItemMeta();
                Levelled levelled = (Levelled) meta.getBlockData(Material.LIGHT);
                int level = data.of("Light_Level").assertRange(0, levelled.getMaximumLevel()).getInt(0);
                levelled.setLevel(level);
                meta.setBlockData(levelled);
                itemStack.setItemMeta(meta);
            } catch (ClassCastException ex) {
                throw data.exception("Light_Level", "Tried to use the Light_Level option on a non 'LIGHT' block");
            }
        }

        if (data.of("Deny_Use_In_Crafting").getBool(false)) {
            CompatibilityAPI.getNBTCompatibility().setInt(itemStack, "MechanicsCore", "deny-crafting", 1);
        }

        return itemStack;
    }

    public ItemStack serializeRecipe(SerializeData data, ItemStack itemStack) throws SerializerException {
        if (!data.has("Recipe"))
            return itemStack;

        // Create a copy of the item with a set amount to be the result of the
        // crafting recipe.
        int resultAmount = data.of("Recipe.Output_Amount").assertRange(1, 64).getInt(1);
        ItemStack result = itemStack.clone();
        result.setAmount(resultAmount);

        // Namespaced keys for recipes were added in MC 1.12
        ShapedRecipe recipe;
        if (ReflectionUtil.getMCVersion() < 12) {
            recipe = new ShapedRecipe(result);
        } else {
            recipe = new ShapedRecipe(new NamespacedKey(MechanicsCore.getPlugin(), data.key), result);

            // Bukkit.getRecipe was added in 1.16. We have a try-catch block
            // below in this method to handle 1.12 through 1.15
            if (ReflectionUtil.getMCVersion() >= 16 && Bukkit.getRecipe(recipe.getKey()) != null) {
                return itemStack;
            }
        }

        // The Recipe.Shape should be a list looking similar to:
        //   - ABC
        //   - DEF
        //   - GHI
        List<Object> shape = data.of("Recipe.Shape").assertExists().assertType(List.class).get();
        String[] shapeArr = shape.stream().map(Object::toString).toArray(String[]::new);
        if (shape.size() < 1 || shape.size() > 3)
            throw new SerializerRangeException(this, 1, shape.size(), 3, data.of("Recipe.Shape").getLocation());

        Set<Character> ingredientChars = new HashSet<>();
        for (int i = 0; i < shapeArr.length; i++) {
            String str = shapeArr[i];

            // Before we get mad at the user, let's try removing trailing whitespace
            if (str.length() > 3)
                shapeArr[i] = str = str.trim();

            if (str.length() < 1 || str.length() > 3)
                throw new SerializerRangeException(this, 1, shape.size(), 3, data.of("Recipe.Shape").getLocation());

            for (char c : str.toCharArray())
                ingredientChars.add(c);
        }

        // Set shape AFTER our checks above, so we can be more verbose to
        // the user and try to correct their mistakes automatically.
        try {
            recipe.shape(shapeArr);
        } catch (Throwable ex) {
            throw data.exception("Recipe.Shape", "Recipe Shape was formatted incorrectly",
                    ex.getMessage(), SerializerException.forValue(shape));
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

            ItemStack item = data.of("Recipe.Ingredients." + c).assertExists().serialize(new ItemSerializer());

            if (CompatibilityAPI.getVersion() < 1.13)
                ingredients.put(c, item);
            else
                ingredients.put(c, new RecipeChoice.ExactChoice(item));
        }

        // Finalize and register the new recipe.
        ReflectionUtil.setField(ingredientsField, recipe, ingredients);
        try {
            Bukkit.addRecipe(recipe);
        } catch (IllegalStateException ex) {
            // rethrow if we don't know where this error came from
            if (!ex.getMessage().startsWith("Duplicate recipe ignored with ID mechanicscore:"))
                throw ex;
        }
        return itemStack;
    }

    /**
     * Fills the required channel methods, fields and constructors.
     */
    private void setupUnbreakable() {
        if (spigotMethod == null) {
            try {
                spigotMethod = ReflectionUtil.getMethod(Class.forName("org.bukkit.inventory.meta.ItemMeta"), "spigot");
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        if (setUnbreakable == null) {
            try {
                setUnbreakable = ReflectionUtil.getMethod(Class.forName("org.bukkit.inventory.meta.ItemMeta$Spigot"), "setUnbreakable", boolean.class);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }
}
