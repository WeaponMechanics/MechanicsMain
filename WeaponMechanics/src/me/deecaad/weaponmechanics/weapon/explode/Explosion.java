package me.deecaad.weaponmechanics.weapon.explode;

import me.deecaad.compatibility.CompatibilityAPI;
import me.deecaad.core.effects.types.ParticleEffect;
import me.deecaad.core.utils.LogLevel;
import me.deecaad.core.utils.NumberUtils;
import me.deecaad.core.utils.StringUtils;
import me.deecaad.weaponmechanics.WeaponMechanics;
import me.deecaad.weaponmechanics.utils.MaterialHelper;
import me.deecaad.weaponmechanics.weapon.BlockDamageData;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static me.deecaad.weaponmechanics.WeaponMechanics.debug;

public class Explosion {

    private final ExplosionShape shape;
    private final boolean isBreakBlocks;
    private final int regenerationDelay;
    private final int regenerationNoise;
    private final boolean isBlacklist;
    private final Set<String> materials;
    private final Set<ExplosionTrigger> triggers;

    public Explosion(@Nonnull ExplosionShape shape,
                     boolean isBreakBlocks,
                     int regenerationDelay,
                     @Nonnegative int regenerationNoise,
                     boolean isBlacklist,
                     @Nonnull Set<String> materials,
                     @Nonnull Set<ExplosionTrigger> triggers) {

        this.shape = shape;
        this.isBreakBlocks = isBreakBlocks;
        this.regenerationDelay = regenerationDelay;
        this.regenerationNoise = regenerationNoise;
        this.isBlacklist = isBlacklist;
        this.materials = materials;
        this.triggers = triggers;
    }

    public ExplosionShape getShape() {
        return shape;
    }

    public int getRegenerationDelay() {
        return regenerationDelay;
    }

    public int getRegenerationNoise() {
        return regenerationNoise;
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

        for (Block block: blocks) {
            String mat = block.getType().name() + (CompatibilityAPI.getVersion() < 1.13 ? ":" + block.getData() : "");
            if (isBlacklist == materials.contains(mat)) continue;

            // The block was already destroyed, we don't want to stack explosions
            if (BlockDamageData.isBroken(block)) {
                return;
            }

            // Break the block, as long as it's not already air
            if (!MaterialHelper.isAir(block.getType())) {

                int regenTime = regenerationDelay + NumberUtils.random(0, regenerationNoise);
                BlockDamageData.damageBlock(block, 1, 1, true, regenTime);
            }

            ParticleEffect effect = new ParticleEffect(Particle.EXPLOSION_LARGE, 2, 0.5, 0.5, 1, null);
            effect.spawn(WeaponMechanics.getPlugin(), block.getLocation());
        }

        for (Map.Entry<LivingEntity, Double> entry : entities.entrySet()) {
            LivingEntity entity = entry.getKey();
            double impact = entry.getValue();

            entity.sendMessage(StringUtils.color("&cYou suffered " + impact * 100 + "% of the impact"));
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
        ENTITIES,

        /**
         * When the projectile hits a liquid
         */
        LIQUID
    }
}
