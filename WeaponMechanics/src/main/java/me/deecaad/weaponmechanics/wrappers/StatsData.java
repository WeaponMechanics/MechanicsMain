package me.deecaad.weaponmechanics.wrappers;

import me.deecaad.weaponmechanics.weapon.stats.PlayerStat;
import me.deecaad.weaponmechanics.weapon.stats.WeaponStat;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiFunction;

public class StatsData {

    private final UUID uuid;
    private final Map<PlayerStat, Object> playerData;
    private final Map<String, Map<WeaponStat, Object>> weaponData;
    private boolean isSync;

    public StatsData(UUID uuid) {
        this.uuid = uuid;
        this.playerData = new HashMap<>();
        this.weaponData = new HashMap<>();
    }

    /**
     * @return whether these stats have been synced with database
     */
    public boolean isSync() {
        return isSync;
    }

    public void setSync(boolean sync) {
        isSync = sync;
    }

    public void add(PlayerStat stat, int data) {
        if (!isSync) return;
        if (!stat.getClassType().isInstance(data)) throw new IllegalArgumentException("Tried to give invalid data for stat " + stat + " " + data);
        playerData.compute(stat, (key, value) -> value == null ? data : (int) value + data);
    }

    public void add(PlayerStat stat, float data) {
        if (!isSync) return;
        if (!stat.getClassType().isInstance(data)) throw new IllegalArgumentException("Tried to give invalid data for stat " + stat + " " + data);
        playerData.compute(stat, (key, value) -> value == null ? data : (float) value + data);
    }

    public void set(PlayerStat stat, String data) {
        if (!isSync) return;
        if (!stat.getClassType().isInstance(data)) throw new IllegalArgumentException("Tried to give invalid data for stat " + stat + " " + data);
        playerData.put(stat, data);
    }

    public Object get(PlayerStat stat, Object defaultValue) {
        if (!isSync) return defaultValue;
        if (stat == PlayerStat.UUID) return uuid;
        return playerData.getOrDefault(stat, defaultValue);
    }

    public void add(String weaponTitle, WeaponStat stat, int data) {
        if (!isSync) return;
        if (!stat.getClassType().isInstance(data)) throw new IllegalArgumentException("Tried to give invalid data for stat " + stat + " " + data);
        weaponData.putIfAbsent(weaponTitle, new HashMap<>());
        weaponData.get(weaponTitle).compute(stat, (key, value) -> value == null ? data : (int) value + data);
    }

    public void add(String weaponTitle, WeaponStat stat, float data) {
        if (!isSync) return;
        if (!stat.getClassType().isInstance(data)) throw new IllegalArgumentException("Tried to give invalid data for stat " + stat + " " + data);
        weaponData.putIfAbsent(weaponTitle, new HashMap<>());
        weaponData.get(weaponTitle).compute(stat, (key, value) -> value == null ? data : (float) value + data);
    }

    public void set(String weaponTitle, WeaponStat stat, BiFunction<WeaponStat, Object, Float> compute) {
        if (!isSync) return;
        weaponData.putIfAbsent(weaponTitle, new HashMap<>());
        weaponData.get(weaponTitle).compute(stat, compute);
    }

    public void set(String weaponTitle, WeaponStat stat, String data) {
        if (!isSync) return;
        if (!stat.getClassType().isInstance(data)) throw new IllegalArgumentException("Tried to give invalid data for stat " + stat + " " + data);
        weaponData.putIfAbsent(weaponTitle, new HashMap<>());
        weaponData.get(weaponTitle).put(stat, data);
    }

    public Set<String> getWeapons() {
        if (!isSync) return null;
        return weaponData.keySet();
    }

    public Object get(String weaponTitle, WeaponStat stat, Object defaultValue) {
        if (!isSync) return defaultValue;
        if (stat == WeaponStat.UUID) return uuid;
        if (stat == WeaponStat.WEAPON_TITLE) return weaponTitle;

        Map<WeaponStat, Object> data = weaponData.get(weaponTitle);
        if (data == null) return defaultValue;

        return data.getOrDefault(stat, defaultValue);
    }

    @Override
    public String toString() {
        return "StatsData{" +
                "playerData=" + playerData +
                ", weaponData=" + weaponData +
                '}';
    }
}