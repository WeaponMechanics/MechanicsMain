package me.deecaad.core.utils;

import me.deecaad.compatibility.CompatibilityAPI;

import java.util.UUID;

/**
 * WeaponMechanics attributes with their UUIDs
 */
public enum AttributeType {

    GENERIC_MOVEMENT_SPEED(new UUID(2872L, 894653L), CompatibilityAPI.getVersion() < 1.16 ? "generic.movementSpeed" : "generic.movement_speed"),
    GENERIC_MAX_HEALTH(new UUID(2872L, 894652L), CompatibilityAPI.getVersion() < 1.16 ? "generic.maxHealth" : "generic.max_health"),
    GENERIC_ATTACK_DAMAGE(new UUID(2872L, 894651L), CompatibilityAPI.getVersion() < 1.16 ? "generic.attackDamage" : "generic.attack_damage"),
    GENERIC_ATTACK_SPEED(new UUID(2872L, 894650L), CompatibilityAPI.getVersion() < 1.16 ? "generic.attackSpeed" : "generic.attack_speed");

    private final UUID uuid;
    private final String minecraftName;

    AttributeType(UUID uuid, String minecraftName) {
        this.uuid = uuid;
        this.minecraftName = minecraftName;
    }

    /**
     * @return the UUID used for main hand attribute
     */
    public UUID getUUID() {
        return uuid;
    }

    /**
     * @return the attribute in its minecraft name
     */
    public String getMinecraftName() {
        return this.minecraftName;
    }

}