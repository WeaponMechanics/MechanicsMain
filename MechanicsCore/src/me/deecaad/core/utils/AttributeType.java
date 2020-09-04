package me.deecaad.core.utils;

import me.deecaad.compatibility.CompatibilityAPI;

import java.util.UUID;

/**
 * WeaponMechanics attributes with their UUIDs
 */
public enum AttributeType {

    GENERIC_MOVEMENT_SPEED(new UUID(2872, 894653), new UUID(2871, 894653), CompatibilityAPI.getVersion() < 1.16 ? "generic.movementSpeed" : "generic.movement_speed"),
    GENERIC_MAX_HEALTH(new UUID(2872, 894652), new UUID(2871, 894653), CompatibilityAPI.getVersion() < 1.16 ? "generic.maxHealth" : "generic.max_health"),
    GENERIC_ATTACK_DAMAGE(new UUID(2872, 894651), new UUID(2870, 894653), CompatibilityAPI.getVersion() < 1.16 ? "generic.attackDamage" : "generic.attack_damage"),
    GENERIC_ATTACK_SPEED(new UUID(2872, 894650), new UUID(2869, 894653), CompatibilityAPI.getVersion() < 1.16 ? "generic.attackSpeed" : "generic.attack_speed");

    private final UUID mainUUID;
    private final UUID offUUID;
    private final String minecraftName;

    AttributeType(UUID mainUUID, UUID offUUID, String minecraftName) {
        this.mainUUID = mainUUID;
        this.offUUID = offUUID;
        this.minecraftName = minecraftName;
    }

    /**
     * @return the UUID used for main hand attribute
     */
    public UUID getMainhandUUID() {
        return this.mainUUID;
    }

    /**
     * @return the UUID used for off hand attribute
     */
    public UUID getOffhandUUID() {
        return this.offUUID;
    }

    /**
     * @return the attribute in its minecraft name
     */
    public String getMinecraftName() {
        return this.minecraftName;
    }

}