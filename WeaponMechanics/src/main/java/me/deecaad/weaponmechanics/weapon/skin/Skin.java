package me.deecaad.weaponmechanics.weapon.skin;

import me.deecaad.core.compatibility.CompatibilityAPI;
import me.deecaad.core.file.Serializer;
import me.deecaad.core.utils.LogLevel;
import me.deecaad.core.utils.MaterialUtil;
import me.deecaad.core.utils.StringUtil;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;

import static me.deecaad.core.MechanicsCore.debug;

public class Skin implements Serializer<Skin>  {

    private Material type;
    private byte data;
    private short durability;
    private int customModelData;

    public Skin() { }

    public Skin(Material type, byte data, short durability, int customModelData) {
        this.type = type;
        this.data = data;
        this.durability = durability;
        this.customModelData = customModelData;
    }

    public Material getType() {
        return type;
    }

    public byte getData() {
        return data;
    }

    public short getDurability() {
        return durability;
    }

    public int getCustomModelData() {
        return customModelData;
    }

    /**
     * Applies this skin to given item stack
     *
     * @param itemStack the item stack for which to apply skin
     */
    public void apply(ItemStack itemStack) {
        double version = CompatibilityAPI.getVersion();

        if (type != null && itemStack.getType() != type) {
            itemStack.setType(type);
        }

        if (data != -1 && version < 1.13 && itemStack.getData().getData() != data) {
            itemStack.getData().setData(data);
        }

        boolean hasMetaChanges = false;
        ItemMeta meta = itemStack.getItemMeta();

        if (durability != -1) {
            if (version >= 1.132) {
                if (((org.bukkit.inventory.meta.Damageable) meta).getDamage() != durability) {
                    ((org.bukkit.inventory.meta.Damageable) meta).setDamage(durability);
                    hasMetaChanges = true;
                }
            } else if (itemStack.getDurability() != durability) {
                itemStack.setDurability(durability);
                hasMetaChanges = true;
            }
        }

        if (customModelData != -1 && version >= 1.14
                && (!meta.hasCustomModelData() || meta.getCustomModelData() != customModelData)) {
            meta.setCustomModelData(customModelData);
            hasMetaChanges = true;
        }

        if (hasMetaChanges) {
            itemStack.setItemMeta(meta);
        }
    }

    @Override
    public String getKeyword() {
        return "Skin";
    }

    @Override
    public Skin serialize(File file, ConfigurationSection configurationSection, String path) {
        return this;
    }

    public Skin serialize0(File file, ConfigurationSection configurationSection, String path) {

        String type = configurationSection.getString(path + ".Type");
        Material material = null;
        byte data = -1;
        if (type != null) {
            type = type.toUpperCase();

            try {
                ItemStack itemStack = MaterialUtil.fromStringToItemStack(type);
                material = itemStack.getType();
                data = itemStack.getData().getData();
            } catch (IllegalArgumentException e) {
                debug.log(LogLevel.ERROR,
                        StringUtil.foundInvalid("material"),
                        StringUtil.foundAt(file, path + ".Type", type),
                        StringUtil.debugDidYouMean(type.split(":")[0], Material.class));
                return null;
            }
        }

        short durability = (short) configurationSection.getInt(path + ".Durability", -1);
        int customModelData = configurationSection.getInt(path + ".Custom_Model_Data", -1);

        if (material == null && data == -1 && durability == -1 && customModelData == -1) {
            return null;
        }

        return new Skin(material, data, durability, customModelData);
    }
}