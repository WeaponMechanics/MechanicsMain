package me.deecaad.weaponmechanics.weapon.explode;

import me.deecaad.core.effects.types.ParticleEffect;
import me.deecaad.core.utils.LogLevel;
import me.deecaad.core.utils.NumberUtils;
import me.deecaad.core.utils.StringUtils;
import me.deecaad.weaponmechanics.WeaponMechanics;
import me.deecaad.weaponmechanics.utils.MaterialHelper;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.libs.jline.internal.Nullable;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.scheduler.BukkitRunnable;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static me.deecaad.weaponmechanics.WeaponMechanics.debug;

public class Explosion {

    private static Map<Block, Material> resetMaterialMap = new HashMap<>();
    private static Material air = Material.valueOf("AIR"); // Times like this that I want XMaterial

    private final ExplosionShape shape;
    private final int regenerationDelay;
    private final int regenerationNoise;
    private final boolean isBlacklist;
    private final Map<String, Integer> blockData;

    public Explosion(@Nonnull ExplosionShape shape,
                     int regenerationDelay,
                     @Nonnegative int regenerationNoise,
                     boolean isBlacklist,
                     @Nonnull Map<String, Integer> blockData) {

        this.shape = shape;
        this.regenerationDelay = regenerationDelay;
        this.regenerationNoise = regenerationNoise;
        this.isBlacklist = isBlacklist;
        this.blockData = blockData;
    }

    public boolean isRegenerateBlocks() {
        return regenerationDelay > 0;
    }

    /**
     * Triggers the explosion at the given location
     *
     * @param origin The center of the explosion
     */
    public void explode(Location origin) {
        List<Block> blocks = shape.getBlocks(origin);
        Map<LivingEntity, Double> entities = shape.getEntities(origin);

        for (Block block: blocks) {

            // The block was already destroyed, we don't want to stack explosions
            if (resetMaterialMap.containsKey(block)) {
                return;
            }

            // Break the block, as long as it's not already air
            if (!MaterialHelper.isAir(block.getType())) {

                Material beforeExplosionType = block.getType();
                block.setType(air, false);

                // Handle block regeneration
                if (isRegenerateBlocks()) {
                    resetMaterialMap.put(block, beforeExplosionType);

                    boolean isSuccessful = regenerate(block);
                    debug.validate(isSuccessful, "Failed to regenerate block " + block + ", is your server lagging? Check /tps");
                }
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

    /**
     * Regenerates the given block, if possible. The block
     * is regenerated after the delay defined by this
     * <code>Explosion</code>'s delay and noise.
     *
     * @param block Which block to regenerate
     * @return true if no errors
     */
    public boolean regenerate(Block block) {
        Material type = resetMaterialMap.get(block);

        if (type == null) return false;

        int noise = NumberUtils.random(-regenerationNoise, regenerationNoise);
        int delay = noise + regenerationDelay;

        if (delay <= 0) {
            debug.log(LogLevel.ERROR, "A block was attempted to regenerate with a negative delay.",
                    "Make sure your Regenerate_After_Ticks and Noise_In_Ticks cannot make a negative number!");

            return false;
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                block.setType(type, false);
            }
        }.runTaskLater(WeaponMechanics.getPlugin(), delay);

        return true;
    }

}
