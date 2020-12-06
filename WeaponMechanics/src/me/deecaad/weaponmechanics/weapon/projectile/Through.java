package me.deecaad.weaponmechanics.weapon.projectile;

import me.deecaad.compatibility.CompatibilityAPI;
import me.deecaad.core.file.Serializer;
import me.deecaad.core.utils.LogLevel;
import me.deecaad.core.utils.MaterialHelper;
import me.deecaad.core.utils.StringUtils;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static me.deecaad.weaponmechanics.WeaponMechanics.debug;

public class Through implements Serializer<Through> {

    private ThroughData blocks;
    private ThroughData entities;

    /**
     * Empty constructor to be used as serializer
     */
    public Through() { }

    public Through(ThroughData blocks, ThroughData entities) {
        this.blocks = blocks;
        this.entities = entities;
    }

    /**
     * @return through data of blocks
     */
    public ThroughData getBlocks() {
        return this.blocks;
    }

    /**
     * @return through data of entities
     */
    public ThroughData getEntities() {
        return this.entities;
    }

    @Override
    public String getKeyword() {
        return "Through";
    }

    @Override
    public Through serialize(File file, ConfigurationSection configurationSection, String path) {
        ThroughData blocks = tryThroughData(file, configurationSection, path + ".Blocks", true);
        ThroughData entities = tryThroughData(file, configurationSection, path + ".Entities", false);
        if (blocks == null && entities == null) {
            return null;
        }
        return new Through(blocks, entities);
    }

    public ThroughData tryThroughData(File file, ConfigurationSection configurationSection, String path, boolean blocks) {
        List<?> list = configurationSection.getList(path + ".List");
        if (list == null || list.isEmpty()) return null;

        Map<String, ExtraThroughData> map = new HashMap<>();
        for (Object data : list) {
            String[] split = StringUtils.split(data.toString());

            if (split.length == 1) {
                debug.error("Found an invalid value in configurations!",
                        "Tried to specify per " + (blocks ? "block" : "entity") + " speed/damage modifiers but only partially completed it",
                        StringUtils.foundAt(file, path));
            }

            double speedModifier = 1.0;
            if (split.length >= 2) {
                try {
                    speedModifier = Double.parseDouble(split[1]);
                } catch (NumberFormatException e) {
                    debug.log(LogLevel.ERROR,
                            "Found an invalid value in configurations!",
                            "Located at file " + file + " in " + path + ".List (" + data.toString() + ") in configurations",
                            "Tried to get get number from " + split[1] + ", but it wasn't a number?");
                    continue;
                }
            }

            double damageModifier = 0;
            if (split.length >= 3) {
                try {
                    damageModifier = Double.parseDouble(split[2]);
                } catch (NumberFormatException e) {
                    debug.log(LogLevel.ERROR,
                            "Found an invalid value in configurations!",
                            "Located at file " + file + " in " + path + ".List (" + data.toString() + ") in configurations",
                            "Tried to get get number from " + split[2] + ", but it wasn't a number?");
                    continue;
                }
            }

            ExtraThroughData extraThroughData;
            if (speedModifier == 1.0 && damageModifier == 0) {
                extraThroughData = null;
            } else {
                extraThroughData = new ExtraThroughData(speedModifier, damageModifier);
            }

            if (blocks) { // blocks
                ItemStack itemStack;
                try {
                    itemStack = MaterialHelper.fromStringToItemStack(split[0].toUpperCase());
                } catch (IllegalArgumentException e) {
                    debug.log(LogLevel.ERROR,
                            "Found an invalid material in configurations!",
                            "Located at file " + file + " in " + path + ".List (" + data.toString() + ") in configurations");
                    continue;
                }
                if (CompatibilityAPI.getVersion() >= 1.13) {
                    map.put(itemStack.getType().name(), extraThroughData);
                } else {
                    map.put(itemStack.getType().name() + ":" + itemStack.getData().getData(), extraThroughData);
                }
            } else { // entities
                EntityType entity;
                try {
                    entity = EntityType.valueOf(split[0].toUpperCase());
                } catch (IllegalArgumentException e) {
                    debug.log(LogLevel.ERROR,
                            "Found an invalid entity type in configurations!",
                            "Located at file " + file + " in " + path + ".List (" + data.toString() + ") in configurations");
                    continue;
                }
                map.put(entity.name(), extraThroughData);
            }
        }
        if (map.isEmpty()) {
            debug.log(LogLevel.ERROR,
                    "For some reason any value in list wasn't valid!",
                    StringUtils.foundAt(file, path + ".List"));
            return null;
        }

        double defaultSpeedModifier = configurationSection.getDouble(path + ".Default_Speed_Modifier", 1.0);
        double defaultDamageModifier = configurationSection.getDouble(path + ".Default_Damage_Modifier", 0);
        int maximumPassThroughs = configurationSection.getInt(path + ".Maximum_Pass_Throughs", 2);
        boolean whitelist = configurationSection.getBoolean(path + ".Whitelist", true);
        return new ThroughData(defaultSpeedModifier, defaultDamageModifier, maximumPassThroughs, whitelist, map);
    }

    public static class ThroughData {

        private final double defaultSpeedModifier;
        private final double defaultDamageModifier;
        private final int maximumPassThroughs;
        private final boolean whitelist;
        private final Map<String, ExtraThroughData> list;

        public ThroughData(double defaultSpeedModifier, double defaultDamageModifier, int maximumPassThroughs, boolean whitelist, Map<String, ExtraThroughData> list) {
            this.defaultSpeedModifier = defaultSpeedModifier;
            this.defaultDamageModifier = defaultDamageModifier;
            this.maximumPassThroughs = maximumPassThroughs;
            this.whitelist = whitelist;
            this.list = list;
        }

        /**
         * @param material the material to check
         * @param data the data to check (only in versions before 1.13)
         * @return the extra through data or null if projectile should be removed
         */
        public ExtraThroughData getModifiers(Material material, byte data) {
            return getModifiers((CompatibilityAPI.getVersion() >= 1.13 ? material.name() : material.name() + ":" + data));
        }

        /**
         * @param entityType the entity type to check
         * @return the extra through data or null if projectile should be removed
         */
        public ExtraThroughData getModifiers(EntityType entityType) {
            return getModifiers(entityType.name());
        }

        /**
         * @param key the key to check
         * @return the extra through data or null if projectile should be removed
         */
        private ExtraThroughData getModifiers(String key) {
            if (!whitelist) {
                // If blacklist and list contains key
                // -> Mark projectile for removal
                // Else return default speed and damage modifiers
                return list.containsKey(key) ? null : new ExtraThroughData(defaultSpeedModifier, defaultDamageModifier);
            }
            // If whitelist and list DOES NOT contain key
            // -> Mark projectile for removal
            // Else return key's own speed and damage modifier OR default speed and damage modifiers
            if (!list.containsKey(key)) return null;
            ExtraThroughData extraThroughData = list.get(key);
            return extraThroughData != null ? extraThroughData : new ExtraThroughData(defaultSpeedModifier, defaultDamageModifier);
        }

        /**
         * This will be used if entity or block doesn't have any specific speed modifier
         *
         * @return the default speed modifier
         */
        public double getDefaultSpeedModifier() {
            return this.defaultSpeedModifier;
        }

        /**
         * This will be used if entity or block doesn't have any specific damage modifier
         *
         * @return the default speed modifier
         */
        public double getDefaultDamageModifier() {
            return this.defaultDamageModifier;
        }

        /**
         * @return maximum amounts entities or blocks can be passed through
         */
        public int getMaximumPassThroughs() {
            return this.maximumPassThroughs;
        }

        /**
         * @return whether this is whitelist or blacklist
         */
        public boolean isWhitelist() {
            return this.whitelist;
        }

        /**
         * ExtraThroughData value CAN be null.
         *
         * @return the map containing entities or blocks as key and extra data as value
         */
        public Map<String, ExtraThroughData> getList() {
            return this.list;
        }
    }

    public static class ExtraThroughData {

        private final double speedModifier;
        private final double damageModifier;

        public ExtraThroughData(double speedModifier, double damageModifier) {
            this.speedModifier = speedModifier;
            this.damageModifier = damageModifier;
        }

        /**
         * @return the speed modifier or 0.0 if should not be used
         */
        public double getSpeedModifier() {
            return speedModifier;
        }

        /**
         * @return the damage modifier or 0.0 if should not be used
         */
        public double getDamageModifier() {
            return damageModifier;
        }
    }
}