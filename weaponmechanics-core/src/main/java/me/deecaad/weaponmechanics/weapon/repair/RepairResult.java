package me.deecaad.weaponmechanics.weapon.repair;

/**
 * Result of attempting to apply a repair kit to a weapon.
 */
public enum RepairResult {

    /** The clicked items were not a weapon/repair-kit pair. */
    NOT_APPLICABLE,

    /** The pair was recognized, but the repair was rejected by configuration or weapon state. */
    DENIED,

    /** The weapon was repaired successfully. */
    REPAIRED;

    /**
     * Returns whether WeaponMechanics should consume the inventory click.
     */
    public boolean isHandled() {
        return this != NOT_APPLICABLE;
    }
}
