package me.deecaad.weaponmechanics.weapon.stats;

import java.util.Arrays;

public enum WeaponStat {

    UUID("VARCHAR(255)"),
    WEAPON_TITLE("VARCHAR(255)"),

    SKIN("VARCHAR(255)"),

    EQUIP_TIMES("INTEGER"),
    EQUIPPED_TIME("INTEGER"),

    // SHOTS_MISSED = SHOTS - TOTAL_HITS
    SHOTS("INTEGER"),

    // TOTAL_HITS = head + body + arm + left + foot
    HEAD_HITS("INTEGER"),
    BODY_HITS("INTEGER"),
    ARM_HITS("INTEGER"),
    LEG_HITS("INTEGER"),
    FOOT_HITS("INTEGER"),
    BACKSTABS("INTEGER"),
    CRITICAL_HITS("INTEGER"),

    TOTAL_DAMAGE("FLOAT"),

    // TOTAL_KILLS = player + other
    PLAYER_KILLS("INTEGER"),
    OTHER_KILLS("INTEGER"),

    HEAD_KILLS("INTEGER"),
    BODY_KILLS("INTEGER"),
    ARM_KILLS("INTEGER"),
    LEG_KILLS("INTEGER"),
    FOOT_KILLS("INTEGER"),
    BACKSTAB_KILLS("INTEGER"),
    CRITICAL_KILLS("INTEGER"),

    BLOCKS_DESTROYED("INTEGER");

    private final String columnType;

    WeaponStat(String columnType) {
        this.columnType = columnType;
    }

    public String getColumnType() {
        return columnType;
    }

    public static String getCreateTableString() {
        StringBuilder builder = new StringBuilder("CREATE TABLE IF NOT EXISTS weapon_stats (");
        Arrays.stream(values()).forEach(stat -> builder
                .append(stat.name().toLowerCase())
                .append(" ")
                .append(stat.getColumnType())
                .append(", "));
        builder.append("PRIMARY KEY (uuid))");

        return builder.toString();
    }
}