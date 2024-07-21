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

    GENERIC_MOVEMENT_SPEED(new UUID(2872L, 894653L), !MinecraftVersions.NETHER_UPDATE.isAtLeast() ? "generic.movementSpeed" : "generic.movement_speed"),
    GENERIC_MAX_HEALTH(new UUID(2872L, 894652L), !MinecraftVersions.NETHER_UPDATE.isAtLeast() ? "generic.maxHealth" : "generic.max_health"),
    GENERIC_ATTACK_DAMAGE(new UUID(2872L, 894651L), !MinecraftVersions.NETHER_UPDATE.isAtLeast() ? "generic.attackDamage" : "generic.attack_damage"),
    GENERIC_ATTACK_SPEED(new UUID(2872L, 894650L), !MinecraftVersions.NETHER_UPDATE.isAtLeast() ? "generic.attackSpeed" : "generic.attack_speed"),
    GENERIC_ARMOR_TOUGHNESS(new UUID(2872L, 894649L), !MinecraftVersions.NETHER_UPDATE.isAtLeast() ? "generic.armorToughness" : "generic.armor_toughness"),
    GENERIC_KNOCKBACK_RESISTANCE(new UUID(2872L, 894648L), !MinecraftVersions.NETHER_UPDATE.isAtLeast() ? "generic.knockbackResistance" : "generic.knockback_resistance"),
    GENERIC_ARMOR(new UUID(2872L, 894647L), "generic.armor"),

    // newer attributes don't need the version check
    GENERIC_FLYING_SPEED(new UUID(2872L, 894646L), "generic.flying_speed"),
    GENERIC_ATTACK_KNOCKBACK(new UUID(2872L, 894645L), "generic.attack_knockback"),
    GENERIC_FALL_DAMAGE_MULTIPLIER(new UUID(2872L, 894644L), "generic.fall_damage_multiplier"),
    GENERIC_SAFE_FALL_DISTANCE(new UUID(2872L, 894643L), "generic.safe_fall_distance"),
    GENERIC_SCALE(new UUID(2872L, 894642L), "generic.scale"),
    GENERIC_STEP_HEIGHT(new UUID(2872L, 894641L), "generic.step_height"),
    GENERIC_GRAVITY(new UUID(2872L, 894640L), "generic.gravity"),
    GENERIC_JUMP_STRENGTH(new UUID(2872L, 894639L), "generic.jump_strength"),
    GENERIC_BURNING_TIME(new UUID(2872L, 894638L), "generic.burning_time"),
    GENERIC_EXPLOSION_KNOCKBACK_RESISTANCE(new UUID(2872L, 894637L), "generic.explosion_knockback_resistance"),
    GENERIC_MOVEMENT_EFFICIENCY(new UUID(2872L, 894636L), "generic.movement_efficiency"),
    GENERIC_OXYGEN_BONUS(new UUID(2872L, 894635L), "generic.oxygen_bonus"),
    GENERIC_WATER_MOVEMENT_EFFICIENCY(new UUID(2872L, 894634L), "generic.water_movement_efficiency"),
    PLAYER_BLOCK_INTERACTION_RANGE(new UUID(2872L, 894633L), "player.block_interaction_range"),
    PLAYER_ENTITY_INTERACTION_RANGE(new UUID(2872L, 894632L), "player.entity_interaction_range"),
    PLAYER_BLOCK_BREAK_SPEED(new UUID(2872L, 894631L), "player.block_break_speed"),
    PLAYER_MINING_EFFICIENCY(new UUID(2872L, 894630L), "player.mining_efficiency"),
    PLAYER_SNEAKING_SPEED(new UUID(2872L, 894629L), "player.sneaking_speed"),
    PLAYER_SUBMERGED_MINING_SPEED(new UUID(2872L, 894628L), "player.submerged_mining_speed"),
    PLAYER_SWEEPING_DAMAGE_RATIO(new UUID(2872L, 894627L), "player.sweeping_damage_ratio");


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