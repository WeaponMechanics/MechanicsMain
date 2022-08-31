package me.deecaad.weaponmechanics.wrappers;

import me.deecaad.weaponmechanics.weapon.stats.PlayerStat;
import me.deecaad.weaponmechanics.weapon.stats.WeaponStat;

import java.util.*;
import java.util.function.BiFunction;

import static org.bukkit.ChatColor.*;

public class StatsData {

    private final UUID uuid;
    private Map<PlayerStat, Object> playerData;
    private Map<String, Map<WeaponStat, Object>> weaponData;
    private boolean isSync;

    public StatsData(UUID uuid) {
        this.uuid = uuid;
    }

    /**
     * @return whether these stats have been synced with database
     */
    public boolean isSync() {
        return isSync;
    }

    public void setData(Map<PlayerStat, Object> playerData, Map<String, Map<WeaponStat, Object>> weaponData) {
        if (isSync) throw new IllegalArgumentException("Tried to set data after sync");
        this.playerData = playerData;
        this.weaponData = weaponData;
        isSync = true;
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

    public List<String> getPlayerData() {
        if (!isSync) return null;
        if (playerData.isEmpty()) return null;
        List<String> data = new ArrayList<>();
        for (Map.Entry<PlayerStat, Object> entry : playerData.entrySet()) {
            data.add("" + GOLD + entry.getKey() + ": " + GRAY + entry.getValue());
        }
        return data;
    }

    public List<String> getWeaponData(String weapon) {
        if (!isSync) return null;
        Map<WeaponStat, Object> data = weaponData.get(weapon);
        if (data == null || data.isEmpty()) return null;
        List<String> dataList = new ArrayList<>();
        for (Map.Entry<WeaponStat, Object> entry : data.entrySet()) {
            dataList.add("" + GOLD + entry.getKey() + ": " + GRAY + entry.getValue());
        }
        return dataList;
    }

    @Override
    public String toString() {
        return "StatsData{" +
                "playerData=" + playerData +
                ", weaponData=" + weaponData +
                '}';
    }
}