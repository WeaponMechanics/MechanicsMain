package me.deecaad.weaponmechanics.weapon.explode;

import me.deecaad.core.effects.types.ParticleEffect;
import me.deecaad.core.utils.StringUtils;
import me.deecaad.weaponmechanics.WeaponMechanics;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.libs.jline.internal.Nullable;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.block.BlockExplodeEvent;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;

public class Explosion {

    private final LivingEntity source;
    private final ExplosionShape shape;

    public Explosion(@Nullable LivingEntity source, @Nonnull ExplosionShape shape) {
        this.source = source;
        this.shape = shape;
    }

    public void explode(Location origin) {
        List<Block> blocks = shape.getBlocks(origin);
        Map<LivingEntity, Double> entities = shape.getEntities(origin);

        for (Block block: blocks) {
            if (block.getType() != Material.AIR) block.breakNaturally();

            ParticleEffect effect = new ParticleEffect(Particle.EXPLOSION_LARGE, 2, 0.5, 0.5, 1, null);
            effect.spawn(WeaponMechanics.getPlugin(), block.getLocation());
        }

        for (Map.Entry<LivingEntity, Double> entry : entities.entrySet()) {
            LivingEntity entity = entry.getKey();
            double impact = entry.getValue();

            entity.sendMessage(StringUtils.color("&cYou suffered " + impact * 100 + "% of the impact"));
        }
    }
}
