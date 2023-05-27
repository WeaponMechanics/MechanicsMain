package me.deecaad.weaponmechanics.utils;

import me.deecaad.core.compatibility.CompatibilityAPI;
import org.bukkit.inventory.ItemStack;

import java.util.Locale;

/**
 * This enum is used to keep a list of all NBT tags used by WeaponMechanics.
 * This enum also wraps the {@link me.deecaad.core.compatibility.nbt.NBTCompatibility}
 * api, making it easier to access data from items.
 */
public enum CustomTag {

    /**
     * The weapon title is stored as a String, and is used by WeaponMechanics
     * to determine if an item is a weapon, and which weapon it is. The title
     * can be used to retrieve values from config.
     */
    WEAPON_TITLE,

    /**
     * Selective fire is stored as an int, and is used by WeaponMechanics to
     * determine the current selective fire rate of the weapon. 0 = single,
     * 1 = burst, and 2 = auto.
     */
    SELECTIVE_FIRE,

    /**
     * Ammo left is stored as an int, and is used by WeaponMechanics to store
     * the amount of ammo left in the gun.
     */
    AMMO_LEFT,

    /**
     * Ammo type index is stored as an int, and is used by WeaponMechanics to
     * determine which ammo type that is currently loaded in the weapon. This
     * is done since config allows for multiple ammo types to be loaded into
     * the weapons. This is used by WeaponMechanicsPlus to add modifiers to
     * different ammo types.
     */
    AMMO_TYPE_INDEX,

    /**
     * The ammo name is stored as a String, and is used by WeaponMechanics
     * to determine if an item can be used as ammunition.
     */
    AMMO_TITLE("ammo-name"),

    /**
     * Ammo magazine is stored as an int, and is used by WeaponMechanics to
     * determine if the ammo was loaded as a magazine, or as individual
     * bullets. 0 = bullets, 1 = magazine.
     */
    AMMO_MAGAZINE,

    /**
     * Firearm action state is stored as an int, and is used by WeaponMechanics
     * to check if the weapon is open/closed. 0 = Ready, 1 = Open, 2 = Closed.
     *
     * @see me.deecaad.weaponmechanics.weapon.firearm.FirearmAction#getState(ItemStack)
     */
    FIREARM_ACTION_STATE,

    /**
     * Durability is stored as an int, and is used by WeaponMechanics for
     * {@link me.deecaad.weaponmechanics.weapon.shoot.CustomDurability}.
     */
    DURABILITY,

    /**
     * Max durability is stored as an int, and is used by WeaponMechanics to
     * prevent abusing repairs over and over.
     */
    MAX_DURABILITY,

    /**
     * Broken weapon is stored as a String, and is used by WeaponMechanics to
     * determine which weapon this item was before breaking. The stored value
     * is a weapon title.
     */
    BROKEN_WEAPON,

    /**
     * Repair kit title is stored as a String, and is used by WeaponMechanics
     * to determine if an item is a repair kit. This title can be used to
     * get repair kit information from config.
     */
    REPAIR_KIT_TITLE,

    /**
     * Attachment title is stored as a string, and is used by
     * WeaponMechanicsPlus to determine if an item is an attachment. This title
     * can be used to get attachment information from config (or the attachment
     * registry, stored in WeaponMechanicsPlus).
     */
    ATTACHMENT_TITLE,

    /**
     * Attachments are stored as a list of strings, and is used by
     * WeaponMechanicsPlus to determine which attachments are attached to a
     * weapon. Each string in the list is an {@link #ATTACHMENT_TITLE}, and can
     * be used to pull information from config.
     */
    ATTACHMENTS;


    private final String id;

    CustomTag() {
        this.id = name().toLowerCase(Locale.ROOT).replace('_', '-');
    }

    /**
     * This is only used for backwards support. For example, AMMO_NAME ->
     * {@link #AMMO_TITLE}. Use the default constructor.
     *
     * @param id The non-null id to be used as the NBT tag.
     */
    CustomTag(String id) {
        this.id = id;
    }

    /**
     * @return the id that should be put to item stack to be used that identifier
     */
    public String getId() {
        return this.id;
    }

    public String getKey() {
        return "WeaponMechanics:" + getId();
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

    public boolean hasStringArray(ItemStack item) {
        return CompatibilityAPI.getNBTCompatibility().hasStringArray(item, "WeaponMechanics", id);
    }

    public String[] getStringArray(ItemStack item) {
        return CompatibilityAPI.getNBTCompatibility().getStringArray(item, "WeaponMechanics", id);
    }

    public void setStringArray(ItemStack item, String[] value) {
        CompatibilityAPI.getNBTCompatibility().setStringArray(item, "WeaponMechanics", id, value);
    }

    public void remove(ItemStack item) {
        CompatibilityAPI.getNBTCompatibility().remove(item, "WeaponMechanics", id);
    }
}