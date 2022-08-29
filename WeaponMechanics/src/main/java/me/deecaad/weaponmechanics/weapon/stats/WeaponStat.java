package me.deecaad.weaponmechanics.weapon.stats;

import java.util.Arrays;

public enum WeaponStat {

    UUID("VARCHAR(255)", String.class),
    WEAPON_TITLE("VARCHAR(255)", String.class),

    SKIN("VARCHAR(255)", String.class),

    EQUIP_TIMES("INTEGER", Integer.class),

    // SHOTS_MISSED = SHOTS - TOTAL_HITS
    SHOTS("INTEGER", Integer.class),

    // TOTAL_HITS = head + body + arm + left + foot
    HEAD_HITS("INTEGER", Integer.class),
    BODY_HITS("INTEGER", Integer.class),
    ARM_HITS("INTEGER", Integer.class),
    LEG_HITS("INTEGER", Integer.class),
    FOOT_HITS("INTEGER", Integer.class),
    BACKSTABS("INTEGER", Integer.class),
    CRITICAL_HITS("INTEGER", Integer.class),
    LONGEST_DISTANCE_HIT("FLOAT", Float.class),

    TOTAL_DAMAGE("FLOAT", Float.class),

    // TOTAL_KILLS = player + other
    PLAYER_KILLS("INTEGER", Integer.class),
    OTHER_KILLS("INTEGER", Integer.class),
    PLAYER_ASSISTS("INTEGER", Integer.class),
    OTHER_ASSISTS("INTEGER", Integer.class),

    HEAD_KILLS("INTEGER", Integer.class),
    BODY_KILLS("INTEGER", Integer.class),
    ARM_KILLS("INTEGER", Integer.class),
    LEG_KILLS("INTEGER", Integer.class),
    FOOT_KILLS("INTEGER", Integer.class),
    BACKSTAB_KILLS("INTEGER", Integer.class),
    CRITICAL_KILLS("INTEGER", Integer.class),
    LONGEST_DISTANCE_KILL("FLOAT", Float.class),

    BLOCKS_DESTROYED("INTEGER", Integer.class);

    private final String columnType;
    private final Class<?> classType;

    WeaponStat(String columnType, Class<?> classType) {
        this.columnType = columnType;
        this.classType = classType;
    }

    public String getColumnType() {
        return columnType;
    }

    public Class<?> getClassType() {
        return classType;
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