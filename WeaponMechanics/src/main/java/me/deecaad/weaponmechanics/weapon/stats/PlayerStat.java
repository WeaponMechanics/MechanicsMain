package me.deecaad.weaponmechanics.weapon.stats;

import java.util.Arrays;

public enum PlayerStat {

    UUID("VARCHAR(255)", String.class),

    WEAPON_DEATHS("INTEGER", Integer.class),
    DAMAGE_TAKEN("FLOAT", Float.class);

    private final String columnType;
    private final Class<?> classType;

    PlayerStat(String columnType, Class<?> classType) {
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
        StringBuilder builder = new StringBuilder("CREATE TABLE IF NOT EXISTS player_stats (");
        Arrays.stream(values()).forEach(stat -> builder
                .append(stat.name().toLowerCase())
                .append(" ")
                .append(stat.getColumnType())
                .append(", "));
        builder.append("PRIMARY KEY (uuid))");

        return builder.toString();
    }
}