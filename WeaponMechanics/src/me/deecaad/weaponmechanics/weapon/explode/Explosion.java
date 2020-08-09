package me.deecaad.weaponmechanics.weapon.explode;

import me.deecaad.core.mechanics.Mechanic;
import me.deecaad.core.mechanics.casters.EntityCaster;
import me.deecaad.core.utils.LogLevel;
import me.deecaad.core.utils.MaterialHelper;
import me.deecaad.core.utils.StringUtils;
import me.deecaad.core.utils.VectorUtils;
import me.deecaad.weaponmechanics.weapon.damage.BlockDamage;
import me.deecaad.weaponmechanics.weapon.damage.BlockDamageData;
import me.deecaad.weaponmechanics.weapon.damage.DamageHandler;
import me.deecaad.weaponmechanics.weapon.explode.regeneration.BlockRegenSorter;
import me.deecaad.weaponmechanics.weapon.explode.regeneration.LayerDistanceSorter;
import me.deecaad.weaponmechanics.weapon.explode.regeneration.RegenerationData;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static me.deecaad.weaponmechanics.WeaponMechanics.debug;

public class Explosion {

    private static DamageHandler damageHandler = new DamageHandler();

    private final String weaponTitle;
    private final ExplosionShape shape;
    private final ExplosionExposure exposure;
    private final BlockDamage blockDamage;
    private final RegenerationData regeneration;
    private final Set<ExplosionTrigger> triggers;
    private final int delay;
    private final boolean isKnockback;
    private final List<Mechanic> mechanics;

    public Explosion(@Nullable String weaponTitle,
                     @Nonnull ExplosionShape shape,
                     @Nonnull ExplosionExposure exposure,
                     @Nullable BlockDamage blockDamage,
                     @Nullable RegenerationData regeneration,
                     @Nonnull Set<ExplosionTrigger> triggers,
                     @Nonnegative int delay,
                     boolean isKnockback) {

        this(weaponTitle, shape, exposure, blockDamage, regeneration, triggers, delay, isKnockback, null);
    }

    public Explosion(@Nullable String weaponTitle,
                     @Nonnull ExplosionShape shape,
                     @Nonnull ExplosionExposure exposure,
                     @Nullable BlockDamage blockDamage,
                     @Nullable RegenerationData regeneration,
                     @Nonnull Set<ExplosionTrigger> triggers,
                     @Nonnegative int delay,
                     boolean isKnockback,
                     @Nullable List<Mechanic> mechanics) {

        this.weaponTitle = weaponTitle;
        this.shape = shape;
        this.exposure = exposure;
        this.blockDamage = blockDamage;
        this.regeneration = regeneration;
        this.triggers = triggers;
        this.delay = delay;
        this.isKnockback = isKnockback;
        this.mechanics = mechanics;
    }

    public static DamageHandler getDamageHandler() {
        return damageHandler;
    }

    public static void setDamageHandler(DamageHandler damageHandler) {
        Explosion.damageHandler = damageHandler;
    }

    public String getWeaponTitle() {
        return weaponTitle;
    }

    public ExplosionShape getShape() {
        return shape;
    }

    public ExplosionExposure getExposure() {
        return exposure;
    }

    public BlockDamage getBlockDamage() {
        return blockDamage;
    }

    public RegenerationData getRegeneration() {
        return regeneration;
    }

    public Set<ExplosionTrigger> getTriggers() {
        return triggers;
    }

    public int getDelay() {
        return delay;
    }

    public boolean isKnockback() {
        return isKnockback;
    }

    public List<Mechanic> getEffects() {
        return mechanics;
    }

    /**
     * Triggers the explosion at the given location
     *
     * @param cause Whoever caused the explosion
     * @param origin The center of the explosion
     */
    public void explode(LivingEntity cause, Location origin) {
        debug.log(LogLevel.DEBUG, "Generating a " + shape + " explosion at " + origin.getBlock());

        List<Block> blocks = blockDamage == null ? new ArrayList<>() : shape.getBlocks(origin);
        Map<LivingEntity, Double> entities = exposure.mapExposures(origin, shape);

        final List<Block> transparent = new ArrayList<>();
        final List<Block> solid = new ArrayList<>();

        for (Block block : blocks) {
            Material type = block.getType();

            if (type.isSolid()) {
                solid.add(block);
            } else if (!MaterialHelper.isAir(type)) {
                transparent.add(block);
            }
        }

        int timeOffset;
        if (regeneration == null) {
            timeOffset = -1;
        } else {
            timeOffset = solid.size() / regeneration.getMaxBlocksPerUpdate() * regeneration.getInterval() + regeneration.getTicksBeforeStart();
        }

        int size = transparent.size();
        for (int i = 0; i < size; i++) {
            Block block = transparent.get(i);

            if (blockDamage.isBlacklisted(block) || BlockDamageData.isBroken(block)) {
                continue;
            }

            // This forces all transparent blocks to regenerate at once.
            // This fixes item sorters breaking and general hopper/redstone stuff
            blockDamage.damage(block, timeOffset);
        }

        BlockRegenSorter sorter = new LayerDistanceSorter(origin, this);
        try {
            solid.sort(sorter);
        } catch (IllegalArgumentException ex) {
            debug.error("A plugin modified the explosion block sorter with an illegal sorter!",
                    "Please report this error to the developers of that plugin", "Sorter: " + sorter);
            debug.log(LogLevel.ERROR, ex);
        }
        size = solid.size();
        for (int i = 0; i < size; i++) {
            Block block = solid.get(i);

            if (blockDamage.isBlacklisted(block) || BlockDamageData.isBroken(block)) {
                continue;
            }

            int regenTime;
            if (regeneration == null) {
                regenTime = -1;
            } else {
                regenTime = regeneration.getTicksBeforeStart() +
                        (i / regeneration.getMaxBlocksPerUpdate()) * regeneration.getInterval();
            }

            blockDamage.damage(block, regenTime);
        }

        if (weaponTitle != null) {
            damageHandler.tryUseExplosion(cause, weaponTitle, entities);

            if (isKnockback) {
                Vector originVector = origin.toVector();
                for (Map.Entry<LivingEntity, Double> entry : entities.entrySet()) {

                    LivingEntity entity = entry.getKey();
                    double exposure = entry.getValue();

                    // Normalized vector between the explosion and entity involved
                    Vector between = VectorUtils.setLength(entity.getLocation().toVector().subtract(originVector), exposure);
                    Vector motion = entity.getVelocity().add(between);

                    entity.setVelocity(motion);
                }
            }
        } else {
            for (Map.Entry<LivingEntity, Double> entry : entities.entrySet()) {
                LivingEntity entity = entry.getKey();
                double impact = entry.getValue();

                entity.sendMessage(StringUtils.color("&cYou suffered " + impact * 100 + "% of the impact"));
            }
        }

        if (mechanics != null) {
            mechanics.forEach(mechanic -> mechanic.cast((EntityCaster) () -> cause));
        }
    }

    public enum ExplosionTrigger {

        /**
         * When the projectile is shot/thrown
         */
        SHOOT,

        /**
         * When the projectile hits a non-air and non-liquid block
         */
        BLOCK,

        /**
         * When the projectile hits an entity
         */
        ENTITY,

        /**
         * When the projectile hits a liquid
         */
        LIQUID
    }
}
