package me.deecaad.weaponmechanics.weapon.stats;

import me.deecaad.core.database.Database;
import me.deecaad.weaponmechanics.WeaponMechanics;
import me.deecaad.weaponmechanics.weapon.WeaponHandler;
import me.deecaad.weaponmechanics.wrappers.PlayerWrapper;
import me.deecaad.weaponmechanics.wrappers.StatsData;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Set;
import java.util.UUID;

public class StatsHandler {

    private WeaponHandler weaponHandler;

    private StringBuilder generatedReplaceWeaponStats;
    private StringBuilder generatedReplacePlayerStats;

    public StatsHandler(WeaponHandler weaponHandler) {
        this.weaponHandler = weaponHandler;
        generateReplaces();
    }

    public void load(PlayerWrapper playerWrapper) {
        Database database = WeaponMechanics.getDatabase();
        if (database == null) return;

        StatsData statsData = playerWrapper.getStatsDataUnsafe();
        if (statsData == null) return;

        if (statsData.isSync()) throw new IllegalArgumentException("Tried to load data to already synced stats data");

        UUID uuid = playerWrapper.getPlayer().getUniqueId();

        database.executeQuery("SELECT * FROM player_stats WHERE UUID='" + uuid + "'", (resultSet -> {
            try {
                while (resultSet.next()) {
                    System.out.println(resultSet);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }));

        database.executeQuery("SELECT * FROM weapon_stats WHERE UUID='" + uuid + "'", (resultSet -> {
            try {
                while (resultSet.next()) {
                    System.out.println(resultSet);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }));

        statsData.setSync(true);
    }

    public void save(PlayerWrapper playerWrapper, boolean forceSync) {
        Database database = WeaponMechanics.getDatabase();
        if (database == null) return;

        StatsData statsData = playerWrapper.getStatsData();
        // This might be null if sync didn't occur...
        if (statsData == null) return;

        database.executeUpdate(forceSync, getSaveStrings(playerWrapper));
    }

    private String[] getSaveStrings(PlayerWrapper playerWrapper) {
        StatsData statsData = playerWrapper.getStatsData();
        if (statsData == null) return null;

        if (!statsData.isSync()) throw new IllegalArgumentException("Tried to use REPLACE when player stat data wasn't yet synced");

        Set<String> weapons = statsData.getWeapons();
        if (weapons == null || weapons.isEmpty()) return new String[]{getPlayerStatReplaceString(playerWrapper)};

        String[] batches = new String[weapons.size() + 1];

        // Create batch for every weapon...
        int i = 0;
        for (String weapon : weapons) {

            StringBuilder builder = new StringBuilder(generatedReplaceWeaponStats);

            WeaponStat[] array = WeaponStat.values();
            for (int j = 0; j < array.length; ++j) {
                WeaponStat stat = array[j];
                if (j != 0) {
                    builder.append(", ");
                }
                if (stat.getClassType() == String.class) {
                    builder.append("'")
                            .append(statsData.get(weapon, stat, stat.getDefaultValue()))
                            .append("'");
                } else {
                    builder.append(statsData.get(weapon, stat, stat.getDefaultValue()));
                }
            }

            batches[i] = builder.append(")").toString();

            ++i;
        }

        // The i is actually incremented above +1 times
        batches[i] = getPlayerStatReplaceString(playerWrapper);

        return batches;
    }

    private String getPlayerStatReplaceString(PlayerWrapper playerWrapper) {
        StatsData statsData = playerWrapper.getStatsData();
        StringBuilder builder = new StringBuilder(generatedReplacePlayerStats);
        PlayerStat[] array = PlayerStat.values();
        for (int i = 0; i < array.length; ++i) {
            PlayerStat stat = array[i];
            if (i != 0) {
                builder.append(", ");
            }
            if (stat.getClassType() == String.class) {
                builder.append("'")
                        .append(statsData.get(stat, stat.getDefaultValue()))
                        .append("'");
            } else {
                builder.append(statsData.get(stat, stat.getDefaultValue()));
            }
        }
        return builder.append(")").toString();
    }

    private void generateReplaces() {
        if (generatedReplaceWeaponStats == null) {
            StringBuilder builder = new StringBuilder("REPLACE INTO weapon_stats (");

            WeaponStat[] array = WeaponStat.values();
            for (int i = 0; i < array.length; ++i) {
                WeaponStat stat = array[i];
                if (i != 0) {
                    builder.append(", ");
                }
                builder.append(stat.name());
            }

            builder.append(") VALUES (");
            generatedReplaceWeaponStats = builder;
        }
        if (generatedReplacePlayerStats == null) {
            StringBuilder builder = new StringBuilder("REPLACE INTO player_stats (");

            PlayerStat[] array = PlayerStat.values();
            for (int i = 0; i < array.length; ++i) {
                PlayerStat stat = array[i];
                if (i != 0) {
                    builder.append(", ");
                }
                builder.append(stat.name());
            }

            builder.append(") VALUES (");
            generatedReplacePlayerStats = builder;
        }
    }
}