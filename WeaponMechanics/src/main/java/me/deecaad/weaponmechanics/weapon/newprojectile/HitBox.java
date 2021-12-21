package me.deecaad.weaponmechanics.weapon.newprojectile;

import me.deecaad.core.file.Configuration;
import me.deecaad.core.utils.LogLevel;
import me.deecaad.weaponmechanics.WeaponMechanics;
import me.deecaad.weaponmechanics.weapon.damage.DamagePoint;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;

import static me.deecaad.weaponmechanics.WeaponMechanics.debug;

public class HitBox {

    private static final Configuration basicConfiguration = WeaponMechanics.getBasicConfigurations();

    private Block block;
    private LivingEntity livingEntity;

    private double minX;
    private double minY;
    private double minZ;
    private double maxX;
    private double maxY;
    private double maxZ;

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
        this.block = block;
    }

    public void setLivingEntity(LivingEntity livingEntity) {
        this.livingEntity = livingEntity;
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

    /**
     * Uses BoundingBox class method rayTrace(Vector, Vector, double) with slight modifications. Easier backwards compatibility this way.
     * https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/util/BoundingBox.html#rayTrace(org.bukkit.util.Vector,org.bukkit.util.Vector,double)
     *
     * @param location the start location of ray
     * @param normalizedMotion the normalized direction
     * @return the ray trace result or null if there is no hit
     */
    private RayTraceResult rayTrace(Vector location, Vector normalizedMotion) {

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

        if (this.block != null) {
            return new RayTraceResult(normalizedMotion.clone().multiply(t).add(location), hitBlockFace, this.block);
        }

        Vector hitLocation = normalizedMotion.clone().multiply(t).add(location);
        return new RayTraceResult(hitLocation, hitBlockFace, this.livingEntity, getDamagePoint(hitLocation, normalizedMotion));
    }

    /**
     * @param hitLocation the entity hit location
     * @param normalizedMotion the normalized direction
     * @return the damage point or null if tried to cast when living entity was not defined
     */
    public DamagePoint getDamagePoint(Vector hitLocation, Vector normalizedMotion) {
        if (this.livingEntity == null) return null;

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

                if (horizontalEntity && collidesFront()) {
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

    private boolean collidesFront() {
        // todo
        return false;
    }
}