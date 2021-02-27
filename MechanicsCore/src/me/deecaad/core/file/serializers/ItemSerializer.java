package me.deecaad.core.file.serializers;

import me.deecaad.compatibility.CompatibilityAPI;
import me.deecaad.compatibility.nbt.NBTCompatibility;
import me.deecaad.core.MechanicsCore;
import me.deecaad.core.file.Serializer;
import me.deecaad.core.utils.AttributeType;
import me.deecaad.core.utils.EnumUtil;
import me.deecaad.core.utils.LogLevel;
import me.deecaad.core.utils.MaterialUtil;
import me.deecaad.core.utils.ReflectionUtil;
import me.deecaad.core.utils.StringUtil;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static me.deecaad.core.MechanicsCore.debug;

public class ItemSerializer implements Serializer<ItemStack> {

    /**
     * Reflection support for versions before 1.11 when setting unbreakable tag
     */
    private static Method spigotMethod;
    private static Method setUnbreakable;

    private final static Field ingredientsField;

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

        itemStack.setItemMeta(itemMeta);

        String owningPlayer = configurationSection.getString(path + ".Skull.Owning_Player");
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
                debug.log(LogLevel.ERROR,
                        "Found an invalid cast in configurations!",
                        "Located at file " + file + " in " + path + ".Skull.Owning_Player in configurations",
                        "Tried to modify skull when the item wasn't skull (" + type + ")");
                return null;
            }
        }
        if (CompatibilityAPI.getVersion() >= 1.11) {
            try {
                String colorString = configurationSection.getString(path + ".Potion.Color");
                if (colorString != null) {
                    colorString = colorString.toUpperCase();

                    Color color = ColorSerializer.ColorType.fromString(colorString);
                    if (color == null) {
                        debug.log(LogLevel.ERROR,
                                "Found an invalid color type in configurations!",
                                "Located at file " + file + " in " + path + ".Potion.Color (" + colorString + ") in configurations");
                        return null;
                    }

                    PotionMeta potionMeta = (PotionMeta) itemStack.getItemMeta();
                    potionMeta.setColor(color);
                    itemStack.setItemMeta(potionMeta);
                }
            } catch (ClassCastException e) {
                debug.log(LogLevel.ERROR,
                        "Found an invalid cast in configurations!",
                        "Located at file " + file + " in " + path + ".Potion.Color in configurations",
                        "Tried to modify potion when the item wasn't potion (" + type + ")");
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
                if (meta == null) {
                    debug.error("Somehow itemMeta was null? Is the material type in serializer AIR?", StringUtil.foundAt(file, path + ".Leather_Color"));
                    return null;
                }

                meta.setColor(color);

            } catch (ClassCastException e) {
                debug.error("You cannot use " + itemStack.getType() + " with leather armor color!", StringUtil.foundAt(file, path + ".Leather_Color"));
                return null;
            }
        }

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