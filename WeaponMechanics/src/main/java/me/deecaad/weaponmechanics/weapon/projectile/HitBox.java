package me.deecaad.weaponmechanics.weapon.projectile;

import me.deecaad.core.compatibility.CompatibilityAPI;
import me.deecaad.core.file.Configuration;
import me.deecaad.core.file.IValidator;
import me.deecaad.core.file.serializers.ColorSerializer;
import me.deecaad.core.utils.LogLevel;
import me.deecaad.weaponmechanics.WeaponMechanics;
import me.deecaad.weaponmechanics.weapon.damage.DamagePoint;
import me.deecaad.weaponmechanics.weapon.projectile.weaponprojectile.RayTraceResult;
import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

import static me.deecaad.weaponmechanics.WeaponMechanics.debug;

public class HitBox implements IValidator {

    /**
     * Simple final modifier for front hit adjusting.
     * Basically -0.2 means that 20% of hit box's front
     */
    private static final double FRONT_HIT = -0.2;

    private Block block;
    private LivingEntity livingEntity;

    private double minX;
    private double minY;
    private double minZ;
    private double maxX;
    private double maxY;
    private double maxZ;

    private Collection<HitBox> voxelShape;

    /**
     * Empty constructor be used as validator
     */
    public HitBox() { }

    public HitBox(Vector start, Vector end) {
        this(start.getX(), start.getY(), start.getZ(), end.getX(), end.getY(), end.getZ());
    }

    public HitBox(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
        modify(minX, minY, minZ, maxX, maxY, maxZ);
    }

    public HitBox modify(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
        this.minX = Math.min(minX, maxX);
        this.minY = Math.min(minY, maxY);
        this.minZ = Math.min(minZ, maxZ);
        this.maxX = Math.max(minX, maxX);
        this.maxY = Math.max(minY, maxY);
        this.maxZ = Math.max(minZ, maxZ);
        return this;
    }

    public void setBlockHitBox(Block block) {
        if (livingEntity != null) throw new IllegalArgumentException("Can't set living entity for block hitbox");
        this.block = block;
    }

    public void setLivingEntity(LivingEntity livingEntity) {
        if (block != null) throw new IllegalArgumentException("Can't set block for living entity hitbox");
        this.livingEntity = livingEntity;
    }

    public void addVoxelShapeParts(Collection<HitBox> hitBoxes) {
        if (voxelShape == null) {
            voxelShape = hitBoxes;
        } else {
            voxelShape = new ArrayList<>(hitBoxes);
        }
    }

    public void addVoxelShapePart(HitBox hitBox) {
        if (voxelShape == null) voxelShape = new ArrayList<>(2);
        voxelShape.add(hitBox);
    }

    public double getMinX() {
        return minX;
    }

    public double getMinY() {
        return minY;
    }

    public double getMinZ() {
        return minZ;
    }

    public double getMaxX() {
        return maxX;
    }

    public double getMaxY() {
        return maxY;
    }

    public double getMaxZ() {
        return maxZ;
    }

    public double getWidth() {
        return this.maxX - this.minX;
    }

    public double getDepth() {
        return this.maxZ - this.minZ;
    }

    public double getHeight() {
        return this.maxY - this.minY;
    }

    public double getCenterX() {
        return this.minX + this.getWidth() * 0.5D;
    }

    public double getCenterY() {
        return this.minY + this.getHeight() * 0.5D;
    }

    public double getCenterZ() {
        return this.minZ + this.getDepth() * 0.5D;
    }

    public Vector getMin() {
        return new Vector(minX, minY, minZ);
    }

    public Vector getMax() {
        return new Vector(maxX, maxY, maxZ);
    }

    /**
     * Check whether point collides with this hitbox
     *
     * @param point the point to check
     * @return true if collides
     */
    public boolean collides(Vector point) {
        if (point == null) return false;
        return point.getX() >= minX
                && point.getX() <= maxX
                && point.getY() >= minY
                && point.getY() <= maxY
                && point.getZ() >= minZ
                && point.getZ() <= maxZ;
    }

    public DamagePoint getDamagePoint(Vector hitLocation, Vector normalizedMotion) {
        return getDamagePoint(hitLocation, normalizedMotion, livingEntity);
    }

    /**
     * @param hitLocation the entity hit location
     * @param normalizedMotion the normalized direction
     * @return the damage point or null if tried to cast when living entity was not defined
     */
    private DamagePoint getDamagePoint(Vector hitLocation, Vector normalizedMotion, LivingEntity livingEntity) {
        if (livingEntity == null) return null;
        Configuration basicConfiguration = WeaponMechanics.getBasicConfigurations();

        EntityType type = livingEntity.getType();
        double entityHeight = maxY - minY;

        double hitY = hitLocation.getY();

        // Check HEAD
        double head = basicConfiguration.getDouble("Entity_Hitboxes." + type.name() + "." + DamagePoint.HEAD.name());
        if (head > 0.0 && maxY - (entityHeight * head) < hitY) {
            return DamagePoint.HEAD;
        }

        // Check BODY
        double body = basicConfiguration.getDouble("Entity_Hitboxes." + type.name() + "." + DamagePoint.BODY.name());
        if (body >= 1.0 || body > 0.0 && maxY - (entityHeight * (head + body)) < hitY) {

            boolean horizontalEntity = basicConfiguration.getBool("Entity_Hitboxes." + type.name() + ".Horizontal_Entity", false);
            boolean arms = basicConfiguration.getBool("Entity_Hitboxes." + type.name() + "." + DamagePoint.ARMS.name(), false);
            if (horizontalEntity || arms) {
                Vector normalizedEntityDirection = livingEntity.getLocation().getDirection();

                if (horizontalEntity && !new HitBox(minX, minY, minZ, maxX, maxY, maxZ).expand(normalizedEntityDirection, FRONT_HIT).collides(hitLocation)) {
                    // Basically removes directionally 0.2 from this entity hitbox and check if the hit location is still in the hitbox
                    return DamagePoint.HEAD;
                }

                if (arms && Math.abs(normalizedMotion.clone().setY(0).dot(normalizedEntityDirection.setY(0))) < 0.5) {
                    return DamagePoint.ARMS;
                }
            }

            return DamagePoint.BODY;
        }

        // Check LEGS
        double legs = basicConfiguration.getDouble("Entity_Hitboxes." + type.name() + "." + DamagePoint.LEGS.name());
        if (legs > 0.0 && maxY - (entityHeight * (head + body + legs)) < hitY) {
            return DamagePoint.LEGS;
        }

        // Check FEET
        double feet = basicConfiguration.getDouble("Entity_Hitboxes." + type.name() + "." + DamagePoint.FEET.name());
        if (feet > 0.0) { // No need for actual check since it can't be HEAD, BODY or LEGS anymore so only option left is FEET
            return DamagePoint.FEET;
        }

        debug.log(LogLevel.WARN, "Something unexpected happened and HEAD, BODY, LEGS or FEET wasn't valid",
                "This should never happen. Using BODY as default value...",
                "This happened with entity type " + type + ".");
        return DamagePoint.BODY;
    }

    public RayTraceResult rayTrace(Vector location, Vector normalizedMotion) {
        RayTraceResult mainBoxHit = rayTrace(location, normalizedMotion, block, livingEntity);

        // Voxel shape not used or didn't hit main hitbox
        if (voxelShape == null || mainBoxHit == null) return mainBoxHit;

        // Here we know main hitbox was hit, now check all voxel shapes
        RayTraceResult hit = null;
        double closestHit = -1;
        for (HitBox boxPart : voxelShape) {
            RayTraceResult boxPartHit = boxPart.rayTrace(location, normalizedMotion, block, livingEntity);
            if (boxPartHit == null) continue;

            // Only closes hit
            if (closestHit == -1 || boxPartHit.getDistanceTravelled() < closestHit) {
                closestHit = boxPartHit.getDistanceTravelled();
                hit = boxPartHit;
            }
        }
        return hit;
    }

    /**
     * Uses BoundingBox class method rayTrace(Vector, Vector, double) with slight modifications. Easier backwards compatibility this way.
     * https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/util/BoundingBox.html#rayTrace(org.bukkit.util.Vector,org.bukkit.util.Vector,double)
     *
     * @param location the start location of ray
     * @param normalizedMotion the normalized direction
     * @return the ray trace result or null if there is no hit
     */
    private RayTraceResult rayTrace(Vector location, Vector normalizedMotion, Block block, LivingEntity livingEntity) {

        double startX = location.getX();
        double startY = location.getY();
        double startZ = location.getZ();

        double dirX = normalizedMotion.getX();
        double dirY = normalizedMotion.getY();
        double dirZ = normalizedMotion.getZ();

        double divX = 1.0D / dirX;
        double divY = 1.0D / dirY;
        double divZ = 1.0D / dirZ;

        double tMin, tMax, tyMin, tyMax;
        BlockFace hitBlockFaceMin, hitBlockFaceMax, hitBlockFaceYMin, hitBlockFaceYMax;

        // x
        if (dirX >= 0.0D) {
            tMin = (this.minX - startX) * divX;
            tMax = (this.maxX - startX) * divX;
            hitBlockFaceMin = BlockFace.WEST;
            hitBlockFaceMax = BlockFace.EAST;
        } else {
            tMin = (this.maxX - startX) * divX;
            tMax = (this.minX - startX) * divX;
            hitBlockFaceMin = BlockFace.EAST;
            hitBlockFaceMax = BlockFace.WEST;
        }

        // y
        if (dirY >= 0.0D) {
            tyMin = (this.minY - startY) * divY;
            tyMax = (this.maxY - startY) * divY;
            hitBlockFaceYMin = BlockFace.DOWN;
            hitBlockFaceYMax = BlockFace.UP;
        } else {
            tyMin = (this.maxY - startY) * divY;
            tyMax = (this.minY - startY) * divY;
            hitBlockFaceYMin = BlockFace.UP;
            hitBlockFaceYMax = BlockFace.DOWN;
        }
        if ((tMin > tyMax) || (tMax < tyMin)) {
            return null;
        }
        if (tyMin > tMin) {
            tMin = tyMin;
            hitBlockFaceMin = hitBlockFaceYMin;
        }
        if (tyMax < tMax) {
            tMax = tyMax;
            hitBlockFaceMax = hitBlockFaceYMax;
        }

        // z
        double tzMin, tzMax;
        BlockFace hitBlockFaceZMin, hitBlockFaceZMax;
        if (dirZ >= 0.0D) {
            tzMin = (this.minZ - startZ) * divZ;
            tzMax = (this.maxZ - startZ) * divZ;
            hitBlockFaceZMin = BlockFace.NORTH;
            hitBlockFaceZMax = BlockFace.SOUTH;
        } else {
            tzMin = (this.maxZ - startZ) * divZ;
            tzMax = (this.minZ - startZ) * divZ;
            hitBlockFaceZMin = BlockFace.SOUTH;
            hitBlockFaceZMax = BlockFace.NORTH;
        }
        if ((tMin > tzMax) || (tMax < tzMin)) {
            return null;
        }
        if (tzMin > tMin) {
            tMin = tzMin;
            hitBlockFaceMin = hitBlockFaceZMin;
        }
        if (tzMax < tMax) {
            tMax = tzMax;
            hitBlockFaceMax = hitBlockFaceZMax;
        }

        if (tMax < 0.0D) return null;

        double t;
        BlockFace hitBlockFace;
        if (tMin < 0.0D) {
            t = tMax;
            hitBlockFace = hitBlockFaceMax;
        } else {
            t = tMin;
            hitBlockFace = hitBlockFaceMin;
        }

        if (block != null) {
            return new RayTraceResult(normalizedMotion.clone().multiply(t).add(location), t, hitBlockFace, block);
        }

        Vector hitLocation = normalizedMotion.clone().multiply(t).add(location);
        return new RayTraceResult(hitLocation, t, hitBlockFace, livingEntity, getDamagePoint(hitLocation, normalizedMotion, livingEntity));
    }

    /**
     * Uses BoundingBox class method expand(double, double, double, double, double, double). Easier backwards compatibility this way.
     * https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/util/BoundingBox.html#expand(double,double,double,double,double,double)
     *
     * @return this hit box with expansion
     */
    public HitBox expand(double negativeX, double negativeY, double negativeZ, double positiveX, double positiveY, double positiveZ) {
        double newMinX = this.minX - negativeX;
        double newMinY = this.minY - negativeY;
        double newMinZ = this.minZ - negativeZ;
        double newMaxX = this.maxX + positiveX;
        double newMaxY = this.maxY + positiveY;
        double newMaxZ = this.maxZ + positiveZ;
        double centerZ;
        if (newMinX > newMaxX) {
            centerZ = this.getCenterX();
            if (newMaxX >= centerZ) {
                newMinX = newMaxX;
            } else if (newMinX <= centerZ) {
                newMaxX = newMinX;
            } else {
                newMinX = centerZ;
                newMaxX = centerZ;
            }
        }

        if (newMinY > newMaxY) {
            centerZ = this.getCenterY();
            if (newMaxY >= centerZ) {
                newMinY = newMaxY;
            } else if (newMinY <= centerZ) {
                newMaxY = newMinY;
            } else {
                newMinY = centerZ;
                newMaxY = centerZ;
            }
        }

        if (newMinZ > newMaxZ) {
            centerZ = this.getCenterZ();
            if (newMaxZ >= centerZ) {
                newMinZ = newMaxZ;
            } else if (newMinZ <= centerZ) {
                newMaxZ = newMinZ;
            } else {
                newMinZ = centerZ;
                newMaxZ = centerZ;
            }
        }

        return this.modify(newMinX, newMinY, newMinZ, newMaxX, newMaxY, newMaxZ);
    }


    /**
     * Uses BoundingBox class method expand(Vector, double). Easier backwards compatibility this way.
     * https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/util/BoundingBox.html#expand(org.bukkit.util.Vector,double)
     *
     * @param direction the direction to expand
     * @param expansion the amount to expand
     * @return this hit box with expansion
     */
    public HitBox expand(Vector direction, double expansion) {
        double dirX = direction.getX(), dirY = direction.getY(), dirZ = direction.getZ();
        double negativeX = dirX < 0.0D ? -dirX * expansion : 0.0D;
        double negativeY = dirY < 0.0D ? -dirY * expansion : 0.0D;
        double negativeZ = dirZ < 0.0D ? -dirZ * expansion : 0.0D;
        double positiveX = dirX > 0.0D ? dirX * expansion : 0.0D;
        double positiveY = dirY > 0.0D ? dirY * expansion : 0.0D;
        double positiveZ = dirZ > 0.0D ? dirZ * expansion : 0.0D;
        return this.expand(negativeX, negativeY, negativeZ, positiveX, positiveY, positiveZ);
    }

    public void outlineAllBoxes(Entity player) {
        if (voxelShape != null) {
            outlineMainBox(player, Color.BLACK);
            ColorSerializer.ColorType[] colors = ColorSerializer.ColorType.values();

            int i = 1;
            for (HitBox voxel : voxelShape) {
                voxel.outlineMainBox(player, colors[i].getBukkitColor());
                if (++i >= colors.length) {
                    i = 1;
                }
            }
        } else {
            outlineMainBox(player, Color.BLACK);
        }
    }

    public void outlineMainBox(Entity player, Color color) {
        double step = 0.05;
        for (double x = minX; x <= maxX; x += step) {
            for (double y = minY; y <= maxY; y += step) {
                for (double z = minZ; z <= maxZ; z += step) {
                    int components = 0;
                    if (x == minX || x + step > maxX) components++;
                    if (y == minY || y + step > maxY) components++;
                    if (z == minZ || z + step > maxZ) components++;
                    if (components >= 2) {
                        if (CompatibilityAPI.getVersion() < 1.13) {
                            player.getWorld().spawnParticle(Particle.CRIT, x, y, z, 1, 0, 0, 0, 0.0001);
                        } else {
                            player.getWorld().spawnParticle(Particle.REDSTONE, x, y, z, 1, 0, 0, 0, 0.0001, new Particle.DustOptions(color, 0.5f), true);
                        }
                    }
                }
            }
        }
    }

    @Override
    public String getKeyword() {
        return "Entity_Hitboxes";
    }

    @Override
    public void validate(Configuration configuration, File file, ConfigurationSection configurationSection, String path) {
        for (EntityType entityType : EntityType.values()) {
            if (!entityType.isAlive()) continue;

            double head = configuration.getDouble("Entity_Hitboxes." + entityType.name() + "." + DamagePoint.HEAD.name(), -1.0);
            double body = configuration.getDouble("Entity_Hitboxes." + entityType.name() + "." + DamagePoint.BODY.name(), -1.0);
            double legs = configuration.getDouble("Entity_Hitboxes." + entityType.name() + "." + DamagePoint.LEGS.name(), -1.0);
            double feet = configuration.getDouble("Entity_Hitboxes." + entityType.name() + "." + DamagePoint.FEET.name(), -1.0);

            if (head < 0 || body < 0 || legs < 0 || feet < 0) {
                debug.log(LogLevel.WARN, "Entity type " + entityType.name() + " is missing some of its damage point values, please add it",
                        "Located at file /WeaponMechanics/config.yml in Entity_Hitboxes." + entityType.name() + " in configurations",
                        "Its missing one of these: HEAD, BODY, LEGS or FEET");

                putDefaults(configuration, entityType);
                continue;
            }

            boolean horizontalEntity = configuration.getBool("Entity_Hitboxes." + entityType.name() + ".Horizontal_Entity", false);
            if (horizontalEntity && head > 0.0) {
                debug.log(LogLevel.WARN, "Entity type " + entityType.name() + " hit box had horizontal entity true and HEAD was not 0.0",
                        "Located at file /WeaponMechanics/config.yml in Entity_Hitboxes." + entityType.name() + " in configurations",
                        "When using horizontal entity true HEAD should be set to 0.0!");

                // Set default value to BODY
                putDefaults(configuration, entityType);
                continue;
            }

            double sumOf = head + body + legs + feet;
            if (Math.abs(sumOf - 1.0) > 1e-5) { // If the numbers are not super close together (floating point issues)
                debug.log(LogLevel.WARN, "Entity type " + entityType.name() + " hit box values sum doesn't match 1.0",
                        "Located at file /WeaponMechanics/config.yml in Entity_Hitboxes." + entityType.name() + " in configurations",
                        "Now the total sum was " + sumOf + ", please make it 1.0.");

                putDefaults(configuration, entityType);
            }
        }
    }

    /**
     * Simply resets hit boxes to default is they're missing or are invalid
     *
     * @param basicConfiguration the config.yml configuration instance
     * @param entityType the entity type
     */
    private void putDefaults(Configuration basicConfiguration, EntityType entityType) {
        basicConfiguration.set("Entity_Hitboxes." + entityType.name() + "." + DamagePoint.HEAD.name(), 0.0);

        // Set default value to BODY 100%
        basicConfiguration.set("Entity_Hitboxes." + entityType.name() + "." + DamagePoint.BODY.name(), 1.0);

        basicConfiguration.set("Entity_Hitboxes." + entityType.name() + "." + DamagePoint.LEGS.name(), 0.0);
        basicConfiguration.set("Entity_Hitboxes." + entityType.name() + "." + DamagePoint.FEET.name(), 0.0);

        basicConfiguration.set("Entity_Hitboxes." + entityType.name() + "." + DamagePoint.ARMS.name(), false);
        basicConfiguration.set("Entity_Hitboxes." + entityType.name() + ".Horizontal_Entity", false);
    }
}