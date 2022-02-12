package me.deecaad.core.file.serializers;

import me.deecaad.core.MechanicsCore;
import me.deecaad.core.compatibility.CompatibilityAPI;
import me.deecaad.core.compatibility.nbt.NBTCompatibility;
import me.deecaad.core.file.SerializeData;
import me.deecaad.core.file.Serializer;
import me.deecaad.core.file.SerializerException;
import me.deecaad.core.file.SerializerOptionsException;
import me.deecaad.core.file.SerializerRangeException;
import me.deecaad.core.utils.*;
import org.bukkit.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.*;

import javax.annotation.Nonnull;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

public class ItemSerializer implements Serializer<ItemStack> {

    /**
     * Reflection support for versions before 1.11 when setting unbreakable tag
     */
    private static Method spigotMethod;
    private static Method setUnbreakable;

    private static final Field ingredientsField;

    static {
        ingredientsField = ReflectionUtil.getField(ShapedRecipe.class, "ingredients");
    }

    /**
     * Empty constructor to be used as serializer
     */
    public ItemSerializer() {}

    @Override
    public String getKeyword() {
        return "Item";
    }

    @Override
    @Nonnull
    public ItemStack serialize(SerializeData data) throws SerializerException {
        ItemStack itemStack = serializeWithoutRecipe(data);
        itemStack = serializeRecipe(data, itemStack);
        return itemStack;
    }

    public ItemStack serializeWithoutRecipe(SerializeData data) throws SerializerException {

        // TODO Add byte data support using 'Data:' or 'Extra_Data:' key
        Material type = data.of("Type").assertExists().getEnum(Material.class);
        ItemStack itemStack = new ItemStack(type);

        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta == null) {
            throw data.exception("Type", "Did you use air as a material? This is not allowed!",
                    SerializerException.forValue(type));
        }

        String name = data.of("Name").assertType(String.class).get(null);
        if (name != null)
            itemMeta.setDisplayName(StringUtil.color(name));

        List<?> lore = data.of("Lore").assertType(List.class).get(null);
        if (lore != null && !lore.isEmpty()) {
            itemMeta.setLore(convertListObject(lore));
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

        int customModelData = data.of("Custom_Model_Data").assertPositive().getInt(-99);
        if (customModelData != -99 && CompatibilityAPI.getVersion() >= 1.14) {
            itemMeta.setCustomModelData(customModelData);
        }

        boolean hideFlags = data.of("Hide_Flags").getBool(false);
        if (hideFlags) {
            itemMeta.addItemFlags(ItemFlag.values());
        }

        List<String[]> enchantments = data.ofList("Enchantments")
                .addArgument(Enchantment.class, true, true)
                .addArgument(int.class, true)
                .get();


        if (enchantments != null) {
            for (String[] split : enchantments) {
                Enchantment enchant;
                if (CompatibilityAPI.getVersion() < 1.13) {
                    enchant = Enchantment.getByName(split[0]);
                } else {
                    enchant = Enchantment.getByKey(org.bukkit.NamespacedKey.minecraft(split[0]));
                }
                if (enchant == null) {
                    throw new SerializerOptionsException("Item", "Enchantment",
                            Arrays.stream(Enchantment.values()).map(Enchantment::getName).collect(Collectors.toList()),
                            split[0], data.of("Enchantments").getLocation());
                }
                int enchantmentLevel = Integer.parseInt(split[1]);
                itemMeta.addEnchant(enchant, enchantmentLevel - 1, true);
            }
        }

        itemStack.setItemMeta(itemMeta);

        List<String[]> attributes = data.ofList("Attributes")
                .addArgument(AttributeType.class, true)
                .addArgument(double.class, true)
                .addArgument(AttributeType.class, false)
                .get();

        if (attributes != null) {
            for (String[] split : attributes) {

                List<AttributeType> attributeTypes = EnumUtil.parseEnums(AttributeType.class, split[0]);
                List<NBTCompatibility.AttributeSlot> attributeSlots = split.length > 2 ? EnumUtil.parseEnums(NBTCompatibility.AttributeSlot.class, split[2]) : null;
                double amount = Double.parseDouble(split[1]);

                for (AttributeType attribute : attributeTypes) {
                    if (attributeSlots == null) {
                        CompatibilityAPI.getNBTCompatibility().setAttribute(itemStack, attribute, null, amount);
                        continue;
                    }

                    for (NBTCompatibility.AttributeSlot slot : attributeSlots) {
                        CompatibilityAPI.getNBTCompatibility().setAttribute(itemStack, attribute, slot, amount);
                    }
                }
            }
        }

        String owningPlayer = data.of("Skull_Owning_Player").assertType(String.class).get(null);
        if (owningPlayer != null) {
            try {
                SkullMeta skullMeta = (SkullMeta) itemStack.getItemMeta();
                UUID uuid;
                try {
                    uuid = UUID.fromString(owningPlayer);
                } catch (IllegalArgumentException e) {
                    uuid = null;
                }
                if (uuid != null) {
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

        if (CompatibilityAPI.getVersion() >= 1.11 && data.config.contains(data.key + ".Potion_Color")) {
            try {
                Color color = data.of("Potion_Color").serializeNonStandardSerializer(new ColorSerializer());
                PotionMeta potionMeta = (PotionMeta) itemStack.getItemMeta();
                potionMeta.setColor(color);
                itemStack.setItemMeta(potionMeta);
            } catch (ClassCastException e) {
                throw data.exception("Potion_Color", "Tried to use Potion Color when the item wasn't a potion!",
                        SerializerException.forValue(type));
            }
        }
        if (data.config.contains(data.key + ".Leather_Color")) {
            try {
                Color color = data.of("Leather_Color").serializeNonStandardSerializer(new ColorSerializer());
                LeatherArmorMeta meta = (LeatherArmorMeta) itemStack.getItemMeta();
                meta.setColor(color);
                itemStack.setItemMeta(meta);
            } catch (ClassCastException e) {
                throw data.exception("Leather_Color", "Tried to use Leather Color when the item wasn't leather armor!",
                        SerializerException.forValue(type));
            }
        }

        if (data.config.contains(data.key + ".Firework")) {

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
                    builder.withColor(new ColorSerializer().fromString(data.move("Firework.Effects"), color));

                builder.trail(split.length > 2 && split[2].equalsIgnoreCase("true"));
                builder.flicker(split.length > 3 && split[3].equalsIgnoreCase("true"));

                // Handle the fade colors
                String[] fadeColors = split.length > 4 ? split[4].split(", ?") : new String[0];
                for (String color : fadeColors)
                    builder.withFade(new ColorSerializer().fromString(data.move("Firework.Effects"), color));

                // Add the newly constructed firework effect to the list.
                meta.addEffect(builder.build());
            }
            itemStack.setItemMeta(meta);
        }

        if (data.of("Deny_Use_In_Crafting").getBool(false)) {
            CompatibilityAPI.getNBTCompatibility().setInt(itemStack, "MechanicsCore", "deny-crafting", 1);
        }

        return itemStack;
    }

    public ItemStack serializeRecipe(SerializeData data, ItemStack itemStack) throws SerializerException {
        if (data.config.contains(data.key + ".Recipe")) {
            ShapedRecipe recipe;
            if (CompatibilityAPI.getVersion() < 1.13) {
                recipe = new ShapedRecipe(itemStack);
            } else {
                recipe = new ShapedRecipe(new NamespacedKey(MechanicsCore.getPlugin(), data.key), itemStack);
            }

            // The Recipe.Shape should be a list looking similar to:
            //   - ABC
            //   - DEF
            //   - GHI
            List<Object> shape = data.of("Recipe.Shape").assertExists().assertType(List.class).get();
            if (shape.size() < 1 || shape.size() > 3)
                throw new SerializerRangeException(this, 1, shape.size(), 3,  data.of("Recipe.Shape").getLocation());

            recipe.shape(shape.stream().map(Object::toString).toArray(String[]::new));

            Set<Character> ingredientChars = new HashSet<>();
            for (String str : recipe.getShape()) {
                if (str.length() < 1 || str.length() > 3)
                    throw new SerializerRangeException(this, 1, shape.size(), 3,  data.of("Recipe.Shape").getLocation());

                for (char c : str.toCharArray())
                    ingredientChars.add(c);
            }

            // Shaped recipes in 1.12 and lower just use a map of
            // characters and ItemStacks. In 1.13 and higher, recipes
            // use Characters and RecipeChoices.
            final Map<Character, Object> ingredients = new HashMap<>();
            data.of("Recipe.Ingredients").assertExists().assertType(ConfigurationSection.class);
            for (char c : ingredientChars) {
                ItemStack item = data.of("Recipe.Ingredients." + c).assertExists().serializeNonStandardSerializer(this);

                if (CompatibilityAPI.getVersion() < 1.13)
                    ingredients.put(c, item);
                else
                    ingredients.put(c, new RecipeChoice.ExactChoice(item));
            }

            // Finalize and register the new recipe.
            ReflectionUtil.setField(ingredientsField, recipe, ingredients);
            Bukkit.addRecipe(recipe);
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

    private List<String> convertListObject(Object object) {
        List<String> list = new ArrayList<>();
        for (Object obj : (List<?>) object) {
            list.add(StringUtil.color(obj.toString()));
        }
        return list;
    }
}