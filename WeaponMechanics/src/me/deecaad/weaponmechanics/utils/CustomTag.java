package me.deecaad.weaponmechanics.utils;

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

    private String id;

    CustomTag(String id) {
        this.id = id;
    }

    /**
     * @return the id that should be put to item stack to be used that identifier
     */
    public String getId() {
        return this.id;
    }
}