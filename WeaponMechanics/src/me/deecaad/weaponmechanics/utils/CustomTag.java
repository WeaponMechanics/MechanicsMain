package me.deecaad.weaponmechanics.utils;

/**
 * NBT tags used by WeaponMechanics
 */
public enum CustomTag {

    /**
     * Tags mainly used when creating or identifying item stack
     */
    WEAPON_TITLE("weapon-title"),
    SELECTIVE_FIRE("selective-fire"),
    AMMO_LEFT("ammo-left"),
    ATTACHMENTS("attachments");

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