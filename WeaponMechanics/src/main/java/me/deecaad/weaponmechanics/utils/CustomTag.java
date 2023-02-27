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
     *
     * Current selective fire state where 0 = single, 1 = burst and 2 = auto
     */
    SELECTIVE_FIRE("selective-fire"),

    /**
     * integer
     */
    AMMO_LEFT("ammo-left"),

    /**
     * integer
     *
     * Current ammo type's index in the ammo types list
     */
    AMMO_TYPE_INDEX("ammo-type-index"),

    /**
     * string
     *
     * Bullet items and magazines items have this.
     */
    AMMO_NAME("ammo-name"),

    /**
     * integer
     *
     * Simple NBT tag to tell whether item is supposed to be magazine.
     * 0 = false, 1 = true
     */
    AMMO_MAGAZINE("ammo-magazine"),

    /**
     * integer
     */
    FIREARM_ACTION_STATE("firearm-action-state"),

    /**
     * integer
     */
    DURABILITY("durability"),

    /**
     * integer
     */
    MAX_DURABILITY("max-durability"),

    /**
     * string
     */
    BROKEN_WEAPON("broken-weapon"),

    /**
     * string
     */
    REPAIR_KIT_TITLE("repair-kit-title"),

    /**
     * string
     */
    ATTACHMENT_TITLE("attachment-title"),

    /**
     * array
     */
    ATTACHMENTS("attachments");


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

    public boolean hasArray(ItemStack item) {
        return CompatibilityAPI.getNBTCompatibility().hasArray(item, "WeaponMechanics", id);
    }

    public int[] getArray(ItemStack item) {
        return CompatibilityAPI.getNBTCompatibility().getArray(item, "WeaponMechanics", id);
    }

    public void setArray(ItemStack item, int[] value) {
        CompatibilityAPI.getNBTCompatibility().setArray(item, "WeaponMechanics", id, value);
    }

    public void remove(ItemStack item) {
        CompatibilityAPI.getNBTCompatibility().remove(item, "WeaponMechanics", id);
    }
}