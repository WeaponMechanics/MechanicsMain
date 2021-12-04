package me.deecaad.weaponmechanics.utils;

import me.deecaad.core.compatibility.CompatibilityAPI;
import org.bukkit.inventory.ItemStack;

/**
 * NBT tags used by WeaponMechanics
 */
public enum CustomTag {

    /**
     * string
     */
    WEAPON_TITLE("weapon-title"),

    /**
     * integer
     */
    SELECTIVE_FIRE("selective-fire"),

    /**
     * integer
     */
    AMMO_LEFT("ammo-left"),

    /**
     * string
     * Ammo item or magazine item name
     */
    ITEM_AMMO_NAME("item-ammo-name"),

    /**
     * integer
     * How much ammo magazine has left
     */
    ITEM_AMMO_LEFT("item-ammo-left"),

    /**
     * integer
     * Whether weapon still has magazine attached (0=true, 1=false)
     */
    HAS_ITEM_MAGAZINE("has-item-magazine"),

    /**
     * integer
     */
    FIREARM_ACTION_STATE("firearm-action-state");


    private final String id;

    CustomTag(String id) {
        this.id = id;
    }

    /**
     * @return the id that should be put to item stack to be used that identifier
     */
    public String getId() {
        return this.id;
    }

    public boolean hasString(ItemStack item) {
        return CompatibilityAPI.getNBTCompatibility().hasString(item, "WeaponMechanics", id);
    }

    public String getString(ItemStack item) {
        return CompatibilityAPI.getNBTCompatibility().getString(item, "WeaponMechanics", id);
    }

    public void setString(ItemStack item, String value) {
        CompatibilityAPI.getNBTCompatibility().setString(item, "WeaponMechanics", id, value);
    }

    public boolean hasInteger(ItemStack item) {
        return CompatibilityAPI.getNBTCompatibility().hasInt(item, "WeaponMechanics", id);
    }

    public int getInteger(ItemStack item) {
        return CompatibilityAPI.getNBTCompatibility().getInt(item, "WeaponMechanics", id);
    }

    public void setInteger(ItemStack item, int value) {
        CompatibilityAPI.getNBTCompatibility().setInt(item, "WeaponMechanics", id, value);
    }

    public boolean hasDouble(ItemStack item) {
        return CompatibilityAPI.getNBTCompatibility().hasDouble(item, "WeaponMechanics", id);
    }

    public double getDouble(ItemStack item) {
        return CompatibilityAPI.getNBTCompatibility().getDouble(item, "WeaponMechanics", id);
    }

    public void setDouble(ItemStack item, double value) {
        CompatibilityAPI.getNBTCompatibility().setDouble(item, "WeaponMechanics", id, value);
    }
}