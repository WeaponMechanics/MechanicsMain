package me.deecaad.weaponmechanics.weapon.stats;

import java.util.Arrays;

public enum PlayerStat {

    UUID("VARCHAR(255) NOT NULL", String.class),

    WEAPON_DEATHS("INTEGER", Integer.class),
    DAMAGE_TAKEN("FLOAT", Float.class);

    private final String columnType;
    private final Class<?> classType;
    private final Object defaultValue;

    PlayerStat(String columnType, Class<?> classType) {
        this.columnType = columnType;
        this.classType = classType;

        if (classType == String.class) {
            this.defaultValue = null;
        } else {
            this.defaultValue = 0;
        }
    }

    public Class<?> getClassType() {
        return classType;
    }

    public Object getDefaultValue() {
        return defaultValue;
    }

    public static String getCreateTableString() {
        StringBuilder builder = new StringBuilder("CREATE TABLE IF NOT EXISTS player_stats (");
        Arrays.stream(values()).forEach(stat -> builder
                .append(stat.name())
                .append(" ")
                .append(stat.columnType)
                .append(", "));
        builder.append("PRIMARY KEY (UUID))");

        return builder.toString();
    }
}