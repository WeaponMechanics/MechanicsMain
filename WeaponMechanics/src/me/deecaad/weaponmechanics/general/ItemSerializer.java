package me.deecaad.weaponmechanics.general;

import me.deecaad.compatibility.CompatibilityAPI;
import me.deecaad.core.file.Serializer;
import me.deecaad.core.utils.LogLevel;
import me.deecaad.core.utils.ReflectionUtil;
import me.deecaad.core.utils.StringUtils;
import me.deecaad.weaponmechanics.utils.AttributeType;
import me.deecaad.weaponmechanics.utils.MaterialHelper;
import me.deecaad.weaponmechanics.utils.TagHelper;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.io.File;
import java.lang.reflect.Method;
import java.util.*;

import static me.deecaad.weaponmechanics.WeaponMechanics.debug;

public class ItemSerializer implements Serializer<ItemStack> {

    /**
     * Reflection support for versions before 1.11 when setting unbreakable tag
     */
    private static Method spigotMethod;
    private static Method setUnbreakable;

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
        ItemStack itemStack;
        try {
            itemStack = MaterialHelper.fromStringToItemStack(type);
        } catch (IllegalArgumentException e) {
            debug.log(LogLevel.ERROR,
                    "Found an invalid material in configurations!",
                    "Located at file " + file + " in " + path + ".Type (" + type + ") in configurations");
            return null;
        }
        ItemMeta itemMeta = itemStack.getItemMeta();
        String name = configurationSection.getString(path + ".Name");
        if (name != null) {
            itemMeta.setDisplayName(colorizeString(name));
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
                String[] splitted = StringUtils.split(enchantment.toString());
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
                String[] splitted = StringUtils.split(attributeData.toString());
                if (splitted.length < 2) {
                    debug.log(LogLevel.ERROR,
                            "Found an invalid configuration format!",
                            "Located at file " + file + " in " + path + ".Attributes (" + attributeData.toString() + ") in configurations",
                            "Correct format has at least Attribute and Value defined (Attribute-Value).");
                    continue;
                }
                AttributeType attribute;
                try {
                    attribute = AttributeType.valueOf(splitted[0].toUpperCase());
                } catch (IllegalArgumentException e) {
                    debug.log(LogLevel.ERROR,
                            "Found an invalid attribute type in configurations!",
                            "Located at file " + file + " in " + path + ".Attributes (" + splitted[0].toUpperCase() + ") in configurations");
                    continue;
                }
                double amount;
                try {
                    amount = Double.parseDouble(splitted[1]);
                } catch (NumberFormatException e) {
                    debug.log(LogLevel.ERROR,
                            "Found an invalid attribute amount in configurations!",
                            "Located at file " + file + " in " + path + ".Attributes (" + splitted[1] + ") in configurations");
                    continue;
                }
                itemStack = TagHelper.setAttributeValue(itemStack, attribute, amount);
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

                    Color color = ColorType.fromString(colorString);
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
            list.add(colorizeString(obj.toString()));
        }
        return list;
    }

    private String colorizeString(String string) {
        return ChatColor.translateAlternateColorCodes('&', string);
    }
}