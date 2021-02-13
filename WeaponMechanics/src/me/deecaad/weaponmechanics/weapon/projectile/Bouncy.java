package me.deecaad.weaponmechanics.weapon.projectile;

import me.deecaad.compatibility.CompatibilityAPI;
import me.deecaad.core.file.Serializer;
import me.deecaad.core.utils.LogLevel;
import me.deecaad.core.utils.MaterialUtil;
import me.deecaad.core.utils.StringUtil;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static me.deecaad.weaponmechanics.WeaponMechanics.debug;

public class Bouncy implements Serializer<Bouncy> {

    public static final double REQUIRED_MOTION_TO_BOUNCE = 0.3;

    private int maximumBounceAmount;
    private boolean bounceFromAnyBlock;
    private boolean bounceFromAnyEntity;
    private BouncyData blocks, entities;

    /**
     * Empty for serializers
     */
    public Bouncy() { }

    public Bouncy(int maximumBounceAmount, boolean bounceFromAnyBlock, boolean bounceFromAnyEntity, BouncyData blocks, BouncyData entities) {
        this.maximumBounceAmount = maximumBounceAmount;
        this.bounceFromAnyBlock = bounceFromAnyBlock;
        this.bounceFromAnyEntity = bounceFromAnyEntity;
        this.blocks = blocks;
        this.entities = entities;
    }

    /**
     * @return true if projectile bounced
     */
    public boolean handleBounce(CustomProjectile projectile, Collisions collisions, CollisionData collision, Vector motion) {
        if (projectile.getMotionLength() < REQUIRED_MOTION_TO_BOUNCE) {
            return false;
        }

        ExtraBouncyData extraBouncyData;
        Set<CollisionData> typeCollisions;

        Block bukkitBlock = collision.getBlock();
        if (bukkitBlock != null) {
            extraBouncyData = blocks.getModifiers(bukkitBlock.getType(), bukkitBlock.getData());
            typeCollisions = collisions.getBlockCollisions();
        } else {
            extraBouncyData = entities.getModifiers(collision.getLivingEntity().getType());
            typeCollisions = collisions.getEntityCollisions();
        }

        if (extraBouncyData == null || maximumBounceAmount - collisions.getBlockCollisions().size() - collisions.getEntityCollisions().size() < 0) { // Projectile should die
            typeCollisions.add(collision);
            return false;
        }

        if (extraBouncyData.getSpeedModifier() != 1.0) {
            motion.multiply(extraBouncyData.getSpeedModifier());
        }

        BlockFace hitFace = collision.getBlockFace();
        if (hitFace != null) {

            switch (hitFace) {
                case UP: case DOWN:
                    motion.setY(-motion.getY());
                    break;
                case EAST: case WEST:
                    motion.setX(-motion.getX());
                    break;
                case NORTH: case SOUTH:
                    motion.setZ(-motion.getZ());
            }
        }

        typeCollisions.add(collision);

        return true;
    }

    public boolean hasBlocks() {
        return bounceFromAnyBlock || blocks != null;
    }

    public boolean hasEntities() {
        return bounceFromAnyEntity || entities != null;
    }

    @Override
    public String getKeyword() {
        return "Bouncy";
    }

    @Override
    public Bouncy serialize(File file, ConfigurationSection configurationSection, String path) {
        int maximumBounceAmount = configurationSection.getInt(path + ".Maximum_Bounce_Amount", 2);
        boolean bounceFromAnyBlock = configurationSection.getBoolean(path + ".Blocks.Bounce_From_Any_Block");
        boolean bounceFromAnyEntity = configurationSection.getBoolean(path + ".Entities.Bounce_From_Any_Entity");

        BouncyData blocks = tryBouncyData(file, configurationSection, path + ".Blocks", true);
        BouncyData entities = tryBouncyData(file, configurationSection, path + ".Entities", false);
        if (blocks == null && entities == null && !bounceFromAnyBlock && !bounceFromAnyEntity) {
            return null;
        }
        return new Bouncy(maximumBounceAmount, bounceFromAnyBlock, bounceFromAnyEntity, blocks, entities);
    }

    public BouncyData tryBouncyData(File file, ConfigurationSection configurationSection, String path, boolean blocks) {
        List<?> list = configurationSection.getList(path + ".List");
        if (list == null || list.isEmpty()) return null;

        Map<String, ExtraBouncyData> map = new HashMap<>();
        for (Object data : list) {
            String[] split = StringUtil.split(data.toString());

            double speedModifier = 1.0;
            if (split.length >= 2) {
                try {
                    speedModifier = Double.parseDouble(split[1]);
                } catch (NumberFormatException e) {
                    debug.log(LogLevel.ERROR,
                            StringUtil.foundInvalid("value"),
                            StringUtil.foundAt(file, path + ".List", data.toString().toUpperCase()),
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
                            StringUtil.foundInvalid("value"),
                            StringUtil.foundAt(file, path + ".List", data.toString().toUpperCase()),
                            "Tried to get get number from " + split[2] + ", but it wasn't a number?");
                    continue;
                }
            }

            ExtraBouncyData extraBouncyData;
            if (speedModifier == 1.0 && damageModifier == 0) {
                extraBouncyData = null;
            } else {
                extraBouncyData = new ExtraBouncyData(speedModifier, damageModifier);
            }

            if (blocks) { // blocks
                ItemStack itemStack;
                try {
                    itemStack = MaterialUtil.fromStringToItemStack(split[0].toUpperCase());
                } catch (IllegalArgumentException e) {
                    debug.log(LogLevel.ERROR,
                            StringUtil.foundInvalid("material"),
                            StringUtil.foundAt(file, path + ".List", data.toString()),
                            StringUtil.debugDidYouMean(split[0].toUpperCase(), Material.class));
                    continue;
                }
                if (CompatibilityAPI.getVersion() >= 1.13) {
                    map.put(itemStack.getType().name(), extraBouncyData);
                } else {
                    map.put(itemStack.getType().name() + ":" + itemStack.getData().getData(), extraBouncyData);
                }
            } else { // entities
                EntityType entity;
                try {
                    entity = EntityType.valueOf(split[0].toUpperCase());
                } catch (IllegalArgumentException e) {
                    debug.log(LogLevel.ERROR,
                            StringUtil.foundInvalid("material"),
                            StringUtil.foundAt(file, path + ".List", data.toString()),
                            StringUtil.debugDidYouMean(split[0].toUpperCase(), EntityType.class));
                    continue;
                }
                map.put(entity.name(), extraBouncyData);
            }
        }
        if (map.isEmpty()) {
            debug.log(LogLevel.ERROR,
                    "For some reason any value in list wasn't valid!",
                    StringUtil.foundAt(file, path + ".List"));
            return null;
        }

        double defaultSpeedModifier = configurationSection.getDouble(path + ".Default_Speed_Modifier", 1.0);
        double defaultDamageModifier = configurationSection.getDouble(path + ".Default_Damage_Modifier", 0);
        boolean whitelist = configurationSection.getBoolean(path + ".Whitelist", true);
        return new BouncyData(defaultSpeedModifier, defaultDamageModifier, whitelist, map);
    }

    public static class BouncyData {

        private final double defaultSpeedModifier;
        private final double defaultDamageModifier;
        private final boolean whitelist;
        private final Map<String, ExtraBouncyData> list;

        public BouncyData(double defaultSpeedModifier, double defaultDamageModifier, boolean whitelist, Map<String, ExtraBouncyData> list) {
            this.defaultSpeedModifier = defaultSpeedModifier;
            this.defaultDamageModifier = defaultDamageModifier;
            this.whitelist = whitelist;
            this.list = list;
        }

        /**
         * @param material the material to check
         * @param data the data to check (only in versions before 1.13)
         * @return the extra through data or null if projectile should be removed
         */
        public ExtraBouncyData getModifiers(Material material, byte data) {
            return getModifiers((CompatibilityAPI.getVersion() >= 1.13 ? material.name() : material.name() + ":" + data));
        }

        /**
         * @param entityType the entity type to check
         * @return the extra through data or null if projectile should be removed
         */
        public ExtraBouncyData getModifiers(EntityType entityType) {
            return getModifiers(entityType.name());
        }

        /**
         * @param key the key to check
         * @return the extra through data or null if projectile should be removed
         */
        private ExtraBouncyData getModifiers(String key) {
            if (!whitelist) {
                // If blacklist and list contains key
                // -> Mark projectile for removal
                // Else return default speed and damage modifiers
                return list.containsKey(key) ? null : new ExtraBouncyData(defaultSpeedModifier, defaultDamageModifier);
            }
            // If whitelist and list DOES NOT contain key
            // -> Mark projectile for removal
            // Else return key's own speed and damage modifier OR default speed and damage modifiers
            if (!list.containsKey(key)) return null;
            ExtraBouncyData extraBouncyData = list.get(key);
            return extraBouncyData != null ? extraBouncyData : new ExtraBouncyData(defaultSpeedModifier, defaultDamageModifier);
        }
    }

    public static class ExtraBouncyData {

        private final double speedModifier;
        private final double damageModifier;

        public ExtraBouncyData(double speedModifier, double damageModifier) {
            this.speedModifier = speedModifier;
            this.damageModifier = damageModifier;
        }

        /**
         * @return the speed modifier or 1.0 if should not be used
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