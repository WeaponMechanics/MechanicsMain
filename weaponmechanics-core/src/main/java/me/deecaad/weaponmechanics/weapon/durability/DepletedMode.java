package me.deecaad.weaponmechanics.weapon.durability;

/**
 * Controls what happens when a weapon reaches its maximum custom damage.
 */
public enum DepletedMode {

    /** Remove the weapon item, matching the legacy durability behavior. */
    BREAK,

    /** Keep the weapon at maximum damage until it is repaired. */
    DISABLE
}
