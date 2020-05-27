package me.deecaad.weaponmechanics.weapon.explode;

import me.deecaad.compatibility.CompatibilityAPI;
import me.deecaad.core.utils.LogLevel;
import me.deecaad.core.utils.StringUtils;
import me.deecaad.weaponmechanics.utils.MaterialHelper;
import me.deecaad.weaponmechanics.weapon.BlockDamageData;
import me.deecaad.weaponmechanics.weapon.explode.regeneration.RegenerationData;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static me.deecaad.weaponmechanics.WeaponMechanics.debug;

public class Explosion {

    private final ExplosionShape shape;
    private final boolean isBreakBlocks;
    private final RegenerationData regeneration;
    private final boolean isBlacklist;
    private final Set<String> materials;
    private final Set<ExplosionTrigger> triggers;

    public Explosion(@Nonnull ExplosionShape shape,
                     boolean isBreakBlocks,
                     @Nonnull RegenerationData regeneration,
                     boolean isBlacklist,
                     @Nonnull Set<String> materials,
                     @Nonnull Set<ExplosionTrigger> triggers) {

        this.shape = shape;
        this.isBreakBlocks = isBreakBlocks;
        this.regeneration = regeneration;
        this.isBlacklist = isBlacklist;
        this.materials = materials;
        this.triggers = triggers;
    }

    public ExplosionShape getShape() {
        return shape;
    }

    public RegenerationData getRegeneration() {
        return regeneration;
    }

    public boolean isBlacklist() {
        return isBlacklist;
    }

    public Set<String> getMaterials() {
        return materials;
    }

    public Set<ExplosionTrigger> getTriggers() {
        return triggers;
    }

    /**
     * Triggers the explosion at the given location
     *
     * @param origin The center of the explosion
     */
    public void explode(Location origin) {
        debug.log(LogLevel.DEBUG, "Generating a " + shape + " explosion at " + origin.getBlock());

        List<Block> blocks = isBreakBlocks ? shape.getBlocks(origin) : new ArrayList<>();
        Map<LivingEntity, Double> entities = shape.getEntities(origin);

        // Sort blocks to the lower blocks come first
        // so lower blocks regenerate first
        blocks.sort(Comparator.comparingInt(Block::getY));

        List<Block> transparent = new ArrayList<>();
        List<Block> air = new ArrayList<>();
        List<Block> solid = new ArrayList<>();

        for (Block block : blocks) {
            Material type = block.getType();

            if (MaterialHelper.isAir(type)) {
                air.add(block);
            } else if (type.isSolid()) {
                solid.add(block);
            } else {
                transparent.add(block);
            }
        }

        int timeOffset = solid.size() / regeneration.getMaxBlocksPerUpdate() * regeneration.getInterval();
        int size = transparent.size();
        for (int i = 0; i < size; i++) {
            Block block = transparent.get(i);

            if (isBlacklisted(block) || BlockDamageData.isBroken(block)) {
                continue;
            }

            int regenTime = regeneration.getTicksBeforeStart() + timeOffset +
                    (i / regeneration.getMaxBlocksPerUpdate()) * regeneration.getInterval();

            BlockDamageData.damageBlock(block, 1, 1, true, regenTime);
        }

        size = blocks.size();
        for (int i = 0; i < size; i++) {
            Block block = blocks.get(i);

            if (isBlacklisted(block) || BlockDamageData.isBroken(block)) {
                continue;
            }

            int regenTime = regeneration.getTicksBeforeStart() +
                    (i / regeneration.getMaxBlocksPerUpdate()) * regeneration.getInterval();

            BlockDamageData.damageBlock(block, 1, 1, true, regenTime);
        }

        // Handle entity damaging
        for (Map.Entry<LivingEntity, Double> entry : entities.entrySet()) {
            LivingEntity entity = entry.getKey();
            double impact = entry.getValue();

            entity.sendMessage(StringUtils.color("&cYou suffered " + impact * 100 + "% of the impact"));
        }
    }

    public boolean isBlacklisted(Block block) {
        String mat = block.getType().name();
        return isBlacklist == (materials.contains(mat) || (
                CompatibilityAPI.getVersion() < 1.13 && isBlacklist == materials.contains(mat + ":" + block.getData())
        ));
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
        ENTITIES,

        /**
         * When the projectile hits a liquid
         */
        LIQUID
    }
}
