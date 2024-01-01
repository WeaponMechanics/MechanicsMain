package me.deecaad.weaponmechanics.weapon.info;

import me.deecaad.core.compatibility.CompatibilityAPI;
import me.deecaad.core.file.SerializeData;
import me.deecaad.core.file.Serializer;
import me.deecaad.core.file.SerializerException;
import me.deecaad.core.utils.ReflectionUtil;
import me.deecaad.weaponmechanics.WeaponMechanics;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class WeaponConverter implements Serializer<WeaponConverter> {

    private boolean type;
    private boolean name;
    private boolean lore;
    private boolean enchantments;
    private boolean cmd;

    /**
     * Default constructor for serializer
     */
    public WeaponConverter() {
    }

    public WeaponConverter(boolean type, boolean name, boolean lore, boolean enchantments, boolean cmd) {
        this.type = type;
        this.name = name;
        this.lore = lore;
        this.enchantments = enchantments;
        this.cmd = cmd;
    }

    /**
     * Checks whether or not weapon stack is matching with the other item stack.
     * If this returns true, then other item should he converted to weapon stack
     *
     * @param weaponStack the weapon item
     * @param other the other item
     * @return true only if weapon stack matches with other item enough (depending on this converter variable values)
     */
    public boolean isMatch(ItemStack weaponStack, ItemStack other) {
        double version = CompatibilityAPI.getVersion();
        if (this.type) {
            if (weaponStack.getType() != other.getType()) {
                return false;
            }
            if (version < 1.13 && weaponStack.getData().getData() != other.getData().getData()) {
                return false;
            }
        }
        ItemMeta weaponMeta = weaponStack.getItemMeta();
        ItemMeta otherMeta = other.getItemMeta();
        if (weaponMeta == null || otherMeta == null)
            throw new IllegalArgumentException("Tried to convert an item that was air");

        if (this.name) {
            if (weaponMeta.hasDisplayName() != otherMeta.hasDisplayName()
                    || (weaponMeta.hasDisplayName() && !weaponMeta.getDisplayName().equals(otherMeta.getDisplayName()))) {
                return false;
            }
        }
        if (this.lore) {
            if (weaponMeta.hasLore() != otherMeta.hasLore()
                    || (weaponMeta.hasLore() && !weaponMeta.getLore().equals(otherMeta.getLore()))) {
                return false;
            }
        }
        if (this.cmd && ReflectionUtil.getMCVersion() >= 14) {
            if (weaponMeta.hasCustomModelData() != otherMeta.hasCustomModelData()) {
                return false;
            }
            if (weaponMeta.hasCustomModelData() && weaponMeta.getCustomModelData() != otherMeta.getCustomModelData()) {
                return false;
            }
        }

        if (this.enchantments) {
            // If weapon would have enchantments, but other doesn't
            // OR
            // If weapon and other enchantments doesn't match
            return weaponMeta.hasEnchants() == otherMeta.hasEnchants()
                    && (!weaponMeta.hasEnchants() || equals(weaponMeta.getEnchants(), otherMeta.getEnchants()));
        }
        return true;
    }

    private static boolean equals(Map<Enchantment, Integer> ench1, Map<Enchantment, Integer> ench2) {
        if (ench1 == ench2)
            return true;
        else if (ench1.size() != ench2.size())
            return false;
        else {
            List<Map.Entry<Enchantment, Integer>> list1 = new ArrayList<>(ench1.entrySet());
            List<Map.Entry<Enchantment, Integer>> list2 = new ArrayList<>(ench2.entrySet());

            for (int i = 0; i < list1.size(); i++) {
                Map.Entry<Enchantment, Integer> entry1 = list1.get(i);
                Map.Entry<Enchantment, Integer> entry2 = list2.get(i);

                if (!entry1.getKey().equals(entry2.getKey()))
                    return false;
                else if (!entry1.getValue().equals(entry2.getValue()))
                    return false;
            }

            return true;
        }
    }

    @Override
    public String getKeyword() {
        return "Weapon_Converter_Check";
    }

    @Override
    public @NotNull WeaponConverter serialize(@NotNull SerializeData data) throws SerializerException {
        boolean type = data.of("Type").getBool(false);
        boolean name = data.of("Name").getBool(false);
        boolean lore = data.of("Lore").getBool(false);
        boolean enchantments = data.of("Enchantments").getBool(false);
        boolean cmd = data.of("Custom_Model_Data").getBool(false);

        if (!type && !name && !lore && !enchantments && !cmd) {
            throw data.exception(null, "'Type', 'Name', 'Lore', 'Enchantments', 'Custom_Model_Data' are all 'false'",
                    "One of them should be 'true' to allow weapon conversion",
                    "If you want to remove the weapon conversion feature, remove the '" + getKeyword() + "' option from config");
        }

        if (cmd && ReflectionUtil.getMCVersion() < 14) {
            throw data.exception("Custom_Model_Data", "Custom_Model_Data is only available for 1.14+");
        }

        WeaponMechanics.getWeaponHandler().getInfoHandler().addWeaponWithConvert(data.key.split("\\.")[0]);

        return new WeaponConverter(type, name, lore, enchantments, cmd);
    }
}