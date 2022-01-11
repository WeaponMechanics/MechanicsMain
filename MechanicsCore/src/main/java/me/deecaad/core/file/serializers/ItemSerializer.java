package me.deecaad.core.file.serializers;

import me.deecaad.core.MechanicsCore;
import me.deecaad.core.compatibility.CompatibilityAPI;
import me.deecaad.core.compatibility.nbt.NBTCompatibility;
import me.deecaad.core.file.Serializer;
import me.deecaad.core.utils.*;
import org.bukkit.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.*;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

import static me.deecaad.core.MechanicsCore.debug;

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
    public ItemStack serialize(File file, ConfigurationSection configurationSection, String path) {
        ItemStack itemStack = serializeWithoutRecipe(file, configurationSection, path);
        itemStack = serializeRecipe(file, configurationSection, path, itemStack);
        return itemStack;
    }

    public ItemStack serializeWithoutRecipe(File file, ConfigurationSection configurationSection, String path) {
        String type = configurationSection.getString(path + ".Type");
        if (type == null) {
            return null;
        }
        type = type.toUpperCase();
        ItemStack itemStack;
        try {
            itemStack = MaterialUtil.fromStringToItemStack(type);
        } catch (IllegalArgumentException e) {
            debug.log(LogLevel.ERROR,
                    StringUtil.foundInvalid("material"),
                    StringUtil.foundAt(file, path + ".Type", type),
                    StringUtil.debugDidYouMean(type.split(":")[0], Material.class));
            return null;
        }
        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta == null) {
            debug.log(LogLevel.ERROR,
                    StringUtil.foundInvalid("material"),
                    StringUtil.foundAt(file, path + ".Type", type),
                    "Tried to use material which doesn't have item meta.");
            return null;
        }

        String name = configurationSection.getString(path + ".Name");
        if (name != null) {
            itemMeta.setDisplayName(StringUtil.color(name));
        }
        List<?> lore = configurationSection.getList(path + ".Lore");
        if (lore != null && !lore.isEmpty()) {
            itemMeta.setLore(convertListObject(lore));
        }
        short durability = (short) configurationSection.getInt(path + ".Durability", -99);
        if (durability != -99) {
            if (CompatibilityAPI.getVersion() >= 1.132) {
                ((org.bukkit.inventory.meta.Damageable) itemMeta).setDamage(durability);
            } else {
                itemStack.setDurability(durability);
            }
        }
        boolean unbreakable = configurationSection.getBoolean(path + ".Unbreakable", false);
        if (CompatibilityAPI.getVersion() >= 1.11) {
            itemMeta.setUnbreakable(unbreakable);
        } else {
            setupUnbreakable();
            ReflectionUtil.invokeMethod(setUnbreakable, ReflectionUtil.invokeMethod(spigotMethod, itemMeta), true);
        }
        int customModelData = configurationSection.getInt(path + ".Custom_Model_Data", -99);
        if (customModelData != -99 && CompatibilityAPI.getVersion() >= 1.14) {
            itemMeta.setCustomModelData(customModelData);
        }
        boolean hideFlags = configurationSection.getBoolean(path + ".Hide_Flags", false);
        if (hideFlags) {
            itemMeta.addItemFlags(ItemFlag.values());
        }
        List<?> enchantments = configurationSection.getList(path + ".Enchantments");
        if (enchantments != null) {
            for (Object enchantment : enchantments) {
                String[] splitted = StringUtil.split(enchantment.toString());
                Enchantment enchant;
                if (CompatibilityAPI.getVersion() < 1.13) {
                    enchant = Enchantment.getByName(splitted[0]);
                } else {
                    enchant = Enchantment.getByKey(org.bukkit.NamespacedKey.minecraft(splitted[0]));
                }
                if (enchant == null) {
                    debug.log(LogLevel.ERROR,
                            "Found an invalid enchantment in configurations!",
                            "Located at file " + file + " in " + path + ".Enchantments (" + splitted[0] + ") in configurations");
                    continue;
                }
                int enchantmentLevel = splitted.length > 1 ? Integer.parseInt(splitted[1]) : 1;
                itemMeta.addEnchant(enchant, enchantmentLevel - 1, true);
            }
        }

        itemStack.setItemMeta(itemMeta);

        List<?> attributes = configurationSection.getList(path + ".Attributes");
        if (attributes != null) {
            for (Object attributeData : attributes) {
                String[] split = StringUtil.split(attributeData.toString());
                if (split.length < 2) {
                    debug.log(LogLevel.ERROR,
                            "Found an invalid configuration format!",
                            StringUtil.foundAt(file, path + ".Attributes", attributeData),
                            "Please use the format: <Attribute>-<Value>-<Slot>. Slot is optional.");
                    continue;
                }
                AttributeType attribute;
                NBTCompatibility.AttributeSlot slot;
                double amount;

                Optional<AttributeType> attributeOptional = EnumUtil.getIfPresent(AttributeType.class, split[0]);
                if (!attributeOptional.isPresent()) {
                    debug.error("Found an invalid Attribute Type in configurations!",
                            StringUtil.foundAt(file, path + ".Attributes", split[0]),
                            StringUtil.debugDidYouMean(split[0], AttributeType.class));
                    continue;
                }
                attribute = attributeOptional.get();

                if (split.length >= 3) {
                    Optional<NBTCompatibility.AttributeSlot> slotOptional =
                            EnumUtil.getIfPresent(NBTCompatibility.AttributeSlot.class, split[2]);
                    if (!slotOptional.isPresent()) {
                        debug.error("Found an invalid Attribute Slot in configurations!",
                                StringUtil.foundAt(file, path + ".Attributes", split[2]),
                                StringUtil.debugDidYouMean(split[2], NBTCompatibility.AttributeSlot.class));

                        continue;
                    }
                    slot = slotOptional.get();
                } else {
                    slot = null;
                }

                try {
                    amount = Double.parseDouble(split[1]);
                } catch (NumberFormatException e) {
                    debug.error("Invalid number format for Attribute Amount in configurations! Make sure there are not extra characters/symbols",
                            StringUtil.foundAt(file, path + ".Attributes", split[1]));
                    continue;
                }

                CompatibilityAPI.getNBTCompatibility().setAttribute(itemStack, attribute, slot, amount);
            }
        }

        String owningPlayer = configurationSection.getString(path + ".Skull_Owning_Player");
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
                debug.log(LogLevel.ERROR, StringUtil.foundInvalid("cast"), StringUtil.foundAt(file, path + ".Skull_Owning_Player",
                        "Tried to modify skull meta when the item wasn't skull (" + type + ")"));
                return null;
            }
        }
        if (CompatibilityAPI.getVersion() >= 1.11 && configurationSection.contains(path + ".Potion_Color")) {
            try {
                Color color = new ColorSerializer().serialize(file, configurationSection, path + ".Potion_Color");
                if (color == null) {
                    debug.warn("Error occurred while serializing Color", StringUtil.foundAt(file, path + ".Potion_Color"));
                    return null;
                }
                PotionMeta potionMeta = (PotionMeta) itemStack.getItemMeta();
                potionMeta.setColor(color);
                itemStack.setItemMeta(potionMeta);
            } catch (ClassCastException e) {
                debug.log(LogLevel.ERROR, StringUtil.foundInvalid("cast"), StringUtil.foundAt(file, path + ".Potion_Color",
                        "Tried to modify potion meta when the item wasn't potion (" + type + ")"));
                return null;
            }
        }
        if (configurationSection.contains(path + ".Leather_Color")) {
            try {
                Color color = new ColorSerializer().serialize(file, configurationSection, path + ".Leather_Color");
                if (color == null) {
                    debug.warn("Error occurred while serializing Color", StringUtil.foundAt(file, path + ".Leather_Color"));
                    return null;
                }
                LeatherArmorMeta meta = (LeatherArmorMeta) itemStack.getItemMeta();
                meta.setColor(color);
                itemStack.setItemMeta(meta);
            } catch (ClassCastException e) {
                debug.log(LogLevel.ERROR, StringUtil.foundInvalid("cast"), StringUtil.foundAt(file, path + ".Leather_Color",
                        "Tried to modify leather armor meta when the item wasn't leather armor (" + type + ")"));
                return null;
            }
        }

        if (configurationSection.contains(path + ".Firework")) {
            try {
                FireworkMeta meta = (FireworkMeta) itemStack.getItemMeta();
                meta.setPower(configurationSection.getInt(path + ".Firework.Power", 1));

                List<?> effects = configurationSection.getList(path + ".Firework.Effects");
                if (effects == null) {
                    debug.log(LogLevel.ERROR, "Firework didn't have any effects defined.", StringUtil.foundAt(file, path + ".Firework.Effects"));
                    return null;
                }

                for (Object effectData : effects) {
                    String[] split = StringUtil.split(effectData.toString());
                    FireworkEffect.Builder effectBuilder = FireworkEffect.builder();

                    if (split.length < 2) {
                        debug.log(LogLevel.ERROR, "Firework effect requires at least type and color.", StringUtil.foundAt(file, path + ".Firework.Effects", effectData));
                        return null;
                    }

                    try {
                        effectBuilder.with(FireworkEffect.Type.valueOf(split[0].toUpperCase()));
                    } catch (IllegalArgumentException e) {
                        debug.log(LogLevel.ERROR,
                                StringUtil.foundInvalid("firework type"),
                                StringUtil.foundAt(file, path + ".Firework.Effects", split[0]),
                                StringUtil.debugDidYouMean(split[0].toUpperCase(), FireworkEffect.Type.class));
                        return null;
                    }

                    ColorSerializer colorSerializer = new ColorSerializer();

                    Color color = colorSerializer.fromString(file, configurationSection, path, split[1]);
                    if (color == null) {
                        debug.log(LogLevel.ERROR,
                                StringUtil.foundInvalid("color"),
                                StringUtil.foundAt(file, path + ".Firework.Effects", split[1]));
                        return null;
                    }
                    effectBuilder.withColor(color);

                    if (split.length >= 4) {
                        effectBuilder.trail(Boolean.parseBoolean(split[2]));
                        effectBuilder.flicker(Boolean.parseBoolean(split[3]));
                    }

                    if (split.length > 4) {
                        Color fadeColor = colorSerializer.fromString(file, configurationSection, path, split[4]);
                        if (fadeColor == null) {
                            debug.log(LogLevel.ERROR,
                                    StringUtil.foundInvalid("fade color"),
                                    StringUtil.foundAt(file, path + ".Firework.Effects", split[4]));
                            return null;
                        }
                        effectBuilder.withFade(color);
                    }

                    meta.addEffect(effectBuilder.build());
                }

                if (meta.getEffectsSize() == 0) {
                    debug.log(LogLevel.ERROR, "Firework effects are empty?", StringUtil.foundAt(file, path + ".Firework.Effects"));
                    return null;
                }

                itemStack.setItemMeta(meta);

            } catch (ClassCastException e) {
                debug.log(LogLevel.ERROR, StringUtil.foundInvalid("cast"), StringUtil.foundAt(file, path + ".Firework",
                        "Tried to modify firework meta when the item wasn't leather armor (" + type + ")"));
                return null;
            }
        }

        if (configurationSection.getBoolean(path + ".Deny_Use_In_Crafting")) {
            CompatibilityAPI.getNBTCompatibility().setInt(itemStack, "MechanicsCore", "deny-crafting", 1);
        }

        return itemStack;
    }

    public ItemStack serializeRecipe(File file, ConfigurationSection configurationSection, String path, ItemStack itemStack) {
        if (configurationSection.contains(path + ".Recipe")) {
            ShapedRecipe recipe;
            if (CompatibilityAPI.getVersion() < 1.13) {
                recipe = new ShapedRecipe(itemStack);
            } else {
                recipe = new ShapedRecipe(new NamespacedKey(MechanicsCore.getPlugin(), path), itemStack);
            }

            if (!configurationSection.contains(path + ".Recipe.Shape")) {
                debug.error("You forgot to specify a shape for your item recipe!", StringUtil.foundAt(file, path + ".Recipe.Shape"));
                return null;
            }

            String[] shape = configurationSection.getStringList(path + ".Recipe.Shape").toArray(new String[0]);
            if (shape.length < 1 || shape.length > 3) {
                debug.error("Your recipe shape must have between 1 and 3 rows! Found " + shape.length,
                        StringUtil.foundAt(file, path + ".Recipe.Shape"));
                return null;
            }

            recipe.shape(shape);

            // Shaped recipes in 1.12 and lower just use a map of
            // characters and ItemStacks. In 1.13 and higher, recipes
            // use Characters and RecipeChoices.
            final Map<Character, Object> ingredients = new HashMap<>();
            ConfigurationSection config = configurationSection.getConfigurationSection(path + ".Recipe.Ingredients");

            if (config == null) {
                debug.error("You need to specify ingredients for your recipe", StringUtil.foundAt(file, path + ".Recipe.Ingredients"));
                return null;
            }

            for (String key : config.getKeys(false)) {
                String[] split = key.split("\\.");
                String last = split[split.length - 1];

                if (last.length() != 1) {
                    debug.error("Recipe ingredients can only be one character!", StringUtil.foundAt(file, path + ".Recipe.Ingredients." + last));
                    return null;
                }

                // At this point, we need to figure out if people are using
                // the simple ingredients (Material~Byte) or advanced ingredients
                // (ItemStack). People can mix and match
                ItemStack item;
                if (config.isString(last)) {
                    item = MaterialUtil.fromStringToItemStack(config.getString(last));
                } else {
                    item = this.serialize(file, configurationSection, path + ".Recipe.Ingredients." + last);
                }

                if (CompatibilityAPI.getVersion() < 1.13) {
                    ingredients.put(last.charAt(0), item);
                } else {
                    ingredients.put(last.charAt(0), new RecipeChoice.ExactChoice(item));
                }

            }

            ReflectionUtil.setField(ingredientsField, recipe, ingredients);

            // Register the recipe to bukkit
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