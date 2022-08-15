package me.deecaad.weaponmechanics.wrappers;

import me.deecaad.weaponmechanics.weapon.stats.PlayerStat;
import me.deecaad.weaponmechanics.weapon.stats.WeaponStat;

import java.util.HashMap;
import java.util.Map;

public class StatsData {

    private Map<PlayerStat, Object> playerData;
    private Map<String, StatsWeaponData> weaponData;

    private static class StatsWeaponData {

        private Map<WeaponStat, Object> data;

        public StatsWeaponData() {
            this.data = new HashMap<>();
        }


    }
}