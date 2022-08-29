package me.deecaad.weaponmechanics.wrappers;

import me.deecaad.weaponmechanics.weapon.stats.PlayerStat;
import me.deecaad.weaponmechanics.weapon.stats.WeaponStat;

import java.util.HashMap;
import java.util.Map;

public class StatsData {

    private final Map<PlayerStat, Object> playerData;
    private final Map<String, Map<WeaponStat, Object>> weaponData;

    public StatsData() {
        this.playerData = new HashMap<>();
        this.weaponData = new HashMap<>();
    }

    public void add(PlayerStat stat, int data) {
        if (!stat.getClassType().isInstance(data)) throw new IllegalArgumentException("Tried to give invalid data for stat " + stat + " " + data);
        playerData.compute(stat, (key, value) -> value == null ? data : (int) value + data);
    }

    public void add(PlayerStat stat, float data) {
        if (!stat.getClassType().isInstance(data)) throw new IllegalArgumentException("Tried to give invalid data for stat " + stat + " " + data);
        playerData.compute(stat, (key, value) -> value == null ? data : (float) value + data);
    }

    public void set(PlayerStat stat, String data) {
        if (!stat.getClassType().isInstance(data)) throw new IllegalArgumentException("Tried to give invalid data for stat " + stat + " " + data);
        playerData.put(stat, data);
    }

    public void add(String weaponTitle, WeaponStat stat, int data) {
        if (!stat.getClassType().isInstance(data)) throw new IllegalArgumentException("Tried to give invalid data for stat " + stat + " " + data);
        weaponData.putIfAbsent(weaponTitle, new HashMap<>());
        weaponData.get(weaponTitle).compute(stat, (key, value) -> value == null ? data : (int) value + data);
    }

    public void add(String weaponTitle, WeaponStat stat, float data) {
        if (!stat.getClassType().isInstance(data)) throw new IllegalArgumentException("Tried to give invalid data for stat " + stat + " " + data);
        weaponData.putIfAbsent(weaponTitle, new HashMap<>());
        weaponData.get(weaponTitle).compute(stat, (key, value) -> value == null ? data : (float) value + data);
    }

    public void set(String weaponTitle, WeaponStat stat, String data) {
        if (!stat.getClassType().isInstance(data)) throw new IllegalArgumentException("Tried to give invalid data for stat " + stat + " " + data);
        weaponData.putIfAbsent(weaponTitle, new HashMap<>());
        weaponData.get(weaponTitle).put(stat, data);
    }

    @Override
    public String toString() {
        return "StatsData{" +
                "playerData=" + playerData +
                ", weaponData=" + weaponData +
                '}';
    }
}