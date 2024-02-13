package me.deecaad.core.utils;

import java.util.UUID;

/**
 * WeaponMechanics attributes with their UUIDs.
 * <a href="https://minecraft.fandom.com/wiki/Attribute">Attribute Reference</a>
 *
 * Attributes have a UUID, string identifier, minecraft name, value, and slot. The UUID should be
 * completely unique, which is why most people say to use a random attribute. If the UUID is not
 * unique, if 2 items with the same uuid exist, one of the effects will be ignored. The string
 * identifier is basically unused by minecraft, so it can be anything. The slot should usually be
 * defined, so people don't hold armor to gain its benefits. Use <code>null</code> for any slot.
 *
 * <p>
 * MechanicsCore modifies the least significant bits of the UUID when the slot is defined. HAND gets
 * +1, HEAD +2, CHEST +3, LEGS +4, FEET +5. This ensures the attributes don't override each other.
 */
public enum AttributeType {

    GENERIC_MOVEMENT_SPEED(new UUID(2872L, 894653L), ReflectionUtil.getMCVersion() < 16 ? "generic.movementSpeed" : "generic.movement_speed"),
    GENERIC_MAX_HEALTH(new UUID(2872L, 894652L), ReflectionUtil.getMCVersion() < 16 ? "generic.maxHealth" : "generic.max_health"),
    GENERIC_ATTACK_DAMAGE(new UUID(2872L, 894651L), ReflectionUtil.getMCVersion() < 16
        ? "generic.attackDamage"
        : "generic.attack_damage"),
    GENERIC_ATTACK_SPEED(new UUID(2872L, 894650L), ReflectionUtil.getMCVersion() < 16 ? "generic.attackSpeed" : "generic.attack_speed"),
    GENERIC_ARMOR_TOUGHNESS(new UUID(2872L, 894649L), ReflectionUtil.getMCVersion() < 16 ? "generic.armorToughness" : "generic.armor_toughness"),
    GENERIC_KNOCKBACK_RESISTANCE(new UUID(2872L, 894648L), ReflectionUtil.getMCVersion() < 16 ? "generic.knockbackResistance" : "generic.knockback_resistance"),
    GENERIC_ARMOR(new UUID(2872L, 894647L), "generic.armor");

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