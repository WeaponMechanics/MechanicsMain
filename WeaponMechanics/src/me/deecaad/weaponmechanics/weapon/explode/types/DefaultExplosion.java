package me.deecaad.weaponmechanics.weapon.explode.types;

import me.deecaad.compatibility.CompatibilityAPI;
import me.deecaad.compatibility.projectile.HitBox;
import me.deecaad.core.utils.DebugUtil;
import me.deecaad.core.utils.LogLevel;
import me.deecaad.core.utils.NumberUtils;
import me.deecaad.weaponmechanics.utils.MaterialHelper;
import me.deecaad.weaponmechanics.weapon.explode.Explosion;
import net.minecraft.server.v1_15_R1.AxisAlignedBB;
import net.minecraft.server.v1_15_R1.EnchantmentProtection;
import net.minecraft.server.v1_15_R1.EntityFallingBlock;
import net.minecraft.server.v1_15_R1.EntityHuman;
import net.minecraft.server.v1_15_R1.EntityLiving;
import net.minecraft.server.v1_15_R1.EntityTNTPrimed;
import net.minecraft.server.v1_15_R1.MathHelper;
import net.minecraft.server.v1_15_R1.RayTrace;
import net.minecraft.server.v1_15_R1.Vec3D;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_15_R1.event.CraftEventFactory;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * This explosion uses the minecraft explosion system,
 * instead of using a shape.
 *
 * https://minecraft.gamepedia.com/Explosion
 */
public class DefaultExplosion implements Explosion {

    private static final int GRID_SIZE = 16;
    private static final int BOUND = GRID_SIZE - 1;
    private static final double DECAY_RATE = 0.3;
    private static final double ABSORB_RATE = 0.3;

    private float yield;
    
    public DefaultExplosion(double yield) {
        this.yield = (float) yield;
    }
    
    @Nonnull
    @Override
    public List<Block> getBlocks(@Nonnull Location origin) {
        if (yield >= 0.1F) {
            World world = origin.getWorld();
            int x = (int) origin.getX();
            int y = (int) origin.getY();
            int z = (int) origin.getZ();

            List<Block> set = new ArrayList<>();

            // Separates the explosion into a 16 by 16 by 16
            // grid.
            for (int k = 0; k < GRID_SIZE; ++k) {
                for (int i = 0; i < GRID_SIZE; ++i) {
                    for (int j = 0; j < GRID_SIZE; ++j) {

                        // Checking if the the point defined by (k, i, j) is on the grid
                        if (k == 0 || k == BOUND || i == 0 || i == BOUND || j == 0 || j == BOUND) {

                            // d representing change, so change in x, change in y, etc
                            double dx = ((double) k / BOUND * 2 - 1);
                            double dy = ((double) i / BOUND * 2 - 1);
                            double dz = ((double) j / BOUND * 2 - 1);
                            double length = Math.sqrt(dx * dx + dy * dy + dz * dz);

                            // normalize
                            dx /= length;
                            dy /= length;
                            dz /= length;

                            // Slightly randomized intensity, based on the yield of the explosion
                            float intensity = yield * (0.7F + NumberUtils.random().nextFloat() * 0.6F);

                            while(intensity > 0.0f) {
                                Block block = world.getBlockAt(x, y, z);
                                Material type = block.getType();

                                boolean isAir = MaterialHelper.isAir(type);

                                if (isAir) {
                                    float resistance = MaterialHelper.getBlastResistance(type);
                                    //if (this.source != null) {
                                    //    f2 = this.source.a(this, this.world, blockposition, iblockdata, fluid, f2);
                                    //}

                                    intensity -= (resistance + 0.3F) * ABSORB_RATE;
                                }

                                if (intensity > 0.0F && y < 256 && y >= 0) {
                                    set.add(block);
                                }

                                x += dx * DECAY_RATE;
                                y += dy * DECAY_RATE;
                                z += dz * DECAY_RATE;

                                // Ray decays over longer distance
                                intensity -= DECAY_RATE * 0.75;
                            }
                        }
                    }
                }
            }
            return set;
        }
        else return new ArrayList<>();
    }
    
    @Nonnull
    @Override
    public List<LivingEntity> getEntities(@Nonnull Location origin) {
        double damageRadius = yield * 2.0F;
        double damageRadiusOuter = damageRadius + 1;

        World world = origin.getWorld();
        double x = origin.getX();
        double y = origin.getY();
        double z = origin.getZ();

        if (world == null) {
            DebugUtil.log(LogLevel.ERROR, "Explosion in null world? Location: " + origin, "Please report error to devs");
            return new ArrayList<>();
        }

        // Get all entities within the damageable radius
        Collection<Entity> entities = world.getNearbyEntities(origin, damageRadiusOuter, damageRadiusOuter, damageRadiusOuter);
        Vector vector = new Vector(x, y, z);

        for (Entity entity: entities) {
            Vector entityLocation = entity.getLocation().toVector();

            // Gets the "rate" or percentage of how far the entity
            // is from the explosion. For example, it the distance
            // is 8 and explosion radius is 10, the rate will be 4/5
            double impactRate = entityLocation.distance(vector) / damageRadius;

            if (impactRate > 1.0D) {
                DebugUtil.log(LogLevel.WARN, "Somehow an entity was damaged outside of the explosion's radius",
                        "is the server lagging?");
                continue;
            }

            Vector betweenEntityAndExplosion = entityLocation.subtract(vector);
            double distance = betweenEntityAndExplosion.length();

            // This should never be false due to double inaccuracy
            //if (distance != 0.0)

            // Normalize
            betweenEntityAndExplosion.multiply(1 / distance);

            double exposure = getExposure(vector, entity);
        }

        for(int l1 = 0; l1 < list.size(); ++l1) {
            Entity entity = (Entity)list.get(l1);
            if (!entity.ca()) {
                double d7 = (double)(MathHelper.sqrt(entity.c(vec3d)) / f3);
                if (d7 <= 1.0D) {
                    double d8 = entity.locX() - this.posX;
                    double d9 = entity.getHeadY() - this.posY;
                    double d10 = entity.locZ() - this.posZ;
                    double d11 = (double)MathHelper.sqrt(d8 * d8 + d9 * d9 + d10 * d10);
                    if (d11 != 0.0D) {
                        d8 /= d11;
                        d9 /= d11;
                        d10 /= d11;
                        double d12 = (double)a(vec3d, entity);
                        double d13 = (1.0D - d7) * d12;
                        CraftEventFactory.entityDamage = this.source;
                        entity.forceExplosionKnockback = false;
                        boolean wasDamaged = entity.damageEntity(this.b(), (float)((int)((d13 * d13 + d13) / 2.0D * 7.0D * (double)f3 + 1.0D)));
                        CraftEventFactory.entityDamage = null;
                        if (wasDamaged || entity instanceof EntityTNTPrimed || entity instanceof EntityFallingBlock || entity.forceExplosionKnockback) {
                            double d14 = d13;
                            if (entity instanceof EntityLiving) {
                                d14 = EnchantmentProtection.a((EntityLiving)entity, d13);
                            }

                            entity.setMot(entity.getMot().add(d8 * d14, d9 * d14, d10 * d14));
                            if (entity instanceof EntityHuman) {
                                EntityHuman entityhuman = (EntityHuman)entity;
                                if (!entityhuman.isSpectator() && (!entityhuman.isCreative() || !entityhuman.abilities.isFlying)) {
                                    this.l.put(entityhuman, new Vec3D(d8 * d13, d9 * d13, d10 * d13));
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public static double getExposure(Vector vec3d, Entity entity) {
        HitBox box = CompatibilityAPI.getCompatibility().getProjectileCompatibility().getHitBox(entity);

        // Get the dimensions of the bounding box
        double width = box.getWidth();
        double height = box.getHeight();
        double depth = box.getDepth();

        // Gets the size of the grid in each axis
        double gridX = 1.0D / (width * 2.0D + 1.0D);
        double gridY = 1.0D / (height * 2.0D + 1.0D);
        double gridZ = 1.0D / (depth * 2.0D + 1.0D);

        // Outside of the grid
        if (gridX < 0 || gridY < 0 || gridZ < 0) return 0;

        // todo name
        double d3 = (1.0D - Math.floor(1.0D / gridX) * gridX) / 2.0D;
        double d4 = (1.0D - Math.floor(1.0D / gridZ) * gridZ) / 2.0D;

        int i = 0;
        int j = 0;

        for (double x = 0; x <= 1; x += gridX) {
            for (double y = 0; y <= 1; y += gridY) {
                for (double z = 0; z <= 1; z += gridZ) {
                    double a = NumberUtils.lerp(x, box.min.getX(), box.max.getX());
                    double b = NumberUtils.lerp(y, box.min.getY(), box.max.getY());
                    double c = NumberUtils.lerp(z, box.min.getZ(), box.max.getZ());
                }
            }
        }


        if (d0 >= 0.0D && d1 >= 0.0D && d2 >= 0.0D) {
            int i = 0;
            int j = 0;

            for(float f = 0.0F; f <= 1.0F; f = (float)((double)f + d0)) {
                for(float f1 = 0.0F; f1 <= 1.0F; f1 = (float)((double)f1 + d1)) {
                    for(float f2 = 0.0F; f2 <= 1.0F; f2 = (float)((double)f2 + d2)) {
                        double d5 = MathHelper.d((double)f, axisalignedbb.minX, axisalignedbb.maxX);
                        double d6 = MathHelper.d((double)f1, axisalignedbb.minY, axisalignedbb.maxY);
                        double d7 = MathHelper.d((double)f2, axisalignedbb.minZ, axisalignedbb.maxZ);
                        Vec3D vec3d1 = new Vec3D(d5 + d3, d6, d7 + d4);
                        if (entity.world.rayTrace(new RayTrace(vec3d1, vec3d, BlockCollisionOption.OUTLINE, FluidCollisionOption.NONE, entity)).getType() == EnumMovingObjectType.MISS) {
                            ++i;
                        }

                        ++j;
                    }
                }
            }

            return (float)i / (float)j;
        } else {
            return 0.0F;
        }
    }
}
