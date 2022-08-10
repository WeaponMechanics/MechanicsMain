package me.deecaad.weaponmechanics.weapon.stats;

public enum WeaponStats {

    UUID,
    WEAPON_TITLE,

    SKIN,

    EQUIP_TIMES,
    EQUIPPED_TIME,

    // SHOTS_MISSED = SHOTS - TOTAL_HITS
    SHOTS,

    // TOTAL_HITS = head + body + arm + left + foot
    HEAD_HITS,
    BODY_HITS,
    ARM_HITS,
    LEG_HITS,
    FOOT_HITS,
    BACKSTABS,
    CRITICAL_HITS,

    TOTAL_DAMAGE,

    // TOTAL_KILLS = player + other
    PLAYER_KILLS,
    OTHER_KILLS,

    HEAD_KILLS,
    BODY_KILLS,
    ARM_KILLS,
    LEG_KILLS,
    FOOT_KILLS,
    BACKSTAB_KILLS,
    CRITICAL_KILLS,

    BLOCKS_DESTROYED
}