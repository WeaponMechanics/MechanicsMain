package me.deecaad.weaponmechanics.weapon.stats;

import me.deecaad.core.database.Database;
import me.deecaad.core.utils.LogLevel;
import me.deecaad.weaponmechanics.WeaponMechanics;
import me.deecaad.weaponmechanics.weapon.WeaponHandler;
import me.deecaad.weaponmechanics.wrappers.PlayerWrapper;
import me.deecaad.weaponmechanics.wrappers.StatsData;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
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

        if (database.isClosed()) throw new IllegalArgumentException("Tried to load data when database was closed");

        StatsData statsData = playerWrapper.getStatsDataUnsafe();
        if (statsData == null) return;

        if (statsData.isSync()) throw new IllegalArgumentException("Tried to load data to already synced stats data");

        fetchAndInsertPlayerStats(database, playerWrapper.getPlayer().getUniqueId(), statsData);
    }

    public void save(PlayerWrapper playerWrapper, boolean forceSync) {
        Database database = WeaponMechanics.getDatabase();
        if (database == null) return;

        if (database.isClosed()) throw new IllegalArgumentException("Tried to save data when database was closed");

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

            WeaponStat[] array = WeaponStat.VALUES;
            for (int j = 0; j < array.length; ++j) {
                WeaponStat stat = array[j];
                if (j != 0) {
                    builder.append(", ");
                }
                if (stat.getClassType() == String.class) {
                    Object value = statsData.get(weapon, stat, stat.getDefaultValue());
                    if (value == null) {
                        builder.append("NULL");
                    } else {
                        builder.append("'")
                                .append(value)
                                .append("'");
                    }
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
        PlayerStat[] array = PlayerStat.VALUES;
        for (int i = 0; i < array.length; ++i) {
            PlayerStat stat = array[i];
            if (i != 0) {
                builder.append(", ");
            }
            if (stat.getClassType() == String.class) {
                Object value = statsData.get(stat, stat.getDefaultValue());
                if (value == null) {
                    builder.append("NULL");
                } else {
                    builder.append("'")
                            .append(value)
                            .append("'");
                }
            } else {
                builder.append(statsData.get(stat, stat.getDefaultValue()));
            }
        }
        return builder.append(")").toString();
    }

    private void generateReplaces() {
        StringBuilder weaponBuilder = new StringBuilder("REPLACE INTO weapon_stats (");

        WeaponStat[] weaponArray = WeaponStat.VALUES;
        for (int i = 0; i < weaponArray.length; ++i) {
            WeaponStat stat = weaponArray[i];
            if (i != 0) {
                weaponBuilder.append(", ");
            }
            weaponBuilder.append(stat.name());
        }

        weaponBuilder.append(") VALUES (");
        generatedReplaceWeaponStats = weaponBuilder;

        StringBuilder playerBuilder = new StringBuilder("REPLACE INTO player_stats (");

        PlayerStat[] playerArray = PlayerStat.VALUES;
        for (int i = 0; i < playerArray.length; ++i) {
            PlayerStat stat = playerArray[i];
            if (i != 0) {
                playerBuilder.append(", ");
            }
            playerBuilder.append(stat.name());
        }

        playerBuilder.append(") VALUES (");
        generatedReplacePlayerStats = playerBuilder;
    }

    private void fetchAndInsertPlayerStats(Database database, UUID uuid, StatsData statsData) {
        database.executeQuery("SELECT * FROM player_stats WHERE UUID='" + uuid + "'", (playerSet -> {
            try {

                Map<PlayerStat, Object> playerData = new HashMap<>();
                while (playerSet.next()) {
                    for (PlayerStat stat : PlayerStat.VALUES) {
                        if (stat == PlayerStat.UUID) continue;

                        if (stat.getClassType() == Integer.class) {
                            int data = playerSet.getInt(stat.name());
                            if (data == 0) continue;
                            playerData.put(stat, data);
                        } else if (stat.getClassType() == Float.class) {
                            float data = playerSet.getFloat(stat.name());
                            if (data == 0.0) continue;
                            playerData.put(stat, data);
                        } else {
                            String data = playerSet.getString(stat.name());
                            if (data == null) continue;
                            playerData.put(stat, data);
                        }
                    }
                }

                fetchAndInsertWeaponStats(database, uuid, statsData, playerData);

            } catch (SQLException e) {
                WeaponMechanics.debug.log(LogLevel.ERROR, e);
            }
        }));
    }

    private void fetchAndInsertWeaponStats(Database database, UUID uuid, StatsData statsData, Map<PlayerStat, Object> playerData) {
        database.executeQuery("SELECT * FROM weapon_stats WHERE UUID='" + uuid + "'", (weaponSet) -> {
            try {
                Map<String, Map<WeaponStat, Object>> weaponData = new HashMap<>();

                while (weaponSet.next()) {
                    String weaponTitle = weaponSet.getString(WeaponStat.WEAPON_TITLE.name());

                    Map<WeaponStat, Object> newWeaponMap = new HashMap<>();
                    weaponData.put(weaponTitle, newWeaponMap);

                    for (WeaponStat stat : WeaponStat.VALUES) {
                        if (stat == WeaponStat.UUID || stat == WeaponStat.WEAPON_TITLE) continue;

                        if (stat.getClassType() == Integer.class) {
                            int data = weaponSet.getInt(stat.name());
                            if (data == 0) continue;
                            newWeaponMap.put(stat, data);
                        } else if (stat.getClassType() == Float.class) {
                            float data = weaponSet.getFloat(stat.name());
                            if (data == 0.0) continue;
                            newWeaponMap.put(stat, data);
                        } else {
                            String data = weaponSet.getString(stat.name());
                            if (data == null) continue;
                            newWeaponMap.put(stat, data);
                        }
                    }
                }

                statsData.setData(playerData, weaponData);

            } catch (SQLException e) {
                WeaponMechanics.debug.log(LogLevel.ERROR, e);
            }
        });
    }
}